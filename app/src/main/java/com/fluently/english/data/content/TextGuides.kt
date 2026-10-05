package com.fluently.english.data.content

/*
 * Before / during / after support for every reading and listening lesson.
 * Key words always appear in the text, so they can be highlighted and tapped.
 */

private fun textGuide(
    hook: String,
    predict: Question.Choice,
    keyWords: List<Word>,
    strategy: String,
    gist: Question.Choice,
    reflect: String,
    sample: String,
) = TextGuide(hook, predict, keyWords, strategy, gist, reflect, sample)

private const val SKIM = "القراءة السريعة (Skimming): اقرأ النص أولاً بسرعة لتفهم الفكرة العامة فقط، دون التوقف عند الكلمات الصعبة. ثم اقرأه مرة ثانية ببطء للتفاصيل."
private const val SCAN = "البحث عن معلومة (Scanning): عندما يسألك سؤال عن رقم أو اسم أو وقت، لا تقرأ كل شيء — حرّك عينك بسرعة بحثاً عن الرقم أو الاسم فقط."
private const val LISTEN_NUMBERS = "في الاستماع الأول ركّز على «من؟ أين؟ ماذا؟» فقط. وفي الاستماع الثاني انتبه للأرقام والأسعار والأوقات — اكتبها على ورقة إن أمكن."
private const val LISTEN_GIST = "لا تحاول فهم كل كلمة! استمع للكلمات المهمة (الأسماء والأفعال) وتجاهل الباقي. الفهم العام أهم من الترجمة الحرفية."

