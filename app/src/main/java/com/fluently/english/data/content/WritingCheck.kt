package com.fluently.english.data.content

/*
 * Free, offline writing feedback. It cannot judge ideas like an examiner, but it
 * catches what learners can fix right away: length, paragraphing, linking words,
 * repetition, capitals and punctuation, and the grammar slips most common among
 * Arabic speakers (he go, a apple, informations, discuss about…).
 */

enum class IssueKind(val labelAr: String) {
    LENGTH("الطول"),
    STRUCTURE("التنظيم"),
    LINKING("أدوات الربط"),
    VOCABULARY("المفردات"),
    GRAMMAR("القواعد"),
    MECHANICS("الإملاء وعلامات الترقيم"),
    STYLE("الأسلوب"),
}

/** One finding. [excerpt] is the text that triggered it (for highlighting), if any. */
data class WritingIssue(val kind: IssueKind, val messageAr: String, val excerpt: String? = null, val fix: String? = null)

data class WritingReport(
    val words: Int,
    val sentences: Int,
    val paragraphs: Int,
    val avgSentenceLength: Int,
    val uniqueRatio: Int,
    val linkers: List<String>,
    val issues: List<WritingIssue>,
    val strengths: List<String>,
    /** Suggested rating 0..3 for each examiner criterion (task, coherence, vocabulary, grammar). */
    val suggested: List<Int>,
) {
    val errorCount: Int get() = issues.count { it.kind == IssueKind.GRAMMAR || it.kind == IssueKind.MECHANICS }
}

object WritingCheck {

    val Linkers = listOf(
        "however", "moreover", "furthermore", "in addition", "therefore", "consequently", "as a result",
        "on the other hand", "in contrast", "although", "even though", "whereas", "while", "despite",
        "for example", "for instance", "such as", "in conclusion", "to sum up", "overall", "firstly",
        "secondly", "finally", "because", "since", "so that", "in my opinion", "in my view", "nevertheless",
        "first of all", "also", "besides", "then", "after that", "eventually", "unless", "otherwise",
    )

    private val stopWords = setOf(
        "the", "a", "an", "and", "or", "but", "to", "of", "in", "on", "at", "for", "with", "is", "are", "was",
        "were", "be", "been", "it", "this", "that", "i", "you", "he", "she", "we", "they", "my", "your", "his",
        "her", "our", "their", "me", "him", "us", "them", "as", "by", "from", "not", "do", "does", "did", "have",
        "has", "had", "will", "would", "can", "could", "should", "so", "if", "there", "which", "who", "what",
        "more", "most", "very", "also", "than", "then", "its", "into", "about", "all", "some", "many", "much",
    )

    /** Simple alternatives for words learners overuse. */
    val Synonyms = mapOf(
        "good" to "beneficial, positive, valuable",
        "bad" to "harmful, negative, damaging",
        "big" to "large, significant, considerable",
        "important" to "essential, crucial, vital",
        "people" to "individuals, citizens, the public",
        "thing" to "aspect, factor, issue",
        "things" to "aspects, factors, issues",
        "very" to "extremely, highly, remarkably",
        "get" to "obtain, receive, gain",
        "make" to "create, produce, cause",
        "show" to "demonstrate, reveal, indicate",
        "think" to "believe, consider, argue",
        "nice" to "pleasant, enjoyable, lovely",
        "help" to "assist, support, benefit",
        "problem" to "issue, challenge, difficulty",
        "lot" to "a great deal, numerous, plenty",
    )

    private data class Rule(val regex: Regex, val messageAr: String, val fix: String)

    private fun rule(pattern: String, messageAr: String, fix: String) =
        Rule(Regex(pattern, RegexOption.IGNORE_CASE), messageAr, fix)

