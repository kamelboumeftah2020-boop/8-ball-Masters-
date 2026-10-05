package com.fluently.english.data.progress

import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.Course
import com.fluently.english.data.content.Lesson

/** A vocabulary flashcard in the Leitner spaced-repetition system. */
data class Card(val box: Int, val dueDay: Long)

data class Progress(
    val onboarded: Boolean = false,
    val name: String = "",
    val placementTaken: Boolean = false,
    /** Highest level proven by the placement test (null = below A1 or not taken). */
    val placementLevel: CefrLevel? = null,
    /** Highest course level the learner can access. */
    val unlockedLevel: CefrLevel = CefrLevel.A1,
    val lessonScores: Map<String, Int> = emptyMap(),
    val examScores: Map<CefrLevel, Int> = emptyMap(),
    val xp: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDay: Long = 0,
    val todayXp: Int = 0,
    val dailyGoal: Int = 50,
    val speechRate: Float = 0.9f,
    val cards: Map<String, Card> = emptyMap(),
    val reviewsDone: Int = 0,
    /** Days with any activity (last few weeks), for the weekly strip. */
    val activeDays: Set<Long> = emptySet(),
    /** Questions answered wrongly in lessons ("lessonId#index"), for the mistakes notebook. */
    val mistakes: Set<String> = emptySet(),
    val conversationStars: Map<String, Int> = emptyMap(),
    val soundScores: Map<String, Int> = emptyMap(),
    val speedBest: Int = 0,
    val gamesPlayed: Int = 0,
    val lastChallengeDay: Long = -1,
    /** Hour of the daily reminder, or -1 when reminders are off. */
    val reminderHour: Int = -1,
) {
    fun challengeDoneToday(today: Long) = lastChallengeDay == today

    fun isLessonDone(id: String) = (lessonScores[id] ?: 0) >= Course.LESSON_PASS_PERCENT

    /** A level is mastered by passing its exam, or by placing above it. */
    fun isLevelPassed(level: CefrLevel): Boolean =
        (examScores[level] ?: 0) >= Course.EXAM_PASS_PERCENT ||
            (placementLevel != null && level.ordinal <= placementLevel.ordinal)

    fun isLevelUnlocked(level: CefrLevel) = level.ordinal <= unlockedLevel.ordinal

    fun isLessonUnlocked(lesson: Lesson): Boolean {
        if (!isLevelUnlocked(lesson.level)) return false
        if (isLevelPassed(lesson.level)) return true
        val lessons = Course.level(lesson.level).lessons
        val index = lessons.indexOfFirst { it.id == lesson.id }
        return index <= 0 || isLessonDone(lessons[index - 1].id)
    }

    fun isExamUnlocked(level: CefrLevel): Boolean =
        isLevelUnlocked(level) &&
            (isLevelPassed(level) || Course.level(level).lessons.all { isLessonDone(it.id) })

    fun levelProgress(level: CefrLevel): Float {
        val lessons = Course.level(level).lessons
        if (isLevelPassed(level)) return 1f
        val done = lessons.count { isLessonDone(it.id) }
        // The exam counts as one extra step.
        return done.toFloat() / (lessons.size + 1)
    }

    /** The level the learner is currently working on. */
    val currentLevel: CefrLevel
        get() = CefrLevel.entries.firstOrNull { isLevelUnlocked(it) && !isLevelPassed(it) } ?: unlockedLevel

    /** The next lesson to study, or null if the current level only needs its exam. */
    fun nextLesson(): Lesson? =
        Course.level(currentLevel).lessons.firstOrNull { !isLessonDone(it.id) && isLessonUnlocked(it) }

    val lessonsCompleted: Int get() = lessonScores.count { it.value >= Course.LESSON_PASS_PERCENT }
    val wordsLearned: Int get() = cards.size
    val wordsMastered: Int get() = cards.count { it.value.box >= 3 }

    fun dueCards(today: Long): List<String> =
        cards.filter { it.value.dueDay <= today }.toList().sortedBy { it.second.dueDay }.map { it.first }
}

/** [icon] is a key the UI maps to an icon. */
data class Achievement(val icon: String, val title: String, val description: String, val unlocked: Boolean)

fun Progress.achievements(): List<Achievement> = listOf(
    Achievement("start", "البداية", "أكمل أول درس", lessonsCompleted >= 1),
    Achievement("compass", "اعرف مستواك", "أكمل اختبار تحديد المستوى", placementTaken),
    Achievement("flame", "أسبوع كامل", "حافظ على سلسلة 7 أيام", bestStreak >= 7),
    Achievement("book", "جامع الكلمات", "تعلم 50 كلمة", wordsLearned >= 50),
    Achievement("brain", "ذاكرة قوية", "أتقن 30 كلمة في المراجعة", wordsMastered >= 30),
    Achievement("bolt", "ألف نقطة", "اجمع 1000 نقطة خبرة", xp >= 1000),
    Achievement("cap", "أول شهادة", "اجتز امتحان أي مستوى", examScores.values.any { it >= Course.EXAM_PASS_PERCENT }),
    Achievement("chat", "متحدث", "أكمل 3 محادثات", conversationStars.size >= 3),
    Achievement("ear", "أذن ذهبية", "أكمل 3 دروس نطق", soundScores.size >= 3),
    Achievement("game", "لاعب", "العب 10 ألعاب", gamesPlayed >= 10),
    Achievement("trophy", "محترف", "اجتز مستوى C2", isLevelPassed(CefrLevel.C2)),
)
