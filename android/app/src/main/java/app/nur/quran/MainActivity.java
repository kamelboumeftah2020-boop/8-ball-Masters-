package app.nur.quran;

import android.os.Bundle;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // إضافة التشغيل في الخلفية (المواعظ والتلاوات)
        registerPlugin(NurMediaPlugin.class);
        // الأذان: منبّهات دقيقة وخدمة ترفع الأذان والتطبيق مغلق
        registerPlugin(NurAdhanPlugin.class);
        super.onCreate(savedInstanceState);
        // تقديم الصوتيات المحمّلة مع دعم التقديم داخل الملف
        getBridge().setWebViewClient(new NurWebViewClient(getBridge()));
        // في وضع القراءة بملء الشاشة: تظهر الأشرطة مؤقتًا بالسحب من الحافة ثم تختفي
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
            .setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}
