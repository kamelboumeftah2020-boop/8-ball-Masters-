package com.sada.podcasts;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;

/** Home-screen widget: what's playing, with a play/pause button. */
public class PlayerWidget extends AppWidgetProvider {

    private static final String PREFS = "player_widget";
    private static final String ART_FILE = "widget_art.png";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        render(context, manager, ids);
    }

    /** Called by the playback service whenever the state changes. */
    static void publish(Context ctx, String title, String artist, boolean playing, @Nullable Bitmap art) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean artChanged = !title.equals(prefs.getString("title", null));
        prefs.edit().putString("title", title).putString("artist", artist).putBoolean("playing", playing).apply();
        if (art != null && artChanged) {
            Bitmap small = Bitmap.createScaledBitmap(art, 200, 200, true);
            try (FileOutputStream out = new FileOutputStream(new File(ctx.getFilesDir(), ART_FILE))) {
                small.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception ignored) {
                // keep the previous artwork
            }
        }
        AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
        int[] ids = manager.getAppWidgetIds(new ComponentName(ctx, PlayerWidget.class));
        if (ids.length > 0) render(ctx, manager, ids);
    }

    private static void render(Context ctx, AppWidgetManager manager, int[] ids) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String title = prefs.getString("title", "");
        boolean playing = prefs.getBoolean("playing", false) && MediaPlaybackService.instance != null;

        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_player);
        views.setTextViewText(R.id.widget_title, title.isEmpty() ? "صدى" : title);
        views.setTextViewText(R.id.widget_subtitle, title.isEmpty() ? "اضغط للاستماع" : prefs.getString("artist", ""));
        views.setImageViewResource(R.id.widget_play, playing ? R.drawable.ic_pause : R.drawable.ic_play);

        File art = new File(ctx.getFilesDir(), ART_FILE);
        Bitmap bmp = art.exists() && !title.isEmpty() ? BitmapFactory.decodeFile(art.getPath()) : null;
        if (bmp != null) views.setImageViewBitmap(R.id.widget_art, bmp);
        else views.setImageViewResource(R.id.widget_art, R.mipmap.ic_launcher);

        Intent open = new Intent(ctx, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPi = PendingIntent.getActivity(ctx, 100, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, openPi);

        PendingIntent buttonPi;
        if (MediaPlaybackService.instance != null) {
            Intent cmd = new Intent(ctx, MediaPlaybackService.class)
                .setAction(MediaPlaybackService.ACTION_COMMAND)
                .putExtra(MediaPlaybackService.EXTRA_COMMAND, playing ? "pause" : "play");
            buttonPi = PendingIntent.getService(ctx, 101, cmd, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            // Player not running: open the app and resume the last episode.
            Intent resume = new Intent(ctx, MainActivity.class)
                .putExtra(EpisodeCheckerPlugin.EXTRA_ROUTE, "action:play")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            buttonPi = PendingIntent.getActivity(ctx, 102, resume, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        views.setOnClickPendingIntent(R.id.widget_play, buttonPi);

        manager.updateAppWidget(ids, views);
    }
}
