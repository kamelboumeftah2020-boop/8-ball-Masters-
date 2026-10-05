package com.fluently.english.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/** Thin wrapper around Android's offline text-to-speech, used for all audio. */
class Speaker(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pending: Pair<String, Float>? = null
    var baseRate: Float = 0.9f

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val result = tts.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.UK)
        }
        ready = true
        pending?.let { (text, factor) -> speak(text, factor) }
        pending = null
    }

    /** Speaks [text]; [factor] scales the learner's chosen rate (e.g. 0.6 for slow playback). */
    fun speak(text: String, factor: Float = 1f) {
        if (!ready) {
            pending = text to factor
            return
        }
        tts.setSpeechRate(baseRate * factor)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun stop() {
        if (ready) tts.stop()
    }

    fun shutdown() = tts.shutdown()
}

val LocalSpeaker = staticCompositionLocalOf<Speaker> { error("Speaker not provided") }
