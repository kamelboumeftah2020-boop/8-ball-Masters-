package com.fluently.english.account

import android.content.Context
import com.fluently.english.BuildConfig
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressCodec
import com.fluently.english.data.progress.ProgressRepository
import com.fluently.english.data.progress.weekIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class SyncState { IDLE, SYNCING, SYNCED, OFFLINE, EXPIRED }

/**
 * Signs learners up and in, remembers the session, and keeps their progress saved
 * in their account: in Firebase when the app is built with a Firebase project,
 * otherwise in an account stored on this device.
 */
class AccountManager(
    context: Context,
    private val repo: ProgressRepository,
    private val scope: CoroutineScope,
    private val backend: AuthBackend = defaultBackend(context),
) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<Session?> = _session.asStateFlow()
    private val _sync = MutableStateFlow(SyncState.IDLE)
    val sync: StateFlow<SyncState> = _sync.asStateFlow()
    val cloud: Boolean get() = backend.cloud

    private var pushJob: Job? = null

    init {
        repo.onChange = { schedulePush() }
        // Pick up changes made on another device since the last launch.
        _session.value?.takeIf { !it.guest }?.let { s -> scope.launch { runCatching { merge(s) } } }
    }

    suspend fun signUp(name: String, email: String, password: String) {
        AuthValidation.signUpError(name, email, password, password)?.let { throw AuthException(it) }
        open(backend.signUp(name, email, password))
    }

    suspend fun signIn(email: String, password: String) {
        AuthValidation.signInError(email, password)?.let { throw AuthException(it) }
        open(backend.signIn(email, password))
    }

    suspend fun sendPasswordReset(email: String) = backend.sendPasswordReset(email)

    /** Emails a confirmation link to the learner's address (optional). */
    suspend fun sendVerification() {
        val s = _session.value?.takeIf { it.cloud && !it.guest } ?: return
        updateSession(guard { backend.sendVerification(s) })
    }

    /** Re-checks the address (confirmed? changed via link?); returns true once confirmed. */
    suspend fun checkVerified(): Boolean {
        val s = _session.value?.takeIf { it.cloud && !it.guest } ?: return true
        val fresh = guard { backend.refreshVerified(s) }
        updateSession(fresh)
        return fresh.emailVerified
    }

    /**
     * Changes the address after confirming the password. Returns true when the
     * change waits for the learner to click a link sent to the new address.
     */
    suspend fun changeEmail(newEmail: String, password: String): Boolean {
        val s = _session.value?.takeIf { !it.guest } ?: return false
        AuthValidation.emailError(newEmail)?.let { throw AuthException(it) }
        if (newEmail.trim().equals(s.email, ignoreCase = true)) throw AuthException("هذا هو بريدك الحالي")
        // Signing in again proves the password and gives a fresh token for this sensitive change.
        val fresh = backend.signIn(s.email, password).copy(name = s.name)
        val (updated, pending) = backend.changeEmail(fresh, newEmail)
        _session.value = updated
        saveSession(updated)
        return pending
    }

    /** Uses the app without an account; progress stays on this device only. */
    fun continueAsGuest() {
        val guest = Session(uid = "guest", name = "", email = "", cloud = false, guest = true)
        repo.owner = ""
        _session.value = guest
        saveSession(guest)
    }

    /** Leaves guest mode for the sign-up screen, keeping the progress so a new account can adopt it. */
    fun leaveGuest() {
        _session.value = null
        saveSession(null)
    }

    /**
     * After the login expired: back to the sign-in screen without clearing local
     * progress, so signing in again (same account) uploads anything not yet saved.
     */
    fun signInAgain() {
        _session.value = null
        saveSession(null)
        _sync.value = SyncState.IDLE
    }

    private suspend fun <T> guard(block: suspend () -> T): T = try {
        block()
    } catch (e: SessionExpiredException) {
        _sync.value = SyncState.EXPIRED
        throw e
    }

    /** Uploads the latest progress, then forgets the session and clears local progress. */
    suspend fun signOut() {
        val s = _session.value ?: return
        if (s.guest) { leaveGuest(); return }
        pushJob?.cancel()
        runCatching { backend.push(s, RemoteProgress(repo.exportJson(), repo.updatedAt)) }
        _session.value = null
        saveSession(null)
        repo.onChange = null
        repo.replace(Progress())
        repo.owner = ""
        repo.onChange = { schedulePush() }
    }

    /** Brings the account's progress in first, then publishes the session (which opens the app). */
    private suspend fun open(s: Session) {
        val fresh = merge(s)
        _session.value = fresh
        saveSession(fresh)
    }

    /**
     * Reconciles local and account progress: the newer copy wins when the local one
     * belongs to this account; progress made before the first sign-in is adopted.
     */
    private suspend fun merge(s: Session): Session {
        _sync.value = SyncState.SYNCING
        var fresh = s
        try {
            val pulled = backend.pull(s)
            fresh = pulled.first
            val remote = pulled.second
            updateSession(fresh)
            val owner = repo.owner
            // Progress made before signing in, or in a device account, moves into the cloud account.
            val localIsMine = owner == s.uid || owner.isEmpty() || (s.cloud && owner.startsWith("local-"))
            val name = s.name
            when {
                remote != null && !(owner == s.uid && repo.updatedAt > remote.updatedAt) -> {
                    val p = runCatching { ProgressCodec.decode(remote.json) }.getOrNull()
                    if (p != null) withoutSync { repo.replace(p.copy(name = p.name.ifBlank { name })) }
                }
                remote == null && !localIsMine -> withoutSync { repo.replace(Progress(name = name)) }
            }
            if (repo.progress.value.name.isBlank()) withoutSync { repo.replace(repo.progress.value.copy(name = name)) }
            repo.owner = s.uid
            if (remote == null || repo.updatedAt > remote.updatedAt) push(fresh)
            _sync.value = SyncState.SYNCED
        } catch (e: AuthException) {
            _sync.value = SyncState.OFFLINE
        }
        return fresh
    }

    private fun schedulePush() {
        val s = _session.value?.takeIf { !it.guest } ?: return
        pushJob?.cancel()
        pushJob = scope.launch {
            delay(if (backend.cloud) 2500 else 300)
            push(s)
        }
    }

    private suspend fun push(s: Session) {
        _sync.value = SyncState.SYNCING
        _sync.value = try {
            updateSession(backend.push(_session.value ?: s, RemoteProgress(repo.exportJson(), repo.updatedAt)))
            publishScore()
            SyncState.SYNCED
        } catch (e: SessionExpiredException) {
            SyncState.EXPIRED
        } catch (e: AuthException) {
            SyncState.OFFLINE
        }
    }

    private var lastScore = ""

    /** Keeps the learner's leaderboard row in step with their weekly XP (only when it changed). */
    private suspend fun publishScore() {
        val s = _session.value ?: return
        if (!backend.cloud) return
        val p = repo.progress.value
        val today = repo.today()
        val week = weekIndex(today)
        val xp = p.weekXp(today)
        val show = p.showOnLeaderboard && xp > 0
        val key = "$week:$xp:${p.name}:$show:${p.streak}"
        if (key == lastScore) return
        val entry = if (show) LeaderEntry(s.uid, p.name.ifBlank { s.name }, xp, p.currentLevel.code, p.streak) else null
        // A learner with no XP yet simply has no row; only delete when they opted out.
        if (entry == null && p.showOnLeaderboard) { lastScore = key; return }
        try {
            updateSession(backend.submitScore(s, week, entry))
            lastScore = key
        } catch (e: AuthException) {
            // The leaderboard is optional; progress is already saved.
        }
    }

    /** This week's top learners (empty for device accounts). */
    suspend fun leaderboard(): List<LeaderEntry> {
        val s = _session.value?.takeIf { !it.guest } ?: return emptyList()
        publishScore()
        val (fresh, list) = backend.topScores(s, weekIndex(repo.today()))
        updateSession(fresh)
        return list
    }

    private fun updateSession(s: Session) {
        if (_session.value?.uid != s.uid) return
        if (s != _session.value) {
            _session.value = s
            saveSession(s)
        }
    }

    private inline fun withoutSync(block: () -> Unit) {
        val hook = repo.onChange
        repo.onChange = null
        block()
        repo.onChange = hook
    }

    private fun loadSession(): Session? {
        val raw = prefs.getString("session", null) ?: return null
        return runCatching {
            val o = JSONObject(raw)
            Session(
                o.getString("uid"), o.getString("name"), o.getString("email"), o.getBoolean("cloud"),
                o.optString("idToken"), o.optString("refreshToken"), o.optLong("tokenTime"),
                emailVerified = o.optBoolean("emailVerified", !o.getBoolean("cloud")),
                guest = o.optBoolean("guest"),
            )
        }.getOrNull()?.takeIf { it.guest || it.cloud == backend.cloud }
    }

    private fun saveSession(s: Session?) {
        val raw = s?.let {
            JSONObject()
                .put("uid", it.uid).put("name", it.name).put("email", it.email).put("cloud", it.cloud)
                .put("idToken", it.idToken).put("refreshToken", it.refreshToken).put("tokenTime", it.tokenTime)
                .put("emailVerified", it.emailVerified).put("guest", it.guest)
                .toString()
        }
        prefs.edit().putString("session", raw).apply()
    }

    companion object {
        fun defaultBackend(context: Context): AuthBackend =
            if (BuildConfig.FIREBASE_API_KEY.isNotBlank() && BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()) {
                FirebaseBackend(BuildConfig.FIREBASE_API_KEY, BuildConfig.FIREBASE_PROJECT_ID)
            } else {
                LocalBackend(context)
            }
    }
}
