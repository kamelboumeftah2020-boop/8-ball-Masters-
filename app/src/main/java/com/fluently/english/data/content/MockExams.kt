package com.fluently.english.data.content

/*
 * Practice tests built in the formats of the major international exams (IELTS
 * Academic, Cambridge B1 Preliminary, Cambridge B2 First): the same sections,
 * task types and instructions, with original content. Listening, reading and
 * use of English are scored automatically; writing and speaking come with a
 * model answer and a self-assessment against the official criteria.
 */

enum class MockKind { IELTS, CAMBRIDGE_B1, CAMBRIDGE_B2 }

enum class SectionType(val labelAr: String) {
    LISTENING("الاستماع"),
    READING("القراءة"),
    USE_OF_ENGLISH("استخدام اللغة"),
    WRITING("الكتابة"),
    SPEAKING("المحادثة"),
}

/** An automatically scored part: optional shared text plus questions. */
data class MockPart(
    val title: String,
    val instructionsAr: String,
    val questions: List<Question>,
    /** Passage (reading), gapped text (use of English) or script (listening, hidden). */
    val text: String? = null,
)

data class WritingTask(
    val prompt: String,
    val promptAr: String,
    val minWords: Int,
    val modelAnswer: String,
    val tipsAr: List<String>,
)

data class SpeakingPart(
    val title: String,
    val instructionsAr: String,
    val questions: List<String>,
    val cueCard: List<String> = emptyList(),
    val prepSeconds: Int = 0,
    val talkSeconds: Int = 0,
    val sample: String,
)

data class MockSection(
    val type: SectionType,
    val title: String,
    val minutes: Int,
    val instructionsAr: String,
    val parts: List<MockPart> = emptyList(),
    val writing: WritingTask? = null,
    val speaking: List<SpeakingPart> = emptyList(),
) {
    val autoScored: Boolean get() = parts.isNotEmpty()
    val questionCount: Int get() = parts.sumOf { it.questions.size }
}

data class MockExam(
    val id: String,
    val kind: MockKind,
    val title: String,
    val titleAr: String,
    val level: CefrLevel,
    val descriptionAr: String,
    val realFormatAr: String,
    val sections: List<MockSection>,
)

/** The self-assessment criteria used by examiners. */
val WritingCriteria = listOf(
    "تحقيق المهمة (Task Response)" to "هل أجبت عن كل أجزاء السؤال ودعمت رأيك بأمثلة؟",
    "الترابط والتماسك (Coherence)" to "هل الفقرات منظمة ومترابطة بأدوات ربط مناسبة؟",
    "ثراء المفردات (Lexical Resource)" to "هل استخدمت مفردات متنوعة ودقيقة؟",
    "القواعد والدقة (Grammar)" to "هل استخدمت تراكيب متنوعة بأخطاء قليلة؟",
)

val SpeakingCriteria = listOf(
    "الطلاقة (Fluency)" to "هل تحدثت بسلاسة دون توقف طويل؟",
    "المفردات (Vocabulary)" to "هل استخدمت كلمات متنوعة ومناسبة؟",
    "القواعد (Grammar)" to "هل كانت جملك صحيحة ومتنوعة؟",
    "النطق (Pronunciation)" to "هل كان نطقك واضحاً ومفهوماً؟",
)

internal val TFNG = arrayOf("TRUE", "FALSE", "NOT GIVEN")

internal fun tfng(statement: String, answer: String, explain: String) =
    Question.Choice(statement, listOf(answer) + TFNG.filter { it != answer }, explanation = explain)

internal fun gap(n: Int, vararg options: String, explain: String? = null) =
    Question.Choice("الفراغ ($n): اختر الكلمة المناسبة", options.toList(), explanation = explain)

internal fun openGap(n: Int, vararg answers: String, explain: String? = null) =
    Question.Typing("الفراغ ($n): اكتب كلمة واحدة فقط", answers.toList(), explanation = explain)

internal fun audioQ(audio: String, prompt: String, vararg options: String) =
    Question.Choice(prompt, options.toList(), audio = audio)

/** All mock exams: the first set below plus the second set in MockExams2.kt. */
val MockExams: List<MockExam> by lazy { MockExamsSet1 + MockExamsSet2 }

