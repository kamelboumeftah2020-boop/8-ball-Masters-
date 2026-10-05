package com.fluently.english.account

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** A signed-in learner. Tokens are only set for cloud accounts. */
data class Session(
    val uid: String,
    val name: String,
    val email: String,
    val cloud: Boolean,
    val idToken: String = "",
    val refreshToken: String = "",
    /** When [idToken] was issued (ms); Firebase tokens last one hour. */
    val tokenTime: Long = 0,
)

/** A saved copy of the learner's progress and when it was written. */
data class RemoteProgress(val json: String, val updatedAt: Long)

/** An error with a message ready to show to the learner (Arabic). */
class AuthException(message: String) : Exception(message)

interface AuthBackend {
    val cloud: Boolean
    suspend fun signUp(name: String, email: String, password: String): Session
    suspend fun signIn(email: String, password: String): Session
    suspend fun sendPasswordReset(email: String)
    suspend fun pull(session: Session): Pair<Session, RemoteProgress?>
    suspend fun push(session: Session, progress: RemoteProgress): Session
}

object AuthValidation {
    private val email = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    /** Returns an Arabic error for the sign-up form, or null if it is valid. */
    fun signUpError(name: String, mail: String, password: String, confirm: String): String? = when {
        name.isBlank() -> "اكتب اسمك"
        !email.matches(mail.trim()) -> "البريد الإلكتروني غير صحيح"
        password.length < 6 -> "كلمة السر يجب أن تكون 6 أحرف على الأقل"
        password != confirm -> "كلمتا السر غير متطابقتين"
        else -> null
    }

    fun signInError(mail: String, password: String): String? = when {
        !email.matches(mail.trim()) -> "البريد الإلكتروني غير صحيح"
        password.isEmpty() -> "اكتب كلمة السر"
        else -> null
    }
}

// ======================================================================
// Firebase (Authentication + Firestore) over REST — no SDK or google-services.json.
// ======================================================================

class FirebaseBackend(private val apiKey: String, private val projectId: String) : AuthBackend {
    override val cloud = true

    override suspend fun signUp(name: String, email: String, password: String): Session {
        val res = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey",
            JSONObject().put("email", email.trim()).put("password", password).put("returnSecureToken", true),
        )
        val session = res.toSession(name.trim())
        post(
            "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey",
            JSONObject().put("idToken", session.idToken).put("displayName", name.trim()).put("returnSecureToken", false),
        )
        return session
    }

    override suspend fun signIn(email: String, password: String): Session {
        val res = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey",
            JSONObject().put("email", email.trim()).put("password", password).put("returnSecureToken", true),
        )
        return res.toSession(res.optString("displayName"))
    }

    override suspend fun sendPasswordReset(email: String) {
        post(
            "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$apiKey",
            JSONObject().put("requestType", "PASSWORD_RESET").put("email", email.trim()),
        )
    }

    override suspend fun pull(session: Session): Pair<Session, RemoteProgress?> {
        val s = fresh(session)
        val (code, body) = request("GET", docUrl(s), null, s.idToken)
        if (code == 404) return s to null
        if (code !in 200..299) throw AuthException(firebaseMessage(body))
        val fields = JSONObject(body).optJSONObject("fields") ?: return s to null
        val json = fields.optJSONObject("progress")?.optString("stringValue").orEmpty()
        val updated = fields.optJSONObject("updatedAt")?.optString("integerValue")?.toLongOrNull() ?: 0
        return s to json.takeIf { it.isNotEmpty() }?.let { RemoteProgress(it, updated) }
    }

    override suspend fun push(session: Session, progress: RemoteProgress): Session {
        val s = fresh(session)
        val doc = JSONObject().put(
            "fields",
            JSONObject()
                .put("progress", JSONObject().put("stringValue", progress.json))
                .put("updatedAt", JSONObject().put("integerValue", progress.updatedAt.toString()))
                .put("name", JSONObject().put("stringValue", s.name)),
        )
        val (code, body) = request("PATCH", docUrl(s), doc.toString(), s.idToken)
        if (code !in 200..299) throw AuthException(firebaseMessage(body))
        return s
    }

    private fun docUrl(s: Session) =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/${s.uid}"

    /** Refreshes the ID token when it is older than 50 minutes. */
    private suspend fun fresh(s: Session): Session {
        if (System.currentTimeMillis() - s.tokenTime < 50 * 60_000L) return s
        val form = "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(s.refreshToken, "UTF-8")
        val (code, body) = request(
            "POST", "https://securetoken.googleapis.com/v1/token?key=$apiKey", form, null,
            contentType = "application/x-www-form-urlencoded",
        )
        if (code !in 200..299) throw AuthException("انتهت الجلسة، سجّل الدخول من جديد")
        val o = JSONObject(body)
        return s.copy(
            idToken = o.getString("id_token"),
            refreshToken = o.optString("refresh_token", s.refreshToken),
            tokenTime = System.currentTimeMillis(),
        )
    }

    private fun JSONObject.toSession(name: String) = Session(
        uid = getString("localId"),
        name = name,
        email = optString("email"),
        cloud = true,
        idToken = getString("idToken"),
        refreshToken = getString("refreshToken"),
        tokenTime = System.currentTimeMillis(),
    )

    private suspend fun post(url: String, body: JSONObject): JSONObject {
        val (code, text) = request("POST", url, body.toString(), null)
        if (code !in 200..299) throw AuthException(firebaseMessage(text))
        return JSONObject(text)
    }

    private suspend fun request(
        method: String,
        url: String,
        body: String?,
        token: String?,
        contentType: String = "application/json",
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            // HttpURLConnection has no PATCH; Google APIs accept the override header.
            if (method == "PATCH") {
                conn.requestMethod = "POST"
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            } else {
                conn.requestMethod = method
            }
            conn.connectTimeout = 15_000
            conn.readTimeout = 20_000
            token?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "$contentType; charset=utf-8")
                conn.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            code to (stream?.bufferedReader()?.use { it.readText() } ?: "")
        } catch (e: IOException) {
            throw AuthException("تعذّر الاتصال بالإنترنت. تحقق من الشبكة وحاول مرة أخرى")
        }
    }

    companion object {
        /** Maps Firebase error codes to Arabic messages. */
        fun firebaseMessage(body: String): String {
            val code = runCatching { JSONObject(body).getJSONObject("error").getString("message") }.getOrDefault("")
            return when {
                code.startsWith("EMAIL_EXISTS") -> "هذا البريد مسجّل مسبقاً، سجّل الدخول بدلاً من ذلك"
                code.startsWith("EMAIL_NOT_FOUND") || code.startsWith("INVALID_LOGIN_CREDENTIALS") ||
                    code.startsWith("INVALID_PASSWORD") -> "البريد الإلكتروني أو كلمة السر غير صحيحة"
                code.startsWith("WEAK_PASSWORD") -> "كلمة السر ضعيفة، استخدم 6 أحرف على الأقل"
                code.startsWith("INVALID_EMAIL") -> "البريد الإلكتروني غير صحيح"
                code.startsWith("USER_DISABLED") -> "تم إيقاف هذا الحساب"
                code.startsWith("TOO_MANY_ATTEMPTS") -> "محاولات كثيرة، انتظر قليلاً ثم حاول مرة أخرى"
                code.startsWith("PERMISSION_DENIED") || code.contains("permission", true) -> "لا توجد صلاحية لحفظ التقدم على الخادم"
                else -> "حدث خطأ غير متوقع، حاول مرة أخرى"
            }
        }
    }
}

