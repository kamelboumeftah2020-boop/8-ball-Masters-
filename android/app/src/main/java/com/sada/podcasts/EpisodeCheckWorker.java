package com.sada.podcasts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/** Periodic background job: looks for new episodes of followed podcasts and notifies. */
public class EpisodeCheckWorker extends Worker {

    static final String CHANNEL_ID = "new_episodes";

    public EpisodeCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(EpisodeCheckerPlugin.PREFS, Context.MODE_PRIVATE);
        String country = prefs.getString(EpisodeCheckerPlugin.KEY_COUNTRY, "us");
        JSONArray podcasts;
        try {
            podcasts = new JSONArray(prefs.getString(EpisodeCheckerPlugin.KEY_PODCASTS, "[]"));
        } catch (Exception e) {
            return Result.success();
        }

        for (int i = 0; i < podcasts.length() && !isStopped(); i++) {
            JSONObject p = podcasts.optJSONObject(i);
            if (p == null) continue;
            String id = p.optString("id");
            String title = p.optString("title");
            String key = EpisodeCheckerPlugin.LATEST_PREFIX + id;
            try {
                List<String[]> episodes = p.optString("feedUrl").isEmpty() ? fetchApple(id, country) : fetchRss(p.optString("feedUrl"));
                String known = prefs.getString(key, "");
                String newest = known;
                List<String> fresh = new ArrayList<>();
                for (String[] ep : episodes) { // {releaseDate ISO, title, podcast title}
                    String date = ep[0];
                    if (date.compareTo(newest) > 0) newest = date;
                    if (!known.isEmpty() && date.compareTo(known) > 0) fresh.add(ep[1]);
                    if (title.isEmpty()) title = ep[2];
                }
                if (!newest.equals(known)) prefs.edit().putString(key, newest).apply();
                if (!fresh.isEmpty()) notifyNew(ctx, id, title, fresh);
            } catch (Exception ignored) {
                // Network hiccup: try this podcast again on the next run.
            }
        }
        return Result.success();
    }

    private static String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString(StandardCharsets.UTF_8.name());
        } finally {
            c.disconnect();
        }
    }

    /** Latest episodes from Apple's directory: {date, title, podcast}. */
    private static List<String[]> fetchApple(String id, String country) throws Exception {
        String url = "https://itunes.apple.com/lookup?id=" + Uri.encode(id) + "&entity=podcastEpisode&limit=10&country=" + Uri.encode(country);
        JSONArray results = new JSONObject(get(url)).optJSONArray("results");
        List<String[]> out = new ArrayList<>();
        if (results == null) return out;
        for (int j = 0; j < results.length(); j++) {
            JSONObject r = results.optJSONObject(j);
            if (r == null || !"podcastEpisode".equals(r.optString("wrapperType"))) continue;
            out.add(new String[] { r.optString("releaseDate", ""), r.optString("trackName"), r.optString("collectionName") });
        }
        return out;
    }

    /** Episodes from a podcast's own RSS feed (podcasts added by link). */
    private static List<String[]> fetchRss(String feedUrl) throws Exception {
        XmlPullParser xp = XmlPullParserFactory.newInstance().newPullParser();
        xp.setInput(new java.io.StringReader(get(feedUrl)));
        List<String[]> out = new ArrayList<>();
        String channelTitle = "";
        String itemTitle = null;
        String itemDate = null;
        boolean inItem = false;
        for (int ev = xp.getEventType(); ev != XmlPullParser.END_DOCUMENT && out.size() < 20; ev = xp.next()) {
            if (ev == XmlPullParser.START_TAG) {
                String name = xp.getName();
                if ("item".equals(name)) {
                    inItem = true;
                    itemTitle = null;
                    itemDate = null;
                } else if ("title".equals(name)) {
                    String t = xp.nextText().trim();
                    if (inItem) itemTitle = t;
                    else if (channelTitle.isEmpty()) channelTitle = t;
                } else if (inItem && "pubDate".equals(name)) {
                    itemDate = toIso(xp.nextText().trim());
                }
            } else if (ev == XmlPullParser.END_TAG && "item".equals(xp.getName())) {
                inItem = false;
                if (itemDate != null && !itemDate.isEmpty()) out.add(new String[] { itemDate, itemTitle == null ? "" : itemTitle, channelTitle });
            }
        }
        return out;
    }

    /** RFC 822 pubDate -> "yyyy-MM-ddTHH:mm:ssZ" (the format the app compares). */
    static String toIso(String rfc822) {
        String[] patterns = { "EEE, dd MMM yyyy HH:mm:ss Z", "EEE, d MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss zzz", "dd MMM yyyy HH:mm:ss Z" };
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (String pattern : patterns) {
            try {
                Date d = new SimpleDateFormat(pattern, Locale.US).parse(rfc822);
                if (d != null) return iso.format(d);
            } catch (Exception ignored) {
                // try the next pattern
            }
        }
        return "";
    }

    private void notifyNew(Context ctx, String podcastId, String podcastTitle, List<String> episodes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        createChannel(ctx);

        Intent open = new Intent(ctx, MainActivity.class)
            .putExtra(EpisodeCheckerPlugin.EXTRA_ROUTE, "/podcast/" + podcastId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(
            ctx, podcastId.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String text = episodes.get(0);
        if (episodes.size() > 1) text += " (+" + (episodes.size() - 1) + ")";
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_podcast)
            .setContentTitle("حلقة جديدة · " + podcastTitle)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(String.join("\n", episodes)))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        try {
            NotificationManagerCompat.from(ctx).notify(podcastId.hashCode(), b.build());
        } catch (SecurityException ignored) {
            // Permission revoked between the check and the post.
        }
    }

    private static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "حلقات جديدة", NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription("تنبيه عند نزول حلقة جديدة من برامجك المفضلة");
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(ch);
    }
}
