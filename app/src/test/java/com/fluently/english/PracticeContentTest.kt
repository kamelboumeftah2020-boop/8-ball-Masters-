package com.fluently.english

import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.IrregularVerbs
import com.fluently.english.data.content.ReplyQuality
import com.fluently.english.data.content.Scenarios
import com.fluently.english.data.content.SoundLessons
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressCodec
import com.fluently.english.ui.screens.dailyQuestions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeContentTest {

    @Test
    fun everyConversationTurnHasOneBestReplyAndFeedback() {
        assertEquals(Scenarios.size, Scenarios.map { it.id }.toSet().size)
        CefrLevel.entries.forEach { level -> assertTrue("$level has no conversation", Scenarios.any { it.level == level }) }
        Scenarios.forEach { s ->
            assertTrue(s.id, s.turns.size >= 2)
            s.turns.forEach { t ->
                assertEquals("${s.id}: '${t.line}'", 1, t.replies.count { it.quality == ReplyQuality.BEST })
                assertTrue(t.replies.any { it.quality == ReplyQuality.WRONG })
                assertTrue(t.replies.all { it.feedback.isNotBlank() })
                assertEquals(t.replies.size, t.replies.map { it.en }.toSet().size)
            }
        }
    }

    @Test
    fun soundLessonsAreMinimalPairs() {
        SoundLessons.forEach { s ->
            assertEquals(s.id, 8, s.pairs.size)
            s.pairs.forEach { (a, b) -> assertTrue("$a/$b", a != b) }
        }
    }

    @Test
    fun irregularVerbsAreUnique() {
        assertEquals(IrregularVerbs.size, IrregularVerbs.map { it.base }.toSet().size)
        assertTrue(IrregularVerbs.size >= 60)
    }

    @Test
    fun dailyChallengeWorksForNewLearners() {
        val questions = dailyQuestions(Progress(), day = 100)
        assertEquals(5, questions.size)
        assertEquals(questions, dailyQuestions(Progress(), day = 100))
    }

    @Test
    fun newProgressFieldsSurviveEncoding() {
        val p = Progress(
            mistakes = setOf("a1-u1-l1#2"), conversationStars = mapOf("conv-a1-cafe" to 3),
            soundScores = mapOf("p-b" to 75), speedBest = 21, gamesPlayed = 4, lastChallengeDay = 20001, reminderHour = 18,
        )
        assertEquals(p, ProgressCodec.decode(ProgressCodec.encode(p)))
    }
}
