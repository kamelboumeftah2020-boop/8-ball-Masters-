package app.nur.quran;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
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

/** اختبار الإضافات الأصلية: منبّه الساعة وشاشة الأذان، والتذكيرات، والويدجت، ومشغّل الإشعارات. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NativeFeaturesTest {

    private Context ctx;
    private ShadowAlarmManager alarms;
    private NotificationManager nm;

    @Before
    public void setUp() {
        ctx = ApplicationProvider.getApplicationContext();
        alarms = shadowOf((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE));
        ShadowAlarmManager.setCanScheduleExactAlarms(true);
        nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        AdhanScheduler.prefs(ctx).edit().clear().commit();
        ReminderScheduler.prefs(ctx).edit().clear().commit();
    }

    @Test
    public void adhanUsesAlarmClockSoItSurvivesDozeAndLockScreen() throws Exception {
        long at = System.currentTimeMillis() + 3600000;
        JSONArray a = new JSONArray().put(new JSONObject().put("id", 1).put("at", at).put("name", "الظهر").put("key", "Dhuhr"));
        AdhanScheduler.save(ctx, a, "008");
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        assertNotNull("منبّه ساعة ظاهر للنظام", am.getNextAlarmClock());
        assertEquals(at, am.getNextAlarmClock().getTriggerTime());
    }

    @Test
    public void serviceNotificationShowsAdhanScreenOverLockAndLogsPlayback() {
        ShadowMediaPlayer.addMediaInfo(DataSource.toDataSource(ctx, AdhanService.rawUri(ctx, "008")), new ShadowMediaPlayer.MediaInfo(180000, 0));
        Intent i = new Intent(ctx, AdhanService.class).putExtra("name", "العصر").putExtra("sound", "008");
        ServiceController<AdhanService> c = Robolectric.buildService(AdhanService.class, i).create().startCommand(0, 1);
        Notification n = shadowOf(c.get()).getLastForegroundNotification();
        assertNotNull(n);
        assertNotNull("شاشة الأذان فوق القفل", n.fullScreenIntent);
        assertEquals(AdhanActivity.class.getName(), shadowOf(n.fullScreenIntent).getSavedIntent().getComponent().getClassName());
        JSONArray log = AdhanScheduler.logs(ctx);
        assertEquals("played", log.optJSONObject(0).optString("r"));
        assertEquals("العصر", log.optJSONObject(0).optString("name"));
    }

    @Test
    public void fallbackNotificationPlaysAdhanSoundOnAlarmStream() {
        AdhanService.showFallbackNotification(ctx, "المغرب", "007");
        Notification n = shadowOf(nm).getNotification(AdhanService.FALLBACK_ID);
        assertNotNull(n);
        assertNotNull(n.fullScreenIntent);
        NotificationChannel ch = nm.getNotificationChannel(n.getChannelId());
        assertNotNull("قناة الأذان الاحتياطية", ch);
        assertTrue("صوت القناة هو الأذان", ch.getSound().toString().endsWith("/" + AdhanService.rawRes(ctx, "007")));
        assertEquals(AudioAttributes.USAGE_ALARM, ch.getAudioAttributes().getUsage());
    }

    @Test
    public void receiverKeepsCpuAwakeAndStartsService() {
        Intent alarm = new Intent(ctx, AdhanReceiver.class).setAction(AdhanScheduler.ACTION_ADHAN).putExtra("name", "الفجر");
        new AdhanReceiver().onReceive(ctx, alarm);
        Intent started = shadowOf((Application) ctx).getNextStartedService();
        assertNotNull(started);
        assertEquals(AdhanService.class.getName(), started.getComponent().getClassName());
        assertEquals("fired", AdhanScheduler.logs(ctx).optJSONObject(0).optString("r"));
    }

    @Test
    public void remindersAreScheduledAndShowNotificationWithRoute() throws Exception {
        long now = System.currentTimeMillis();
        JSONArray items = new JSONArray()
            .put(new JSONObject().put("id", 10051).put("at", now - 1000).put("title", "قديم").put("text", "").put("route", "#/"))
            .put(new JSONObject().put("id", 10061).put("at", now + 600000).put("title", "أذكار الصباح").put("text", "حان وقت أذكار الصباح").put("route", "#/adhkar/morning"))
            .put(new JSONObject().put("id", 10063).put("at", now + 900000).put("title", "الكهف").put("text", "سورة الكهف").put("route", "#/mushaf/18"));
        int n = ReminderScheduler.save(ctx, items);
        assertEquals(2, n);
        assertEquals(2, alarms.getScheduledAlarms().size());
        Intent fired = shadowOf(alarms.getNextScheduledAlarm().operation).getSavedIntent();
        new ReminderReceiver().onReceive(ctx, fired);
        Notification shown = shadowOf(nm).getAllNotifications().get(0);
        assertEquals("أذكار الصباح", shown.extras.getString(Notification.EXTRA_TITLE));
        assertEquals("#/adhkar/morning", shadowOf(shown.contentIntent).getSavedIntent().getStringExtra("route"));
        // إعادة الحفظ تلغي القديم
        ReminderScheduler.save(ctx, new JSONArray());
        assertEquals(0, alarms.getScheduledAlarms().size());
    }

    @Test
    public void widgetShowsNextPrayerAndTodaysTimes() throws Exception {
        long now = System.currentTimeMillis();
        JSONArray items = new JSONArray();
        String[] names = { "الفجر", "الظهر", "العصر", "المغرب", "العشاء" };
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 5);
        cal.set(java.util.Calendar.MINUTE, 0);
        for (int i = 0; i < 5; i++) items.put(new JSONObject().put("at", cal.getTimeInMillis() + i * 3L * 3600000).put("name", names[i]));
        PrayerWidget.prefs(ctx).edit().putString("items", items.toString()).putString("city", "الجزائر العاصمة").commit();
        View v = PrayerWidget.build(ctx, now).apply(ctx, new FrameLayout(ctx));
        assertEquals("الفجر", ((TextView) v.findViewById(R.id.name)).getText().toString());
        assertEquals("الجزائر العاصمة", ((TextView) v.findViewById(R.id.city)).getText().toString());
        assertEquals("العشاء", ((TextView) v.findViewById(R.id.n5)).getText().toString());
        assertTrue(((TextView) v.findViewById(R.id.label)).getText().toString().contains("غدًا"));
        // بلا بيانات: دعوة لفتح التطبيق
        PrayerWidget.prefs(ctx).edit().clear().commit();
        View empty = PrayerWidget.build(ctx, now).apply(ctx, new FrameLayout(ctx));
        assertEquals("افتح التطبيق", ((TextView) empty.findViewById(R.id.name)).getText().toString());
    }

    @Test
    public void mediaNotificationHasPlayerControlsAndForwardsCommands() {
        List<String> got = new ArrayList<>();
        NurMediaService.listener = (action, pos) -> got.add(action);
        Intent up = new Intent(ctx, NurMediaService.class).setAction(NurMediaService.ACTION_UPDATE)
            .putExtra("title", "سورة الإخلاص").putExtra("text", "مشاري العفاسي").putExtra("playing", true)
            .putExtra("repeat", false).putExtra("duration", 60000L);
        ServiceController<NurMediaService> c = Robolectric.buildService(NurMediaService.class, up).create().startCommand(0, 1);
        Notification n = shadowOf(c.get()).getLastForegroundNotification();
        assertNotNull(n);
        assertEquals("سورة الإخلاص", n.extras.getString(Notification.EXTRA_TITLE));
        assertEquals(5, n.actions.length);
        assertEquals("السابق", n.actions[0].title.toString());
        assertEquals("إيقاف مؤقت", n.actions[1].title.toString());
        assertEquals("التالي", n.actions[2].title.toString());
        assertEquals("تكرار", n.actions[3].title.toString());
        // زر من الإشعار يصل إلى التطبيق
        c.withIntent(new Intent(ctx, NurMediaService.class).setAction(NurMediaService.CMD).putExtra("cmd", "next")).startCommand(0, 2);
        assertEquals("next", got.get(got.size() - 1));
        // الإيقاف المؤقت يغيّر الزر
        c.withIntent(new Intent(up).putExtra("playing", false)).startCommand(0, 3);
        Notification paused = shadowOf(nm).getNotification(7001);
        assertEquals("تشغيل", paused.actions[1].title.toString());
        NurMediaService.listener = null;
    }
}
