package com.fluently.english

import com.fluently.english.data.content.Answers
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Question
import com.fluently.english.data.progress.Card
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressCodec
import com.fluently.english.data.progress.withXp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {

    @Test
    fun lessonsUnlockSequentially() {
        val lessons = Course.level(CefrLevel.A1).lessons
        val fresh = Progress()
        assertTrue(fresh.isLessonUnlocked(lessons[0]))
        assertFalse(fresh.isLessonUnlocked(lessons[1]))
        val after = fresh.copy(lessonScores = mapOf(lessons[0].id to 80))
        assertTrue(after.isLessonUnlocked(lessons[1]))
        assertFalse(after.isExamUnlocked(CefrLevel.A1))
        assertFalse(fresh.isLevelUnlocked(CefrLevel.A2))
    }

    @Test
    fun placementOpensLowerLevels() {
        val p = Progress(placementTaken = true, placementLevel = CefrLevel.B1, unlockedLevel = CefrLevel.B2)
        assertTrue(p.isLevelPassed(CefrLevel.A2))
        assertTrue(p.isLessonUnlocked(Course.level(CefrLevel.A1).lessons.last()))
        assertEquals(CefrLevel.B2, p.currentLevel)
        assertEquals(Course.level(CefrLevel.B2).lessons.first(), p.nextLesson())
    }

    @Test
    fun streakGrowsOnConsecutiveDaysAndResetsAfterGap() {
        var p = Progress().withXp(10, day = 100)
        assertEquals(1, p.streak)
        p = p.withXp(10, day = 100)
        assertEquals(1, p.streak)
        assertEquals(20, p.todayXp)
        p = p.withXp(10, day = 101)
        assertEquals(2, p.streak)
        assertEquals(10, p.todayXp)
        p = p.withXp(10, day = 105)
        assertEquals(1, p.streak)
        assertEquals(2, p.bestStreak)
    }

    @Test
    fun codecRoundTrips() {
        val p = Progress(
            onboarded = true, name = "Sara", placementTaken = true, placementLevel = CefrLevel.A2,
            unlockedLevel = CefrLevel.B1, lessonScores = mapOf("a1-u1-l1" to 90),
            examScores = mapOf(CefrLevel.A1 to 75), xp = 420, streak = 3, bestStreak = 5,
            lastActiveDay = 20000, todayXp = 30, dailyGoal = 100, speechRate = 0.8f,
            cards = mapOf("hello" to Card(2, 20003)), reviewsDone = 7, activeDays = setOf(19999L, 20000L),
        )
        assertEquals(p, ProgressCodec.decode(ProgressCodec.encode(p)))
    }

    @Test
    fun speechScoreToleratesSmallDifferences() {
        assertEquals(1f, Answers.speechScore("I am a student.", "i am a student"))
        assertTrue(Answers.speechScore("She works in a bank", "she work in a bank") >= Answers.SPEECH_PASS)
        assertTrue(Answers.speechScore("She works in a bank", "hello") < Answers.SPEECH_PASS)
    }

    @Test
    fun typingIgnoresCaseAndPunctuation() {
        val q = Question.Typing("", listOf("can't", "cannot"))
        assertTrue(Answers.checkTyping(q, "  Can’t. "))
        assertTrue(Answers.checkTyping(q, "CANNOT"))
        assertFalse(Answers.checkTyping(q, "cant"))
    }
}
