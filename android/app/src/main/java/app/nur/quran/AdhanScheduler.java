package app.nur.quran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

/** جدولة منبّهات الأذان الدقيقة وحفظها لإعادة جدولتها بعد إعادة التشغيل. */
public final class AdhanScheduler {

    static final String ACTION_ADHAN = "app.nur.quran.ADHAN";
    private static final String PREFS = "nur_adhan";

    private AdhanScheduler() {}

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** يحفظ قائمة الأذان (JSON) والصوت ثم يجدولها. */
    static void save(Context c, JSONArray items, String sound) {
        cancelAll(c);
        prefs(c).edit().putString("items", items.toString()).putString("sound", sound).apply();
        scheduleStored(c);
    }

    static JSONArray stored(Context c) {
        try {
            return new JSONArray(prefs(c).getString("items", "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    static String sound(Context c) {
        return prefs(c).getString("sound", "008");
    }

    /** يجدول كل الأوقات القادمة المحفوظة. */
    static int scheduleStored(Context c) {
        JSONArray items = stored(c);
        long now = System.currentTimeMillis();
        int n = 0;
        for (int i = 0; i < items.length(); i++) {
            JSONObject o = items.optJSONObject(i);
            if (o == null) continue;
            long at = o.optLong("at");
            if (at <= now) continue;
            setAlarm(c, o.optInt("id"), at, o.optString("name"), o.optString("key"));
            n++;
        }
        return n;
    }

    static boolean canExact(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        return am != null && am.canScheduleExactAlarms();
    }

    static void setAlarm(Context c, int id, long at, String name, String key) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(c, id, name, key, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (canExact(c)) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    static void cancelAll(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        JSONArray items = stored(c);
        for (int i = 0; i < items.length(); i++) {
            JSONObject o = items.optJSONObject(i);
            if (o == null || am == null) continue;
            PendingIntent pi = pending(c, o.optInt("id"), "", "", PendingIntent.FLAG_NO_CREATE);
            if (pi != null) { am.cancel(pi); pi.cancel(); }
        }
    }

    private static PendingIntent pending(Context c, int id, String name, String key, int flag) {
        Intent i = new Intent(c, AdhanReceiver.class).setAction(ACTION_ADHAN)
            .putExtra("name", name).putExtra("key", key).putExtra("id", id);
        return PendingIntent.getBroadcast(c, id, i, flag | PendingIntent.FLAG_IMMUTABLE);
    }
}
