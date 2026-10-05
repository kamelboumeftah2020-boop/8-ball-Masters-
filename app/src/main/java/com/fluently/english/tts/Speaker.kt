package com.fluently.english.tts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
        sequence = null
        tts.setSpeechRate(baseRate * factor)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun stop() {
        sequence = null
        if (ready) tts.stop()
    }

    private val main = Handler(Looper.getMainLooper())
    private var sequence: Sequence? = null
    private var sequenceToken = 0

    private class Sequence(val token: Int, val size: Int, val onIndex: (Int) -> Unit, val onDone: () -> Unit)

    /**
     * Reads [texts] one after another, calling [onIndex] as each starts (to
     * highlight it) and [onDone] at the end. Any other speech cancels it.
     */
    fun speakSequence(texts: List<String>, onIndex: (Int) -> Unit, onDone: () -> Unit) {
        if (!ready || texts.isEmpty()) { onDone(); return }
        val token = ++sequenceToken
        sequence = Sequence(token, texts.size, onIndex, onDone)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = dispatch(utteranceId, done = false)
            override fun onDone(utteranceId: String) = dispatch(utteranceId, done = true)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) = dispatch(utteranceId, done = true)
        })
        tts.setSpeechRate(baseRate)
        texts.forEachIndexed { i, text ->
            tts.speak(text, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "seq-$token-$i")
        }
    }

    private fun dispatch(id: String, done: Boolean) {
        val parts = id.split('-')
        if (parts.size != 3 || parts[0] != "seq") return
        val token = parts[1].toIntOrNull() ?: return
        val index = parts[2].toIntOrNull() ?: return
        main.post {
            val seq = sequence ?: return@post
            if (seq.token != token) return@post
            if (!done) seq.onIndex(index)
            else if (index == seq.size - 1) { sequence = null; seq.onDone() }
        }
    }

    fun shutdown() = tts.shutdown()
}

val LocalSpeaker = staticCompositionLocalOf<Speaker> { error("Speaker not provided") }
