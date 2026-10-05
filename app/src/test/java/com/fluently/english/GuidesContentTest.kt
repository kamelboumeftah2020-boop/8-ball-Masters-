package com.fluently.english

import com.fluently.english.data.content.Course
import com.fluently.english.data.content.LessonType
import com.fluently.english.data.content.TextGuides
import com.fluently.english.data.content.VocabGuides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidesContentTest {
    private val lessons = Course.levels.flatMap { it.lessons }

    @Test
    fun everyVocabularyLessonIsFullyTaught() {
        val vocab = lessons.filter { it.type == LessonType.VOCABULARY }
        assertEquals(30, vocab.size)
        assertEquals(30, VocabGuides.size)
        vocab.forEach { lesson ->
            val g = lesson.vocabGuide
            assertTrue("${lesson.id} has no guide", g != null)
            g!!
            val words = lesson.words.map { it.en }
            assertEquals("${lesson.id}: hints must cover every word", words.toSet(), g.hints.keys)
            g.groups.flatMap { it.words }.forEach { assertTrue("${lesson.id}: unknown group word $it", it in words) }
            assertEquals("${lesson.id}: groups must cover every word", words.toSet(), g.groups.flatMap { it.words }.toSet())
            val story = g.story.lowercase()
            assertEquals("${lesson.id}: unbalanced brackets", g.story.count { it == '[' }, g.story.count { it == ']' })
            assertTrue("${lesson.id}: story too short", g.story.count { it == '[' } >= 6)
            g.checks.forEach { assertEquals(it.options.size, it.options.toSet().size) }
            assertTrue(story.isNotBlank() && g.storyAr.isNotBlank())
        }
    }

    @Test
    fun everyReadingAndListeningLessonHasATextGuide() {
        val texts = lessons.filter { it.type == LessonType.READING || it.type == LessonType.LISTENING }
        assertEquals(30, texts.size)
        assertEquals(30, TextGuides.size)
        texts.forEach { lesson ->
            val g = lesson.textGuide
            assertTrue("${lesson.id} has no text guide", g != null)
            val passage = lesson.passage.orEmpty().lowercase()
            g!!.keyWords.forEach { kw ->
                assertTrue("${lesson.id}: key word '${kw.en}' not in text", passage.contains(kw.en.lowercase()))
            }
            assertTrue(g.keyWords.size >= 4)
            listOf(g.predict, g.gist).forEach { assertEquals(it.options.size, it.options.toSet().size) }
        }
    }
}
