package com.sada.podcasts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Lets the web app hand its followed podcasts to {@link EpisodeCheckWorker}, which
 * checks for new episodes in the background and posts notifications. Tapping a
 * notification opens the app on that podcast ("openRoute" event / consumeRoute()).
 */
@CapacitorPlugin(
    name = "EpisodeChecker",
    permissions = { @Permission(alias = "notifications", strings = { Manifest.permission.POST_NOTIFICATIONS }) }
)
public class EpisodeCheckerPlugin extends Plugin {

    static final String PREFS = "episode_checker";
    static final String KEY_PODCASTS = "podcasts";
    static final String KEY_COUNTRY = "country";
    static final String LATEST_PREFIX = "latest_";
    static final String EXTRA_ROUTE = "route";
    static final String WORK_NAME = "episode-check";

    private String pendingRoute;

    @Override
    public void load() {
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            pendingRoute = intent.getStringExtra(EXTRA_ROUTE);
            intent.removeExtra(EXTRA_ROUTE);
        }
    }

    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);
        String route = intent.getStringExtra(EXTRA_ROUTE);
        if (route == null) return;
        intent.removeExtra(EXTRA_ROUTE);
        JSObject data = new JSObject();
        data.put("route", route);
        notifyListeners("openRoute", data, true);
    }

    @PluginMethod
    public void consumeRoute(PluginCall call) {
        JSObject res = new JSObject();
        res.put("route", pendingRoute);
        pendingRoute = null;
        call.resolve(res);
    }

    @PluginMethod
    public void sync(PluginCall call) {
        JSArray podcasts = call.getArray("podcasts", new JSArray());
        String country = call.getString("country", "us");
        Context ctx = getContext();
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        JSONArray stored = new JSONArray();
        try {
            for (int i = 0; i < podcasts.length(); i++) {
                JSONObject p = podcasts.getJSONObject(i);
                String id = p.optString("id");
                if (id.isEmpty()) continue;
                JSONObject entry = new JSONObject();
                entry.put("id", id);
                entry.put("title", p.optString("title"));
                if (!p.optString("feedUrl").isEmpty()) entry.put("feedUrl", p.optString("feedUrl"));
                stored.put(entry);
                // Keep whichever "newest known episode" date is later: the app's or the worker's.
                String fromApp = p.optString("latest", "");
                String known = prefs.getString(LATEST_PREFIX + id, "");
                if (fromApp.compareTo(known) > 0) edit.putString(LATEST_PREFIX + id, fromApp);
            }
        } catch (JSONException e) {
            call.reject("Invalid podcasts", e);
            return;
        }
        edit.putString(KEY_PODCASTS, stored.toString()).putString(KEY_COUNTRY, country).apply();

        WorkManager wm = WorkManager.getInstance(ctx);
        if (stored.length() == 0) {
            wm.cancelUniqueWork(WORK_NAME);
        } else {
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(EpisodeCheckWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();
            wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req);
        }
        call.resolve();
    }

    @PluginMethod
    public void requestPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || getPermissionState("notifications") == PermissionState.GRANTED) {
            JSObject res = new JSObject();
            res.put("granted", true);
            call.resolve(res);
            return;
        }
        requestPermissionForAlias("notifications", call, "permissionCallback");
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        JSObject res = new JSObject();
        res.put("granted", getPermissionState("notifications") == PermissionState.GRANTED);
        call.resolve(res);
    }
}
