package com.fluently.english.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fluently.english.account.AccountManager
import com.fluently.english.account.Session
import com.fluently.english.account.SyncState
import com.fluently.english.data.progress.LearningGoal
import com.fluently.english.data.progress.SkillKey
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressRepository
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProgressRepository(app)
    val progress: StateFlow<Progress> = repo.progress
    private val accounts = AccountManager(app, repo, viewModelScope)
    val session: StateFlow<Session?> = accounts.session
    val sync: StateFlow<SyncState> = accounts.sync
    val cloudAccounts: Boolean get() = accounts.cloud

    suspend fun signUp(name: String, email: String, password: String) = accounts.signUp(name, email, password)
    suspend fun signIn(email: String, password: String) = accounts.signIn(email, password)
    suspend fun sendPasswordReset(email: String) = accounts.sendPasswordReset(email)
    suspend fun signOut() = accounts.signOut()
    suspend fun sendVerification() = accounts.sendVerification()
    suspend fun checkVerified() = accounts.checkVerified()
    suspend fun changeEmail(newEmail: String, password: String) = accounts.changeEmail(newEmail, password)
    fun continueAsGuest() = accounts.continueAsGuest()
    fun leaveGuest() = accounts.leaveGuest()
    fun signInAgain() = accounts.signInAgain()
    suspend fun leaderboard() = accounts.leaderboard()

    fun setGoal(goal: LearningGoal) = repo.setGoal(goal)
    fun recordSkill(skill: SkillKey, right: Int, total: Int) = repo.recordSkill(skill, right, total)
    fun completeReaderChapter(storyId: String, chapter: Int, words: Int, right: Int, total: Int): Int =
        repo.completeReaderChapter(storyId, chapter, words, right, total)
    fun completeWriting(id: String, words: Int, rating: Int): Int = repo.completeWriting(id, words, rating)
    fun setShowOnLeaderboard(show: Boolean) = repo.setShowOnLeaderboard(show)
    fun exportBackup(): String = repo.exportJson()
    fun importBackup(raw: String): Boolean = repo.importJson(raw)

    fun today(): Long = repo.today()

    fun finishOnboarding(name: String) = repo.finishOnboarding(name)
    fun setName(name: String) = repo.setName(name)
    fun setDailyGoal(goal: Int) = repo.setDailyGoal(goal)
    fun setSpeechRate(rate: Float) = repo.setSpeechRate(rate)
    fun applyPlacement(result: CefrLevel?, recommended: CefrLevel) = repo.applyPlacement(result, recommended)
    fun completeLesson(id: String, correct: Int, total: Int): Int = repo.completeLesson(id, correct, total)
    fun completeExam(level: CefrLevel, correct: Int, total: Int): Int = repo.completeExam(level, correct, total)
    fun reviewCard(word: String, known: Boolean) = repo.reviewCard(word, known)
    fun recordLessonAnswers(id: String, wrong: List<Int>, right: List<Int>) = repo.recordLessonAnswers(id, wrong, right)
    fun resolveMistakes(fixed: List<String>): Int = repo.resolveMistakes(fixed)
    fun completeConversation(id: String, stars: Int, points: Int): Int = repo.completeConversation(id, stars, points)
    fun completeSound(id: String, percent: Int, correct: Int): Int = repo.completeSound(id, percent, correct)
    fun completeGame(correct: Int, speedScore: Int? = null): Int = repo.completeGame(correct, speedScore)
    fun completeMock(id: String, score: Int, correct: Int, skills: List<Triple<SkillKey, Int, Int>> = emptyList()): Int =
        repo.completeMock(id, score, correct, skills)
    fun completeDailyChallenge(correct: Int): Int = repo.completeDailyChallenge(correct)
    fun setReminderHour(hour: Int) {
        repo.setReminderHour(hour)
        com.fluently.english.reminder.Reminders.schedule(getApplication(), hour)
    }
    fun addWordToReview(word: String) = repo.addWordToReview(word)
    fun reset() = repo.reset()
}
