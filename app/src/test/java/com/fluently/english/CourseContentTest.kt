package com.fluently.english

import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.content.PlacementBank
import com.fluently.english.data.content.PlacementEngine
import com.fluently.english.data.content.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseContentTest {

    private val allQuestions: List<Question> =
        Course.levels.flatMap { level -> level.lessons.flatMap { it.questions } + level.examQuestions } +
            PlacementBank.map { it.question }

    @Test
    fun everyLevelHasFourUnitsOfThreeLessons() {
        assertEquals(CefrLevel.entries.toList(), Course.levels.map { it.level })
        Course.levels.forEach { level ->
            assertEquals("${level.level} units", 4, level.units.size)
            level.units.forEach { assertEquals("${it.id} lessons", 3, it.lessons.size) }
            assertTrue("${level.level} exam too short", level.examQuestions.size >= 10)
        }
    }

    @Test
    fun lessonIdsAndWordsAreUnique() {
        val ids = Course.levels.flatMap { it.lessons }.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        val words = Course.levels.flatMap { it.words }.map { it.en }
        assertEquals("duplicate words: ${words.groupBy { it }.filter { it.value.size > 1 }.keys}", words.size, words.toSet().size)
    }

    @Test
    fun lessonsHaveTheContentTheirTypeNeeds() {
        Course.levels.flatMap { it.lessons }.forEach { lesson ->
            assertTrue("${lesson.id} has too few questions", lesson.questions.size >= 5)
            when (lesson.type) {
                LessonType.GRAMMAR -> assertTrue(lesson.id, lesson.notes.isNotEmpty() && lesson.examples.isNotEmpty())
                LessonType.VOCABULARY -> assertEquals(lesson.id, 8, lesson.words.size)
                LessonType.READING, LessonType.LISTENING -> assertTrue(lesson.id, !lesson.passage.isNullOrBlank())
            }
        }
    }

    @Test
    fun questionsAreWellFormed() {
        allQuestions.forEach { q ->
            when (q) {
                is Question.Choice -> {
                    assertTrue("too few options: ${q.prompt}", q.options.size >= 2)
                    assertEquals("duplicate options: ${q.prompt}", q.options.size, q.options.toSet().size)
                    assertTrue(q.options.none { it.isBlank() })
                }
                is Question.Order -> assertTrue("order too short: ${q.sentence}", q.tokens.size >= 3)
                is Question.Typing -> assertTrue(q.answers.isNotEmpty() && q.answers.none { it.isBlank() })
            }
        }
    }

    @Test
    fun placementBankHasTwoBlocksPerLevel() {
        CefrLevel.entries.forEach { level ->
            assertEquals("$level items", 2 * PlacementEngine.BLOCK_SIZE, PlacementBank.count { it.level == level })
        }
    }

    @Test
    fun examSamplesLessonQuestions() {
        val exam = Course.examQuestions(CefrLevel.B1, seed = 42)
        assertEquals(Course.level(CefrLevel.B1).examQuestions.size + Course.EXAM_SAMPLE_SIZE, exam.size)
    }
}