private val MockExamsSet1: List<MockExam> = listOf(

    // ======================== IELTS ACADEMIC ========================
    MockExam(
        id = "mock-ielts-1",
        kind = MockKind.IELTS,
        title = "IELTS Academic — Practice Test",
        titleAr = "اختبار IELTS الأكاديمي التجريبي",
        level = CefrLevel.B2,
        descriptionAr = "اختبار تجريبي بنفس أقسام IELTS الأكاديمي وأنواع أسئلته: الاستماع، القراءة (بما فيها TRUE / FALSE / NOT GIVEN)، الكتابة Task 2 والمحادثة بأجزائها الثلاثة. النتيجة تقدير لدرجة Band من 9.",
        realFormatAr = "الاختبار الحقيقي: استماع 30 دقيقة (40 سؤالاً)، قراءة 60 دقيقة (40 سؤالاً)، كتابة 60 دقيقة (مهمتان)، محادثة 11–14 دقيقة. هذه نسخة مختصرة بنفس الأنماط.",
        sections = listOf(
            MockSection(
                SectionType.LISTENING, "Listening", 12,
                "ستسمع تسجيلين. في الاختبار الحقيقي تسمع كل تسجيل مرة واحدة فقط — حاول أن تلتزم بذلك.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Form completion",
                        "استمع إلى مكالمة هاتفية واكتب الإجابات. اكتب كلمة واحدة و/أو رقماً لكل إجابة (NO MORE THAN ONE WORD AND/OR A NUMBER).",
                        text = "Receptionist: Good morning, Riverside Community Centre. How can I help? Caller: Hi, I'd like to join the evening photography course. Receptionist: Certainly. Can I have your surname, please? Caller: Yes, it's Haddad. H-A-D-D-A-D. Receptionist: Thank you. And a contact number? Caller: It's 0 7 9 4, 3 6 1, 5 8 2. Receptionist: Great. The course runs on Wednesdays from seven to nine. Caller: Perfect. How much does it cost? Receptionist: It's ninety-five pounds for ten weeks. That includes all materials except a camera — you'll need to bring your own. Caller: No problem. Where exactly is the class? Receptionist: In Room 12, on the second floor.",
                        questions = listOf(
                            Question.Typing("Surname: ________", listOf("Haddad"), explanation = "تهجئة الاسم: H-A-D-D-A-D."),
                            Question.Typing("Phone number: ________", listOf("0794361582", "0794 361 582", "07943 61582"), explanation = "0794 361 582"),
                            Question.Typing("Day of the course: ________", listOf("Wednesday", "Wednesdays"), explanation = "The course runs on Wednesdays."),
                            Question.Typing("Cost for ten weeks: £________", listOf("95", "ninety-five", "ninety five"), explanation = "ninety-five pounds"),
                            Question.Typing("Students must bring their own ________", listOf("camera", "a camera"), explanation = "you'll need to bring your own camera"),
                        ),
                    ),
                    MockPart(
                        "Part 4 — Academic lecture",
                        "استمع إلى جزء من محاضرة واختر الإجابة الصحيحة (A, B أو C).",
                        text = "Today we're looking at coral reefs, which cover less than one percent of the ocean floor yet support around a quarter of all marine species. Corals are tiny animals that live in partnership with algae. The algae provide food through photosynthesis and give corals their bright colours. However, when water temperatures rise even slightly, corals expel the algae and turn white — a process called bleaching. Bleached corals are not dead, but if high temperatures continue for several weeks, they starve. The good news is that reefs can recover within ten to fifteen years if conditions improve. Some scientists are now breeding heat-resistant corals in laboratories, although critics argue that this treats the symptoms rather than the cause, which is climate change.",
                        questions = listOf(
                            Question.Choice("Coral reefs support approximately…", listOf("25% of marine species", "1% of marine species", "50% of marine species")),
                            Question.Choice("What do algae provide for corals?", listOf("Food and colour", "Protection from predators", "Cooler water")),
                            Question.Choice("Coral bleaching happens when…", listOf("water becomes too warm", "algae grow too fast", "corals die of old age")),
                            Question.Choice("Reefs can recover in…", listOf("10–15 years", "a few weeks", "over 50 years")),
                            Question.Choice("Critics of heat-resistant corals say that…", listOf("they don't address the real cause", "they are too expensive", "they harm fish")),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.READING, "Reading", 25,
                "اقرأ النصين وأجب عن الأسئلة. لا تعتمد على معلوماتك الخاصة — الإجابة يجب أن تكون من النص فقط.",
                parts = listOf(
                    MockPart(
                        "Passage 1 — The Rise of Urban Farming",
                        "Questions 1–5: هل العبارات تتفق مع النص؟ TRUE = تتفق، FALSE = تتعارض مع النص، NOT GIVEN = النص لا يذكر هذه المعلومة. Questions 6–8: اختر الإجابة الصحيحة.",
                        text = "Over the past two decades, urban farming has grown from a niche hobby into a global movement. In cities from Singapore to Detroit, rooftops, abandoned car parks and even underground tunnels are being transformed into productive gardens. Supporters argue that growing food close to where it is eaten reduces transport emissions and provides fresher produce. In Singapore, which imports more than 90 percent of its food, the government has set a target of producing 30 percent of the country's nutritional needs locally by 2030.\n\nVertical farms, in which crops are grown in stacked layers under artificial light, have attracted particular attention. These facilities can use up to 95 percent less water than conventional agriculture and are unaffected by weather. However, they consume large amounts of electricity, and critics point out that unless this energy comes from renewable sources, their environmental benefits may be limited. Furthermore, vertical farms currently focus on leafy greens and herbs; staple crops such as wheat and rice remain far too expensive to grow indoors.\n\nBeyond food production, community gardens appear to offer social benefits. Studies in several American cities have linked them to stronger neighbourhood relationships and improved mental health among participants. Nevertheless, most experts agree that urban farming will complement, rather than replace, traditional agriculture.",
                        questions = listOf(
                            tfng("Urban farming started as a small-scale activity.", "TRUE", "«grown from a niche hobby» = بدأ كهواية محدودة."),
                            tfng("Singapore currently produces 30 percent of its own food.", "FALSE", "30% هدف لعام 2030، والدولة تستورد أكثر من 90% حالياً."),
                            tfng("Vertical farms use less water than traditional farms.", "TRUE", "«up to 95 percent less water»."),
                            tfng("Most vertical farms are located in Asia.", "NOT GIVEN", "النص لا يذكر أين توجد معظم المزارع العمودية."),
                            tfng("Wheat is commonly grown in vertical farms today.", "FALSE", "«staple crops such as wheat … remain far too expensive to grow indoors»."),
                            Question.Choice("What is the main concern about vertical farms?", listOf("Their high electricity use", "Their poor-quality crops", "Their dependence on weather", "Their high water use")),
                            Question.Choice("According to the passage, community gardens…", listOf("may improve participants' mental health", "produce most food in US cities", "replace supermarkets", "are popular only in Singapore")),
                            Question.Choice("What is the experts' view of urban farming?", listOf("It will support, not replace, traditional farming.", "It will soon replace traditional farming.", "It has no real benefits.", "It should be banned in cities.")),
                        ),
                    ),
                    MockPart(
                        "Passage 2 — Why We Forget",
                        "Questions 9–11: أكمل الجمل بكلمة واحدة فقط من النص (ONE WORD ONLY). Questions 12–13: اختر الإجابة الصحيحة.",
                        text = "In the 1880s, the German psychologist Hermann Ebbinghaus conducted a series of experiments on himself, memorising lists of meaningless syllables and testing how much he could recall over time. His results produced what is now known as the 'forgetting curve': a sharp decline in memory within the first hour, followed by a slower loss over the following days. Without review, he found, we forget roughly half of new information within a day.\n\nEbbinghaus also discovered a remedy. Each time information was reviewed, the curve became flatter, meaning that memories lasted longer. This principle underlies 'spaced repetition', a technique in which material is reviewed at gradually increasing intervals. Modern research has confirmed that spacing study sessions is far more effective than 'cramming' — studying intensively just before a test.\n\nSleep also plays a crucial role. During deep sleep, the brain replays recent experiences and transfers them from short-term to long-term storage, a process called consolidation. Students who sleep after learning consistently outperform those who stay awake, even when total study time is identical.",
                        questions = listOf(
                            Question.Typing("Ebbinghaus tested his memory using lists of meaningless ________.", listOf("syllables"), explanation = "«memorising lists of meaningless syllables»"),
                            Question.Typing("Studying intensively just before a test is called ________.", listOf("cramming"), explanation = "«'cramming' — studying intensively just before a test»"),
                            Question.Typing("The transfer of memories to long-term storage during sleep is called ________.", listOf("consolidation"), explanation = "«a process called consolidation»"),
                            Question.Choice("According to Ebbinghaus, without review we forget about…", listOf("half of new information in a day", "all information in an hour", "10% of information in a week", "nothing for several days")),
                            Question.Choice("What is the main idea of the passage?", listOf("Review and sleep help us remember", "Ebbinghaus had a poor memory", "Cramming is the best strategy", "Memory cannot be improved")),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.WRITING, "Writing — Task 2", 40,
                "اكتب مقالاً لا يقل عن 250 كلمة. في الاختبار الحقيقي لديك 40 دقيقة لهذه المهمة.",
                writing = WritingTask(
                    prompt = "Some people think that children should start learning a foreign language at primary school. Others believe it is better to start at secondary school. Discuss both views and give your own opinion.",
                    promptAr = "يرى البعض أن الأطفال يجب أن يبدؤوا تعلم لغة أجنبية في المرحلة الابتدائية، بينما يرى آخرون أن الأفضل البدء في المرحلة الثانوية. ناقش الرأيين واذكر رأيك.",
                    minWords = 250,
                    modelAnswer = "The question of when children should begin learning a foreign language divides parents and educators. While some argue that secondary school is the ideal starting point, I believe that the benefits of an early start are considerably greater.\n\nThose who favour a later start make several reasonable points. Older children have a stronger grasp of their first language, which allows them to understand grammar explicitly. They are also more disciplined learners and can use effective study strategies. Furthermore, introducing a second language too early might, some claim, overload young pupils who are still mastering basic literacy.\n\nHowever, there are compelling reasons to begin at primary school. Young children are remarkably skilled at imitating sounds, which is why early learners tend to develop a more natural accent. They are also less self-conscious, so they are more willing to speak and make mistakes — an essential part of learning any language. In addition, starting early gives students more years of exposure, and research consistently shows that the total amount of practice is a key factor in reaching fluency.\n\nIn my view, the concerns about overloading children can be addressed through playful, communicative teaching rather than formal grammar lessons. Songs, games and stories allow young learners to absorb a language naturally, while explicit grammar can be introduced later.\n\nIn conclusion, although older learners have certain cognitive advantages, I am convinced that starting a foreign language at primary school offers lasting benefits, particularly in pronunciation, confidence and overall exposure.",
                    tipsAr = listOf(
                        "هيكل مقال «ناقش الرأيين»: مقدمة (إعادة صياغة السؤال + رأيك) ← فقرة للرأي الأول ← فقرة للرأي الثاني ← خاتمة.",
                        "استخدم أدوات ربط: However, Furthermore, In addition, In my view, In conclusion.",
                        "ادعم كل فكرة بسبب أو مثال، ولا تنسخ كلمات السؤال حرفياً.",
                        "راجع الأخطاء الشائعة في آخر 3 دقائق: الأزمنة، s مع he/she، وأدوات التعريف.",
                    ),
                ),
            ),
            MockSection(
                SectionType.SPEAKING, "Speaking", 12,
                "سيقرأ عليك الممتحن الأسئلة بصوته. أجب بصوت عالٍ كأنك في الاختبار الحقيقي — حاول أن تتحدث بجمل كاملة وتوسع إجاباتك.",
                speaking = listOf(
                    SpeakingPart(
                        "Part 1 — Introduction",
                        "أسئلة قصيرة عن نفسك. أجب في جملتين أو ثلاث لكل سؤال.",
                        listOf(
                            "Where are you from, and what do you like about your hometown?",
                            "Do you work or are you a student?",
                            "What do you usually do in your free time?",
                            "How often do you use the internet, and what for?",
                        ),
                        sample = "I'm from Amman, the capital of Jordan. What I like most about it is that it's a mix of old and new — you can visit ancient Roman ruins in the morning and modern cafés in the evening.",
                    ),
                    SpeakingPart(
                        "Part 2 — Long turn",
                        "لديك دقيقة للتحضير (يمكنك كتابة ملاحظات)، ثم تحدث لمدة دقيقتين عن الموضوع.",
                        listOf("Describe a skill that you learned and that is useful to you."),
                        cueCard = listOf("what the skill is", "when and how you learned it", "how difficult it was", "and explain why it is useful to you"),
                        prepSeconds = 60,
                        talkSeconds = 120,
                        sample = "I'd like to talk about learning to cook. I started about three years ago when I moved to another city for university and had to look after myself. At first, I learned mainly from YouTube videos and by calling my mother — she was very patient! It was harder than I expected, because timing is everything, and I burned quite a few meals. But it's been incredibly useful: I eat more healthily, I save money, and cooking for friends has become one of my favourite ways to relax.",
                    ),
                    SpeakingPart(
                        "Part 3 — Discussion",
                        "أسئلة أعمق مرتبطة بموضوع الجزء الثاني. أعطِ رأيك مع أسباب وأمثلة.",
                        listOf(
                            "Which skills do you think schools should teach that they don't teach now?",
                            "Is it better to learn new skills from a teacher or online?",
                            "How might the skills people need change in the future?",
                        ),
                        sample = "I think schools should teach practical financial skills, like managing a budget. Many young people leave school without knowing how to save money or avoid debt, and these are skills they'll need for the rest of their lives.",
                    ),
                ),
            ),
        ),
    ),

    // ======================== CAMBRIDGE B1 PRELIMINARY ========================
    MockExam(
        id = "mock-pet-1",
        kind = MockKind.CAMBRIDGE_B1,
        title = "B1 Preliminary — Practice Test",
        titleAr = "اختبار Cambridge B1 Preliminary التجريبي",
        level = CefrLevel.B1,
        descriptionAr = "اختبار تجريبي بأنماط امتحان كامبريدج للمستوى B1 (PET): قراءة الإعلانات القصيرة، اختيار الكلمة المناسبة في نص، الفراغات المفتوحة، الاستماع، كتابة بريد إلكتروني، والمحادثة. النتيجة تقدير على مقياس Cambridge English Scale.",
        realFormatAr = "الاختبار الحقيقي: قراءة 45 دقيقة (6 أجزاء)، كتابة 45 دقيقة (جزءان)، استماع 30 دقيقة (4 أجزاء)، محادثة 12–17 دقيقة.",
        sections = listOf(
            MockSection(
                SectionType.READING, "Reading", 15,
                "ثلاثة أجزاء من قسم القراءة بنفس نمط الامتحان.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Short texts",
                        "اقرأ كل رسالة أو إعلان قصير واختر المعنى الصحيح.",
                        questions = listOf(
                            Question.Choice("NOTICE: «Swimming pool closed for cleaning until Thursday. Gym open as usual.»\nWhat does the notice say?", listOf("You can use the gym today.", "The gym is closed until Thursday.", "The pool opens on Wednesday.")),
                            Question.Choice("MESSAGE: «Hi Tom, the film starts at 8, not 7.30. Let's meet outside the cinema at 7.45. — Rana»\nWhy did Rana write?", listOf("To change the meeting time", "To cancel the plan", "To choose a different cinema")),
                            Question.Choice("LABEL: «Keep in the fridge. Use within 3 days of opening.»\nWhat does the label tell you?", listOf("The food goes bad quickly after opening.", "You must eat it today.", "It doesn't need a fridge.")),
                        ),
                    ),
                    MockPart(
                        "Part 5 — Multiple-choice cloze",
                        "اقرأ النص واختر الكلمة المناسبة لكل فراغ.",
                        text = "My first trip abroad\n\nWhen I was sixteen, I (1)___ the chance to spend two weeks with a family in Ireland. At first I was nervous because I had never (2)___ so far from home. But the family were very kind and made me feel (3)___ at once. Every day I went to an English school in the morning, and in the afternoon we (4)___ trips to beautiful places. By the end, I could (5)___ much more easily, and I still write to the family today.",
                        questions = listOf(
                            gap(1, "had", "made", "did", "took", explain = "have the chance = تتاح لي الفرصة."),
                            gap(2, "travelled", "visited", "moved", "walked"),
                            gap(3, "welcome", "pleased", "happy", "glad", explain = "make someone feel welcome = تجعله يشعر بالترحيب."),
                            gap(4, "went on", "made up", "got on", "put on", explain = "go on a trip = يذهب في رحلة."),
                            gap(5, "communicate", "tell", "say", "talk to"),
                        ),
                    ),
                    MockPart(
                        "Part 6 — Open cloze",
                        "اكتب كلمة واحدة فقط في كل فراغ.",
                        text = "Dear Sam,\nThank you (1)___ your email. I'm writing to tell you about my new job. I started work (2)___ a hotel last month. The work is hard, but I really enjoy meeting people (3)___ different countries. I'm saving money because I want (4)___ visit you next summer!\nLove, Mia",
                        questions = listOf(
                            openGap(1, "for", explain = "Thank you for…"),
                            openGap(2, "at", "in", explain = "work at / in a hotel"),
                            openGap(3, "from", explain = "people from different countries"),
                            openGap(4, "to", explain = "want to + فعل"),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.LISTENING, "Listening", 8,
                "ستسمع أربعة مقاطع قصيرة. اختر الإجابة الصحيحة لكل سؤال. في الامتحان الحقيقي تسمع كل مقطع مرتين.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Short extracts",
                        "استمع واختر الإجابة الصحيحة.",
                        questions = listOf(
                            audioQ("Woman: Are you taking the bus to the airport? Man: I was going to, but there's a train every fifteen minutes now, so that's quicker. A taxi's too expensive.", "How will the man get to the airport?", "By train", "By bus", "By taxi"),
                            audioQ("Girl: What did you get Mum for her birthday? Boy: I wanted to buy her a scarf, but they were all too expensive, so I got her some flowers and a book about gardening.", "What didn't the boy buy?", "A scarf", "Flowers", "A book"),
                            audioQ("Man: The museum opens at nine thirty on weekdays, but at weekends it doesn't open until ten. It closes at five every day.", "When does the museum open on Saturday?", "10:00", "9:30", "5:00"),
                            audioQ("Woman: I'm really sorry I'm late. I left home on time, but I had to go back because I'd forgotten my phone.", "Why was the woman late?", "She went back for her phone.", "She missed the bus.", "She got up late."),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.WRITING, "Writing — Part 1 (Email)", 20,
                "اكتب بريداً إلكترونياً من حوالي 100 كلمة، وتأكد من الرد على النقاط الأربع.",
                writing = WritingTask(
                    prompt = "Read this email from your English friend Alex.\n\n«Hi! I'm coming to your city for a weekend next month. Which places should I visit? What food should I try? And what's the best way to get around? Would you be free to meet me on Saturday?»\n\nWrite your email to Alex, answering all the questions.",
                    promptAr = "اقرأ بريد صديقك أليكس الذي سيزور مدينتك، واكتب رداً تجيب فيه عن: الأماكن التي يزورها، الطعام الذي يجربه، أفضل وسيلة تنقل، وهل أنت متاح لمقابلته يوم السبت.",
                    minWords = 100,
                    modelAnswer = "Hi Alex,\n\nThat's great news! I can't wait to see you.\n\nYou should definitely visit the old city — the markets and the castle are amazing. If you like history, the national museum is also worth seeing.\n\nAs for food, you must try mansaf, our traditional dish with lamb and rice. There's a small restaurant near the castle that makes the best one.\n\nThe easiest way to get around is by taxi or app-based car service, because buses can be confusing for visitors.\n\nAnd yes, I'm free on Saturday! Why don't we meet for lunch and then I'll show you around?\n\nSee you soon,\nOmar",
                    tipsAr = listOf(
                        "ابدأ بتحية مناسبة (Hi Alex,) واختم بعبارة ودية (See you soon,).",
                        "أجب عن كل الأسئلة الأربعة — نسيان سؤال يخفض الدرجة.",
                        "استخدم عبارات النصيحة: You should… / You must try… / Why don't we…?",
                    ),
                ),
            ),
            MockSection(
                SectionType.SPEAKING, "Speaking", 8,
                "في امتحان كامبريدج تتحدث مع ممتحن ومع طالب آخر. هنا تدرّب على أسئلة الممتحن.",
                speaking = listOf(
                    SpeakingPart(
                        "Part 1 — Interview",
                        "أجب عن أسئلة شخصية قصيرة.",
                        listOf("What's your name, and where do you live?", "Who do you live with?", "What did you do last weekend?", "Tell us about a place you would like to visit."),
                        sample = "Last weekend I visited my grandparents in the countryside. We had a big lunch together and in the afternoon I helped my grandfather in his garden. It was really relaxing.",
                    ),
                    SpeakingPart(
                        "Part 4 — Discussion",
                        "تحدث عن رأيك في الموضوع مع أسباب.",
                        listOf("Do you prefer travelling with your family or with friends? Why?", "What's the best way to learn about another country's culture?"),
                        sample = "I prefer travelling with friends because we usually like the same activities, like hiking or trying street food. With my family, we tend to visit museums, which can be a bit boring for me.",
                    ),
                ),
            ),
        ),
    ),

    // ======================== CAMBRIDGE B2 FIRST ========================
    MockExam(
        id = "mock-fce-1",
        kind = MockKind.CAMBRIDGE_B2,
        title = "B2 First — Practice Test",
        titleAr = "اختبار Cambridge B2 First التجريبي",
        level = CefrLevel.B2,
        descriptionAr = "اختبار تجريبي بأنماط امتحان B2 First (FCE): الأجزاء الأربعة لاستخدام اللغة (الاختيار من متعدد، الفراغات المفتوحة، تكوين الكلمات، إعادة الصياغة بكلمة مفتاحية)، القراءة، الاستماع، كتابة مقال والمحادثة.",
        realFormatAr = "الاختبار الحقيقي: القراءة واستخدام اللغة 75 دقيقة (7 أجزاء)، الكتابة 80 دقيقة، الاستماع 40 دقيقة، المحادثة 14 دقيقة.",
        sections = listOf(
            MockSection(
                SectionType.USE_OF_ENGLISH, "Reading & Use of English", 20,
                "الأجزاء الأربعة لاستخدام اللغة في امتحان B2 First.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Multiple-choice cloze",
                        "اختر الكلمة الأنسب لكل فراغ — انتبه للتلازمات اللفظية.",
                        text = "The four-day working week\n\nA growing number of companies are (1)___ a four-day week. Early trials suggest that employees are not only happier but also (2)___ productive. One firm reported that staff took fewer days off (3)___ to illness. However, critics warn that the model may not (4)___ for every industry, especially those that need to provide services seven days a week. Still, the idea is clearly (5)___ ground.",
                        questions = listOf(
                            gap(1, "trialling", "trying out to", "testing on", "attempting", explain = "trial (v) = يجرّب رسمياً."),
                            gap(2, "more", "most", "much", "very"),
                            gap(3, "due", "because", "owing", "thanks", explain = "due to illness = بسبب المرض."),
                            gap(4, "work", "do", "make", "go", explain = "work for = ينجح مع."),
                            gap(5, "gaining", "winning", "earning", "taking", explain = "gain ground = يكتسب انتشاراً."),
                        ),
                    ),
                    MockPart(
                        "Part 2 — Open cloze",
                        "اكتب كلمة واحدة فقط في كل فراغ.",
                        text = "Learning to code\n\nMany people believe that coding is only (1)___ maths geniuses, but this is far from true. In fact, (2)___ of the most successful programmers started with no technical background at all. What matters most is patience, because you will make mistakes (3)___ time you write a program. The good news is that there (4)___ never been more free resources available online.",
                        questions = listOf(
                            openGap(1, "for"),
                            openGap(2, "some", "many", "most"),
                            openGap(3, "every", "each"),
                            openGap(4, "has"),
                        ),
                    ),
                    MockPart(
                        "Part 3 — Word formation",
                        "استخدم الكلمة المكتوبة بأحرف كبيرة لتكوين كلمة مناسبة للجملة.",
                        questions = listOf(
                            Question.Typing("The ________ of the new bridge took three years. (CONSTRUCT)", listOf("construction"), explanation = "construct → construction (اسم)."),
                            Question.Typing("She was ________ about the results of her exam. (OPTIMISM)", listOf("optimistic"), explanation = "optimism → optimistic (صفة)."),
                            Question.Typing("It's ________ to drive without a licence. (LEGAL)", listOf("illegal"), explanation = "legal → illegal (عكس)."),
                            Question.Typing("The museum has a large ________ of ancient coins. (COLLECT)", listOf("collection"), explanation = "collect → collection."),
                            Question.Typing("He answered the question very ________. (CONFIDENT)", listOf("confidently"), explanation = "confident → confidently (ظرف)."),
                        ),
                    ),
                    MockPart(
                        "Part 4 — Key word transformation",
                        "أكمل الجملة الثانية لتعطي نفس معنى الأولى باستخدام الكلمة المعطاة (من كلمتين إلى خمس كلمات). اكتب الكلمات الناقصة فقط.",
                        questions = listOf(
                            Question.Typing("«I haven't been to the cinema for months.» (LAST)\nIt's months ________ to the cinema.", listOf("since I last went", "since i last went"), explanation = "It's months since I last went to the cinema."),
                            Question.Typing("«It wasn't necessary for you to bring food.» (NEED)\nYou ________ food.", listOf("needn't have brought", "did not need to bring", "didn't need to bring", "need not have brought"), explanation = "You needn't have brought food."),
                            Question.Typing("«They say the hotel is very comfortable.» (SAID)\nThe hotel ________ very comfortable.", listOf("is said to be"), explanation = "The hotel is said to be very comfortable."),
                            Question.Typing("«I regret not studying harder.» (WISH)\nI ________ harder.", listOf("wish i had studied", "wish i'd studied"), explanation = "I wish I had studied harder."),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.READING, "Reading — Part 5", 12,
                "اقرأ المقال واختر الإجابة الأنسب لكل سؤال.",
                parts = listOf(
                    MockPart(
                        "Part 5 — Multiple choice",
                        "أسئلة عن الفكرة والتفاصيل ورأي الكاتب ومعنى الكلمات.",
                        text = "When I told my friends I was giving up my smartphone for a month, most of them laughed. 'You won't last a week,' said one. To be honest, I shared their doubts. Like most people my age, I reached for my phone the moment I woke up and checked it dozens of times a day, often without any real reason.\n\nThe first few days were genuinely uncomfortable. I kept feeling for my phone in my pocket, and I felt anxious that I was missing important messages. But by the end of the first week, something unexpected happened: I started noticing things. I read three books, rediscovered my love of cooking, and had longer, deeper conversations with my family.\n\nThat isn't to say the experiment was entirely positive. Arranging to meet people became complicated, and I got lost twice without a map app. Yet when the month ended, I didn't rush back to my old habits. I now keep my phone in another room in the evenings — a small change, but one that has made a remarkable difference.",
                        questions = listOf(
                            Question.Choice("How did the writer feel before the experiment?", listOf("Doubtful about succeeding", "Confident it would be easy", "Angry with his friends", "Excited to start immediately")),
                            Question.Choice("What surprised the writer in the first week?", listOf("He became more aware of the world around him", "He received fewer messages", "His friends stopped laughing", "He felt more anxious than before")),
                            Question.Choice("What problem does the writer mention?", listOf("Getting lost without a map app", "Losing his job", "Reading too much", "Cooking badly")),
                            Question.Choice("What is the writer's attitude at the end?", listOf("He has kept a small but valuable change", "He regrets the experiment", "He never uses his phone now", "He uses his phone more than before")),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.LISTENING, "Listening — Part 1", 8,
                "ستسمع أربعة مقاطع لا علاقة بينها. اختر الإجابة الصحيحة لكل مقطع.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Short extracts",
                        "استمع واختر الإجابة الصحيحة.",
                        questions = listOf(
                            audioQ("I'd been looking forward to the concert for months, and the band were brilliant, no doubt about it. But honestly, the venue let it down — it was so crowded that I could hardly see the stage, and the sound kept cutting out.", "What did the speaker dislike about the concert?", "The venue", "The band", "The ticket price"),
                            audioQ("People assume I became a chef because I grew up in a family of cooks, but actually nobody in my family can cook! It was a summer job in a restaurant kitchen that changed everything — I was hooked from the first day.", "What made the speaker become a chef?", "A summer job", "His family", "A cooking course"),
                            audioQ("Woman: So are you going to accept the offer? Man: Well, the salary's better and it's closer to home, but I'd be leaving a team I really get on with. I need to think about it a bit longer.", "How does the man feel about the job offer?", "Undecided", "Delighted", "Disappointed"),
                            audioQ("If you're planning to visit the island, my advice is to avoid August. Not only is it extremely hot, but every hotel is fully booked and prices double. Late spring is ideal — the weather's pleasant and it's much quieter.", "When does the speaker recommend visiting the island?", "In late spring", "In August", "In winter"),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.WRITING, "Writing — Part 1 (Essay)", 35,
                "اكتب مقالاً من 140 إلى 190 كلمة. استخدم الملاحظتين وأضف فكرة ثالثة خاصة بك.",
                writing = WritingTask(
                    prompt = "In your English class you have been talking about cities. Now your teacher has asked you to write an essay.\n\nIs it better to live in a big city or in a small town?\n\nNotes — write about:\n1. jobs and opportunities\n2. quality of life\n3. ........ (your own idea)",
                    promptAr = "هل الأفضل العيش في مدينة كبيرة أم في بلدة صغيرة؟ اكتب عن: فرص العمل، وجودة الحياة، وفكرة ثالثة من عندك.",
                    minWords = 140,
                    modelAnswer = "Many young people dream of moving to a big city, while others prefer the peace of a small town. Both options have clear advantages.\n\nThe main attraction of a big city is the range of opportunities it offers. There are far more jobs, especially in areas such as technology and finance, and salaries tend to be higher. Cities also provide better universities and more cultural activities.\n\nOn the other hand, quality of life is often better in small towns. The air is cleaner, housing is cheaper and people usually know their neighbours, which creates a strong sense of community. In contrast, city life can be stressful and expensive.\n\nAnother important factor is transport. In a city, public transport makes it easy to get around without a car, whereas in a small town a car is often essential.\n\nIn my opinion, big cities are better for young people starting their careers, but small towns may be ideal for families who value a calmer lifestyle.",
                    tipsAr = listOf(
                        "اكتب عن النقطتين المعطاتين وأضف فكرة ثالثة خاصة بك — هذا مطلوب في الامتحان.",
                        "التزم بعدد الكلمات (140–190).",
                        "استخدم مقارنات: whereas, In contrast, On the other hand.",
                        "اختم برأيك الواضح في الخاتمة.",
                    ),
                ),
            ),
            MockSection(
                SectionType.SPEAKING, "Speaking", 8,
                "تدرّب على أسئلة الممتحن في الجزأين الأول والرابع.",
                speaking = listOf(
                    SpeakingPart(
                        "Part 1 — Interview",
                        "أسئلة عامة عن حياتك واهتماماتك.",
                        listOf("What do you enjoy most about the area where you live?", "How important is music in your life?", "If you could learn a new skill, what would it be?"),
                        sample = "If I could learn a new skill, it would definitely be playing the guitar. I've always admired people who can just pick up an instrument and play, and I think it would be a great way to relax after work.",
                    ),
                    SpeakingPart(
                        "Part 4 — Discussion",
                        "أسئلة نقاشية — أعطِ رأيك وبرره، واذكر وجهات نظر أخرى.",
                        listOf("Do you think people will still read printed books in the future?", "Some people say we spend too much time on social media. What do you think?"),
                        sample = "I think printed books will survive, although they might become less common. Many people, including me, find it easier to concentrate on paper, and there's something special about owning a physical book. That said, e-books are clearly more convenient for travelling.",
                    ),
                ),
            ),
        ),
    ),
)

object MockScoring {
    private val listeningBands = listOf(39 to 9.0, 37 to 8.5, 35 to 8.0, 32 to 7.5, 30 to 7.0, 26 to 6.5, 23 to 6.0, 18 to 5.5, 16 to 5.0, 13 to 4.5, 10 to 4.0, 6 to 3.5, 4 to 3.0)
    private val readingBands = listOf(39 to 9.0, 37 to 8.5, 35 to 8.0, 33 to 7.5, 30 to 7.0, 27 to 6.5, 23 to 6.0, 19 to 5.5, 15 to 5.0, 13 to 4.5, 10 to 4.0, 8 to 3.5, 6 to 3.0)

    /** IELTS band from a raw score scaled to 40 questions (published conversion tables). */
    fun ieltsBand(correct: Int, total: Int, listening: Boolean): Double {
        val raw = Math.round(correct * 40.0 / total.coerceAtLeast(1)).toInt()
        val table = if (listening) listeningBands else readingBands
        return table.firstOrNull { raw >= it.first }?.second ?: 2.5
    }

    /** Self-assessment ratings (0..3 per criterion) → band 4.5..8.0. */
    fun selfBand(ratings: List<Int>): Double {
        if (ratings.isEmpty()) return 0.0
        val avg = ratings.average()
        return roundHalf(4.5 + avg * (3.5 / 3))
    }

    /** IELTS rounding: averages are rounded to the nearest half band (.25 up, .75 up). */
    fun overallIelts(bands: List<Double>): Double = roundHalf(bands.average())

    private fun roundHalf(x: Double): Double = Math.floor(x * 2 + 0.5) / 2

    /** Cambridge English Scale estimate from a percentage. */
    fun cambridgeScale(kind: MockKind, percent: Int): Int {
        val points = when (kind) {
            MockKind.CAMBRIDGE_B1 -> listOf(0 to 102, 70 to 140, 85 to 153, 92 to 160, 100 to 170)
            else -> listOf(0 to 122, 60 to 160, 75 to 173, 80 to 180, 100 to 190)
        }
        val p = percent.coerceIn(0, 100)
        val (lo, hi) = points.zipWithNext().first { p <= it.second.first }
        val t = (p - lo.first).toDouble() / (hi.first - lo.first)
        return Math.round(lo.second + t * (hi.second - lo.second)).toInt()
    }

    /** Grade and CEFR result as printed on Cambridge certificates. */
    fun cambridgeGrade(kind: MockKind, scale: Int): Pair<String, String> = when (kind) {
        MockKind.CAMBRIDGE_B1 -> when {
            scale >= 160 -> "Grade A (Distinction)" to "B2"
            scale >= 153 -> "Grade B (Merit)" to "B1"
            scale >= 140 -> "Grade C (Pass)" to "B1"
            scale >= 120 -> "Level A2" to "A2"
            else -> "Below A2" to "—"
        }
        else -> when {
            scale >= 180 -> "Grade A" to "C1"
            scale >= 173 -> "Grade B" to "B2"
            scale >= 160 -> "Grade C" to "B2"
            scale >= 140 -> "Level B1" to "B1"
            else -> "Below B1" to "—"
        }
    }
}