    /** Common grammar slips (especially for Arabic speakers). */
    private val grammarRules = listOf(
        rule("(?<!(does|did|can|will|would|should|could|must|may|might|let|make|help|why|to)\\s)\\b(he|she|it)\\s+(go|have|do|want|like|need|make|play|work|live|study|think|say|come|get|know)\\b",
            "مع he / she / it نضيف s للفعل في المضارع البسيط.", "he goes / she has / it does"),
        rule("\\b(he|she|it)\\s+don'?t\\b", "مع he / she / it نستخدم doesn't وليس don't.", "doesn't"),
        rule("\\b(i|you|we|they)\\s+(goes|has|does|wants|likes|needs|makes|works|lives)\\b", "مع I / you / we / they لا نضيف s للفعل.", "I go / they have"),
        rule("\\b(i|you|we|they)\\s+doesn'?t\\b", "مع I / you / we / they نستخدم don't.", "don't"),
        rule("\\b(am|is|are)\\s+agree\\b", "agree فعل، فلا نضع قبله am / is / are.", "I agree"),
        rule("\\bdiscuss\\s+about\\b", "discuss لا تحتاج about بعدها.", "discuss the problem"),
        rule("\\bexplain\\s+me\\b", "explain تحتاج to قبل الشخص.", "explain to me"),
        rule("\\bdepends?\\s+of\\b", "نقول depend on وليس depend of.", "depend on"),
        rule("\\bin the other hand\\b", "التعبير الصحيح on the other hand.", "on the other hand"),
        rule("\\baccording of\\b", "نقول according to.", "according to"),
        rule("\\bmarried with\\b", "نقول married to.", "married to"),
        rule("\\bmore (better|worse|bigger|smaller|easier|faster|cheaper)\\b", "لا نجمع more مع صيغة المقارنة -er.", "better / bigger"),
        rule("\\bmost (best|worst|biggest|easiest)\\b", "لا نجمع most مع صيغة التفضيل -est.", "the best"),
        rule("\\b(informations|advices|furnitures|equipments|homeworks|knowledges|researches|evidences)\\b",
            "هذه الكلمة غير معدودة ولا تُجمع بـ s.", "information / advice"),
        rule("\\b(a)\\s+(?!(one|once|uni|use|usu|eu|euro|ewe|u\\b))([aeio]\\w+)", "نستخدم an قبل الكلمة التي تبدأ بصوت متحرك.", "an apple / an idea"),
        rule("\\ban\\s+(?!(hour|honest|honou?r|heir|herb)\\w*)([bcdfgjklmnpqrstvwxyz]\\w+)", "نستخدم a قبل الكلمة التي تبدأ بصوت ساكن.", "a book / a car"),
        rule("\\b(can|could|should|must|will|would|may|might)\\s+to\\s+\\w+", "بعد الأفعال المساعدة (can, must…) يأتي الفعل بدون to.", "can go / must study"),
        rule("\\b(can|could|should|must|will|would|may|might)\\s+(goes|went|has|does|wants|studied|played)\\b", "بعد can / must / will… يأتي الفعل في صيغته الأصلية.", "can go / will have"),
        rule("\\bdid(n'?t)?\\s+(went|saw|ate|came|took|made|got|bought|had)\\b", "بعد did / didn't يأتي الفعل في صيغته الأصلية.", "didn't go"),
        rule("\\b(people|children|police)\\s+(is|was|has)\\b", "people / children جمع، فنستخدم معها are / were / have.", "people are / people were"),
        rule("\\bpeoples\\b", "people جمع أصلاً (مفردها person).", "people"),
        rule("\\bsince\\s+\\d+\\s+(years|months|days|weeks)\\b", "مع المدة نستخدم for، ومع نقطة البداية since.", "for 3 years / since 2020"),
        rule("\\bthe (life|society|nature) is\\b", "مع الأسماء العامة المجردة لا نستخدم the غالباً.", "Life is / Society is"),
        rule("\\b(?!(?:that|had)\\b)(\\w+)\\s+\\1\\b", "كلمة مكررة مرتين متتاليتين.", "احذف التكرار"),
    )

