package com.fluently.english.data.content

object Course {
    val levels: List<LevelCourse> = listOf(LevelA1, LevelA2, LevelB1, LevelB2, LevelC1, LevelC2)

    private val lessonsById: Map<String, Lesson> = levels.flatMap { it.lessons }.associateBy { it.id }

    val wordsByEn: Map<String, Word> = levels.flatMap { it.words }.associateBy { it.en }

    fun level(level: CefrLevel): LevelCourse = levels.first { it.level == level }

    fun lesson(id: String): Lesson? = lessonsById[id]

    /**
     * Exam = the level's hand-written items + a sample of lesson items, so every
     * attempt covers the whole level and differs slightly from the last.
     */
    fun examQuestions(level: CefrLevel, seed: Long): List<Question> {
        val course = level(level)
        val random = kotlin.random.Random(seed)
        val pool = course.lessons
            .filter { it.type == LessonType.GRAMMAR || it.type == LessonType.VOCABULARY }
            .flatMap { it.questions }
            .filter { it is Question.Choice || it is Question.Order || it is Question.Typing }
        return (course.examQuestions + pool.shuffled(random).take(EXAM_SAMPLE_SIZE)).shuffled(random)
    }

    const val EXAM_SAMPLE_SIZE = 10
    const val LESSON_PASS_PERCENT = 60
    const val EXAM_PASS_PERCENT = 70
}

/** Methods the course is built on, shown in the app's "methodology" page. */
val LearningMethods: List<Pair<String, String>> = listOf(
    "إطار CEFR الأوروبي" to "المنهج مقسم إلى ستة مستويات A1 حتى C2 وفق الإطار الأوروبي المرجعي المشترك للغات — المعيار الذي تعتمده جامعة كامبريدج وأكسفورد والمجلس الثقافي البريطاني وIELTS وTOEFL.",
    "اختبار تحديد مستوى تكيّفي" to "مصمم على طريقة Oxford Placement Test وCambridge English Placement Test وEF SET: أقسام للقواعد والمفردات والقراءة والاستماع، وتتغير صعوبة الأسئلة حسب إجاباتك.",
    "التكرار المتباعد (Spaced Repetition)" to "تراجع الكلمات في فترات تتزايد تدريجياً (نظام صناديق لايتنر) قبل أن تنساها مباشرة، وهي أقوى طريقة مثبتة علمياً لحفظ المفردات على المدى الطويل.",
    "الاستدعاء النشط (Active Recall)" to "كل درس ينتهي بتمارين تجبرك على استرجاع المعلومة لا مجرد قراءتها: اختيار، ترتيب جمل، كتابة واستماع.",
    "المدخلات المفهومة (Comprehensible Input)" to "نصوص قراءة واستماع بمستوى أعلى قليلاً من مستواك الحالي، وفق نظرية ستيفن كراشن في اكتساب اللغة.",
    "التظليل (Shadowing)" to "بعد تمارين الاستماع يظهر النص لتعيد الجمل مع الصوت مباشرة، فتتحسن طلاقتك ونطقك وإيقاع كلامك.",
    "المنهج التواصلي (CLT)" to "المحتوى مبني على مواقف حقيقية: التعارف، السفر، العمل، المقابلات، التفاوض والخطابة — مثل مناهج Headway وEnglish File وCambridge Empower.",
    "مصادر موصى بها للتوسع" to "British Council LearnEnglish، BBC Learning English، Cambridge English Write & Improve، قوائم Oxford 3000/5000، English Grammar in Use (Raymond Murphy)، وبودكاست 6 Minute English.",
)

/** Rotating tips on the home screen. */
val DailyTips: List<String> = listOf(
    "عشرون دقيقة يومياً أفضل من ساعتين مرة في الأسبوع. الاستمرارية هي السر.",
    "كرر الجمل بصوت عالٍ بعد سماعها — دماغك يتعلم النطق بالتقليد.",
    "تعلم الكلمات داخل جمل كاملة لا منفردة، فهكذا تتذكرها وتعرف كيف تستخدمها.",
    "لا تخف من الأخطاء؛ كل خطأ في التمارين فرصة ليتذكره دماغك بشكل أقوى.",
    "غيّر لغة هاتفك إلى الإنجليزية لتتعرض للغة كل يوم دون مجهود.",
    "شاهد مقاطع قصيرة بالإنجليزية مع ترجمة إنجليزية لا عربية.",
    "راجع بطاقاتك قبل النوم؛ النوم يثبت الذكريات الجديدة.",
    "اكتب ثلاث جمل يومياً عن يومك بالإنجليزية لتنشيط مهارة الكتابة.",
)
