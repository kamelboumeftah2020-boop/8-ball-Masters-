package com.fluently.english.data.content

/*
 * Minimal-pair drills targeting the English sounds Arabic speakers find hardest.
 * The learner hears one word of a pair and decides which one it was, training
 * the ear before the mouth.
 */

data class SoundLesson(
    val id: String,
    val title: String,
    val titleAr: String,
    /** How to produce the sound, in Arabic. */
    val howAr: String,
    val tipAr: String,
    val pairs: List<Pair<String, String>>,
)

val SoundLessons: List<SoundLesson> = listOf(
    SoundLesson(
        "p-b", "/p/ vs /b/", "الفرق بين p و b",
        "لا يوجد حرف p في العربية، لذلك ننطقه غالباً b. السر: الحرفان يخرجان من الشفتين، لكن p «انفجار هواء» بدون اهتزاز الحنجرة، وb مع اهتزاز.",
        "ضع ورقة أمام فمك: مع p يجب أن تتحرك الورقة بقوة، ومع b لا تتحرك تقريباً.",
        listOf("park" to "bark", "pen" to "Ben", "pie" to "buy", "pear" to "bear", "pull" to "bull", "cap" to "cab", "pig" to "big", "pack" to "back"),
    ),
    SoundLesson(
        "v-f", "/v/ vs /f/", "الفرق بين v و f",
        "v غير موجود في العربية. الحرفان يخرجان بوضع الأسنان العليا على الشفة السفلى. f بدون صوت من الحنجرة (مثل الفاء)، وv مع اهتزاز الحنجرة.",
        "ضع إصبعك على حنجرتك: مع v يجب أن تشعر بالاهتزاز، ومع f لا.",
        listOf("very" to "ferry", "van" to "fan", "vine" to "fine", "view" to "few", "vast" to "fast", "veil" to "fail", "leave" to "leaf", "save" to "safe"),
    ),
    SoundLesson(
        "i-ee", "/ɪ/ vs /iː/", "الكسرة القصيرة والطويلة",
        "في ship الصوت قصير ومرتخٍ مثل كسرة سريعة، وفي sheep الصوت طويل مع شفتين مشدودتين كأنك تبتسم.",
        "مع الصوت الطويل ابتسم وأطِل الصوت: «شييييب». مع القصير لا تبتسم.",
        listOf("ship" to "sheep", "live" to "leave", "fill" to "feel", "sit" to "seat", "hit" to "heat", "bit" to "beat", "still" to "steal", "rich" to "reach"),
    ),
    SoundLesson(
        "e-i", "/e/ vs /ɪ/", "الفرق بين e و i القصيرة",
        "كثير من العرب ينطقون pen مثل pin. في /e/ الفم مفتوح أكثر قليلاً (مثل «إيـ» مفتوحة)، وفي /ɪ/ الفم شبه مغلق.",
        "افتح فمك بعرض إصبع واحد مع /e/ في bed.",
        listOf("pen" to "pin", "bed" to "bid", "set" to "sit", "ten" to "tin", "bell" to "bill", "left" to "lift", "check" to "chick", "desk" to "disk"),
    ),
    SoundLesson(
        "th-s", "/θ/ vs /s/ and /t/", "صوت th",
        "صوت th في think مثل «ث» العربية تماماً! ضع طرف لسانك بين أسنانك وأخرج الهواء. لا تنطقه s أو t.",
        "أنت محظوظ: العربية فيها «ث» و«ذ». think = «ثِنك»، this = «ذِس».",
        listOf("think" to "sink", "thick" to "sick", "three" to "tree", "thank" to "tank", "path" to "pass", "mouth" to "mouse", "thin" to "tin", "faith" to "face"),
    ),
    SoundLesson(
        "ch-sh", "/tʃ/ vs /ʃ/", "الفرق بين ch و sh",
        "sh مثل «ش» العربية. أما ch فهي «تش» — تبدأ بإغلاق اللسان على سقف الفم ثم «انفجار» إلى ش.",
        "ch = «ت» + «ش» بسرعة: chair = «تشير».",
        listOf("chair" to "share", "cheap" to "sheep", "chip" to "ship", "watch" to "wash", "match" to "mash", "chop" to "shop", "catch" to "cash", "cheese" to "she's"),
    ),
    SoundLesson(
        "short-a", "/æ/ vs /ʌ/", "الفتحة المفتوحة والضمة القصيرة",
        "في cat الفم مفتوح جداً للأسفل مع شد الشفتين للجانبين، وفي cut الصوت قصير ومحايد مثل «أَ» سريعة في وسط الفم.",
        "مع /æ/ تخيل أنك تقول «إيه» بفم مفتوح كبير.",
        listOf("cat" to "cut", "bag" to "bug", "hat" to "hut", "match" to "much", "ran" to "run", "cap" to "cup", "bad" to "bud", "fan" to "fun"),
    ),
)
