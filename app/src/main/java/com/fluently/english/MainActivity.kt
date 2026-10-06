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
import com.fluently.english.ui.IntroAnimation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fluently.english.ui.theme.FluentlyTheme

class MainActivity : ComponentActivity() {
    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        CrashReporter.install(this)
        com.fluently.english.ads.Ads.init(this)
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
                    // The intro plays once per launch (not again after rotation or returning).
                    var intro by rememberSaveable { mutableStateOf(savedInstanceState == null) }
                    Box(Modifier.fillMaxSize()) {
                        FluentlyApp()
                        if (intro) IntroAnimation(onFinished = { intro = false })
                    }
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
