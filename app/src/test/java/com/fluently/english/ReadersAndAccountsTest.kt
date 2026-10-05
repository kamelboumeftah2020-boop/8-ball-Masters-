package com.fluently.english

import com.fluently.english.account.AuthValidation
import com.fluently.english.account.FirebaseBackend
import com.fluently.english.account.PasswordHash
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Readers
import com.fluently.english.data.content.Question
import com.fluently.english.data.content.Scenarios
import com.fluently.english.data.content.WritingCheck
import com.fluently.english.data.content.WritingPrompts
import com.fluently.english.data.content.IssueKind
import com.fluently.english.data.content.activities
import com.fluently.english.data.progress.weekIndex
import com.fluently.english.data.progress.weekStart
import com.fluently.english.data.progress.LearningGoal
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressCodec
import com.fluently.english.data.progress.SkillKey
import com.fluently.english.data.progress.withSkill
import com.fluently.english.data.progress.withXp
import com.fluently.english.ui.screens.goalPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadersAndAccountsTest {

    @Test
    fun readersAreWellFormed() {
        assertEquals(Readers.size, Readers.map { it.id }.toSet().size)
        CefrLevel.entries.forEach { level -> assertTrue("no reader for $level", Readers.count { it.level == level } >= 3) }
        Readers.forEach { r ->
            assertTrue(r.chapters.size >= 3)
            r.chapters.forEach { ch ->
                assertTrue("${r.id} ${ch.title} has no questions", ch.questions.size >= 2)
                assertTrue("${r.id} ${ch.title} has brackets", '[' !in ch.text && ']' !in ch.text)
                ch.questions.forEach { q -> assertEquals("duplicate options in ${q.prompt}", q.options.size, q.options.toSet().size) }
                ch.glossary.keys.forEach { term ->
                    val found = Regex("\\b" + Regex.escape(term) + "\\b", RegexOption.IGNORE_CASE).containsMatchIn(ch.text)
                    assertTrue("${r.id}: glossary term '$term' not in '${ch.title}'", found)
                }
            }
        }
    }

    @Test
    fun formValidation() {
        assertNull(AuthValidation.signUpError("Sara", "sara@mail.com", "secret1", "secret1"))
        assertNotNull(AuthValidation.signUpError("", "sara@mail.com", "secret1", "secret1"))
        assertNotNull(AuthValidation.signUpError("Sara", "sara@mail", "secret1", "secret1"))
        assertNotNull(AuthValidation.signUpError("Sara", "sara@mail.com", "123", "123"))
        assertNotNull(AuthValidation.signUpError("Sara", "sara@mail.com", "secret1", "secret2"))
        assertNull(AuthValidation.signInError("sara@mail.com", "x"))
    }

    @Test
    fun passwordsAreHashedWithSalt() {
        val salt1 = ByteArray(16) { 1 }
        val salt2 = ByteArray(16) { 2 }
        assertEquals(PasswordHash.hash("secret", salt1), PasswordHash.hash("secret", salt1))
        assertNotEquals(PasswordHash.hash("secret", salt1), PasswordHash.hash("secret", salt2))
        assertNotEquals(PasswordHash.hash("secret", salt1), PasswordHash.hash("Secret", salt1))
    }

    @Test
    fun firebaseErrorsAreTranslated() {
        val body = """{"error":{"code":400,"message":"EMAIL_EXISTS"}}"""
        assertTrue(FirebaseBackend.firebaseMessage(body).contains("مسجّل"))
        val creds = """{"error":{"message":"INVALID_LOGIN_CREDENTIALS"}}"""
        assertTrue(FirebaseBackend.firebaseMessage(creds).contains("غير صحيحة"))
        assertTrue(FirebaseBackend.firebaseMessage("""{"error":{"message":"CONFIGURATION_NOT_FOUND"}}""").contains("غير مفعّلة"))
    }

    @Test
    fun skillsDailyXpAndGoalSurviveTheCodec() {
        val p = Progress(goal = LearningGoal.WORK.name, readerChapters = mapOf("r-a1-1" to 2), wordsRead = 480)
            .withSkill(SkillKey.READING, 3, 4)
            .withSkill(SkillKey.READING, 1, 2)
            .withXp(30, 20000)
            .withXp(20, 20000)
        assertEquals(66, p.skillAccuracy()[SkillKey.READING])
        assertEquals(50, p.dayXp[20000L])
        assertEquals(p, ProgressCodec.decode(ProgressCodec.encode(p)))
        // Old XP days fall out of the five-week window.
        assertTrue(p.withXp(5, 20100).dayXp.keys == setOf(20100L))
    }

    @Test
    fun everyGoalHasAPlan() {
        LearningGoal.entries.forEach { goal ->
            val plan = goalPlan(goal, Progress())
            assertTrue("$goal plan too short", plan.size >= 3)
            assertEquals(plan.size, plan.map { it.route }.toSet().size)
        }
    }

    @Test
    fun everyChapterGetsPracticeActivities() {
        Readers.forEach { r ->
            r.chapters.forEach { ch ->
                val acts = ch.activities()
                assertTrue("${r.id} ${ch.title}: no activities", acts.isNotEmpty())
                acts.filterIsInstance<Question.Order>().forEach { assertTrue(it.sentence in ch.text) }
            }
        }
    }

    @Test
    fun writingCheckerFindsCommonMistakes() {
        val bad = WritingCheck.check("i think he go to school every day. She have a apple and we discuss about informations", 50)
        val messages = bad.issues.map { it.excerpt.orEmpty().lowercase() }
        assertTrue("he go", messages.any { it.contains("he go") })
        assertTrue("she have", messages.any { it.contains("she have") })
        assertTrue("a apple", messages.any { it.contains("a apple") })
        assertTrue("discuss about", messages.any { it.contains("discuss about") })
        assertTrue("informations", messages.any { it.contains("informations") })
        assertTrue(bad.issues.any { it.kind == IssueKind.LENGTH })
        assertTrue(bad.issues.any { it.kind == IssueKind.MECHANICS })

        val good = WritingCheck.check(
            "Many people believe that technology improves our lives. However, it also creates new problems.\n\n" +
                "Firstly, does he go online too often? For example, teenagers spend hours on their phones. " +
                "In addition, they had had little sleep before exams.\n\nIn conclusion, we should use technology wisely.",
        )
        assertTrue("false positives: ${good.issues.filter { it.kind == IssueKind.GRAMMAR }}", good.issues.none { it.kind == IssueKind.GRAMMAR })
        assertTrue(good.linkers.size >= 4)
        assertEquals(3, good.paragraphs)
    }

    @Test
    fun writingPromptsAndConversationsAreValid() {
        assertEquals(WritingPrompts.size, WritingPrompts.map { it.id }.toSet().size)
        CefrLevel.entries.forEach { l -> assertTrue(WritingPrompts.any { it.level == l }) }
        assertEquals(Scenarios.size, Scenarios.map { it.id }.toSet().size)
        assertTrue(Scenarios.size >= 16)
        assertEquals(Scenarios.sortedBy { it.level.ordinal }, Scenarios)
    }

    @Test
    fun leaderboardWeeksStartOnMonday() {
        // 2026-10-05 is a Monday (epoch day 20731).
        val monday = 20731L
        assertEquals(monday, weekStart(monday))
        assertEquals(monday, weekStart(monday + 6))
        assertEquals(weekIndex(monday), weekIndex(monday + 6))
        assertNotEquals(weekIndex(monday), weekIndex(monday + 7))
        val p = Progress().withXp(40, monday - 1).withXp(25, monday).withXp(10, monday + 3)
        assertEquals(35, p.weekXp(monday + 3))
    }

    @Test
    fun emailAddressesAreCheckedCarefully() {
        assertNull(AuthValidation.emailError("sara.ali@gmail.com"))
        assertNull(AuthValidation.emailError("omar@uni.edu.sa"))
        assertNotNull(AuthValidation.emailError("sara@gmail"))
        assertNotNull(AuthValidation.emailError("sara gmail.com"))
        assertNotNull(AuthValidation.emailError("sara@@gmail.com"))
        assertNotNull(AuthValidation.emailError("sara..ali@gmail.com"))
        assertNotNull(AuthValidation.emailError("sara@mailinator.com"))
        assertEquals("sara@gmail.com", AuthValidation.suggestion("Sara@gmial.com"))
        assertEquals("ali@hotmail.com", AuthValidation.suggestion("ali@hotmial.com"))
        assertNull(AuthValidation.suggestion("ali@gmail.com"))
        assertTrue(AuthValidation.emailError("sara@gmial.com")!!.contains("gmail.com"))
    }
}
