package com.fluently.english.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.fluently.english.tts.LocalSpeaker
import com.fluently.english.ui.theme.Coral
import com.fluently.english.ui.theme.Danger

/**
 * Microphone input. Listens inside the app with Android's speech recogniser
 * (asking for the microphone permission first) and shows what it hears as you
 * speak; on phones without an in-app recogniser it falls back to the system
 * voice-typing screen. Every failure ends in a message instead of silence.
 */
@Stable
class SpeechInput internal constructor() {
    /** Kept for callers; the button is always shown and explains any problem. */
    val available: Boolean = true
    var listening by mutableStateOf(false)
        internal set
    /** Words recognised so far while the learner is still speaking. */
    var partial by mutableStateOf("")
        internal set
    /** Arabic explanation of the last failure, or null. */
    var error by mutableStateOf<String?>(null)
        internal set
    /** Voice loudness 0..1 while listening (for the pulsing button). */
    var level by mutableFloatStateOf(0f)
        internal set
    /** The phone has no speech service: offer the offline English pack. */
    var offlineNeeded by mutableStateOf(false)
        internal set
    /** Download progress 0..1 of the offline pack, or null when not downloading. */
    var downloading by mutableStateOf<Float?>(null)
        internal set
    /** A short positive note (e.g. the offline pack is ready). */
    var notice by mutableStateOf<String?>(null)
        internal set
    internal var onStart: () -> Unit = {}
    internal var onDownload: () -> Unit = {}

    /** Starts listening, or stops if already listening. */
    fun start() = onStart()

    /** Downloads the offline English speech pack (about 40 MB, once). */
    fun downloadOffline() = onDownload()
}

private fun recognizerIntent(context: Context) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
    .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
    .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    .putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")

private fun isNetworkError(code: Int) =
    code == SpeechRecognizer.ERROR_NETWORK || code == SpeechRecognizer.ERROR_NETWORK_TIMEOUT || code == 11

private fun errorMessage(code: Int): String? = when (code) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> null // handled as "didn't hear you"
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, 11 /* SERVER_DISCONNECTED */ ->
        "التعرّف على الصوت يحتاج اتصالاً بالإنترنت. تحقق من الشبكة، أو نزّل الإنجليزية للتعرّف بدون إنترنت من إعدادات «Google ← الصوت»."
    SpeechRecognizer.ERROR_SERVER -> "خدمة التعرّف على الصوت لا تستجيب الآن، حاول بعد قليل."
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
        "تطبيق Google لا يملك إذن الميكروفون. افتح إعدادات الهاتف ← التطبيقات ← Google ← الأذونات وفعّل الميكروفون."
    SpeechRecognizer.ERROR_AUDIO -> "تعذّر تسجيل الصوت. تأكد أن تطبيقاً آخر لا يستخدم الميكروفون الآن."
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "الميكروفون مشغول، انتظر لحظة ثم اضغط مرة أخرى."
    12, 13 /* LANGUAGE_NOT_SUPPORTED / LANGUAGE_UNAVAILABLE */ ->
        "اللغة الإنجليزية غير متاحة في خدمة الصوت. أضف English (US) من إعدادات «Google ← الصوت ← اللغات»."
    else -> "حدث خطأ في الميكروفون (رمز $code)، حاول مرة أخرى."
}

