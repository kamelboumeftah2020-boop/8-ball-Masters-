package com.sada.podcasts;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Foreground service holding the MediaSession and the playback notification. */
public class MediaPlaybackService extends Service {

    public interface Listener {
        void onAction(String action, double position);
    }

    static final String CHANNEL_ID = "playback";
    static final int NOTIFICATION_ID = 1001;
    static final String ACTION_COMMAND = "com.sada.podcasts.COMMAND";
    static final String EXTRA_COMMAND = "command";

    @Nullable static MediaPlaybackService instance;
    @Nullable static Listener listener;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private MediaSessionCompat session;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private String title = "";
    private String artist = "";
    private String artworkUrl = "";
    private boolean playing;
    private double position;
    private double duration;
    private float rate = 1f;
    @Nullable private Bitmap artwork;
    private boolean inForeground;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createChannel();

        session = new MediaSessionCompat(this, "Sada");
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlay() { emit("play", -1); }
            @Override public void onPause() { emit("pause", -1); }
            @Override public void onStop() { emit("pause", -1); }
            @Override public void onSkipToNext() { emit("nexttrack", -1); }
            @Override public void onSkipToPrevious() { emit("previoustrack", -1); }
            @Override public void onFastForward() { emit("seekforward", -1); }
            @Override public void onRewind() { emit("seekbackward", -1); }
            @Override public void onSeekTo(long pos) { emit("seekto", pos / 1000.0); }
        });
        session.setSessionActivity(contentIntent());
        session.setActive(true);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sada:playback");
        wakeLock.setReferenceCounted(false);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Sada:stream");
            wifiLock.setReferenceCounted(false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_COMMAND.equals(intent.getAction())) {
            String cmd = intent.getStringExtra(EXTRA_COMMAND);
            if ("dismiss".equals(cmd)) {
                if (!playing) shutdown();
            } else if (cmd != null) {
                emit(cmd, -1);
            }
            if (!inForeground && playing) refresh();
            return START_NOT_STICKY;
        }
        Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) apply(extras);
        else refresh();
        return START_NOT_STICKY;
    }

    /** Update state from the web player and redraw the notification. */
    void apply(Bundle s) {
        title = s.getString("title", title);
        artist = s.getString("artist", artist);
        playing = s.getBoolean("playing", playing);
        position = s.getDouble("position", position);
        duration = s.getDouble("duration", duration);
        rate = (float) s.getDouble("rate", rate);
        String art = s.getString("artwork", "");
        if (!art.equals(artworkUrl)) {
            artworkUrl = art;
            loadArtwork(art);
        }
        refresh();
    }

    void shutdown() {
        releaseLocks();
        stopForegroundCompat(true);
        inForeground = false;
        stopSelf();
    }

    private void refresh() {
        MediaMetadataCompat.Builder meta = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "صدى")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long) (duration * 1000));
        if (artwork != null) meta.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork);
        session.setMetadata(meta.build());

        session.setPlaybackState(new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO
                | PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_REWIND
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setState(playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                (long) (position * 1000), playing ? rate : 0f)
            .build());

        Notification notification = buildNotification();
        if (playing) {
            acquireLocks();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                } else {
                    startForeground(NOTIFICATION_ID, notification);
                }
                inForeground = true;
            } catch (RuntimeException e) {
                // Background start not allowed right now: keep showing the notification anyway.
                notifyManager(notification);
            }
        } else {
            releaseLocks();
            if (inForeground) {
                stopForegroundCompat(false);
                inForeground = false;
            }
            notifyManager(notification);
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_podcast)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(artwork)
            .setContentIntent(contentIntent())
            .setDeleteIntent(command("dismiss", 9))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(playing)
            .addAction(R.drawable.ic_replay_15, "رجوع 15 ثانية", command("seekbackward", 1))
            .addAction(playing
                ? new NotificationCompat.Action(R.drawable.ic_pause, "إيقاف مؤقت", command("pause", 2))
                : new NotificationCompat.Action(R.drawable.ic_play, "تشغيل", command("play", 3)))
            .addAction(R.drawable.ic_forward_30, "تقديم 30 ثانية", command("seekforward", 4))
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        return b.build();
    }

    private PendingIntent command(String cmd, int requestCode) {
        Intent i = new Intent(this, MediaPlaybackService.class).setAction(ACTION_COMMAND).putExtra(EXTRA_COMMAND, cmd);
        return PendingIntent.getService(this, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent contentIntent() {
        Intent i = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void loadArtwork(String url) {
        if (url.isEmpty()) {
            artwork = null;
            return;
        }
        io.execute(() -> {
            Bitmap bmp = null;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                try (InputStream in = c.getInputStream()) {
                    bmp = BitmapFactory.decodeStream(in);
                }
            } catch (Exception ignored) {
                // Artwork is optional.
            }
            final Bitmap result = bmp;
            main.post(() -> {
                if (!url.equals(artworkUrl)) return;
                artwork = result;
                refresh();
            });
        });
    }

    private void emit(String action, double pos) {
        Listener l = listener;
        if (l != null) main.post(() -> l.onAction(action, pos));
    }

    private void notifyManager(Notification n) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIFICATION_ID, n);
    }

    @SuppressWarnings("deprecation")
    private void stopForegroundCompat(boolean remove) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(remove ? STOP_FOREGROUND_REMOVE : STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(remove);
        }
        if (remove) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(NOTIFICATION_ID);
        }
    }

    private void acquireLocks() {
        if (!wakeLock.isHeld()) wakeLock.acquire(3 * 60 * 60 * 1000L);
        if (wifiLock != null && !wifiLock.isHeld()) wifiLock.acquire();
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "التشغيل", NotificationManager.IMPORTANCE_LOW);
        ch.setDescription("التحكم في تشغيل البودكاست");
        ch.setShowBadge(false);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(ch);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // The WebView (and its audio) goes away with the task.
        shutdown();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        releaseLocks();
        session.setActive(false);
        session.release();
        io.shutdownNow();
        if (instance == this) instance = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
