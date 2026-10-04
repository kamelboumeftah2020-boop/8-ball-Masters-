package app.nur.quran;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import androidx.test.core.app.ApplicationProvider;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

/** اختبار منظومة الأذان: الجدولة، والمنبّه، والخدمة، والإشعار، وإعادة الجدولة بعد التشغيل. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AdhanTest {

    private Context ctx;
    private ShadowAlarmManager alarms;

    @Before
    public void setUp() {
        ctx = ApplicationProvider.getApplicationContext();
        alarms = shadowOf((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE));
        ShadowAlarmManager.setCanScheduleExactAlarms(true);
        AdhanScheduler.prefs(ctx).edit().clear().commit();
    }

    private JSONArray items(long... ats) throws Exception {
        JSONArray a = new JSONArray();
        String[] names = {"الفجر", "الظهر", "العصر", "المغرب", "العشاء"};
        for (int i = 0; i < ats.length; i++) {
            a.put(new JSONObject().put("id", 1000 + i).put("at", ats[i]).put("name", names[i % 5]).put("key", "k" + i));
        }
        return a;
    }

    @Test
    public void schedulesExactAlarmsForFutureTimesOnly() throws Exception {
        long now = System.currentTimeMillis();
        AdhanScheduler.save(ctx, items(now - 60000, now + 3600000, now + 7200000), "007");
        List<ShadowAlarmManager.ScheduledAlarm> list = alarms.getScheduledAlarms();
        assertEquals("الأوقات الماضية لا تُجدول", 2, list.size());
        for (ShadowAlarmManager.ScheduledAlarm a : list) {
            assertEquals(AlarmManager.RTC_WAKEUP, a.getType());
            assertTrue("منبّه دقيق يعمل في وضع توفير الطاقة", a.isAllowWhileIdle());
        }
        assertEquals(now + 3600000, alarms.getNextScheduledAlarm().getTriggerAtMs());
        assertEquals("007", AdhanScheduler.sound(ctx));
    }

    @Test
    public void rescheduleReplacesOldAlarms() throws Exception {
        long now = System.currentTimeMillis();
        AdhanScheduler.save(ctx, items(now + 3600000, now + 7200000, now + 9000000), "008");
        AdhanScheduler.save(ctx, items(now + 4000000), "008");
        assertEquals("إعادة الجدولة تلغي المنبّهات السابقة", 1, alarms.getScheduledAlarms().size());
    }

    @Test
    public void alarmStartsAdhanServiceWhichPlaysOnAlarmStream() throws Exception {
        long now = System.currentTimeMillis();
        AdhanScheduler.save(ctx, items(now + 1000), "001");
        ShadowAlarmManager.ScheduledAlarm a = alarms.getNextScheduledAlarm();
        // إطلاق المنبّه كما يفعل النظام
        Intent fired = shadowOf(a.operation).getSavedIntent();
        assertEquals(AdhanScheduler.ACTION_ADHAN, fired.getAction());
        new AdhanReceiver().onReceive(ctx, fired);
        Intent svc = shadowOf((Application) ctx).getNextStartedService();
        assertNotNull("المستقبل يشغّل خدمة الأذان", svc);
        assertEquals(AdhanService.class.getName(), svc.getComponent().getClassName());
        assertEquals("الفجر", svc.getStringExtra("name"));
        assertEquals("001", svc.getStringExtra("sound"));

        // تشغيل الخدمة: إشعار أمامي وصوت الأذان على قناة المنبّه
        int res = ctx.getResources().getIdentifier("adhan_001", "raw", ctx.getPackageName());
        assertTrue("ملف صوت الأذان مضمّن", res != 0);
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(ctx, android.net.Uri.parse("android.resource://" + ctx.getPackageName() + "/" + res)), new ShadowMediaPlayer.MediaInfo(132000, 0));
        ServiceController<AdhanService> sc = Robolectric.buildService(AdhanService.class, svc).create().startCommand(0, 1);
        AdhanService service = sc.get();
        Notification n = shadowOf(service).getLastForegroundNotification();
        assertNotNull("إشعار الأذان ظاهر", n);
        assertTrue(n.extras.getString(Notification.EXTRA_TITLE).contains("الفجر"));
        assertEquals("فيه زر إيقاف الأذان", 1, n.actions.length);
        assertEquals("إيقاف الأذان", n.actions[0].title.toString());
        assertEquals(AdhanService.CHANNEL, n.getChannelId());
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        assertEquals(NotificationManager.IMPORTANCE_HIGH, nm.getNotificationChannel(AdhanService.CHANNEL).getImportance());

        android.media.MediaPlayer mp = (android.media.MediaPlayer) org.robolectric.util.ReflectionHelpers.getField(service, "player");
        assertNotNull("مشغّل الأذان يعمل", mp);
        ShadowMediaPlayer smp = shadowOf(mp);
        assertEquals(ShadowMediaPlayer.State.STARTED, smp.getState());
        AudioAttributes attrs = smp.getAudioAttributes();
        assertEquals("يُرفع على صوت المنبّه فيُسمع في الوضع الصامت", AudioAttributes.USAGE_ALARM, attrs.getUsage());

        // زر الإيقاف يوقف الخدمة
        Intent stop = new Intent(ctx, AdhanService.class).setAction(AdhanService.ACTION_STOP);
        sc.withIntent(stop).startCommand(0, 2);
        assertTrue("زر الإيقاف يوقف الأذان", shadowOf(service).isStoppedBySelf());
    }

    @Test
    public void bootReschedulesStoredAlarms() throws Exception {
        long now = System.currentTimeMillis();
        AdhanScheduler.save(ctx, items(now + 3600000, now + 7200000), "008");
        // محاكاة إعادة تشغيل الهاتف: تُمسح المنبّهات من النظام
        for (ShadowAlarmManager.ScheduledAlarm a : alarms.getScheduledAlarms()) {
            ((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).cancel(a.operation);
        }
        assertEquals(0, alarms.getScheduledAlarms().size());
        new AdhanReceiver().onReceive(ctx, new Intent(Intent.ACTION_BOOT_COMPLETED));
        assertEquals("تُعاد الجدولة بعد إعادة التشغيل", 2, alarms.getScheduledAlarms().size());
    }

    @Test
    public void fallsBackToInexactWhenExactNotAllowed() throws Exception {
        ShadowAlarmManager.setCanScheduleExactAlarms(false);
        long now = System.currentTimeMillis();
        AdhanScheduler.save(ctx, items(now + 3600000), "008");
        assertEquals("يُجدول ولو دون إذن المنبّه الدقيق", 1, alarms.getScheduledAlarms().size());
        assertFalse(AdhanScheduler.canExact(ctx));
    }
}