    private val contraction = Regex("\\b\\w+'(t|re|ve|ll|d|m)\\b", RegexOption.IGNORE_CASE)

    fun words(text: String): List<String> =
        Regex("[A-Za-z']+").findAll(text).map { it.value.lowercase().trim('\'') }.filter { it.isNotEmpty() }.toList()

    private fun sentencesOf(text: String): List<String> =
        text.split(Regex("(?<=[.!?])\\s+|\\n+")).map { it.trim() }.filter { it.any(Char::isLetter) }

    /**
     * Checks [text]. [minWords] is the task's minimum (0 = none); [formal] warns
     * about contractions in essays and reports.
     */
    fun check(text: String, minWords: Int = 0, formal: Boolean = false): WritingReport {
        val ws = words(text)
        val sentences = sentencesOf(text)
        val paragraphs = text.split(Regex("\\n\\s*\\n")).count { it.any(Char::isLetter) }
        val issues = mutableListOf<WritingIssue>()
        val strengths = mutableListOf<String>()
        val lower = " " + text.lowercase().replace('\n', ' ') + " "

        // ---- Length ----
        if (minWords > 0) {
            when {
                ws.size < minWords * 0.6 -> issues += WritingIssue(IssueKind.LENGTH, "النص قصير جداً: ${ws.size} كلمة من ${minWords} مطلوبة. الإجابة القصيرة تفقد درجات كثيرة.")
                ws.size < minWords -> issues += WritingIssue(IssueKind.LENGTH, "ينقصك ${minWords - ws.size} كلمة للوصول إلى الحد الأدنى (${minWords}).")
                ws.size > minWords * 1.8 -> issues += WritingIssue(IssueKind.LENGTH, "النص أطول بكثير من المطلوب؛ الأطول ليس أفضل دائماً، ركّز على الجودة.")
                else -> strengths += "طول مناسب للمهمة (${ws.size} كلمة)."
            }
        }

        // ---- Structure ----
        val avg = if (sentences.isEmpty()) 0 else ws.size / sentences.size
        if (ws.size >= 80 && paragraphs < 2) {
            issues += WritingIssue(IssueKind.STRUCTURE, "اكتب النص في فقرات (اترك سطراً فارغاً بينها): مقدمة، فقرات للأفكار، وخاتمة.")
        } else if (paragraphs >= 3) {
            strengths += "مقسّم إلى $paragraphs فقرات — تنظيم واضح."
        }
        if (avg > 28) issues += WritingIssue(IssueKind.STRUCTURE, "جملك طويلة جداً (متوسط $avg كلمة). قسّم الجمل الطويلة لتصبح أوضح.")
        if (sentences.size >= 4 && avg in 1..6) issues += WritingIssue(IssueKind.STRUCTURE, "جملك قصيرة جداً (متوسط $avg كلمات). اربط بعضها بـ and / because / which.")

        // ---- Linking words ----
        val linkers = Linkers.filter { Regex("\\b" + Regex.escape(it) + "\\b").containsMatchIn(lower) }
        when {
            ws.size >= 60 && linkers.size < 2 -> issues += WritingIssue(
                IssueKind.LINKING, "استخدم أدوات ربط أكثر لتسلسل الأفكار، مثل: However, In addition, For example, Therefore, In conclusion.",
            )
            linkers.size >= 4 -> strengths += "استخدام جيد لأدوات الربط (${linkers.size})."
        }

        // ---- Vocabulary ----
        val content = ws.filter { it.length > 2 && it !in stopWords }
        val unique = if (ws.isEmpty()) 0 else ws.toSet().size * 100 / ws.size
        content.groupingBy { it }.eachCount().filter { (w, n) -> n >= (if (ws.size > 150) 4 else 3) }
            .entries.sortedByDescending { it.value }.take(3).forEach { (w, n) ->
                val alt = Synonyms[w]
                issues += WritingIssue(
                    IssueKind.VOCABULARY,
                    "كررت كلمة «$w» $n مرات." + (alt?.let { " جرّب بدائل مثل: $it." } ?: " حاول التنويع."),
                    excerpt = w,
                )
            }
        if (ws.size >= 60 && unique >= 60) strengths += "مفردات متنوعة."

        // ---- Grammar ----
        grammarRules.forEach { r ->
            r.regex.findAll(text).take(2).forEach { m ->
                issues += WritingIssue(IssueKind.GRAMMAR, r.messageAr, excerpt = m.value, fix = r.fix)
            }
        }

        // ---- Mechanics ----
        sentences.filter { it.first().isLowerCase() }.take(2).forEach {
            issues += WritingIssue(IssueKind.MECHANICS, "ابدأ الجملة بحرف كبير.", excerpt = it.take(24))
        }
        Regex("(^|\\s)i(\\s|'|,|\\.)").findAll(text).take(1).forEach {
            issues += WritingIssue(IssueKind.MECHANICS, "الضمير I يُكتب دائماً بحرف كبير.", excerpt = it.value.trim(), fix = "I")
        }
        Regex("\\s+[,.!?;:]").findAll(text).take(1).forEach {
            issues += WritingIssue(IssueKind.MECHANICS, "لا تضع مسافة قبل علامة الترقيم.", excerpt = it.value)
        }
        Regex("[,;][A-Za-z]|\\.[A-Z][a-z]").findAll(text).take(1).forEach {
            issues += WritingIssue(IssueKind.MECHANICS, "ضع مسافة بعد الفاصلة والنقطة.", excerpt = it.value)
        }
        if (text.trim().isNotEmpty() && text.trim().last() !in ".!?\"”") {
            issues += WritingIssue(IssueKind.MECHANICS, "أنهِ النص بعلامة ترقيم (نقطة).")
        }

        // ---- Style ----
        if (formal) {
            val c = contraction.findAll(text).map { it.value }.distinct().take(3).toList()
            if (c.isNotEmpty()) {
                issues += WritingIssue(IssueKind.STYLE, "في المقال الرسمي تجنّب الاختصارات: ${c.joinToString("، ")}.", excerpt = c.first(), fix = "do not / cannot / it is")
            }
        }

        val grammarErrors = issues.count { it.kind == IssueKind.GRAMMAR || it.kind == IssueKind.MECHANICS }
        val per100 = if (ws.isEmpty()) 10.0 else grammarErrors * 100.0 / ws.size
        val task = when {
            ws.isEmpty() -> 0
            minWords > 0 && ws.size < minWords * 0.6 -> 0
            minWords > 0 && ws.size < minWords -> 1
            paragraphs >= 3 || minWords == 0 -> 3
            else -> 2
        }
        val coherence = when {
            linkers.size >= 4 && paragraphs >= 3 -> 3
            linkers.size >= 2 && paragraphs >= 2 -> 2
            linkers.isNotEmpty() -> 1
            else -> 0
        }
        val repeated = issues.count { it.kind == IssueKind.VOCABULARY }
        val vocab = when {
            unique >= 60 && repeated == 0 -> 3
            unique >= 50 && repeated <= 1 -> 2
            unique >= 40 -> 1
            else -> 0
        }
        val grammar = when {
            per100 < 1 -> 3
            per100 < 3 -> 2
            per100 < 6 -> 1
            else -> 0
        }
        if (ws.size >= 40 && grammarErrors == 0) strengths += "لم نجد أخطاء شائعة في القواعد أو الإملاء — أحسنت!"

        return WritingReport(
            words = ws.size, sentences = sentences.size, paragraphs = paragraphs, avgSentenceLength = avg,
            uniqueRatio = unique, linkers = linkers, issues = issues, strengths = strengths,
            suggested = listOf(task, coherence, vocab, grammar),
        )
    }
}
