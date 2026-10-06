package com.fluently.english.crash

import android.content.Context
import android.os.Build
import com.fluently.english.BuildConfig
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Minimal crash reporting without extra libraries: an uncaught exception is
 * written to a file before the app closes, and uploaded (to Firestore
 * "crashes") the next time a signed-in learner opens the app.
 */
object CrashReporter {
    private const val FILE = "pending_crash.json"

    fun install(context: Context) {
        val file = File(context.applicationContext.filesDir, FILE)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { file.writeText(describe(error).toString()) }
            previous?.uncaughtException(thread, error)
        }
    }

    fun describe(error: Throwable): JSONObject {
        val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
        return JSONObject()
            .put("time", System.currentTimeMillis().toString())
            .put("version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            .put("device", "${Build.MANUFACTURER} ${Build.MODEL}".take(80))
            .put("android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            .put("trace", trace.take(9000))
    }

    /** The saved report, if the app crashed last time. */
    fun pending(context: Context): Map<String, String>? {
        val file = File(context.filesDir, FILE)
        if (!file.exists()) return null
        return runCatching {
            val o = JSONObject(file.readText())
            o.keys().asSequence().associateWith { o.getString(it) }
        }.getOrNull() ?: run { file.delete(); null }
    }

    fun clear(context: Context) {
        File(context.filesDir, FILE).delete()
    }
}
