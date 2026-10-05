package app.nur.quran;

import static org.junit.Assert.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/** شاشة الأذان: اسم الصلاة وزر الإيقاف، ثم الدعاء وزر الإغلاق بعد انتهاء الأذان. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AdhanScreenTest {

    private static List<TextView> texts(View v, List<TextView> out) {
        if (v instanceof TextView) out.add((TextView) v);
        if (v instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) texts(((ViewGroup) v).getChildAt(i), out);
        return out;
    }

    private static TextView find(View root, String s) {
        for (TextView t : texts(root, new ArrayList<>())) if (t.getText().toString().equals(s)) return t;
        return null;
    }

    private static void snap(View root, String name) throws Exception {
        root.measure(View.MeasureSpec.makeMeasureSpec(1233, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(2673, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, 1233, 2673);
        Bitmap b = Bitmap.createBitmap(1233, 2673, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(b));
        File dir = new File("build/screens");
        dir.mkdirs();
        try (FileOutputStream f = new FileOutputStream(new File(dir, name))) { b.compress(Bitmap.CompressFormat.PNG, 100, f); }
    }

    @Test
    public void showsPrayerAndStopThenDuaAndClose() throws Exception {
        ApplicationProvider.getApplicationContext().getSharedPreferences("nur_widget", 0).edit().putString("city", "الجزائر العاصمة").commit();
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), AdhanActivity.class).putExtra("name", "الفجر");
        ActivityController<AdhanActivity> c = Robolectric.buildActivity(AdhanActivity.class, i).setup();
        View root = c.get().getWindow().getDecorView();
        assertNotNull("اسم الصلاة", find(root, "الفجر"));
        assertNotNull("المدينة", find(root, "الجزائر العاصمة"));
        TextView stop = find(root, "إيقاف الأذان");
        TextView close = find(root, "إغلاق");
        assertEquals(View.VISIBLE, stop.getVisibility());
        assertEquals(View.GONE, close.getVisibility());
        snap(((ViewGroup) root.findViewById(android.R.id.content)).getChildAt(0), "adhan-1.png");
        // انتهى الأذان: يظهر الدعاء وزر الإغلاق
        c.get().sendBroadcast(new Intent(AdhanService.ACTION_DONE).setPackage(c.get().getPackageName()));
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        assertEquals(View.GONE, stop.getVisibility());
        assertEquals(View.VISIBLE, close.getVisibility());
        assertNotNull(find(root, "الدعاء بعد الأذان"));
        snap(((ViewGroup) root.findViewById(android.R.id.content)).getChildAt(0), "adhan-2.png");
        close.performClick();
        assertTrue(c.get().isFinishing());
    }
}
