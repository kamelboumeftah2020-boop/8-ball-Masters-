package com.fluently.english.data.progress

import android.content.Context
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.TimeZone

/** Persists [Progress] as a single JSON document in SharedPreferences. */
class ProgressRepository(context: Context, private val today: () -> Long = ::localEpochDay) {

    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)
    private val _progress = MutableStateFlow(load())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    fun today(): Long = today.invoke()

    private fun update(transform: (Progress) -> Progress) {
        val updated = transform(_progress.value)
        _progress.value = updated
        prefs.edit().putString(KEY, ProgressCodec.encode(updated)).apply()
    }

    fun finishOnboarding(name: String) = update { it.copy(onboarded = true, name = name.trim()) }

    fun setName(name: String) = update { it.copy(name = name.trim()) }

    fun setDailyGoal(goal: Int) = update { it.copy(dailyGoal = goal) }

    fun setSpeechRate(rate: Float) = update { it.copy(speechRate = rate) }

    fun applyPlacement(result: CefrLevel?, recommended: CefrLevel) = update {
        val unlocked = if (recommended.ordinal > it.unlockedLevel.ordinal) recommended else it.unlockedLevel
        it.copy(
            onboarded = true,
            placementTaken = true,
            // Retaking the test never takes away levels already proven.
            placementLevel = listOfNotNull(result, it.placementLevel).maxByOrNull { l -> l.ordinal },
            unlockedLevel = unlocked,
        ).withXp(PLACEMENT_XP, today())
    }

    /** Records a lesson attempt; returns the XP earned. */
    fun completeLesson(lessonId: String, correct: Int, total: Int): Int {
        val lesson = Course.lesson(lessonId) ?: return 0
        val percent = percent(correct, total)
        val xp = correct * 10 + if (correct == total) PERFECT_BONUS else 0
        update { p ->
            val best = maxOf(p.lessonScores[lessonId] ?: 0, percent)
            var next = p.copy(lessonScores = p.lessonScores + (lessonId to best)).withXp(xp, today())
            if (percent >= Course.LESSON_PASS_PERCENT && lesson.words.isNotEmpty()) {
                val newCards = lesson.words
                    .filter { it.en !in next.cards }
                    .associate { it.en to Card(box = 0, dueDay = today()) }
                next = next.copy(cards = next.cards + newCards)
            }
            next
        }
        return xp
    }

    /** Records an exam attempt; passing unlocks the next level. Returns the XP earned. */
    fun completeExam(level: CefrLevel, correct: Int, total: Int): Int {
        val percent = percent(correct, total)
        val passed = percent >= Course.EXAM_PASS_PERCENT
        val xp = correct * 15 + if (passed) EXAM_PASS_BONUS else 0
        update { p ->
            val best = maxOf(p.examScores[level] ?: 0, percent)
            val unlocked = if (passed) {
                val next = level.next ?: level
                if (next.ordinal > p.unlockedLevel.ordinal) next else p.unlockedLevel
            } else {
                p.unlockedLevel
            }
            p.copy(examScores = p.examScores + (level to best), unlockedLevel = unlocked).withXp(xp, today())
        }
        return xp
    }

    /** Leitner review: a known card moves up a box, a forgotten one goes back to box 0. */
    fun reviewCard(word: String, known: Boolean) = update { p ->
        val card = p.cards[word] ?: return@update p
        val box = if (known) minOf(card.box + 1, INTERVALS.lastIndex) else 0
        val interval = if (known) INTERVALS[box] else 1
        p.copy(
            cards = p.cards + (word to Card(box, today() + interval)),
            reviewsDone = p.reviewsDone + 1,
        ).withXp(REVIEW_XP, today())
    }

    fun reset() = update { Progress() }

    private fun load(): Progress {
        val raw = prefs.getString(KEY, null) ?: return Progress()
        val p = runCatching { ProgressCodec.decode(raw) }.getOrDefault(Progress())
        // Reset today's XP and break the streak if the learner skipped days.
        val day = today()
        return when {
            p.lastActiveDay == day -> p
            p.lastActiveDay == day - 1 -> p.copy(todayXp = 0)
            else -> p.copy(todayXp = 0, streak = 0)
        }
    }

    companion object {
        private const val KEY = "progress_v1"
        const val PLACEMENT_XP = 50
        const val PERFECT_BONUS = 20
        const val EXAM_PASS_BONUS = 100
        const val REVIEW_XP = 2

        /** Days until the next review for each Leitner box. */
        val INTERVALS = listOf(1, 2, 4, 7, 15, 30)

        fun percent(correct: Int, total: Int) = if (total == 0) 0 else correct * 100 / total
    }
}

