package app.nur.quran;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** يعرض إشعار التذكير في وقته. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ReminderScheduler.ACTION.equals(intent.getAction())) return;
        ReminderScheduler.show(context, intent.getIntExtra("id", 0),
            intent.getStringExtra("title"), intent.getStringExtra("text"), intent.getStringExtra("route"));
    }
}
