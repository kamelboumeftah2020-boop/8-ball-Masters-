package app.nur.quran;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * التذكيرات (أذكار الصباح والمساء، الكهف، الصيام، الورد): يحسب التطبيق أوقاتها لثلاثين يومًا
 * ويرسلها هنا، فتُجدول في النظام وتعمل والتطبيق مغلق، ويُعاد جدولتها بعد إعادة التشغيل.
 */
public final class ReminderScheduler {

    static final String ACTION = "app.nur.quran.REMIND";
    static final String CHANNEL = "nur_reminders";
    private static final String PREFS = "nur_reminders";

    private ReminderScheduler() {}

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static JSONArray stored(Context c) {
        try {
            return new JSONArray(prefs(c).getString("items", "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    static int save(Context c, JSONArray items) {
        cancelAll(c);
        prefs(c).edit().putString("items", items.toString()).apply();
        return scheduleStored(c);
    }

    static int scheduleStored(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return 0;
        JSONArray items = stored(c);
        long now = System.currentTimeMillis();
        int n = 0;
        for (int i = 0; i < items.length(); i++) {
            JSONObject o = items.optJSONObject(i);
            if (o == null || o.optLong("at") <= now) continue;
            PendingIntent pi = pending(c, o, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                if (AdhanScheduler.canExact(c)) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, o.optLong("at"), pi);
                else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, o.optLong("at"), pi);
            } catch (SecurityException e) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, o.optLong("at"), pi);
            }
            n++;
        }
        return n;
    }

    static void cancelAll(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        JSONArray items = stored(c);
        for (int i = 0; i < items.length(); i++) {
            JSONObject o = items.optJSONObject(i);
            if (o == null || am == null) continue;
            PendingIntent pi = pending(c, o, PendingIntent.FLAG_NO_CREATE);
            if (pi != null) { am.cancel(pi); pi.cancel(); }
        }
    }

    private static PendingIntent pending(Context c, JSONObject o, int flag) {
        Intent i = new Intent(c, ReminderReceiver.class).setAction(ACTION)
            .putExtra("title", o.optString("title"))
            .putExtra("text", o.optString("text"))
            .putExtra("route", o.optString("route"))
            .putExtra("id", o.optInt("id"));
        return PendingIntent.getBroadcast(c, 500000 + (o.optInt("id") % 400000), i, flag | PendingIntent.FLAG_IMMUTABLE);
    }

    static void show(Context c, int id, String title, String text, String route) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "التذكيرات", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("أذكار الصباح والمساء، وسورة الكهف، والصيام، والورد اليومي");
            nm.createNotificationChannel(ch);
        }
        Intent open = new Intent(c, MainActivity.class).putExtra("route", route)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(c, 600000 + (id % 400000), open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        nm.notify(8000 + (id % 1000), new NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_adhan)
            .setColor(0xFF0F6B5C)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(content)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build());
    }
}
