package com.fluently.english

import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.IrregularVerbs
import com.fluently.english.data.content.MockExams
import com.fluently.english.data.content.MockKind
import com.fluently.english.data.content.MockScoring
import com.fluently.english.data.content.Question
import com.fluently.english.data.content.SectionType
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
            mockBest = mapOf("mock-ielts-1" to 65, "mock-pet-1" to 151),
        )
        assertEquals(p, ProgressCodec.decode(ProgressCodec.encode(p)))
    }

    @Test
    fun mockExamsAreComplete() {
        assertEquals(MockExams.size, MockExams.map { it.id }.toSet().size)
        assertEquals(MockKind.entries.toSet(), MockExams.map { it.kind }.toSet())
        MockExams.forEach { exam ->
            val types = exam.sections.map { it.type }.toSet()
            listOf(SectionType.LISTENING, SectionType.READING, SectionType.WRITING, SectionType.SPEAKING).forEach {
                assertTrue("${exam.id} lacks $it", it in types)
            }
            exam.sections.forEach { s ->
                assertTrue("${exam.id} ${s.title} is empty", s.parts.isNotEmpty() || s.writing != null || s.speaking.isNotEmpty())
                assertTrue(s.minutes > 0)
                s.parts.forEach { part ->
                    assertTrue("${part.title} has no questions", part.questions.isNotEmpty())
                    part.questions.forEach { q ->
                        when (q) {
                            is Question.Choice -> assertEquals("duplicate options in ${q.prompt}", q.options.size, q.options.toSet().size)
                            is Question.Typing -> assertTrue(q.answers.isNotEmpty())
                            else -> {}
                        }
                    }
                    if (s.type == SectionType.LISTENING) {
                        assertTrue("${part.title}: no audio", part.text != null || part.questions.all { (it as? Question.Choice)?.audio != null })
                    }
                }
                s.writing?.let { w -> assertTrue(w.modelAnswer.split(" ").size >= w.minWords * 0.9) }
                s.speaking.forEach { sp ->
                    assertTrue(sp.questions.isNotEmpty())
                    if (sp.cueCard.isNotEmpty()) assertTrue(sp.prepSeconds > 0 && sp.talkSeconds > 0)
                }
            }
        }
    }

    @Test
    fun mockScoringFollowsTheOfficialScales() {
        assertEquals(9.0, MockScoring.ieltsBand(10, 10, listening = true), 0.0)
        assertEquals(9.0, MockScoring.ieltsBand(10, 10, listening = false), 0.0)
        assertEquals(6.0, MockScoring.ieltsBand(23, 40, listening = true), 0.0)
        assertEquals(2.5, MockScoring.ieltsBand(0, 10, listening = true), 0.0)
        assertEquals(6.5, MockScoring.overallIelts(listOf(6.5, 6.5, 5.0, 7.0)), 0.0) // 6.25 rounds up
        assertEquals(7.0, MockScoring.overallIelts(listOf(6.5, 7.0, 7.0, 7.5)), 0.0)
        assertEquals(8.0, MockScoring.selfBand(listOf(3, 3, 3, 3)), 0.0)
        assertEquals(4.5, MockScoring.selfBand(listOf(0, 0, 0, 0)), 0.0)
        listOf(MockKind.CAMBRIDGE_B1, MockKind.CAMBRIDGE_B2).forEach { kind ->
            val scale = (0..100).map { MockScoring.cambridgeScale(kind, it) }
            assertTrue(scale.zipWithNext().all { (a, b) -> a <= b })
        }
        assertEquals(140, MockScoring.cambridgeScale(MockKind.CAMBRIDGE_B1, 70))
        assertEquals(160, MockScoring.cambridgeScale(MockKind.CAMBRIDGE_B2, 60))
        assertEquals("C1", MockScoring.cambridgeGrade(MockKind.CAMBRIDGE_B2, 185).second)
        assertEquals("B1", MockScoring.cambridgeGrade(MockKind.CAMBRIDGE_B1, 145).second)
    }
}
