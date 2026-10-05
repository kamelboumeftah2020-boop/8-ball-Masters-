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
    /** Whether the learner confirmed the address via the emailed link (always true for device accounts). */
    val emailVerified: Boolean = true,
    /** Using the app without an account: nothing is saved to an account. */
    val guest: Boolean = false,
)

/** A saved copy of the learner's progress and when it was written. */
data class RemoteProgress(val json: String, val updatedAt: Long)

/** One row of the weekly leaderboard. */
data class LeaderEntry(val uid: String, val name: String, val xp: Int, val level: String, val streak: Int)

/** An error with a message ready to show to the learner (Arabic). */
open class AuthException(message: String) : Exception(message)

/** The saved login is no longer valid (e.g. after changing the email); the learner must sign in again. */
class SessionExpiredException : AuthException("انتهت الجلسة، سجّل الدخول من جديد")

interface AuthBackend {
    val cloud: Boolean
    suspend fun signUp(name: String, email: String, password: String): Session
    suspend fun signIn(email: String, password: String): Session
    suspend fun sendPasswordReset(email: String)

    /** Emails a confirmation link to the learner's address. */
    suspend fun sendVerification(session: Session): Session = session

    /** Asks the server whether the address has been confirmed yet (and picks up a changed address). */
    suspend fun refreshVerified(session: Session): Session = session

    /**
     * Changes the account's address. Returns the updated session and whether the
     * change still waits for the learner to click a link sent to the new address.
     */
    suspend fun changeEmail(session: Session, newEmail: String): Pair<Session, Boolean>

    suspend fun pull(session: Session): Pair<Session, RemoteProgress?>
    suspend fun push(session: Session, progress: RemoteProgress): Session

    /** Publishes (or with [entry] = null, removes) the learner's score for [week]. */
    suspend fun submitScore(session: Session, week: Long, entry: LeaderEntry?): Session = session

    /** Top learners of [week], best first. */
    suspend fun topScores(session: Session, week: Long): Pair<Session, List<LeaderEntry>> = session to emptyList()
}

object AuthValidation {
    private val email = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$")

    /** Misspellings of popular mail providers → the intended domain. */
    private val domainTypos = mapOf(
        "gmail.com" to listOf("gmial.com", "gmal.com", "gmai.com", "gmail.co", "gmail.cm", "gmail.con", "gmaill.com", "gamil.com", "gnail.com", "gmail.om", "gmil.com", "gmali.com"),
        "hotmail.com" to listOf("hotmial.com", "hotmal.com", "hotmai.com", "hotmail.co", "hotmail.con", "hotmil.com", "hotamil.com", "homail.com"),
        "yahoo.com" to listOf("yaho.com", "yahooo.com", "yahoo.co", "yahoo.con", "yhoo.com", "yahho.com"),
        "outlook.com" to listOf("outlok.com", "outloo.com", "outlook.co", "outlook.con", "otlook.com", "outllok.com"),
        "icloud.com" to listOf("iclod.com", "icloud.co", "icoud.com", "icloud.con"),
    )

    /** Temporary inbox services: accounts on them can't be recovered later. */
    private val disposable = setOf(
        "mailinator.com", "tempmail.com", "temp-mail.org", "10minutemail.com", "guerrillamail.com",
        "yopmail.com", "trashmail.com", "sharklasers.com", "getnada.com", "dispostable.com", "maildrop.cc",
    )

    /** "Did you mean …?" for a mistyped provider, or null. */
    fun suggestion(mail: String): String? {
        val m = mail.trim().lowercase()
        val at = m.lastIndexOf('@').takeIf { it > 0 } ?: return null
        val domain = m.substring(at + 1)
        val fixed = domainTypos.entries.firstOrNull { domain in it.value }?.key ?: return null
        return m.substring(0, at + 1) + fixed
    }

    /** Arabic error for the address itself (format, typos, temporary inboxes), or null. */
    fun emailError(mail: String): String? {
        val m = mail.trim()
        val domain = m.substringAfterLast('@', "").lowercase()
        return when {
            m.isEmpty() -> "اكتب بريدك الإلكتروني"
            ' ' in m -> "البريد الإلكتروني لا يحتوي على مسافات"
            !email.matches(m) || ".." in m -> "البريد الإلكتروني غير صحيح — مثال: name@gmail.com"
            suggestion(m) != null -> "هل تقصد ${suggestion(m)}؟ يبدو أن في البريد خطأً إملائياً"
            domain in disposable -> "استخدم بريدك الحقيقي؛ البريد المؤقت لا يمكن استعادة الحساب منه"
            else -> null
        }
    }

    /** Returns an Arabic error for the sign-up form, or null if it is valid. */
    fun signUpError(name: String, mail: String, password: String, confirm: String): String? = when {
        name.isBlank() -> "اكتب اسمك"
        emailError(mail) != null -> emailError(mail)
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
        val session = res.toSession(name.trim()).copy(emailVerified = false)
        post(
            "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey",
            JSONObject().put("idToken", session.idToken).put("displayName", name.trim()).put("returnSecureToken", false),
        )
        return session
    }

