package app.nur.quran;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

/**
 * يرفع الأذان بصوت المؤذن على قناة المنبّه، مع إشعار فيه زر «إيقاف الأذان»،
 * وشاشة أذان تظهر فوق قفل الشاشة (كالمنبّه).
 */
public class AdhanService extends Service {

    static final String CHANNEL = "nur_adhan_alert";
    static final String ACTION_STOP = "app.nur.quran.ADHAN_STOP";
    static final String ACTION_DONE = "app.nur.quran.ADHAN_DONE";
    static final int NOTIFICATION_ID = 7101;
    static final int FALLBACK_ID = 7102;
    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;
    private String name = "";

    static void createChannel(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "الأذان", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("رفع الأذان عند دخول وقت الصلاة");
            ch.setSound(null, null); // الصوت يُشغَّل من الخدمة نفسها
            ch.enableVibration(true);
            ch.setBypassDnd(true);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(ch);
        }
    }

    /** قناة احتياطية صوتها الأذان نفسه، تُستعمل إن منع النظام بدء الخدمة. */
    static String soundChannel(Context c, String sound) {
        String id = "nur_adhan_sound_" + sound;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return id;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(id) == null) {
            NotificationChannel ch = new NotificationChannel(id, "الأذان (احتياطي)", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("يُستعمل إن تعذّر تشغيل خدمة الأذان");
            ch.setSound(rawUri(c, sound), new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
            ch.enableVibration(true);
            ch.setBypassDnd(true);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(ch);
        }
        return id;
    }

    static int rawRes(Context c, String sound) {
        int res = c.getResources().getIdentifier("adhan_" + sound, "raw", c.getPackageName());
        return res != 0 ? res : c.getResources().getIdentifier("adhan_008", "raw", c.getPackageName());
    }

    static Uri rawUri(Context c, String sound) {
        return Uri.parse("android.resource://" + c.getPackageName() + "/" + rawRes(c, sound));
    }

    static NotificationCompat.Builder base(Context c, String channel, String name) {
        Intent open = new Intent(c, MainActivity.class).putExtra("route", "#/adhan")
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(c, 1, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent screen = new Intent(c, AdhanActivity.class).putExtra("name", name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        PendingIntent full = PendingIntent.getActivity(c, 4, screen, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(c, channel)
            .setSmallIcon(R.drawable.ic_stat_adhan)
            .setColor(0xFF0F6B5C)
            .setContentTitle("حان الآن موعد صلاة " + (name == null ? "" : name))
            .setContentText("حيّ على الصلاة، حيّ على الفلاح")
            .setContentIntent(content)
            .setFullScreenIntent(full, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true);
    }

    static Notification build(Context c, String name) {
        createChannel(c);
        Intent stop = new Intent(c, AdhanService.class).setAction(ACTION_STOP);
        PendingIntent ps = PendingIntent.getService(c, 2, stop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return base(c, CHANNEL, name)
            .addAction(R.drawable.ic_stat_adhan, "إيقاف الأذان", ps)
            .setDeleteIntent(ps)
            .build();
    }

    static void showFallbackNotification(Context c, String name, String sound) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        Notification n = base(c, soundChannel(c, sound), name)
            .setSound(rawUri(c, sound), android.media.AudioManager.STREAM_ALARM)
            .build();
        nm.notify(FALLBACK_ID, n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        name = intent != null && intent.getStringExtra("name") != null ? intent.getStringExtra("name") : "";
        String sound = intent != null && intent.getStringExtra("sound") != null ? intent.getStringExtra("sound") : "008";
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK : 0;
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, build(this, name), type);
        } catch (Exception e) {
            AdhanScheduler.log(this, name, "fallback");
            showFallbackNotification(this, name, sound);
            AdhanScheduler.releaseWake();
            stopSelf();
            return START_NOT_STICKY;
        }
        play(sound);
        AdhanScheduler.releaseWake();
        return START_NOT_STICKY;
    }

    private void play(String sound) {
        release();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "nur:adhan");
        wakeLock.acquire(10 * 60 * 1000L);
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
            player.setDataSource(this, rawUri(this, sound));
            player.setOnCompletionListener(mp -> stopSelf());
            player.setOnErrorListener((mp, what, extra) -> {
                AdhanScheduler.log(this, name, "error");
                stopSelf();
                return true;
            });
            player.prepare();
            player.start();
            AdhanScheduler.log(this, name, "played");
        } catch (Exception e) {
            AdhanScheduler.log(this, name, "error");
            stopSelf();
        }
    }

    private void release() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            player.release();
            player = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    }

    @Override
    public void onDestroy() {
        release();
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        // إغلاق شاشة الأذان إن كانت ظاهرة
        sendBroadcast(new Intent(ACTION_DONE).setPackage(getPackageName()));
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
