package com.fluently.english

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.fluently.english.crash.CrashReporter
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.tts.Speaker
import com.fluently.english.ui.FluentlyApp
import com.fluently.english.ui.theme.FluentlyTheme

class MainActivity : ComponentActivity() {
    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReporter.install(this)
        enableEdgeToEdge()
        speaker = Speaker(this)
        setContent {
            FluentlyTheme {
                // The interface is Arabic, so the whole layout is right-to-left;
                // English content switches back to LTR locally.
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalSpeaker provides speaker,
                ) {
                    FluentlyApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Picks up an English voice installed while the learner was away.
        if (::speaker.isInitialized) speaker.recheck()
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}
