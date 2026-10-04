package app.nur.quran;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

/** يستقبل منبّه الأذان فيشغّل خدمة الأذان، ويعيد الجدولة بعد إعادة التشغيل أو تغيّر الوقت. */
public class AdhanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AdhanScheduler.ACTION_ADHAN.equals(action)) {
            Intent svc = new Intent(context, AdhanService.class)
                .putExtra("name", intent.getStringExtra("name"))
                .putExtra("sound", AdhanScheduler.sound(context));
            try {
                ContextCompat.startForegroundService(context, svc);
            } catch (Exception e) {
                // إن منع النظام الخدمة نعرض إشعارًا على الأقل
                AdhanService.showFallbackNotification(context, intent.getStringExtra("name"));
            }
            return;
        }
        // BOOT_COMPLETED وTIME_SET وTIMEZONE_CHANGED وتحديث التطبيق
        AdhanScheduler.scheduleStored(context);
    }
}
