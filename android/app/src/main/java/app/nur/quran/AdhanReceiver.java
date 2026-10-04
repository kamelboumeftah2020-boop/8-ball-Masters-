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
            String name = intent.getStringExtra("name");
            String sound = AdhanScheduler.sound(context);
            // يبقى المعالج مستيقظًا حتى تبدأ الخدمة (تُفلته الخدمة بعد أخذ قفلها)
            AdhanScheduler.holdWake(context);
            Intent svc = new Intent(context, AdhanService.class).putExtra("name", name).putExtra("sound", sound);
            try {
                ContextCompat.startForegroundService(context, svc);
                AdhanScheduler.log(context, name, "fired");
            } catch (Exception e) {
                // منع النظامُ الخدمة: إشعار بصوت الأذان نفسه وشاشة الأذان فوق القفل
                AdhanScheduler.log(context, name, "fallback");
                AdhanService.showFallbackNotification(context, name, sound);
                AdhanScheduler.releaseWake();
            }
            return;
        }
        // BOOT_COMPLETED وTIME_SET وTIMEZONE_CHANGED وتحديث التطبيق
        AdhanScheduler.scheduleStored(context);
        ReminderScheduler.scheduleStored(context);
        PrayerWidget.updateAll(context);
    }
}
