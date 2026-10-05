package app.nur.quran;

import android.app.NotificationManager;
import android.content.ComponentName;
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

/**
 * واجهة الأذان للتطبيق: الجدولة، والتجربة، والحالة وسجل آخر أذان، وإعدادات البطارية والمنبّهات
 * والتشغيل التلقائي؛ ومعها التذكيرات، وبيانات الويدجت، وفتح الصفحة المطلوبة عند لمس إشعار.
 */
@CapacitorPlugin(name = "NurAdhan")
public class NurAdhanPlugin extends Plugin {

    private String pendingRoute;

    @Override
    public void load() {
        if (getActivity() != null) takeIntent(getActivity().getIntent());
    }

    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);
        takeIntent(intent);
        if (pendingRoute != null) {
            JSObject d = new JSObject();
            d.put("route", pendingRoute);
            pendingRoute = null;
            notifyListeners("route", d, true);
        }
    }

    private void takeIntent(Intent intent) {
        if (intent == null) return;
        String r = intent.getStringExtra("route");
        if (r != null && r.startsWith("#/")) {
            pendingRoute = r;
            intent.removeExtra("route");
        }
    }

    @PluginMethod
    public void takeRoute(PluginCall call) {
        JSObject r = new JSObject();
        if (pendingRoute != null) r.put("route", pendingRoute);
        pendingRoute = null;
        call.resolve(r);
    }

    @PluginMethod
    public void reminders(PluginCall call) {
        JSObject r = new JSObject();
        r.put("scheduled", ReminderScheduler.save(getContext(), call.getArray("items", new JSArray())));
        call.resolve(r);
    }

    @PluginMethod
    public void widget(PluginCall call) {
        PrayerWidget.save(getContext(), call.getArray("items", new JSArray()), call.getString("city", ""));
        call.resolve();
    }

    @PluginMethod
    public void openFullScreenSettings(PluginCall call) {
        Context c = getContext();
        Intent i;
        if (Build.VERSION.SDK_INT >= 34) {
            i = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + c.getPackageName()));
        } else {
            i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + c.getPackageName()));
        }
        start(i);
        call.resolve();
    }

    /** إعداد «التشغيل التلقائي» في هواتف شاومي وأوبو وفيفو وهواوي وغيرها، وإلا صفحة التطبيق. */
    @PluginMethod
    public void openAutostart(PluginCall call) {
        String[][] targets = {
            { "com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity" },
            { "com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity" },
            { "com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity" },
            { "com.oplus.safecenter", "com.oplus.safecenter.permission.startup.StartupAppListActivity" },
            { "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity" },
            { "com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity" },
            { "com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity" },
            { "com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity" },
            { "com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity" },
            { "com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity" },
            { "com.asus.mobilemanager", "com.asus.mobilemanager.powersaver.PowerSaverSettings" },
            { "com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity" },
        };
        boolean opened = false;
        for (String[] t : targets) {
            try {
                Intent i = new Intent().setComponent(new ComponentName(t[0], t[1]));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                opened = true;
                break;
            } catch (Exception ignored) { }
        }
        if (!opened) start(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName())));
        JSObject r = new JSObject();
        r.put("special", opened);
        call.resolve(r);
    }

    /** شاومي: إذن «العرض على شاشة القفل» و«النوافذ المنبثقة في الخلفية» لتظهر شاشة الأذان. */
    @PluginMethod
    public void openLockScreenSettings(PluginCall call) {
        String m = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase();
        boolean opened = false;
        if (m.contains("xiaomi") || m.contains("redmi") || m.contains("poco")) {
            for (String cls : new String[] { "com.miui.permcenter.permissions.PermissionsEditorActivity", "com.miui.permcenter.permissions.AppPermissionsEditorActivity" }) {
                try {
                    Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR").setClassName("com.miui.securitycenter", cls)
                        .putExtra("extra_pkgname", getContext().getPackageName()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(i);
                    opened = true;
                    break;
                } catch (Exception ignored) { }
            }
        }
        if (!opened) start(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName())));
        call.resolve();
    }

    private void start(Intent i) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception e) {
            Intent d = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
            d.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { getContext().startActivity(d); } catch (Exception ignored) { }
        }
    }

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
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        r.put("fullScreen", Build.VERSION.SDK_INT < 34 || (nm != null && nm.canUseFullScreenIntent()));
        r.put("manufacturer", Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase());
        r.put("sdk", Build.VERSION.SDK_INT);
        try {
            r.put("log", new JSArray(AdhanScheduler.logs(c).toString()));
        } catch (Exception ignored) { }
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
