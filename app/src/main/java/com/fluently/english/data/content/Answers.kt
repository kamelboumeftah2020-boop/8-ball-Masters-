package com.fluently.english.data.content

/** Answer checking, kept free of Android so it can be unit-tested. */
object Answers {
    fun normalize(text: String): String = text
        .lowercase()
        .replace('’', '\'')
        .replace('‘', '\'')
        .trim()
        .trimEnd('.', '!', '?', ',')
        .replace(Regex("\\s+"), " ")

    fun checkTyping(question: Question.Typing, input: String): Boolean =
        question.answers.any { normalize(it) == normalize(input) }

    fun checkOrder(question: Question.Order, tokens: List<String>): Boolean =
        tokens.joinToString(" ") == question.sentence

    fun checkChoice(question: Question.Choice, selected: String?): Boolean = selected == question.answer

    private fun words(text: String): List<String> =
        normalize(text).replace(Regex("[^a-z0-9' ]"), " ").split(" ").filter { it.isNotBlank() }

    /** Share of the target's words found in what the recogniser heard (0..1). */
    fun speechScore(target: String, heard: String): Float {
        val expected = words(target)
        if (expected.isEmpty()) return 0f
        val pool = words(heard).toMutableList()
        val hits = expected.count { pool.remove(it) }
        return hits.toFloat() / expected.size
    }

    const val SPEECH_PASS = 0.7f

    /** The correct answer as text, for feedback and mistake review. */
    fun correctAnswer(question: Question): String = when (question) {
        is Question.Choice -> question.answer
        is Question.Order -> question.sentence
        is Question.Typing -> question.answers.first()
        is Question.Match -> question.pairs.joinToString("  ·  ") { "${it.first} = ${it.second}" }
        is Question.Speak -> question.sentence
    }

    fun prompt(question: Question): String = when (question) {
        is Question.Choice -> question.prompt
        is Question.Order -> question.translation.ifEmpty { "رتّب الكلمات" }
        is Question.Typing -> question.prompt
        is Question.Match -> "صِل كل كلمة بمعناها"
        is Question.Speak -> question.sentence
    }
}
