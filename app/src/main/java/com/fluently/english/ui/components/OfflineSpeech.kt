package com.fluently.english.ui.components

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Offline English speech recognition (Vosk) for phones without Google's speech
 * service. The ~40 MB English model is downloaded once, on request, and then
 * works without internet.
 */
object OfflineSpeech {
    private const val MODEL = "vosk-model-small-en-us-0.15"
    private const val URL_PRIMARY = "https://alphacephei.com/vosk/models/$MODEL.zip"
    const val SIZE_MB = 40

    private var model: Model? = null

    private fun dir(context: Context) = File(context.filesDir, MODEL)
    private fun marker(context: Context) = File(dir(context), ".complete")

    fun isReady(context: Context): Boolean = marker(context).exists()

    /** Downloads and unpacks the model; [onProgress] gets 0..1. Throws IOException on failure. */
    suspend fun download(context: Context, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val zip = File(context.cacheDir, "$MODEL.zip")
        try {
            val conn = URL(URL_PRIMARY).openConnection() as HttpURLConnection
            conn.connectTimeout = 20_000
            conn.readTimeout = 30_000
            if (conn.responseCode !in 200..299) throw IOException("HTTP ${conn.responseCode}")
            val total = conn.contentLengthLong.takeIf { it > 0 } ?: (SIZE_MB * 1_048_576L)
            conn.inputStream.use { input ->
                zip.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        onProgress((done.toFloat() / total * 0.9f).coerceAtMost(0.9f))
                    }
                }
            }
            // Unpack next to the app's files (guarding against paths escaping the folder).
            val target = context.filesDir.canonicalFile
            dir(context).deleteRecursively()
            ZipInputStream(zip.inputStream().buffered()).use { zin ->
                while (true) {
                    val entry = zin.nextEntry ?: break
                    val file = File(target, entry.name).canonicalFile
                    if (!file.path.startsWith(target.path + File.separator)) throw IOException("bad zip entry")
                    if (entry.isDirectory) file.mkdirs() else {
                        file.parentFile?.mkdirs()
                        file.outputStream().use { zin.copyTo(it) }
                    }
                }
            }
            if (!File(dir(context), "conf").exists()) throw IOException("model files missing")
            marker(context).writeText("ok")
            onProgress(1f)
        } finally {
            zip.delete()
        }
    }

    private suspend fun load(context: Context): Model = withContext(Dispatchers.IO) {
        model ?: Model(dir(context).path).also { model = it }
    }

    /**
     * Listens until the learner stops speaking (or [timeoutMs]). Callbacks run
     * on the main thread. Returns a handle to stop early.
     */
    suspend fun listen(
        context: Context,
        onPartial: (String) -> Unit,
        onDone: (List<String>) -> Unit,
        onError: (String) -> Unit,
        timeoutMs: Int = 10_000,
    ): Session {
        val recognizer = withContext(Dispatchers.IO) { Recognizer(load(context), 16000f) }
        val service = SpeechService(recognizer, 16000f)
        val session = Session(service, recognizer)
        var partial = ""
        fun text(json: String?, key: String) = runCatching { JSONObject(json ?: "").optString(key) }.getOrDefault("").trim()
        service.startListening(object : RecognitionListener {
            override fun onPartialResult(hypothesis: String?) {
                text(hypothesis, "partial").takeIf { it.isNotEmpty() }?.let { partial = it; onPartial(it) }
            }
            override fun onResult(hypothesis: String?) {
                // The engine detected the end of a phrase: that's the answer.
                val t = text(hypothesis, "text")
                if (t.isNotEmpty() && session.finish()) onDone(listOf(t))
            }
            override fun onFinalResult(hypothesis: String?) {
                val t = text(hypothesis, "text").ifEmpty { partial }
                if (session.finish()) onDone(listOfNotNull(t.takeIf { it.isNotEmpty() }))
            }
            override fun onError(exception: Exception?) {
                if (session.finish()) onError("تعذّر تشغيل الميكروفون: ${exception?.message ?: "خطأ غير معروف"}")
            }
            override fun onTimeout() {
                if (session.finish()) onDone(listOfNotNull(partial.takeIf { it.isNotEmpty() }))
            }
        }, timeoutMs)
        return session
    }

    class Session internal constructor(private val service: SpeechService, private val recognizer: Recognizer) {
        private var finished = false

        /** Stops listening and frees the engine; true the first time only. */
        internal fun finish(): Boolean {
            if (finished) return false
            finished = true
            runCatching { service.stop() }
            runCatching { service.shutdown() }
            runCatching { recognizer.close() }
            return true
        }

        /** Stops early; the words heard so far are delivered as the result. */
        fun stop() {
            runCatching { service.stop() } // triggers onFinalResult
        }
    }
}