@Composable
fun rememberSpeechInput(onResult: (List<String>) -> Unit): SpeechInput {
    val context = LocalContext.current
    val onResultState by rememberUpdatedState(onResult)
    val input = remember { SpeechInput() }

    val scope = rememberCoroutineScope()
    val speaker = LocalSpeaker.current
    var offlineSession by remember { mutableStateOf<OfflineSpeech.Session?>(null) }
    DisposableEffect(Unit) { onDispose { offlineSession?.stop() } }

    // Last resort, no Google needed: the built-in offline engine (after a one-time download).
    fun listenOffline() {
        if (!OfflineSpeech.isReady(context)) {
            input.offlineNeeded = true
            return
        }
        speaker.stop()
        input.error = null
        input.notice = null
        input.partial = ""
        input.listening = true
        scope.launch {
            try {
                offlineSession = OfflineSpeech.listen(
                    context,
                    onPartial = { input.partial = it },
                    onDone = { heard -> input.listening = false; offlineSession = null; onResultState(heard) },
                    onError = { msg -> input.listening = false; offlineSession = null; input.error = msg },
                )
            } catch (e: Throwable) {
                input.listening = false
                input.error = "تعذّر تشغيل التعرّف على الصوت بدون إنترنت على هذا الهاتف."
            }
        }
    }

    input.onDownload = {
        if (input.downloading == null) {
            input.downloading = 0f
            input.error = null
            scope.launch {
                try {
                    OfflineSpeech.download(context) { p -> scope.launch { input.downloading = p } }
                    input.offlineNeeded = false
                    input.notice = "جاهز ✓ الآن اضغط على الميكروفون وتكلّم — يعمل حتى بدون إنترنت."
                } catch (e: Exception) {
                    input.error = "تعذّر تنزيل الحزمة. تحقق من الإنترنت ثم حاول مرة أخرى."
                } finally {
                    input.downloading = null
                }
            }
        }
    }

    // Fallback: the system voice-typing screen (needs no permission from us).
    val systemScreen = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        input.listening = false
        onResultState(res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty())
    }
    fun openSystemScreen(): Boolean {
        input.error = null
        val intent = recognizerIntent(context)
        if (intent.resolveActivity(context.packageManager) == null) return false
        return try {
            input.listening = true
            systemScreen.launch(intent)
            true
        } catch (e: ActivityNotFoundException) {
            input.listening = false
            false
        }
    }

    val recognizer = remember {
        runCatching {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) return@runCatching null
            // Prefer Google's service: some phones' default recogniser handles English poorly.
            val google = context.packageManager
                .queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE), 0)
                .firstOrNull { it.serviceInfo.packageName.contains("google") }
            if (google != null) {
                SpeechRecognizer.createSpeechRecognizer(context, ComponentName(google.serviceInfo.packageName, google.serviceInfo.name))
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        }.getOrNull()
    }
    DisposableEffect(recognizer) { onDispose { runCatching { recognizer?.destroy() } } }

    /** Whatever works on this phone: Google's recogniser, the voice-typing screen, or offline. */
    fun listen() {
        val r = recognizer ?: run {
            if (!openSystemScreen()) listenOffline()
            return
        }
        speaker.stop() // the microphone would otherwise hear the app's own voice
        input.error = null
        input.partial = ""
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { input.listening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) { input.level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { input.level = 0f }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { input.partial = it }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onResults(results: Bundle?) {
                input.listening = false
                input.level = 0f
                onResultState(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty())
            }
            override fun onError(error: Int) {
                input.listening = false
                input.level = 0f
                // Client errors and a missing English model: let the system voice-typing screen try instead.
                if (error == SpeechRecognizer.ERROR_CLIENT || error == 12 || error == 13) {
                    if (!openSystemScreen()) listenOffline()
                    return
                }
                val message = errorMessage(error)
                when {
                    message == null -> onResultState(emptyList())
                    // No internet but the offline pack is installed: just use it.
                    isNetworkError(error) && OfflineSpeech.isReady(context) -> listenOffline()
                    else -> input.error = message
                }
            }
        })
        try {
            input.listening = true
            r.startListening(recognizerIntent(context))
        } catch (e: Exception) {
            input.listening = false
            if (!openSystemScreen()) listenOffline()
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen()
        else input.error = "اسمح للتطبيق باستخدام الميكروفون لتتدرّب على النطق (من إعدادات الهاتف ← التطبيقات ← طلاقة ← الأذونات)."
    }

    input.onStart = {
        when {
            input.listening -> {
                offlineSession?.stop()
                runCatching { recognizer?.stopListening() }
                if (offlineSession == null) input.listening = false
            }
            input.downloading != null -> Unit
            // No Google recogniser: the voice-typing screen if present, else the offline engine (needs the permission).
            recognizer == null && openSystemScreen() -> Unit
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> listen()
            else -> permission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    return input
}

/** "Listening…" with live words, or the last error, under a microphone button. */
@Composable
fun SpeechStatus(input: SpeechInput, modifier: Modifier = Modifier) {
    when {
        input.listening -> Column(modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("🎙️ أستمع… تكلّم الآن، واضغط مرة أخرى للإيقاف", style = MaterialTheme.typography.labelLarge, color = Coral, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (input.partial.isNotBlank()) {
                Ltr { Text("“${input.partial}”", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
            }
        }
        input.downloading != null -> Column(modifier.fillMaxWidth().padding(top = 10.dp)) {
            Text(
                "جارٍ تنزيل حزمة التعرّف على الصوت… ${((input.downloading ?: 0f) * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            VSpace(6.dp)
            LinearMeter(input.downloading ?: 0f, height = 6.dp)
        }
        input.offlineNeeded -> Column(
            modifier.fillMaxWidth().padding(top = 10.dp).clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp),
        ) {
            Text("شغّل الميكروفون بدون Google", style = MaterialTheme.typography.titleSmall)
            VSpace(4.dp)
            Text(
                "هاتفك لا يحتوي على خدمة Google للتعرّف على الصوت. يمكن للتطبيق أن يتعرّف على كلامك بنفسه — حتى بدون إنترنت — بعد تنزيل حزمة الإنجليزية مرة واحدة (حوالي ${OfflineSpeech.SIZE_MB} ميغابايت، يُفضّل عبر Wi-Fi).",
                style = MaterialTheme.typography.bodySmall,
            )
            input.error?.let {
                VSpace(4.dp)
                Text(it, style = MaterialTheme.typography.bodySmall, color = Danger)
            }
            VSpace(10.dp)
            PrimaryButton("تنزيل الحزمة (${OfflineSpeech.SIZE_MB} ميغابايت)", onClick = { input.downloadOffline() })
        }
        input.notice != null && input.error == null -> Text(
            input.notice.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center, modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        )
        input.error != null -> Text(
            input.error.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Danger,
            textAlign = TextAlign.Center, modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

/** Gentle pulse for the mic button while listening, following the voice level. */
@Composable
fun Modifier.micPulse(input: SpeechInput): Modifier {
    if (!input.listening) return this
    val t = rememberInfiniteTransition(label = "mic")
    val beat by t.animateFloat(1f, 1.12f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "beat")
    return scale(beat + input.level * 0.15f)
}
