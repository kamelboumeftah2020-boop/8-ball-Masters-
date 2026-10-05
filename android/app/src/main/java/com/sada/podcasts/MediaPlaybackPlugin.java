package com.sada.podcasts;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Bridges the web player to {@link MediaPlaybackService}: the service keeps the app
 * alive while audio plays in the background and shows the media notification /
 * lock-screen controls. Button presses come back to JS as "action" events.
 */
@CapacitorPlugin(name = "MediaPlayback")
public class MediaPlaybackPlugin extends Plugin {

    @Override
    public void load() {
        MediaPlaybackService.listener = (action, position) -> {
            JSObject data = new JSObject();
            data.put("action", action);
            if (position >= 0) data.put("position", position);
            notifyListeners("action", data);
        };
    }

    @PluginMethod
    public void update(PluginCall call) {
        Bundle state = new Bundle();
        state.putString("title", call.getString("title", ""));
        state.putString("artist", call.getString("artist", ""));
        state.putString("artwork", call.getString("artwork", ""));
        state.putBoolean("playing", Boolean.TRUE.equals(call.getBoolean("playing", false)));
        state.putDouble("position", call.getDouble("position", 0.0));
        state.putDouble("duration", call.getDouble("duration", 0.0));
        state.putDouble("rate", call.getDouble("rate", 1.0));

        getActivity().runOnUiThread(() -> {
            MediaPlaybackService service = MediaPlaybackService.instance;
            if (service != null) {
                service.apply(state);
            } else if (state.getBoolean("playing")) {
                // Only start the foreground service on user-initiated playback (app is in the foreground).
                Intent intent = new Intent(getContext(), MediaPlaybackService.class);
                intent.putExtras(state);
                ContextCompat.startForegroundService(getContext(), intent);
            }
            call.resolve();
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (MediaPlaybackService.instance != null) MediaPlaybackService.instance.shutdown();
            call.resolve();
        });
    }

    @Override
    protected void handleOnDestroy() {
        MediaPlaybackService.listener = null;
        if (MediaPlaybackService.instance != null) MediaPlaybackService.instance.shutdown();
    }
}
