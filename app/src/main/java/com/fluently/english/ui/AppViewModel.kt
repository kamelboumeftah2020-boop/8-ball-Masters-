package com.fluently.english.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressRepository
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProgressRepository(app)
    val progress: StateFlow<Progress> = repo.progress

    fun today(): Long = repo.today()

    fun finishOnboarding(name: String) = repo.finishOnboarding(name)
    fun setName(name: String) = repo.setName(name)
    fun setDailyGoal(goal: Int) = repo.setDailyGoal(goal)
    fun setSpeechRate(rate: Float) = repo.setSpeechRate(rate)
    fun applyPlacement(result: CefrLevel?, recommended: CefrLevel) = repo.applyPlacement(result, recommended)
    fun completeLesson(id: String, correct: Int, total: Int): Int = repo.completeLesson(id, correct, total)
    fun completeExam(level: CefrLevel, correct: Int, total: Int): Int = repo.completeExam(level, correct, total)
    fun reviewCard(word: String, known: Boolean) = repo.reviewCard(word, known)
    fun reset() = repo.reset()
}
