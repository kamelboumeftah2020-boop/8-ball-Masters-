package app.nur.quran;

import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * يربط المشغّل في التطبيق بإشعار التشغيل: يرسل إليه العنوان والحالة،
 * ويعيد إلى التطبيق أوامر الإشعار وشاشة القفل وأزرار السماعة (حدث action).
 */
@CapacitorPlugin(name = "NurMedia")
public class NurMediaPlugin extends Plugin {

    @Override
    public void load() {
        NurMediaService.listener = (action, position) -> {
            JSObject d = new JSObject();
            d.put("action", action);
            d.put("position", position);
            notifyListeners("action", d, true);
        };
    }

    @PluginMethod
    public void update(PluginCall call) {
        Intent i = new Intent(getContext(), NurMediaService.class).setAction(NurMediaService.ACTION_UPDATE)
            .putExtra("title", call.getString("title", "نور"))
            .putExtra("text", call.getString("text", ""))
            .putExtra("playing", call.getBoolean("playing", true))
            .putExtra("repeat", call.getBoolean("repeat", false))
            .putExtra("hasPrev", call.getBoolean("hasPrev", true))
            .putExtra("hasNext", call.getBoolean("hasNext", true))
            .putExtra("position", (long) (call.getDouble("position", 0d) * 1000))
            .putExtra("duration", (long) (call.getDouble("duration", 0d) * 1000));
        try {
            ContextCompat.startForegroundService(getContext(), i);
            call.resolve();
        } catch (Exception e) {
            call.reject("تعذّر تشغيل خدمة الخلفية", e);
        }
    }

    /** للتوافق مع النسخ السابقة */
    @PluginMethod
    public void start(PluginCall call) {
        update(call);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), NurMediaService.class));
        call.resolve();
    }
}
