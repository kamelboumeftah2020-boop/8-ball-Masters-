package app.nur.quran;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // إضافة التشغيل في الخلفية (المواعظ والتلاوات)
        registerPlugin(NurMediaPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
