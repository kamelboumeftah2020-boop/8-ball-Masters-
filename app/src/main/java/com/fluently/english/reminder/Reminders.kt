package com.fluently.english.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fluently.english.MainActivity
import com.fluently.english.R
import com.fluently.english.data.progress.ProgressRepository
import java.util.Calendar

/** Daily study reminder, delivered only on days the learner hasn't studied yet. */
object Reminders {
    private const val CHANNEL = "daily_reminder"
    private const val PREFS = "reminder"
    private const val KEY_HOUR = "hour"

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** Schedules the reminder at [hour] every day, or cancels it when [hour] is -1. */
    fun schedule(context: Context, hour: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_HOUR, hour).apply()
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        alarms.cancel(pendingIntent(context))
        if (hour < 0) return
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent(context))
    }

    fun savedHour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_HOUR, -1)

    fun notify(context: Context) {
        val progress = ProgressRepository(context).progress.value
        val today = com.fluently.english.data.progress.localEpochDay()
        if (progress.lastActiveDay == today) return // already studied today

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "تذكير يومي بالدراسة", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        val open = PendingIntent.getActivity(
            context, 1, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val due = progress.dueCards(today).size
        val text = when {
            progress.streak > 0 -> "لا تخسر سلسلة ${progress.streak} يوم! 5 دقائق فقط تكفي اليوم 🔥"
            due > 0 -> "لديك $due كلمة تنتظر المراجعة قبل أن تنساها 🧠"
            else -> "درس قصير اليوم يقربك خطوة من الطلاقة ✨"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("حان وقت الإنجليزية")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(1, notification) }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Reminders.schedule(context, Reminders.savedHour(context))
        } else {
            Reminders.notify(context)
        }
    }
}