// ======================================================================
// Accounts kept on this device (used when no Firebase project is configured).
// ======================================================================

class LocalBackend(context: Context) : AuthBackend {
    override val cloud = false
    private val prefs = context.getSharedPreferences("local_accounts", Context.MODE_PRIVATE)

    private fun key(email: String) = "acc_" + email.trim().lowercase()

    override suspend fun signUp(name: String, email: String, password: String): Session {
        if (prefs.contains(key(email))) throw AuthException("هذا البريد مسجّل مسبقاً، سجّل الدخول بدلاً من ذلك")
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val uid = "local-" + hex(ByteArray(8).also { SecureRandom().nextBytes(it) })
        val account = JSONObject()
            .put("uid", uid).put("name", name.trim()).put("email", email.trim())
            .put("salt", hex(salt)).put("hash", PasswordHash.hash(password, salt))
        prefs.edit().putString(key(email), account.toString()).apply()
        return Session(uid, name.trim(), email.trim(), cloud = false)
    }

    override suspend fun signIn(email: String, password: String): Session {
        val raw = prefs.getString(key(email), null) ?: throw AuthException("لا يوجد حساب بهذا البريد على هذا الجهاز")
        val o = JSONObject(raw)
        if (PasswordHash.hash(password, unhex(o.getString("salt"))) != o.getString("hash")) {
            throw AuthException("البريد الإلكتروني أو كلمة السر غير صحيحة")
        }
        return Session(o.getString("uid"), o.getString("name"), o.getString("email"), cloud = false)
    }

    override suspend fun sendPasswordReset(email: String) {
        throw AuthException("استعادة كلمة السر تحتاج إلى حساب سحابي")
    }

    override suspend fun pull(session: Session): Pair<Session, RemoteProgress?> {
        val o = JSONObject(prefs.getString(key(session.email), null) ?: return session to null)
        val json = o.optString("progress")
        return session to json.takeIf { it.isNotEmpty() }?.let { RemoteProgress(it, o.optLong("updatedAt")) }
    }

    override suspend fun push(session: Session, progress: RemoteProgress): Session {
        val raw = prefs.getString(key(session.email), null) ?: return session
        val o = JSONObject(raw).put("progress", progress.json).put("updatedAt", progress.updatedAt)
        prefs.edit().putString(key(session.email), o.toString()).apply()
        return session
    }
}

object PasswordHash {
    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 20_000, 256)
        return hex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded)
    }
}

internal fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

internal fun unhex(s: String): ByteArray = ByteArray(s.length / 2) { i -> s.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
