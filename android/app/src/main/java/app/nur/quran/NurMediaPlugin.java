package app.nur.quran;

import android.content.Intent;
import android.os.Build;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * يشغّل خدمة في المقدّمة أثناء تشغيل الصوت، حتى يستمر الاستماع
 * والشاشة مطفأة أو التطبيق في الخلفية.
 */
@CapacitorPlugin(name = "NurMedia")
public class NurMediaPlugin extends Plugin {

    @PluginMethod
    public void start(PluginCall call) {
        Intent intent = new Intent(getContext(), NurMediaService.class);
        intent.putExtra("title", call.getString("title", "نور"));
        intent.putExtra("text", call.getString("text", ""));
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("تعذّر تشغيل خدمة الخلفية", e);
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), NurMediaService.class));
        call.resolve();
    }
}