    override suspend fun sendVerification(session: Session): Session {
        val s = fresh(session)
        post(
            "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$apiKey",
            JSONObject().put("requestType", "VERIFY_EMAIL").put("idToken", s.idToken),
        )
        return s
    }

    override suspend fun refreshVerified(session: Session): Session {
        val s = fresh(session)
        val res = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=$apiKey",
            JSONObject().put("idToken", s.idToken),
        )
        val user = res.optJSONArray("users")?.optJSONObject(0)
        return s.copy(
            emailVerified = user?.optBoolean("emailVerified") == true,
            email = user?.optString("email")?.takeIf { it.isNotBlank() } ?: s.email,
        )
    }

    override suspend fun changeEmail(session: Session, newEmail: String): Pair<Session, Boolean> {
        val s = fresh(session)
        // Projects with email-enumeration protection only allow a verified change:
        // the address switches once the learner clicks the link sent to it.
        post(
            "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$apiKey",
            JSONObject().put("requestType", "VERIFY_AND_CHANGE_EMAIL").put("idToken", s.idToken).put("newEmail", newEmail.trim()),
        )
        return s to true
    }

    override suspend fun signIn(email: String, password: String): Session {
        val res = post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey",
            JSONObject().put("email", email.trim()).put("password", password).put("returnSecureToken", true),
        )
        return refreshVerified(res.toSession(res.optString("displayName")))
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

    override suspend fun submitScore(session: Session, week: Long, entry: LeaderEntry?): Session {
        val s = fresh(session)
        val url = "$base/leaderboards/w$week/entries/${s.uid}"
        val (code, body) = if (entry == null) {
            request("DELETE", url, null, s.idToken)
        } else {
            val doc = JSONObject().put(
                "fields",
                JSONObject()
                    .put("name", JSONObject().put("stringValue", entry.name.take(30)))
                    .put("xp", JSONObject().put("integerValue", entry.xp.toString()))
                    .put("level", JSONObject().put("stringValue", entry.level))
                    .put("streak", JSONObject().put("integerValue", entry.streak.toString())),
            )
            request("PATCH", url, doc.toString(), s.idToken)
        }
        if (code !in 200..299 && code != 404) throw AuthException(firebaseMessage(body))
        return s
    }

    override suspend fun topScores(session: Session, week: Long): Pair<Session, List<LeaderEntry>> {
        val s = fresh(session)
        val query = JSONObject().put(
            "structuredQuery",
            JSONObject()
                .put("from", org.json.JSONArray().put(JSONObject().put("collectionId", "entries")))
                .put("orderBy", org.json.JSONArray().put(JSONObject().put("field", JSONObject().put("fieldPath", "xp")).put("direction", "DESCENDING")))
                .put("limit", 50),
        )
        val (code, body) = request("POST", "$base/leaderboards/w$week:runQuery", query.toString(), s.idToken)
        if (code !in 200..299) throw AuthException(firebaseMessage(body))
        val rows = org.json.JSONArray(body)
        val list = (0 until rows.length()).mapNotNull { i ->
            val doc = rows.getJSONObject(i).optJSONObject("document") ?: return@mapNotNull null
            val f = doc.getJSONObject("fields")
            LeaderEntry(
                uid = doc.getString("name").substringAfterLast('/'),
                name = f.optJSONObject("name")?.optString("stringValue").orEmpty(),
                xp = f.optJSONObject("xp")?.optString("integerValue")?.toIntOrNull() ?: 0,
                level = f.optJSONObject("level")?.optString("stringValue").orEmpty(),
                streak = f.optJSONObject("streak")?.optString("integerValue")?.toIntOrNull() ?: 0,
            )
        }
        return s to list
    }

    private val base = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

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
        if (code !in 200..299) throw SessionExpiredException()
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
                code.startsWith("INVALID_ID_TOKEN") || code.startsWith("TOKEN_EXPIRED") || code.startsWith("CREDENTIAL_TOO_OLD") ->
                    "انتهت الجلسة، سجّل الدخول من جديد"
                code.startsWith("CONFIGURATION_NOT_FOUND") || code.startsWith("OPERATION_NOT_ALLOWED") ->
                    "خدمة الحسابات غير مفعّلة بعد على الخادم، حاول لاحقاً"
                code.contains("has not been used") || code.contains("is disabled") -> "قاعدة البيانات غير مفعّلة بعد على الخادم"
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

    override suspend fun changeEmail(session: Session, newEmail: String): Pair<Session, Boolean> {
        if (prefs.contains(key(newEmail))) throw AuthException("هذا البريد مسجّل مسبقاً")
        val raw = prefs.getString(key(session.email), null) ?: throw AuthException("الحساب غير موجود على هذا الجهاز")
        val o = JSONObject(raw).put("email", newEmail.trim())
        prefs.edit().remove(key(session.email)).putString(key(newEmail), o.toString()).apply()
        return session.copy(email = newEmail.trim()) to false
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
