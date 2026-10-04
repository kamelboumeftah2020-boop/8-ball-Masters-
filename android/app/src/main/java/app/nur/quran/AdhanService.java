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

/** يرفع الأذان بصوت المؤذن على قناة المنبّه، مع إشعار فيه زر «إيقاف الأذان». */
public class AdhanService extends Service {

    static final String CHANNEL = "nur_adhan_alert";
    static final String ACTION_STOP = "app.nur.quran.ADHAN_STOP";
    private static final int NOTIFICATION_ID = 7101;
    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;

    static void createChannel(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "الأذان", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("رفع الأذان عند دخول وقت الصلاة");
            ch.setSound(null, null); // الصوت يُشغَّل من الخدمة نفسها
            ch.enableVibration(true);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(ch);
        }
    }

    static Notification build(Context c, String name, boolean withStop) {
        createChannel(c);
        Intent open = new Intent(c, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(c, 1, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_adhan)
            .setColor(0xFF0F6B5C)
            .setContentTitle("حان الآن موعد صلاة " + (name == null || name.isEmpty() ? "" : name))
            .setContentText("حيّ على الصلاة، حيّ على الفلاح")
            .setContentIntent(content)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true);
        if (withStop) {
            Intent stop = new Intent(c, AdhanService.class).setAction(ACTION_STOP);
            PendingIntent ps = PendingIntent.getService(c, 2, stop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            b.addAction(R.drawable.ic_stat_adhan, "إيقاف الأذان", ps).setDeleteIntent(ps).setOngoing(false);
        }
        return b.build();
    }

    static void showFallbackNotification(Context c, String name) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIFICATION_ID + 1, build(c, name, false));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String name = intent != null ? intent.getStringExtra("name") : "";
        String sound = intent != null ? intent.getStringExtra("sound") : "008";
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK : 0;
        ServiceCompat.startForeground(this, NOTIFICATION_ID, build(this, name, true), type);
        play(sound);
        return START_NOT_STICKY;
    }

    private void play(String sound) {
        release();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "nur:adhan");
        wakeLock.acquire(10 * 60 * 1000L);
        int res = getResources().getIdentifier("adhan_" + sound, "raw", getPackageName());
        if (res == 0) res = getResources().getIdentifier("adhan_008", "raw", getPackageName());
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
            player.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + res));
            player.setOnCompletionListener(mp -> stopSelf());
            player.setOnErrorListener((mp, what, extra) -> { stopSelf(); return true; });
            player.prepare();
            player.start();
        } catch (Exception e) {
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
        // يبقى الإشعار ظاهرًا بعد انتهاء الأذان دون زر الإيقاف
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
