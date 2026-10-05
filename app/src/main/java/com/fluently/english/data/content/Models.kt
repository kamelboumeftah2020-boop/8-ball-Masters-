package com.fluently.english.data.content

/**
 * The six levels of the Common European Framework of Reference (CEFR) — the
 * standard used by Cambridge English, Oxford, the British Council, IELTS and
 * TOEFL to describe language ability.
 */
enum class CefrLevel(
    val code: String,
    val titleAr: String,
    val stageAr: String,
    val canDoAr: String,
    val cambridge: String,
    val ielts: String,
    val toefl: String,
) {
    A1(
        "A1", "مبتدئ", "المرحلة الأساسية",
        "تفهم وتستخدم عبارات يومية بسيطة، وتعرّف بنفسك وتسأل وتجيب عن معلومات شخصية.",
        "Pre A1 / A1 Movers", "أقل من 3.0", "—",
    ),
    A2(
        "A2", "أساسي", "المرحلة الأساسية",
        "تفهم جملاً شائعة عن العائلة والتسوق والعمل، وتتواصل في مواقف روتينية بسيطة.",
        "A2 Key (KET)", "3.0 – 3.5", "—",
    ),
    B1(
        "B1", "متوسط", "المرحلة المستقلة",
        "تتعامل مع معظم مواقف السفر، وتتحدث عن تجاربك وأحلامك وتبرر آراءك باختصار.",
        "B1 Preliminary (PET)", "4.0 – 5.0", "42 – 71",
    ),
    B2(
        "B2", "فوق المتوسط", "المرحلة المستقلة",
        "تفهم الأفكار الرئيسية في نصوص معقدة، وتتحدث بطلاقة وعفوية مع المتحدثين الأصليين.",
        "B2 First (FCE)", "5.5 – 6.5", "72 – 94",
    ),
    C1(
        "C1", "متقدم", "مرحلة الإتقان",
        "تفهم نصوصاً طويلة وصعبة، وتعبّر عن نفسك بطلاقة ومرونة في الحياة الأكاديمية والمهنية.",
        "C1 Advanced (CAE)", "7.0 – 8.0", "95 – 113",
    ),
    C2(
        "C2", "احترافي", "مرحلة الإتقان",
        "تفهم كل ما تسمعه أو تقرؤه تقريباً، وتعبّر عن نفسك بدقة عالية وتميّز الفروق الدقيقة في المعنى.",
        "C2 Proficiency (CPE)", "8.5 – 9.0", "114 – 120",
    );

    val next: CefrLevel? get() = entries.getOrNull(ordinal + 1)
    val previous: CefrLevel? get() = entries.getOrNull(ordinal - 1)
}

enum class LessonType(val labelAr: String) {
    GRAMMAR("قواعد"),
    VOCABULARY("مفردات"),
    READING("قراءة"),
    LISTENING("استماع"),
}

data class Word(
    val en: String,
    val ar: String,
    val example: String,
)

data class Example(val en: String, val ar: String)

sealed interface Question {
    /** Optional Arabic feedback shown after answering. */
    val explanation: String?

    /**
     * Multiple choice. [options] are stored with the correct answer first and are
     * shuffled when shown. If [audio] is set, the prompt is spoken (listening item).
     */
    data class Choice(
        val prompt: String,
        val options: List<String>,
        val audio: String? = null,
        override val explanation: String? = null,
    ) : Question {
        val answer: String get() = options.first()
    }

    /** Rebuild a sentence by tapping shuffled words in the correct order. */
    data class Order(
        val sentence: String,
        val translation: String,
        override val explanation: String? = null,
    ) : Question {
        val tokens: List<String> get() = sentence.split(" ")
    }

    /** Type the answer. Comparison ignores case and surrounding punctuation. */
    data class Typing(
        val prompt: String,
        val answers: List<String>,
        val audio: String? = null,
        override val explanation: String? = null,
    ) : Question
}

data class Lesson(
    val id: String,
    val level: CefrLevel,
    val title: String,
    val titleAr: String,
    val type: LessonType,
    /** Arabic explanation paragraphs. */
    val notes: List<String> = emptyList(),
    val examples: List<Example> = emptyList(),
    val words: List<Word> = emptyList(),
    /** Reading passage, or listening script (hidden until the exercise ends). */
    val passage: String? = null,
    val questions: List<Question>,
)

data class CourseUnit(
    val id: String,
    val title: String,
    val titleAr: String,
    val lessons: List<Lesson>,
)

data class LevelCourse(
    val level: CefrLevel,
    val units: List<CourseUnit>,
    /** Hand-written exam items; the exam also samples questions from the lessons. */
    val examQuestions: List<Question>,
) {
    val lessons: List<Lesson> get() = units.flatMap { it.lessons }
    val words: List<Word> get() = lessons.flatMap { it.words }
}

/** A placement-test item, tagged by CEFR level and skill. */
data class PlacementItem(
    val level: CefrLevel,
    val skill: Skill,
    val question: Question.Choice,
    /** Short text the question refers to (reading items). */
    val text: String? = null,
)

enum class Skill(val labelAr: String) {
    USE_OF_ENGLISH("القواعد والمفردات"),
    READING("القراءة"),
    LISTENING("الاستماع"),
}
