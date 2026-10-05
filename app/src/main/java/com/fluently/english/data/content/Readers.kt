package com.fluently.english.data.content

/**
 * Graded readers: short stories written for each CEFR level (in the spirit of
 * Oxford Bookworms / Cambridge English Readers) for extensive reading.
 */
data class GradedReader(
    val id: String,
    val level: CefrLevel,
    val title: String,
    val titleAr: String,
    /** Arabic genre label, e.g. "مغامرة", "غموض", "خيال علمي". */
    val genreAr: String,
    val summaryAr: String,
    val chapters: List<ReaderChapter>,
) {
    val wordCount: Int get() = chapters.sumOf { it.wordCount }
}

data class ReaderChapter(
    val title: String,
    /** Paragraphs separated by blank lines. Glossary terms occur verbatim in the text. */
    val text: String,
    /** Harder words/phrases of the chapter → Arabic meaning (tap to see). */
    val glossary: Map<String, String>,
    /** 2–3 comprehension questions; the correct option first (shuffled on screen). */
    val questions: List<Question.Choice>,
) {
    val wordCount: Int get() = text.split(Regex("\\s+")).count { it.any(Char::isLetter) }
}

/** All readers, easiest first. Stories live in ReaderStories.kt. */
val Readers: List<GradedReader> by lazy { ReaderStories.sortedBy { it.level.ordinal } }
