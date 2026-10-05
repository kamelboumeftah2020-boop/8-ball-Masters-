package com.fluently.english.data.progress

import android.content.Context
import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.LessonType
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
        prefs.edit().putString(KEY, ProgressCodec.encode(updated)).putLong(KEY_UPDATED, System.currentTimeMillis()).apply()
        onChange?.invoke(updated)
    }

    /** When the local progress last changed (ms), to choose between local and cloud copies. */
    val updatedAt: Long get() = prefs.getLong(KEY_UPDATED, 0)

    /** The account the local progress belongs to ("" = created before signing in). */
    var owner: String
        get() = prefs.getString(KEY_OWNER, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_OWNER, value).apply()

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
            var next = p.copy(lessonScores = p.lessonScores + (lessonId to best))
                .withSkill(lesson.type.skill(), correct, total).withXp(xp, today())
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

    /** Updates the mistakes notebook after a lesson: wrong items are added, right ones removed. */
    fun recordLessonAnswers(lessonId: String, wrong: List<Int>, right: List<Int>) = update { p ->
        p.copy(mistakes = p.mistakes + wrong.map { "$lessonId#$it" } - right.map { "$lessonId#$it" }.toSet())
    }

    /** Mistakes-notebook review: items answered correctly leave the notebook. */
    fun resolveMistakes(fixed: List<String>): Int {
        update { p -> p.copy(mistakes = p.mistakes - fixed.toSet()).withXp(fixed.size * 5, today()) }
        return fixed.size * 5
    }

    fun completeConversation(id: String, stars: Int, points: Int): Int {
        val xp = points * 5
        update { p ->
            p.copy(conversationStars = p.conversationStars + (id to maxOf(stars, p.conversationStars[id] ?: 0)))
                .withSkill(SkillKey.SPEAKING, stars, 3).withXp(xp, today())
        }
        return xp
    }

    fun completeSound(id: String, percent: Int, correct: Int): Int {
        val xp = correct * 3
        update { p ->
            p.copy(soundScores = p.soundScores + (id to maxOf(percent, p.soundScores[id] ?: 0)))
                .withSkill(SkillKey.SPEAKING, percent / 10, 10).withXp(xp, today())
        }
        return xp
    }

    /** Records a finished game; returns the XP earned. */
    fun completeGame(correct: Int, speedScore: Int? = null): Int {
        val xp = correct * 3
        update { p ->
            p.copy(
                gamesPlayed = p.gamesPlayed + 1,
                speedBest = maxOf(p.speedBest, speedScore ?: 0),
            ).withXp(xp, today())
        }
        return xp
    }

    /** Records a finished mock exam; [score] is band × 10 (IELTS) or the scale score (Cambridge). */
    fun completeMock(id: String, score: Int, correct: Int, skills: List<Triple<SkillKey, Int, Int>> = emptyList()): Int {
        val xp = correct * 5
        update { p ->
            skills.fold(p.copy(mockBest = p.mockBest + (id to maxOf(score, p.mockBest[id] ?: 0)))) { acc, (k, r, t) ->
                acc.withSkill(k, r, t)
            }.withXp(xp, today())
        }
        return xp
    }

    fun completeDailyChallenge(correct: Int): Int {
        val already = _progress.value.challengeDoneToday(today())
        val xp = correct * 10 + if (already) 0 else DAILY_BONUS
        update { p -> p.copy(lastChallengeDay = today()).withXp(xp, today()) }
        return xp
    }

    fun setReminderHour(hour: Int) = update { it.copy(reminderHour = hour) }

    fun setGoal(goal: LearningGoal) = update { it.copy(goal = goal.name) }

    /** Adds answers to the per-skill statistics. */
    fun recordSkill(skill: SkillKey, right: Int, total: Int) = update { it.withSkill(skill, right, total) }

    /** Marks a reader chapter as read; returns the XP earned (0 if it was already read). */
    fun completeReaderChapter(storyId: String, chapter: Int, words: Int, right: Int, total: Int): Int {
        val already = (_progress.value.readerChapters[storyId] ?: 0) > chapter
        val xp = if (already) right * 2 else words / 10 + right * 5
        update { p ->
            p.copy(
                readerChapters = p.readerChapters + (storyId to maxOf(chapter + 1, p.readerChapters[storyId] ?: 0)),
                wordsRead = p.wordsRead + if (already) 0 else words,
            ).withSkill(SkillKey.READING, right, total).withXp(xp, today())
        }
        return xp
    }

    /** Replaces the whole progress (sign-in, restore from backup). */
    fun replace(p: Progress) = update { p }

    fun exportJson(): String = ProgressCodec.encode(_progress.value)

    /** Restores a backup; returns false if the file is not a valid progress backup. */
    fun importJson(raw: String): Boolean {
        val p = runCatching { ProgressCodec.decode(raw) }.getOrNull() ?: return false
        update { p }
        return true
    }

    /** Called after every change (used for cloud sync). */
    var onChange: ((Progress) -> Unit)? = null

    fun addWordToReview(word: String) = update { p ->
        if (word in p.cards) p else p.copy(cards = p.cards + (word to Card(box = 0, dueDay = today())))
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
        private const val KEY_UPDATED = "progress_updated"
        private const val KEY_OWNER = "progress_owner"
        const val PLACEMENT_XP = 50
        const val PERFECT_BONUS = 20
        const val EXAM_PASS_BONUS = 100
        const val REVIEW_XP = 2
        const val DAILY_BONUS = 30

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

internal fun LessonType.skill(): SkillKey = when (this) {
    LessonType.GRAMMAR -> SkillKey.GRAMMAR
    LessonType.VOCABULARY -> SkillKey.VOCABULARY
    LessonType.READING -> SkillKey.READING
    LessonType.LISTENING -> SkillKey.LISTENING
}

internal fun Progress.withSkill(skill: SkillKey, right: Int, total: Int): Progress =
    if (total <= 0) this else copy(
        skillRight = skillRight + (skill.name to (skillRight[skill.name] ?: 0) + right),
        skillTotal = skillTotal + (skill.name to (skillTotal[skill.name] ?: 0) + total),
    )

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
        activeDays = (activeDays + day).filter { it > day - 35 }.toSet(),
        dayXp = (dayXp + (day to (dayXp[day] ?: 0) + amount)).filterKeys { it > day - 35 },
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
        put("activeDays", org.json.JSONArray(p.activeDays.toList()))
        put("mistakes", org.json.JSONArray(p.mistakes.toList()))
        put("conversationStars", JSONObject(p.conversationStars))
        put("soundScores", JSONObject(p.soundScores))
        put("speedBest", p.speedBest)
        put("gamesPlayed", p.gamesPlayed)
        put("lastChallengeDay", p.lastChallengeDay)
        put("reminderHour", p.reminderHour)
        put("mockBest", JSONObject(p.mockBest))
        put("goal", p.goal)
        put("dayXp", JSONObject(p.dayXp.mapKeys { it.key.toString() }))
        put("skillRight", JSONObject(p.skillRight))
        put("skillTotal", JSONObject(p.skillTotal))
        put("readerChapters", JSONObject(p.readerChapters))
        put("wordsRead", p.wordsRead)
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
            activeDays = o.optJSONArray("activeDays")?.let { a -> (0 until a.length()).map { a.getLong(it) }.toSet() } ?: emptySet(),
            mistakes = o.optJSONArray("mistakes")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet(),
            conversationStars = intMap(o.optJSONObject("conversationStars")),
            soundScores = intMap(o.optJSONObject("soundScores")),
            speedBest = o.optInt("speedBest"),
            gamesPlayed = o.optInt("gamesPlayed"),
            lastChallengeDay = o.optLong("lastChallengeDay", -1),
            reminderHour = o.optInt("reminderHour", -1),
            mockBest = intMap(o.optJSONObject("mockBest")),
            goal = o.optString("goal"),
            dayXp = intMap(o.optJSONObject("dayXp")).mapKeys { it.key.toLong() },
            skillRight = intMap(o.optJSONObject("skillRight")),
            skillTotal = intMap(o.optJSONObject("skillTotal")),
            readerChapters = intMap(o.optJSONObject("readerChapters")),
            wordsRead = o.optInt("wordsRead"),
            cards = cardsObj?.keys()?.asSequence()?.associateWith {
                val c = cardsObj.getJSONObject(it)
                Card(c.getInt("box"), c.getLong("due"))
            } ?: emptyMap(),
        )
    }
}
