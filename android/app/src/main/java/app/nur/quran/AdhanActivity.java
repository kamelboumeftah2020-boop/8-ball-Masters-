package app.nur.quran;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/** شاشة الأذان: تظهر فوق قفل الشاشة وتوقظها، مع زر لإيقاف الأذان. */
public class AdhanActivity extends Activity {

    private final BroadcastReceiver done = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(0xFF0B3F37);
        getWindow().setNavigationBarColor(0xFF0B3F37);
        setContentView(layout(getIntent().getStringExtra("name")));
        ContextCompat.registerReceiver(this, done, new IntentFilter(AdhanService.ACTION_DONE), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private View layout(String name) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { 0xFF0B3F37, 0xFF0F6B5C });
        root.setBackground(bg);

        TextView small = text("حان الآن موعد صلاة", 20, 0xCCFFFFFF, false);
        TextView big = text(name == null || name.isEmpty() ? "الصلاة" : name, 56, Color.WHITE, true);
        TextView sub = text("حيّ على الصلاة، حيّ على الفلاح", 18, 0xFFE9D9AB, false);
        sub.setPadding(0, dp(6), 0, dp(40));
        TextView hint = text("يُستحب أن تقول مثل ما يقول المؤذن، ثم تصلي على النبي ﷺ وتسأل له الوسيلة.", 15, 0xB3FFFFFF, false);
        hint.setPadding(0, 0, 0, dp(36));

        Button stop = new Button(this);
        stop.setText("إيقاف الأذان");
        stop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        stop.setTextColor(0xFF0B3F37);
        stop.setAllCaps(false);
        GradientDrawable sb = new GradientDrawable();
        sb.setColor(Color.WHITE);
        sb.setCornerRadius(dp(30));
        stop.setBackground(sb);
        stop.setPadding(dp(36), dp(14), dp(36), dp(14));
        stop.setOnClickListener(v -> stopAdhan());

        root.addView(small);
        root.addView(big);
        root.addView(sub);
        root.addView(hint);
        root.addView(stop, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return root;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void stopAdhan() {
        stopService(new Intent(this, AdhanService.class));
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(AdhanService.FALLBACK_ID);
        // فتح التطبيق بعد الإيقاف إن كان الهاتف غير مقفل
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (km != null && !km.isKeyguardLocked()) {
            startActivity(new Intent(this, MainActivity.class).putExtra("route", "#/adhan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        finish();
    }

    /** زر الرجوع وأزرار الصوت توقف الأذان، كما في المنبّه. */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            stopAdhan();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        try { unregisterReceiver(done); } catch (Exception ignored) { }
        super.onDestroy();
    }
}
