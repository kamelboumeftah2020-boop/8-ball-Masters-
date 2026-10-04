package app.nur.quran;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

/**
 * مشغّل في الإشعارات وعلى شاشة القفل: السابق، والتشغيل/الإيقاف المؤقت، والتالي، والتكرار، والإغلاق،
 * مع شريط التقدم. الصوت نفسه يُشغَّل في التطبيق، وهذه الخدمة تُبقيه حيًّا في الخلفية وتنقل الأوامر إليه.
 */
public class NurMediaService extends Service {

    static final String ACTION_UPDATE = "app.nur.quran.MEDIA_UPDATE";
    static final String CMD = "app.nur.quran.MEDIA_CMD";
    private static final String CHANNEL_ID = "nur_playback";
    private static final int NOTIFICATION_ID = 7001;

    /** مستقبل الأوامر (إضافة NurMedia)، يُضبط عند تحميلها. */
    interface Listener { void onCommand(String action, long position); }
    static Listener listener;

    private MediaSessionCompat session;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private boolean foreground;

    private String title = "نور", text = "";
    private boolean playing, repeat, hasPrev = true, hasNext = true;
    private long position, duration;

    @Override
    public void onCreate() {
        super.onCreate();
        session = new MediaSessionCompat(this, "nur");
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlay() { send("play", 0); }
            @Override public void onPause() { send("pause", 0); }
            @Override public void onSkipToNext() { send("next", 0); }
            @Override public void onSkipToPrevious() { send("prev", 0); }
            @Override public void onStop() { send("close", 0); }
            @Override public void onSeekTo(long pos) { send("seek", pos); }
            @Override public void onCustomAction(String action, android.os.Bundle extras) { send(action, 0); }
        });
        Intent open = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        session.setSessionActivity(PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        session.setActive(true);
    }

    private static void send(String action, long pos) {
        if (listener != null) listener.onCommand(action, pos);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && CMD.equals(intent.getAction())) {
            String c = intent.getStringExtra("cmd");
            send(c, 0);
            // الإغلاق يوقف الخدمة حتى لو لم يستجب التطبيق
            if (!foreground || "close".equals(c)) stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null) {
            title = orElse(intent.getStringExtra("title"), title);
            text = orElse(intent.getStringExtra("text"), text);
            playing = intent.getBooleanExtra("playing", true);
            repeat = intent.getBooleanExtra("repeat", false);
            hasPrev = intent.getBooleanExtra("hasPrev", true);
            hasNext = intent.getBooleanExtra("hasNext", true);
            position = intent.getLongExtra("position", 0);
            duration = intent.getLongExtra("duration", 0);
        }
        updateSession();
        Notification n = buildNotification();
        if (!foreground) {
            int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK : 0;
            ServiceCompat.startForeground(this, NOTIFICATION_ID, n, type);
            foreground = true;
        } else {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify(NOTIFICATION_ID, n);
        }
        if (playing) acquireLocks(); else releaseLocks();
        return START_NOT_STICKY;
    }

    private static String orElse(String a, String b) {
        return a != null ? a : b;
    }

    private void updateSession() {
        session.setMetadata(new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, text)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration > 0 ? duration : -1)
            .build());
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_SEEK_TO;
        if (hasPrev) actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        if (hasNext) actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        session.setPlaybackState(new PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, position, playing ? 1f : 0f)
            .addCustomAction(new PlaybackStateCompat.CustomAction.Builder("repeat", "تكرار", repeat ? R.drawable.ic_m_repeat_on : R.drawable.ic_m_repeat).build())
            .build());
    }

    private PendingIntent cmd(String c, int code) {
        Intent i = new Intent(this, NurMediaService.class).setAction(CMD).putExtra("cmd", c);
        return PendingIntent.getService(this, 100 + code, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification buildNotification() {
        createChannel();
        Intent open = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_adhan)
            .setColor(0xFF0F6B5C)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(content)
            .setDeleteIntent(cmd("close", 5))
            .setOngoing(playing)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_m_prev, "السابق", cmd("prev", 0))
            .addAction(playing ? R.drawable.ic_m_pause : R.drawable.ic_m_play, playing ? "إيقاف مؤقت" : "تشغيل", cmd("toggle", 1))
            .addAction(R.drawable.ic_m_next, "التالي", cmd("next", 2))
            .addAction(repeat ? R.drawable.ic_m_repeat_on : R.drawable.ic_m_repeat, repeat ? "إلغاء التكرار" : "تكرار", cmd("repeat", 3))
            .addAction(R.drawable.ic_m_close, "إغلاق", cmd("close", 4))
            .setStyle(new MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
            .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "التشغيل", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("يظهر أثناء الاستماع إلى التلاوات والمواعظ");
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
    }

    private void acquireLocks() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "nur:playback");
            wakeLock.setReferenceCounted(false);
        }
        if (!wakeLock.isHeld()) wakeLock.acquire(6 * 60 * 60 * 1000L);
        if (wifiLock == null) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "nur:stream");
                wifiLock.setReferenceCounted(false);
            }
        }
        if (wifiLock != null && !wifiLock.isHeld()) wifiLock.acquire();
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();
    }

    @Override
    public void onDestroy() {
        releaseLocks();
        session.setActive(false);
        session.release();
        foreground = false;
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
