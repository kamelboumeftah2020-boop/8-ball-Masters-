package com.fluently.english

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fluently.english.crash.CrashReporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CrashReporterTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun reportDescribesTheErrorWithinFirestoreLimits() {
        val report = CrashReporter.describe(IllegalStateException("boom"))
        assertEquals(setOf("time", "version", "device", "android", "trace"), report.keys().asSequence().toSet())
        assertTrue(report.getString("trace").contains("IllegalStateException: boom"))
        assertTrue(report.getString("trace").length <= 9000)
    }

    @Test fun pendingReportIsReadAndCleared() {
        assertNull(CrashReporter.pending(context))
        File(context.filesDir, "pending_crash.json").writeText(CrashReporter.describe(RuntimeException("x")).toString())
        val pending = CrashReporter.pending(context)
        assertTrue(pending!!.getValue("trace").contains("RuntimeException: x"))
        CrashReporter.clear(context)
        assertNull(CrashReporter.pending(context))
    }

    @Test fun corruptFileIsDiscarded() {
        File(context.filesDir, "pending_crash.json").writeText("not json")
        assertNull(CrashReporter.pending(context))
        assertTrue(!File(context.filesDir, "pending_crash.json").exists())
    }
}
