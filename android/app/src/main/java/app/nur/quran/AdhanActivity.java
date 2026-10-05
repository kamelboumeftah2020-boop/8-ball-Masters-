package app.nur.quran;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/**
 * شاشة الأذان: تضيء الشاشة وتظهر فوق القفل بملء الشاشة، فيها الوقت واسم الصلاة والمدينة
 * وزر كبير لإيقاف الأذان؛ وبعد انتهائه (أو إيقافه) يظهر دعاء ما بعد الأذان وزر الإغلاق.
 */
public class AdhanActivity extends Activity {

    private static final String DUA = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout duaBox;
    private Button stop, close;
    private TextView hint;
    private boolean finished;

    private final BroadcastReceiver done = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showDua();
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
        getWindow().setNavigationBarColor(0xFF0F6B5C);
        setContentView(layout(getIntent().getStringExtra("name")));
        ContextCompat.registerReceiver(this, done, new IntentFilter(AdhanService.ACTION_DONE), ContextCompat.RECEIVER_NOT_EXPORTED);
        // الرجوع يوقف الأذان أولًا، ثم يغلق الشاشة
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::onBackKey);
        }
    }

    private void onBackKey() {
        if (!finished) stopAdhan(); else closeScreen(false);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setLineSpacing(0, 1.25f);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private Button button(String s, int bg, int fg) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(fg);
        GradientDrawable d = new GradientDrawable();
        d.setColor(bg);
        d.setCornerRadius(dp(32));
        b.setBackground(d);
        b.setPadding(dp(40), dp(16), dp(40), dp(16));
        b.setStateListAnimator(null);
        return b;
    }

    private LinearLayout.LayoutParams wrap(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(top);
        p.gravity = Gravity.CENTER_HORIZONTAL;
        return p;
    }

    private View layout(String name) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28), dp(36), dp(28), dp(28));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { 0xFF072B26, 0xFF0B3F37, 0xFF0F6B5C }));

        TextView app = text("نور · الأذان", 14, 0x99FFFFFF, false);
        root.addView(app, wrap(0));

        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("h:mm");
        clock.setFormat24Hour("H:mm");
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64);
        clock.setTextColor(Color.WHITE);
        clock.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        root.addView(clock, wrap(8));

        String city = getSharedPreferences("nur_widget", MODE_PRIVATE).getString("city", "");
        if (city != null && !city.isEmpty()) root.addView(text(city, 15, 0xB3FFFFFF, false), wrap(0));

        ImageView mosque = new ImageView(this);
        mosque.setImageResource(R.drawable.ic_mosque);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(dp(120), dp(120));
        mp.topMargin = dp(22);
        mp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(mosque, mp);

        root.addView(text("حان الآن موعد صلاة", 20, 0xD9FFFFFF, false), wrap(18));
        root.addView(text(name == null || name.isEmpty() ? "الصلاة" : name, 58, 0xFFE9D9AB, true), wrap(0));
        hint = text("حيّ على الصلاة، حيّ على الفلاح\nيُسنّ أن تقول مثل ما يقول المؤذن", 16, 0xB3FFFFFF, false);
        root.addView(hint, wrap(10));

        // دعاء ما بعد الأذان (يظهر بعد انتهائه أو إيقافه)
        duaBox = new LinearLayout(this);
        duaBox.setOrientation(LinearLayout.VERTICAL);
        duaBox.setPadding(dp(18), dp(14), dp(18), dp(14));
        GradientDrawable db = new GradientDrawable();
        db.setColor(0x26FFFFFF);
        db.setCornerRadius(dp(20));
        duaBox.setBackground(db);
        duaBox.addView(text("الدعاء بعد الأذان", 15, 0xFFE9D9AB, true));
        duaBox.addView(text("صلِّ على النبي ﷺ ثم قل:\n" + DUA, 18, Color.WHITE, false));
        duaBox.addView(text("«من قالها حلّت له شفاعتي يوم القيامة» — رواه البخاري", 12, 0x99FFFFFF, false));
        duaBox.setVisibility(View.GONE);
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dl.topMargin = dp(16);
        root.addView(duaBox, dl);

        View spacer = new View(this);
        root.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));

        stop = button("إيقاف الأذان", Color.WHITE, 0xFF0B3F37);
        stop.setOnClickListener(v -> stopAdhan());
        root.addView(stop, wrap(12));

        close = button("إغلاق", 0x33FFFFFF, Color.WHITE);
        close.setOnClickListener(v -> closeScreen(false));
        close.setVisibility(View.GONE);
        root.addView(close, wrap(12));

        Button open = new Button(this);
        open.setText("فتح التطبيق");
        open.setAllCaps(false);
        open.setTextColor(0xCCFFFFFF);
        open.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        open.setBackgroundColor(Color.TRANSPARENT);
        open.setOnClickListener(v -> closeScreen(true));
        root.addView(open, wrap(4));
        // يمرَّر المحتوى في الشاشات الصغيرة، ويملأ الشاشة في غيرها
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackground(root.getBackground());
        sv.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        return sv;
    }

    private void stopAdhan() {
        stopService(new Intent(this, AdhanService.class));
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(AdhanService.FALLBACK_ID);
        showDua();
    }

    /** بعد الأذان: الدعاء وزر الإغلاق، وتُغلق الشاشة وحدها بعد دقيقتين. */
    private void showDua() {
        if (finished) return;
        finished = true;
        duaBox.setVisibility(View.VISIBLE);
        hint.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);
        close.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> closeScreen(false), 2 * 60 * 1000L);
    }

    private void closeScreen(boolean openApp) {
        stopService(new Intent(this, AdhanService.class));
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(AdhanService.FALLBACK_ID);
        if (openApp) {
            startActivity(new Intent(this, MainActivity.class).putExtra("route", "#/adhan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        finish();
    }

    /** أزرار الصوت توقف الأذان كما في المنبّه، وزر الرجوع في أندرويد ١٢ فما قبل. */
    @SuppressLint("GestureBackNavigation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!finished) stopAdhan();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && Build.VERSION.SDK_INT < 33) {
            onBackKey();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        try { unregisterReceiver(done); } catch (Exception ignored) { }
        super.onDestroy();
    }
}
