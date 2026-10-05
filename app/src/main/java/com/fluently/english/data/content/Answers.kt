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

    /** The correct answer as text, for feedback and mistake review. */
    fun correctAnswer(question: Question): String = when (question) {
        is Question.Choice -> question.answer
        is Question.Order -> question.sentence
        is Question.Typing -> question.answers.first()
    }

    fun prompt(question: Question): String = when (question) {
        is Question.Choice -> question.prompt
        is Question.Order -> question.translation.ifEmpty { "رتّب الكلمات" }
        is Question.Typing -> question.prompt
    }
}
