package com.fluently.english.data.content

/*
 * A tiny DSL that keeps the course content readable. For multiple-choice items
 * the correct answer is always written first; options are shuffled at runtime.
 */

fun q(prompt: String, vararg options: String, explain: String? = null) =
    Question.Choice(prompt, options.toList(), explanation = explain)

fun listen(audio: String, prompt: String, vararg options: String, explain: String? = null) =
    Question.Choice(prompt, options.toList(), audio = audio, explanation = explain)

fun order(sentence: String, translation: String, explain: String? = null) =
    Question.Order(sentence, translation, explain)

fun type(prompt: String, vararg answers: String, explain: String? = null) =
    Question.Typing(prompt, answers.toList(), explanation = explain)

fun speak(sentence: String, translation: String) = Question.Speak(sentence, translation)

fun concept(
    title: String,
    body: String,
    formula: String? = null,
    table: Table? = null,
    examples: List<Example> = emptyList(),
    check: Question.Choice? = null,
) = Concept(title, body, formula, table, examples, check)

fun table(headers: List<String>, vararg rows: List<String>) = Table(headers, rows.toList())

fun mistake(wrong: String, right: String, why: String) = Mistake(wrong, right, why)

fun guide(hook: String, goals: List<String>, concepts: List<Concept>, mistakes: List<Mistake>, tip: String) =
    Guide(hook, goals, concepts, mistakes, tip)

/** Removes the [highlight] markers from an example sentence. */
fun String.plain(): String = replace("[", "").replace("]", "")

fun w(en: String, ar: String, example: String) = Word(en, ar, example)

infix fun String.means(ar: String) = Example(this, ar)

class LevelBuilder(private val level: CefrLevel) {
    private val units = mutableListOf<CourseUnit>()

    fun unit(title: String, titleAr: String, block: UnitBuilder.() -> Unit) {
        val index = units.size + 1
        val builder = UnitBuilder(level, "${level.code.lowercase()}-u$index").apply(block)
        units += CourseUnit(builder.id, title, titleAr, builder.lessons)
    }

    fun build(exam: List<Question>) = LevelCourse(level, units, exam)
}

class UnitBuilder(private val level: CefrLevel, val id: String) {
    val lessons = mutableListOf<Lesson>()
    private fun nextId() = "$id-l${lessons.size + 1}"

    fun grammar(
        title: String,
        titleAr: String,
        notes: List<String>,
        examples: List<Example>,
        questions: List<Question>,
    ) {
        val speaking = examples.firstOrNull()?.let { listOf(Question.Speak(it.en.substringAfter("→ ").plain(), it.ar)) } ?: emptyList()
        lessons += Lesson(nextId(), level, title, titleAr, LessonType.GRAMMAR, notes, examples, questions = questions + speaking)
    }

    fun vocabulary(title: String, titleAr: String, words: List<Word>, extra: List<Question> = emptyList()) {
        lessons += Lesson(
            nextId(), level, title, titleAr, LessonType.VOCABULARY,
            notes = listOf("استمع لكل كلمة وكررها بصوت عالٍ، ثم اقرأ المثال. ستُضاف الكلمات إلى بطاقات المراجعة الذكية بعد إنهاء الدرس."),
            words = words,
            questions = vocabularyQuestions(words) + extra,
        )
    }

    fun reading(title: String, titleAr: String, passage: String, questions: List<Question>, words: List<Word> = emptyList()) {
        lessons += Lesson(
            nextId(), level, title, titleAr, LessonType.READING,
            notes = listOf("اقرأ النص كاملاً مرة لفهم الفكرة العامة، ثم اقرأه مرة ثانية بتركيز للإجابة عن الأسئلة."),
            words = words, passage = passage, questions = questions,
        )
    }

    fun listening(title: String, titleAr: String, script: String, questions: List<Question>, words: List<Word> = emptyList()) {
        lessons += Lesson(
            nextId(), level, title, titleAr, LessonType.LISTENING,
            notes = listOf("استمع إلى المقطع مرتين على الأقل دون قراءة. يمكنك إبطاء السرعة. سيظهر النص بعد إنهاء التمارين لتتدرب على النطق بطريقة التظليل (Shadowing)."),
            words = words, passage = script, questions = questions,
        )
    }
}

fun level(level: CefrLevel, exam: List<Question>, block: LevelBuilder.() -> Unit): LevelCourse =
    LevelBuilder(level).apply(block).build(exam)

/**
 * Builds a varied, deterministic exercise set from a word list: meaning,
 * listening, reverse meaning and spelling, plus one sentence-building item.
 */
internal fun vocabularyQuestions(words: List<Word>): List<Question> {
    fun others(i: Int, pick: (Word) -> String): List<String> =
        words.indices.filter { it != i }.map { pick(words[it]) }
            .let { list -> List(3) { k -> list[(i + k * 2) % list.size] } }
            .distinct()

    val generated = words.mapIndexed { i, word ->
        when (i % 4) {
            0 -> Question.Choice("ما معنى «${word.en}»؟", listOf(word.ar) + others(i) { it.ar })
            1 -> Question.Choice(
                "استمع واختر الكلمة الصحيحة", listOf(word.en) + others(i) { it.en }, audio = word.en,
            )
            2 -> Question.Choice("اختر الترجمة الإنجليزية لـ «${word.ar}»", listOf(word.en) + others(i) { it.en })
            else -> Question.Typing("اكتب بالإنجليزية: «${word.ar}»", listOf(word.en))
        }
    }
    val match = Question.Match(words.take(4).map { it.en to it.ar })
    val sentence = words.firstOrNull { it.example.split(" ").size in 4..9 }
    val order = sentence?.let { Question.Order(it.example.trimEnd('.', '!', '?'), "") }
    val spoken = words.lastOrNull()?.let { Question.Speak(it.example, "انطق الجملة: ${it.ar}") }
    return listOf(match) + generated + listOfNotNull(order, spoken)
}