val TextGuides: Map<String, TextGuide> = mapOf(

    // ---------------- A1 ----------------
    "a1-u1-l3" to textGuide(
        hook = "ستسمع شابة تعرّف بنفسها وبصديقها. هذا ما يقوله الناس في أول لقاء: الاسم، العمر، البلد، والعمل.",
        predict = q("What information do people usually give when they meet?", "Name, age and country", "Their phone password", "The weather tomorrow", explain = "صحيح! في أول لقاء نذكر الاسم والعمر والبلد عادة."),
        keyWords = listOf(
            w("years old", "عمر (سنة)", "I am twenty years old."),
            w("live", "يسكن", "I live in London now."),
            w("student", "طالب", "I am a student at the university."),
            w("teacher", "معلم", "He is a teacher."),
        ),
        strategy = LISTEN_GIST,
        gist = q("What is the recording mainly about?", "Two people introducing themselves", "A trip to Canada", "A university exam"),
        reflect = "عرّف بنفسك بنفس الطريقة: اسمك، عمرك، بلدك، وعملك.",
        sample = "Hi! My name is Omar. I am 25 years old. I am from Jordan. I am an engineer.",
    ),
    "a1-u2-l3" to textGuide(
        hook = "كريم من الجزائر يحدثنا عن عائلته. قبل أن تقرأ: كم شخصاً في عائلتك؟",
        predict = q("The text is called «My family». What will it describe?", "The people in Karim's family", "Karim's favourite food", "A football match"),
        keyWords = listOf(
            w("engineer", "مهندس", "He is an engineer."),
            w("primary school", "مدرسة ابتدائية", "She is a teacher at a primary school."),
            w("clever", "ذكي", "She is very clever."),
            w("dog", "كلب", "We have a small dog."),
        ),
        strategy = SCAN,
        gist = q("What is the main idea of the text?", "Karim describes his family members", "Karim wants a new job", "Karim visits Algeria"),
        reflect = "صف عائلتك في ثلاث جمل على طريقة كريم.",
        sample = "There are four people in my family. My father is a doctor. I have one sister. She is very clever.",
    ),
    "a1-u3-l3" to textGuide(
        hook = "نورا ممرضة تبدأ يومها مبكراً جداً. لاحظ الأوقات والكلمات مثل usually و never — إنها تكشف روتينها.",
        predict = q("Nora is a nurse. When do you think she starts work?", "Early in the morning", "At midnight", "She doesn't work"),
        keyWords = listOf(
            w("nurse", "ممرضة", "I'm a nurse in a big hospital."),
            w("usually", "عادةً", "I usually wake up at 5:30."),
            w("quick", "سريع", "I have a quick breakfast."),
            w("hard", "صعب / شاق", "My work is hard, but I love it."),
            w("swimming", "السباحة", "I go swimming with my friends."),
        ),
        strategy = SCAN,
        gist = q("What does the text describe?", "Nora's daily routine", "Nora's holiday", "How to become a nurse"),
        reflect = "صف يومك أنت: متى تستيقظ؟ ماذا تأكل؟ متى تنهي عملك؟",
        sample = "I usually wake up at 7. I have tea and bread. I finish work at 4 p.m. In the evening, I read.",
    ),
    "a1-u4-l3" to textGuide(
        hook = "زبون في مقهى يطلب طعاماً ويدفع. ستسمع العبارات الحقيقية التي يستخدمها النادل والزبون.",
        predict = q("What does a waiter usually ask first?", "What can I get you?", "Where do you live?", "How old are you?"),
        keyWords = listOf(
            w("sandwich", "شطيرة", "Can I have a chicken sandwich?"),
            w("Anything else", "شيء آخر؟", "Of course. Anything else?"),
            w("salad", "سلطة", "Yes, a small salad, please."),
            w("How much", "بكم؟", "How much is that?"),
        ),
        strategy = LISTEN_NUMBERS,
        gist = q("Where does the conversation take place?", "In a café", "In a bank", "At school"),
        reflect = "اطلب وجبتك المفضلة كأنك في مقهى.",
        sample = "Can I have a cheese sandwich and a coffee, please? How much is that?",
    ),

    // ---------------- A2 ----------------
    "a2-u1-l3" to textGuide(
        hook = "عائلة سافرت إلى إسطنبول. القصة كلها بالماضي البسيط — لاحظ الأفعال مثل went و visited و took.",
        predict = q("A holiday story is usually told in…", "the past", "the future", "questions only"),
        keyWords = listOf(
            w("flight", "رحلة جوية", "The flight took about two hours."),
            w("clean", "نظيف", "Our hotel was small but very clean."),
            w("amazing", "مذهل", "They were amazing!"),
            w("scared", "خائف", "My little brother was a bit scared."),
            w("delicious", "لذيذ", "The food was delicious."),
        ),
        strategy = SKIM,
        gist = q("How did the writer feel about the holiday overall?", "Happy, despite the heat", "Bored all the time", "Angry about the hotel"),
        reflect = "احكِ عن آخر رحلة قمت بها في ثلاث جمل بالماضي.",
        sample = "Last year I went to Dubai. I visited the Burj Khalifa. The food was delicious!",
    ),
    "a2-u2-l3" to textGuide(
        hook = "شخص عاش في الرياض وجدة يقارن بينهما. استمع لكلمات المقارنة: bigger و more relaxing و the best.",
        predict = q("When comparing two cities, people talk about…", "size, weather and people", "only the prices of cars", "their phone numbers"),
        keyWords = listOf(
            w("buildings", "مبانٍ", "It has more modern buildings."),
            w("relaxing", "مريح / مهدّئ", "Jeddah is more relaxing."),
            w("humid", "رطب", "In Jeddah it's more humid."),
            w("seafood", "مأكولات بحرية", "If you want the best seafood, go to Jeddah!"),
        ),
        strategy = LISTEN_GIST,
        gist = q("What does the speaker do in the recording?", "Compares two cities", "Describes a job", "Gives directions"),
        reflect = "قارن بين مدينتين تعرفهما في جملتين.",
        sample = "Cairo is bigger than Alexandria, but Alexandria is more relaxing because it's by the sea.",
    ),
    "a2-u3-l3" to textGuide(
        hook = "رسالة من آدم لصديقه عن خططه للصيف. ابحث عن going to — إنها علامة الخطط!",
        predict = q("An email about «plans for the summer» will use…", "going to and will", "only the past simple", "no verbs"),
        keyWords = listOf(
            w("exams", "امتحانات", "I finished my exams last week."),
            w("save", "يدّخر", "I want to save some money."),
            w("apartment", "شقة", "We're staying in a small apartment."),
            w("course", "دورة", "I'm going to take a short Spanish course."),
        ),
        strategy = SCAN,
        gist = q("What is the purpose of Adam's email?", "To share his summer plans", "To complain about a hotel", "To ask for a job"),
        reflect = "اكتب جملتين عن خططك للعطلة القادمة باستخدام going to.",
        sample = "This summer I'm going to visit my grandparents. I'm also going to learn to swim.",
    ),
    "a2-u4-l3" to textGuide(
        hook = "زبون يبحث عن سترة شتوية. انتبه للسعر والخصم والمقاس — تفاصيل ستُسأل عنها!",
        predict = q("What do people usually ask in a clothes shop?", "The price and the size", "The time of the next train", "The name of the manager's dog"),
        keyWords = listOf(
            w("jacket", "سترة", "I'm looking for a jacket for winter."),
            w("Medium", "مقاس متوسط", "Medium, I think."),
            w("percent", "بالمئة", "There's a twenty percent discount."),
            w("changing rooms", "غرف القياس", "The changing rooms are over there."),
        ),
        strategy = LISTEN_NUMBERS,
        gist = q("What is the result of the conversation?", "The customer buys a large jacket", "The customer leaves without buying", "The shop is closed"),
        reflect = "تخيل أنك تشتري حذاءً: اسأل عن السعر والمقاس.",
        sample = "Excuse me, how much are these shoes? Do you have them in size 42?",
    ),

    // ---------------- B1 ----------------
    "b1-u1-l3" to textGuide(
        hook = "طالب عاش سنة في ألمانيا يحكي عن الصعوبات وكيف تغلب عليها. لاحظ كيف يستخدم المضارع التام ليتحدث عن نتائج التجربة الآن.",
        predict = q("A story about «living abroad» probably includes…", "difficulties and personal growth", "only shopping lists", "football results"),
        keyWords = listOf(
            w("master's degree", "درجة الماجستير", "I moved to Germany to do a master's degree."),
            w("lonely", "وحيد", "I felt lonely."),
            w("language exchange", "تبادل لغوي", "I joined a language exchange group."),
            w("independent", "مستقل", "I've become much more independent."),
        ),
        strategy = SKIM,
        gist = q("What is the writer's main message?", "Living abroad was hard but life-changing", "Germany is too cold to live in", "Master's degrees are useless"),
        reflect = "تحدث عن تحدٍّ واجهته وكيف تغيرت بعده.",
        sample = "Learning English was a big challenge for me, but I've become more confident since then.",
    ),
    "b1-u2-l3" to textGuide(
        hook = "رئيس نادي البيئة في مدرسة يعلن خطة جديدة. ستسمع الشرطية الأولى: If we don't change… this number will grow.",
        predict = q("What might an environment club announce?", "A plan to reduce plastic", "A new football coach", "Cheaper school uniforms"),
        keyWords = listOf(
            w("tonnes", "أطنان", "Our school throws away about two tonnes of plastic."),
            w("habits", "عادات", "If we don't change our habits…"),
            w("fountains", "نوافير / مشارب", "There will be free water fountains on every floor."),
            w("bins", "صناديق (قمامة)", "We're going to put recycling bins in every classroom."),
        ),
        strategy = LISTEN_NUMBERS,
        gist = q("What is the speaker's main goal?", "To reduce plastic waste at school", "To sell water bottles", "To close the café"),
        reflect = "اقترح فكرة واحدة لتقليل البلاستيك في بيتك أو عملك.",
        sample = "If we use reusable bags, we'll reduce plastic waste at home.",
    ),
    "b1-u3-l3" to textGuide(
        hook = "مقال عن أهمية النوم ونصائح الخبراء. لاحظ الأفعال الناقصة should و shouldn't — إنها لغة النصائح.",
        predict = q("An article called «Sleep: the forgotten medicine» will probably…", "explain why sleep is important", "sell sleeping pills", "describe a hotel"),
        keyWords = listOf(
            w("immune system", "جهاز المناعة", "It can weaken your immune system."),
            w("risk", "خطر", "It can increase your risk of heart disease."),
            w("screens", "شاشات", "You should avoid screens for an hour before bed."),
            w("relaxing", "مريح", "Do something relaxing until you feel sleepy."),
        ),
        strategy = SKIM,
        gist = q("What does the article mainly do?", "Explains the effects of poor sleep and gives advice", "Tells a story about a doctor", "Compares different beds"),
        reflect = "ما النصيحة التي ستطبقها من المقال؟ قلها بـ I should…",
        sample = "I should stop using my phone an hour before bed.",
    ),
    "b1-u4-l3" to textGuide(
        hook = "قصة غامضة حدثت في فندق ليلاً. القصص تستخدم الماضي المستمر للخلفية (was reading) والماضي البسيط للأحداث (heard).",
        predict = q("A story called «A strange night» will probably be…", "mysterious", "a recipe", "a weather report"),
        keyWords = listOf(
            w("reception desk", "مكتب الاستقبال", "I was reading a book at the reception desk."),
            w("knocking", "يطرق", "Someone was knocking on the front door."),
            w("shaking", "يرتجف", "He was shaking with cold."),
            w("coin", "عملة معدنية", "He left a note and a gold coin."),
        ),
        strategy = LISTEN_GIST,
        gist = q("What is the message of the story?", "Kindness comes back to you", "Never work at night", "Hotels are dangerous"),
        reflect = "احكِ عن موقف غريب حدث معك باستخدام: I was … when …",
        sample = "I was walking home when I saw a cat with a note on its collar.",
    ),

    // ---------------- B2 ----------------
    "b2-u1-l3" to textGuide(
        hook = "مقال عن إدمان الهواتف الذكية. ستجد المبني للمجهول كثيراً (are designed to be addictive) — أسلوب المقالات الجادة.",
        predict = q("What will an article about phone addiction probably say?", "Apps are designed to keep us using them", "Phones are always healthy", "Nobody uses phones anymore"),
        keyWords = listOf(
            w("addictive", "مسبب للإدمان", "Many apps are deliberately designed to be addictive."),
            w("scrolling", "التمرير", "Features such as endless scrolling…"),
            w("notifications", "الإشعارات", "push notifications are built to keep users engaged"),
            w("anxiety", "القلق", "linked heavy phone use to … anxiety"),
            w("digital detox", "صيام رقمي", "Some people have responded by taking a 'digital detox'."),
        ),
        strategy = "تتبع رأي الكاتب: ابحث عن كلمات مثل However و warn و suggest — إنها تكشف موقف الكاتب الحقيقي.",
        gist = q("What is the writer's conclusion?", "Use technology more consciously", "Throw away your phone", "Buy a newer phone"),
        reflect = "كم ساعة تستخدم هاتفك يومياً؟ وما الذي ستغيره؟",
        sample = "I use my phone about five hours a day. I'm going to turn off notifications at night.",
    ),
    "b2-u2-l3" to textGuide(
        hook = "موسيقي يتحدث عن قرارات حياته وندمه. ستسمع الشرطية الثالثة و wish كثيراً.",
        predict = q("When people talk about regrets, they often use…", "If I had…, I would have…", "only the present simple", "imperatives like «Sit down!»"),
        keyWords = listOf(
            w("record company", "شركة إنتاج موسيقي", "a big record company offered me a contract"),
            w("contract", "عقد", "offered me a contract"),
            w("music theory", "نظرية الموسيقى", "I wouldn't have learned so much about music theory."),
            w("passed away", "توفي", "before he passed away"),
        ),
        strategy = LISTEN_GIST,
        gist = q("What is the musician's real regret?", "Not spending more time with his father", "Refusing the contract", "Studying at university"),
        reflect = "ما القرار الذي لو عاد بك الزمن لاتخذته بشكل مختلف؟",
        sample = "If I had started learning English earlier, I would have studied abroad.",
    ),
    "b2-u3-l3" to textGuide(
        hook = "دراسة من MIT عن انتشار الأخبار الكاذبة. انتبه للأرقام ولأفعال النقل مثل reported و explained.",
        predict = q("Which spreads faster online, according to most research?", "False news", "True news", "Weather reports"),
        keyWords = listOf(
            w("researchers", "باحثون", "The researchers analysed around 126,000 stories."),
            w("analysed", "حلّل", "The researchers analysed around 126,000 stories."),
            w("bots", "روبوتات (برامج آلية)", "the main cause was not automated 'bots'"),
            w("eager", "متلهف", "which makes people more eager to share them"),
        ),
        strategy = SCAN,
        gist = q("What did the study find?", "Humans spread false news faster than true news", "Bots are the only problem", "People never share news"),
        reflect = "كيف تتأكد من صحة خبر قبل أن تشاركه؟",
        sample = "Before I share news, I check if a reliable website reports the same story.",
    ),
    "b2-u4-l3" to textGuide(
        hook = "مقابلة عمل حقيقية لمطورة برمجيات. لاحظ كيف تجيب بثقة وتحوّل نقطة ضعفها إلى شيء إيجابي.",
        predict = q("What does an interviewer usually ask about?", "Experience, motivation and weaknesses", "Your favourite film only", "Your neighbour's job"),
        keyWords = listOf(
            w("graduated", "تخرج", "I graduated in computer science."),
            w("software developer", "مطور برمجيات", "I've been working as a software developer."),
            w("start-up", "شركة ناشئة", "at a start-up in Beirut"),
            w("prioritise", "يرتب الأولويات", "a planning app that helps me prioritise"),
        ),
        strategy = LISTEN_GIST,
        gist = q("Why does the candidate want a new job?", "To work on larger projects and grow as a manager", "She was fired", "She hates programming"),
        reflect = "أجب عن سؤال: What is your greatest strength?",
        sample = "My greatest strength is that I learn quickly and I work well in a team.",
    ),

    // ---------------- C1 ----------------
    "c1-u1-l3" to textGuide(
        hook = "مقال أكاديمي يتحدى فكرة شائعة: هل تعدد المهام مهارة حقاً؟ الكاتب يبني حجته خطوة بخطوة — تتبعها.",
        predict = q("The title is «The myth of multitasking». What will the writer argue?", "Multitasking doesn't really work", "Everyone should multitask more", "Multitasking is a new sport"),
        keyWords = listOf(
            w("illusion", "وهم", "what we call multitasking is largely an illusion"),
            w("switching cost", "تكلفة التبديل", "known as 'switching cost'"),
            w("productivity", "الإنتاجية", "reduce productivity by as much as 40%"),
            w("Paradoxically", "من المفارقة", "Paradoxically, those who consider themselves the best multitaskers…"),
            w("deep work", "العمل العميق", "encouraging 'deep work'"),
        ),
        strategy = "في النصوص الأكاديمية، أول جملة في كل فقرة (topic sentence) تلخص الفقرة، وكلمات مثل Yet و Consequently تكشف تحوّل الحجة.",
        gist = q("What is the writer's main argument?", "Multitasking is mostly an illusion that harms performance", "Multitasking is the key to success", "The brain can do two things at once easily"),
        reflect = "هل تعمل على أكثر من مهمة في نفس الوقت؟ هل ستغير ذلك بعد قراءة المقال؟",
        sample = "I often study with my phone next to me, but I'll try 'deep work' sessions without it.",
    ),
    "c1-u2-l3" to textGuide(
        hook = "محقق يحلّ لغز سرقة لوحة. كل جملة استنتاج: must have, can't have, might have. استمع كأنك المحقق!",
        predict = q("A detective solving a theft will mostly…", "make deductions about the past", "sing songs", "talk about the future weather"),
        keyWords = listOf(
            w("security code", "رمز الأمان", "the thief must have known the security code"),
            w("forced entry", "دخول بالقوة", "there's no sign of forced entry"),
            w("cleaner", "عامل النظافة", "Could it have been the cleaner?"),
            w("guard", "حارس", "What about the night guard?"),
        ),
        strategy = LISTEN_GIST,
        gist = q("Who seems most responsible for what went wrong?", "The night guard, who fell asleep", "The cleaner, who was in Spain", "The detective"),
        reflect = "من تظن أنه السارق؟ برر رأيك بـ must have / can't have.",
        sample = "I think the manager must have taken it, because she had a key.",
    ),
    "c1-u3-l3" to textGuide(
        hook = "مقال يعرض رأيين حول مجانية التعليم الجامعي ثم يقترح حلاً وسطاً. هذا هو هيكل المقال الجدلي في IELTS!",
        predict = q("An essay on «Should university be free?» will probably…", "present arguments for and against", "describe a campus tour", "list university sports"),
        keyWords = listOf(
            w("public good", "منفعة عامة", "education is a public good"),
            w("tuition fees", "رسوم دراسية", "tuition fees deter talented students"),
            w("deter", "يُثني / يمنع", "tuition fees deter talented students"),
            w("taxpayers", "دافعو الضرائب", "it is funded by taxpayers"),
            w("threshold", "حد / عتبة", "exceed a certain threshold"),
        ),
        strategy = "حدد «من يقول ماذا»: Advocates argue… / Opponents point out… / economists propose… — ثلاثة أصوات في نص واحد.",
        gist = q("What solution do economists propose?", "Income-contingent loans", "Completely free university", "Closing universities"),
        reflect = "هل تؤيد التعليم الجامعي المجاني؟ اذكر سبباً واحداً.",
        sample = "I believe university should be free, because talented students shouldn't be limited by money.",
    ),
    "c1-u4-l3" to textGuide(
        hook = "مفاوضة تجارية بين مورّد ومشترٍ. لاحظ كيف يقدم كل طرف تنازلاً مقابل ميزة — هذا فن التفاوض.",
        predict = q("In a negotiation, both sides usually…", "make concessions to reach a deal", "agree immediately on everything", "refuse to talk"),
        keyWords = listOf(
            w("competitive", "تنافسي", "our price … is already very competitive"),
            w("units", "وحدات", "fifty thousand units a year"),
            w("commit", "يلتزم", "If you could commit to paying within thirty days…"),
            w("penalty clause", "شرط جزائي", "let's include a penalty clause"),
        ),
        strategy = LISTEN_NUMBERS,
        gist = q("How does the negotiation end?", "Both sides agree on a deal", "The buyer walks away", "The supplier raises the price"),
        reflect = "كيف تتفاوض على سعر أقل في السوق؟ قدم عرضاً مقابل تنازل.",
        sample = "If I buy two, could you give me a ten percent discount?",
    ),

    // ---------------- C2 ----------------
    "c2-u1-l3" to textGuide(
        hook = "مقال فلسفي يدافع عن الملل! النص مليء بالمفردات الراقية والتراكيب البلاغية — اقرأه ببطء واستمتع بأسلوبه.",
        predict = q("An essay titled «On the virtue of boredom» will argue that boredom…", "can be valuable", "should be eliminated", "is a disease"),
        keyWords = listOf(
            w("malfunction", "عطل / خلل", "regards boredom as a malfunction"),
            w("tedium", "السأم", "in our zeal to abolish tedium"),
            w("aversive", "منفّر", "far from being a merely aversive state"),
            w("idleness", "الفراغ / الكسل", "tolerate a little more idleness"),
        ),
        strategy = "في النصوص الأدبية، الكاتب يستخدم المجاز والتضاد. اسأل: ما الذي «يدافع» عنه الكاتب؟ وما الذي «ينتقده»؟",
        gist = q("What does the writer suggest?", "Tolerating boredom can spark original thought", "Screens cure boredom", "Boredom proves laziness"),
        reflect = "متى كانت آخر مرة شعرت فيها بالملل دون هاتف؟ ماذا خطر في بالك؟",
        sample = "While waiting at the airport without my phone, I came up with a new idea for my project.",
    ),
    "c2-u2-l3" to textGuide(
        hook = "بودكاست مع عالم لغويات: هل اللغة التي نتحدثها تشكّل طريقة تفكيرنا؟ موضوع رائع لمتعلمي اللغات!",
        predict = q("A linguist on a podcast about language and thought will probably…", "give examples from different languages", "talk about football", "teach cooking"),
        keyWords = listOf(
            w("linguist", "عالم لغويات", "My guest today is a linguist"),
            w("caveat", "تحفظ", "I'd add a caveat"),
            w("compass directions", "اتجاهات البوصلة", "they use compass directions"),
            w("orientation", "حس الاتجاه", "an astonishing sense of orientation"),
        ),
        strategy = LISTEN_GIST,
        gist = q("What is the guest's main point?", "Language influences, but doesn't fully determine, thought", "All languages are the same", "English is the best language"),
        reflect = "هل تشعر أنك تفكر بطريقة مختلفة عندما تتحدث الإنجليزية؟",
        sample = "When I speak English, I feel more direct, while in Arabic I'm more poetic.",
    ),
    "c2-u3-l3" to textGuide(
        hook = "مقال علمي عن العلاقة بين بكتيريا الأمعاء والمزاج. لاحظ لغة الحذر العلمي: may، suggests، premature.",
        predict = q("A scientific article about gut bacteria and the mind will probably be…", "careful and cautious in its claims", "a funny story", "an advertisement for medicine"),
        keyWords = listOf(
            w("microbiome", "الميكروبيوم", "research into the human microbiome"),
            w("bidirectional", "ثنائي الاتجاه", "the bidirectional communication"),
            w("correlational", "ارتباطي", "Much of the existing research is correlational"),
            w("premature", "سابق لأوانه", "It would therefore be premature to conclude"),
        ),
        strategy = "في المقالات العلمية، ميّز بين ما «ثبت» (demonstrated) وما «يُقترح» (suggests, may). هذا يحدد مدى ثقة الكاتب.",
        gist = q("What is the writer's conclusion?", "More rigorous research is needed", "Probiotics cure depression", "Diet doesn't affect the mind"),
        reflect = "اكتب جملة عن عادة صحية بلغة علمية حذرة (may / suggest).",
        sample = "Research suggests that regular exercise may improve mood and reduce stress.",
    ),
    "c2-u4-l3" to textGuide(
        hook = "خطاب تخرج ملهم مليء بالأساليب البلاغية: ثلاثيات، تضاد، وأسئلة بلاغية. استمع لإيقاعه.",
        predict = q("A graduation speech usually…", "inspires graduates about the future", "explains exam rules", "reports the news"),
        keyWords = listOf(
            w("threshold", "عتبة / بداية", "Today, you stand at a threshold."),
            w("sacrifice", "تضحية", "years of effort, of sacrifice"),
            w("Daunting", "مخيف / شاق", "Daunting as that may sound"),
            w("fail forward", "تتعلم من الفشل وتتقدم", "Fail, if you must — but fail forward."),
        ),
        strategy = LISTEN_GIST,
        gist = q("What is the speech's main message?", "Build the future and measure success by what you give", "Avoid all risks", "Money is the only measure of success"),
        reflect = "اكتب جملة ملهمة لنفسك باستخدام ثلاثية (ثلاث كلمات متوازية).",
        sample = "Learn every day, speak with courage, and never stop growing.",
    ),
)
