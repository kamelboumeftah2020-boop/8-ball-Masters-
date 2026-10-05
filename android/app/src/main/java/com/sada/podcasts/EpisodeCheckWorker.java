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
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

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
                JSONArray results = fetch(id, country);
                String known = prefs.getString(key, "");
                String newest = known;
                List<String> fresh = new ArrayList<>();
                for (int j = 0; j < results.length(); j++) {
                    JSONObject r = results.optJSONObject(j);
                    if (r == null || !"podcastEpisode".equals(r.optString("wrapperType"))) continue;
                    String date = r.optString("releaseDate", "");
                    if (date.compareTo(newest) > 0) newest = date;
                    if (!known.isEmpty() && date.compareTo(known) > 0) fresh.add(r.optString("trackName"));
                    if (title.isEmpty()) title = r.optString("collectionName");
                }
                if (!newest.equals(known)) prefs.edit().putString(key, newest).apply();
                if (!fresh.isEmpty()) notifyNew(ctx, id, title, fresh);
            } catch (Exception ignored) {
                // Network hiccup: try this podcast again on the next run.
            }
        }
        return Result.success();
    }

    private JSONArray fetch(String id, String country) throws Exception {
        String url = "https://itunes.apple.com/lookup?id=" + Uri.encode(id) + "&entity=podcastEpisode&limit=10&country=" + Uri.encode(country);
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return new JSONObject(out.toString(StandardCharsets.UTF_8.name())).optJSONArray("results");
        } finally {
            c.disconnect();
        }
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
