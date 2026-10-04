package app.nur.quran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/** ويدجت الشاشة الرئيسية: الصلاة القادمة مع عدّ تنازلي، ومواقيت اليوم، واسم المدينة. */
public class PrayerWidget extends AppWidgetProvider {

    static final String ACTION_TICK = "app.nur.quran.WIDGET_TICK";
    private static final String PREFS = "nur_widget";
    private static final int[] CELLS = { R.id.c1, R.id.c2, R.id.c3, R.id.c4, R.id.c5 };
    private static final int[] NAMES = { R.id.n1, R.id.n2, R.id.n3, R.id.n4, R.id.n5 };
    private static final int[] TIMES = { R.id.t1, R.id.t2, R.id.t3, R.id.t4, R.id.t5 };

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** يحفظ مواقيت الأيام القادمة (الصلوات الخمس) واسم المدينة، ثم يحدّث الويدجت. */
    static void save(Context c, JSONArray items, String city) {
        prefs(c).edit().putString("items", items.toString()).putString("city", city == null ? "" : city).apply();
        updateAll(c);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateAll(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TICK.equals(intent.getAction())) updateAll(context);
    }

    static String fmt(long at) {
        Date d = new Date(at);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return new SimpleDateFormat("h:mm", Locale.US).format(d) + (cal.get(Calendar.AM_PM) == Calendar.AM ? " ص" : " م");
    }

    private static boolean sameDay(long a, long b) {
        Calendar x = Calendar.getInstance(), y = Calendar.getInstance();
        x.setTimeInMillis(a);
        y.setTimeInMillis(b);
        return x.get(Calendar.YEAR) == y.get(Calendar.YEAR) && x.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR);
    }

    static RemoteViews build(Context c, long now) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_prayer);
        Intent open = new Intent(c, MainActivity.class).putExtra("route", "#/adhan")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        v.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(c, 9, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        v.setTextViewText(R.id.city, prefs(c).getString("city", ""));

        List<JSONObject> items = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString("items", "[]"));
            for (int i = 0; i < arr.length(); i++) items.add(arr.getJSONObject(i));
        } catch (Exception ignored) { }
        JSONObject next = null;
        for (JSONObject o : items) {
            if (o.optLong("at") > now && (next == null || o.optLong("at") < next.optLong("at"))) next = o;
        }
        if (next == null) {
            v.setTextViewText(R.id.name, "افتح التطبيق");
            v.setTextViewText(R.id.time, "");
            v.setTextViewText(R.id.label, "حدّد موقعك لعرض المواقيت");
            v.setViewVisibility(R.id.countdown, View.GONE);
            v.setViewVisibility(R.id.row, View.GONE);
            return v;
        }
        long at = next.optLong("at");
        v.setTextViewText(R.id.label, sameDay(at, now) ? "الصلاة القادمة" : "الصلاة القادمة · غدًا");
        v.setTextViewText(R.id.name, next.optString("name"));
        v.setTextViewText(R.id.time, fmt(at));
        v.setViewVisibility(R.id.countdown, View.VISIBLE);
        v.setChronometer(R.id.countdown, SystemClock.elapsedRealtime() + (at - now), null, true);
        v.setChronometerCountDown(R.id.countdown, true);

        // مواقيت يوم الصلاة القادمة
        List<JSONObject> day = new ArrayList<>();
        for (JSONObject o : items) if (sameDay(o.optLong("at"), at)) day.add(o);
        day.sort((a, b) -> Long.compare(a.optLong("at"), b.optLong("at")));
        v.setViewVisibility(R.id.row, day.isEmpty() ? View.GONE : View.VISIBLE);
        for (int i = 0; i < CELLS.length; i++) {
            if (i < day.size()) {
                JSONObject o = day.get(i);
                boolean isNext = o.optLong("at") == at;
                v.setViewVisibility(CELLS[i], View.VISIBLE);
                v.setTextViewText(NAMES[i], o.optString("name"));
                v.setTextViewText(TIMES[i], fmt(o.optLong("at")).replace(" ص", "").replace(" م", ""));
                v.setTextColor(TIMES[i], isNext ? 0xFFE9D9AB : 0xFFFFFFFF);
                v.setInt(CELLS[i], "setBackgroundResource", isNext ? R.drawable.widget_chip : 0);
            } else {
                v.setViewVisibility(CELLS[i], View.GONE);
            }
        }
        return v;
    }

    static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        if (m == null) return;
        int[] ids = m.getAppWidgetIds(new ComponentName(c, PrayerWidget.class));
        if (ids == null || ids.length == 0) return;
        long now = System.currentTimeMillis();
        m.updateAppWidget(ids, build(c, now));
        scheduleTick(c, now);
    }

    /** تحديث الويدجت عند دخول الصلاة القادمة. */
    private static void scheduleTick(Context c, long now) {
        long next = 0;
        try {
            JSONArray arr = new JSONArray(prefs(c).getString("items", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                long at = arr.getJSONObject(i).optLong("at");
                if (at > now && (next == 0 || at < next)) next = at;
            }
        } catch (Exception ignored) { }
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null || next == 0) return;
        Intent i = new Intent(c, PrayerWidget.class).setAction(ACTION_TICK);
        PendingIntent pi = PendingIntent.getBroadcast(c, 9, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (AdhanScheduler.canExact(c)) am.setExact(AlarmManager.RTC, next + 1000, pi);
            else am.set(AlarmManager.RTC, next + 1000, pi);
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC, next + 1000, pi);
        }
    }
}