/** Days since 1970-01-01 in the device's time zone (java.time needs API 26). */
fun localEpochDay(): Long {
    val now = System.currentTimeMillis()
    return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
}

internal fun Progress.withXp(amount: Int, day: Long): Progress {
    val streakNow = when (lastActiveDay) {
        day -> maxOf(streak, 1)
        day - 1 -> streak + 1
        else -> 1
    }
    return copy(
        xp = xp + amount,
        todayXp = (if (lastActiveDay == day) todayXp else 0) + amount,
        lastActiveDay = day,
        streak = streakNow,
        bestStreak = maxOf(bestStreak, streakNow),
    )
}

internal object ProgressCodec {
    fun encode(p: Progress): String = JSONObject().apply {
        put("onboarded", p.onboarded)
        put("name", p.name)
        put("placementTaken", p.placementTaken)
        put("placementLevel", p.placementLevel?.name ?: "")
        put("unlockedLevel", p.unlockedLevel.name)
        put("lessonScores", JSONObject(p.lessonScores))
        put("examScores", JSONObject(p.examScores.mapKeys { it.key.name }))
        put("xp", p.xp)
        put("streak", p.streak)
        put("bestStreak", p.bestStreak)
        put("lastActiveDay", p.lastActiveDay)
        put("todayXp", p.todayXp)
        put("dailyGoal", p.dailyGoal)
        put("speechRate", p.speechRate.toDouble())
        put("reviewsDone", p.reviewsDone)
        put("cards", JSONObject().apply {
            p.cards.forEach { (word, card) -> put(word, JSONObject().put("box", card.box).put("due", card.dueDay)) }
        })
    }.toString()

    fun decode(raw: String): Progress {
        val o = JSONObject(raw)
        fun intMap(obj: JSONObject?): Map<String, Int> =
            obj?.keys()?.asSequence()?.associateWith { obj.getInt(it) } ?: emptyMap()
        val cardsObj = o.optJSONObject("cards")
        return Progress(
            onboarded = o.optBoolean("onboarded"),
            name = o.optString("name"),
            placementTaken = o.optBoolean("placementTaken"),
            placementLevel = o.optString("placementLevel").takeIf { it.isNotEmpty() }?.let { CefrLevel.valueOf(it) },
            unlockedLevel = CefrLevel.valueOf(o.optString("unlockedLevel", CefrLevel.A1.name)),
            lessonScores = intMap(o.optJSONObject("lessonScores")),
            examScores = intMap(o.optJSONObject("examScores")).mapKeys { CefrLevel.valueOf(it.key) },
            xp = o.optInt("xp"),
            streak = o.optInt("streak"),
            bestStreak = o.optInt("bestStreak"),
            lastActiveDay = o.optLong("lastActiveDay"),
            todayXp = o.optInt("todayXp"),
            dailyGoal = o.optInt("dailyGoal", 50),
            speechRate = o.optDouble("speechRate", 0.9).toFloat(),
            reviewsDone = o.optInt("reviewsDone"),
            cards = cardsObj?.keys()?.asSequence()?.associateWith {
                val c = cardsObj.getJSONObject(it)
                Card(c.getInt("box"), c.getLong("due"))
            } ?: emptyMap(),
        )
    }
}
