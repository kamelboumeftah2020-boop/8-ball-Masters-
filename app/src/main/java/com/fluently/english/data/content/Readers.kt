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

private fun ReaderChapter.sentences(): List<String> =
    text.split(Regex("(?<=[.!?])\\s+")).map { it.trim() }.filter { it.isNotEmpty() }

/** Sentences suitable for ordering and speaking: 5–10 words, no dialogue quotes. */
private fun ReaderChapter.practiceSentences(): List<String> =
    sentences().filter { s ->
        val n = s.split(" ").size
        n in 5..10 && s.none { it in "“”\"‘:;()" } && s.first().isUpperCase()
    }

/**
 * Extra practice after a chapter, built from its own text: match the chapter's
 * words, rebuild one of its sentences and say another aloud.
 */
fun ReaderChapter.activities(): List<Question> {
    val out = mutableListOf<Question>()
    val pairs = glossary.entries.take(4).map { it.key to it.value }
    if (pairs.size >= 3) out += Question.Match(pairs, explanation = "كلمات هذا الفصل — ستجدها في بطاقات المراجعة إن أضفتها.")
    val candidates = practiceSentences()
    if (candidates.isNotEmpty()) {
        val pick = candidates[Math.floorMod(title.hashCode(), candidates.size)]
        out += Question.Order(pick, "رتّب الجملة كما وردت في الفصل", explanation = pick)
        val other = candidates.filter { it != pick }.maxByOrNull { it.length } ?: pick
        out += Question.Speak(other, "اقرأ هذه الجملة من الفصل بصوت عالٍ")
    }
    return out
}
