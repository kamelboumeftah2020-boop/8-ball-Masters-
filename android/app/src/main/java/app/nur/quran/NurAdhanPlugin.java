package app.nur.quran;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

/** واجهة الأذان للتطبيق: الجدولة، والتجربة، والحالة، وإعدادات البطارية والمنبّهات الدقيقة. */
@CapacitorPlugin(name = "NurAdhan")
public class NurAdhanPlugin extends Plugin {

    @PluginMethod
    public void schedule(PluginCall call) {
        JSArray items = call.getArray("items", new JSArray());
        String sound = call.getString("sound", "008");
        AdhanScheduler.save(getContext(), items, sound);
        JSObject r = new JSObject();
        r.put("scheduled", AdhanScheduler.scheduleStored(getContext()));
        r.put("exact", AdhanScheduler.canExact(getContext()));
        call.resolve(r);
    }

    @PluginMethod
    public void cancelAll(PluginCall call) {
        AdhanScheduler.cancelAll(getContext());
        AdhanScheduler.prefs(getContext()).edit().remove("items").apply();
        call.resolve();
    }

    @PluginMethod
    public void test(PluginCall call) {
        int seconds = call.getInt("seconds", 10);
        AdhanScheduler.prefs(getContext()).edit().putString("sound", call.getString("sound", AdhanScheduler.sound(getContext()))).apply();
        AdhanScheduler.setAlarm(getContext(), 999999, System.currentTimeMillis() + seconds * 1000L, call.getString("name", "تجربة"), "test");
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), AdhanService.class));
        call.resolve();
    }

    @PluginMethod
    public void status(PluginCall call) {
        Context c = getContext();
        JSObject r = new JSObject();
        r.put("exact", AdhanScheduler.canExact(c));
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        r.put("batteryIgnored", Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (pm != null && pm.isIgnoringBatteryOptimizations(c.getPackageName())));
        r.put("notifications", androidx.core.app.NotificationManagerCompat.from(c).areNotificationsEnabled());
        JSONArray items = AdhanScheduler.stored(c);
        long now = System.currentTimeMillis();
        JSONObject next = null;
        int upcoming = 0;
        for (int i = 0; i < items.length(); i++) {
            JSONObject o = items.optJSONObject(i);
            if (o == null || o.optLong("at") <= now) continue;
            upcoming++;
            if (next == null || o.optLong("at") < next.optLong("at")) next = o;
        }
        r.put("upcoming", upcoming);
        if (next != null) {
            r.put("nextAt", next.optLong("at"));
            r.put("nextName", next.optString("name"));
        }
        call.resolve(r);
    }

    @PluginMethod
    public void openExactSettings(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        }
        call.resolve();
    }

    @PluginMethod
    public void requestIgnoreBattery(PluginCall call) {
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception e) {
            Intent i = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        }
        call.resolve();
    }
}
