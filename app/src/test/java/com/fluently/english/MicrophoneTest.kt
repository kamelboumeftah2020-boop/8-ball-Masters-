package com.fluently.english

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.tts.Speaker
import com.fluently.english.ui.components.SpeakPractice
import com.fluently.english.ui.theme.FluentlyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSpeechRecognizer

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class MicrophoneTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private var scored: Pair<Float, String>? = null

    private fun installRecognitionService() {
        val pm = shadowOf(rule.activity.packageManager)
        val info = ResolveInfo().apply {
            serviceInfo = ServiceInfo().apply { packageName = "com.google.android.tts"; name = "FakeRecognitionService" }
        }
        pm.addResolveInfoForIntent(Intent(RecognitionService.SERVICE_INTERFACE), info)
        pm.addServiceIfNotPresent(ComponentName("com.google.android.tts", "FakeRecognitionService"))
    }

    private fun show() {
        val speaker = Speaker(rule.activity)
        rule.setContent {
            FluentlyTheme(dark = false) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, LocalSpeaker provides speaker) {
                    SpeakPractice("I would like a coffee, please.") { s, heard -> scored = s to heard }
                }
            }
        }
    }

    private fun results(vararg texts: String) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(*texts))
    }

    @Test fun speakingIsRecognisedAndScored() {
        installRecognitionService()
        shadowOf(rule.activity.application).grantPermissions(Manifest.permission.RECORD_AUDIO)
        show()
        rule.onNode(hasContentDescription("تحدث")).performClick()
        rule.waitForIdle()
        val recognizer = ShadowSpeechRecognizer.getLatestSpeechRecognizer()
        val shadow = shadowOf(recognizer)
        shadow.triggerOnReadyForSpeech(Bundle())
        shadow.triggerOnPartialResults(results("I would like"))
        rule.waitForIdle()
        rule.onNode(hasText("أستمع", substring = true)).assertExists()
        rule.onNode(hasText("I would like", substring = true)).assertExists()
        shadow.triggerOnResults(results("I would like a coffee please"))
        rule.waitForIdle()
        assertEquals("I would like a coffee please", scored?.second)
        assertTrue((scored?.first ?: 0f) > 0.9f)
        rule.onNode(hasText("نطق رائع", substring = true)).assertExists()
    }

    @Test fun networkErrorShowsArabicMessage() {
        installRecognitionService()
        shadowOf(rule.activity.application).grantPermissions(Manifest.permission.RECORD_AUDIO)
        show()
        rule.onNode(hasContentDescription("تحدث")).performClick()
        rule.waitForIdle()
        shadowOf(ShadowSpeechRecognizer.getLatestSpeechRecognizer()).triggerOnError(SpeechRecognizer.ERROR_NETWORK)
        rule.waitForIdle()
        rule.onNode(hasText("يحتاج اتصالاً بالإنترنت", substring = true)).assertExists()
    }

    @Test fun noSpeechServiceOffersTheOfflinePack() {
        // No Google recogniser and no voice-typing screen on this "phone".
        shadowOf(rule.activity.application).grantPermissions(Manifest.permission.RECORD_AUDIO)
        show()
        rule.onNode(hasContentDescription("تحدث")).performClick()
        rule.waitForIdle()
        rule.onNode(hasText("شغّل الميكروفون بدون Google", substring = true)).assertExists()
        rule.onNode(hasText("تنزيل الحزمة", substring = true)).assertExists()
    }

    @Test fun silenceCountsAsNotHeard() {
        installRecognitionService()
        shadowOf(rule.activity.application).grantPermissions(Manifest.permission.RECORD_AUDIO)
        show()
        rule.onNode(hasContentDescription("تحدث")).performClick()
        rule.waitForIdle()
        shadowOf(ShadowSpeechRecognizer.getLatestSpeechRecognizer()).triggerOnError(SpeechRecognizer.ERROR_NO_MATCH)
        rule.waitForIdle()
        rule.onNode(hasText("لم أسمع شيئاً", substring = true)).assertExists()
    }
}
