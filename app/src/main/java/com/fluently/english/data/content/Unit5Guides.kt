package com.fluently.english.data.content

/** Step-by-step teaching for the fifth unit of every level. */

internal val GuidesU5: Map<String, Guide> = mapOf(
    "a1-u5-l1" to guide(
        hook = "في العربية نقول «كتابي» و«سيارة أحمد» بإضافة ضمير أو اسم. الإنجليزية تفعل ذلك بكلمات صغيرة قبل الاسم (my, your…) أو بإضافة 's بعد اسم الشخص — كأنها «لاصقة» تقول: هذا يخصه!",
        goals = listOf("تقول «كتابي» و«بيتهم»", "تستخدم 's للملكية", "تقول «لديّ» بـ have got"),
        concepts = listOf(
            concept(
                "my, your, his, her…",
                "كل ضمير له «صفة ملكية» توضع قبل الاسم ولا تتغير مع المفرد أو الجمع: my book, my books.",
                table = table(
                    listOf("الضمير", "الملكية", "مثال"),
                    listOf("I", "my", "my phone"),
                    listOf("you", "your", "your bag"),
                    listOf("he / she", "his / her", "his car / her car"),
                    listOf("we / they", "our / their", "our house / their house"),
                ),
                examples = listOf("This is [my] room." means "هذه غرفتي.", "[Their] house is big." means "بيتهم كبير."),
                check = q("Sara loves ___ cat.", "her", "his", "their"),
            ),
            concept(
                "لاصقة الملكية 's",
                "مع أسماء الأشخاص نضيف 's: Ali's car = سيارة علي. ومع الجمع المنتهي بـ s نضيف الفاصلة فقط: my parents' house.",
                formula = "الشخص + 's + الشيء",
                examples = listOf("It's [Omar's] bike." means "إنها دراجة عمر.", "My [sister's] room is pink." means "غرفة أختي وردية."),
                check = q("This is the phone of my brother = This is my ___ phone.", "brother's", "brothers", "brother"),
            ),
            concept(
                "have got = لديّ",
                "have got طريقة بريطانية شائعة لقول «يملك». مع he/she تصبح has got. والاختصار شائع جداً: I've got, she's got.",
                formula = "الفاعل + have / has got + الشيء",
                examples = listOf("I[’ve got] two sisters." means "لديّ أختان.", "[Has] he [got] a car?" means "هل لديه سيارة؟"),
                check = q("She ___ got blue eyes.", "has", "have", "is"),
            ),
        ),
        mistakes = listOf(
            mistake("This is the car of Ali.", "This is Ali's car.", "مع الأشخاص نستخدم 's."),
            mistake("She have got a dog.", "She has got a dog.", "مع she نستخدم has."),
            mistake("Is this you bag?", "Is this your bag?", "you ضمير، والملكية your."),
        ),
        tip = "تجوّل في بيتك وأشر إلى الأشياء: my bed, my sister's phone, our kitchen — بصوت عالٍ!",
    ),
    "a2-u5-l1" to guide(
        hook = "تخيل الزمن كهرم من ثلاث طبقات: في القمة نقطة صغيرة دقيقة (at 7:00)، في الوسط يوم كامل (on Monday)، وفي القاعدة فترات طويلة (in July, in 2025). كلما كبرت الفترة نزلنا في الهرم!",
        goals = listOf("تختار بين in و on و at بثقة", "تتحدث عن المواعيد والتواريخ", "تعرف متى لا نستخدم حرف جر"),
        concepts = listOf(
            concept(
                "هرم الزمن",
                "at = نقطة دقيقة 🎯، on = يوم 📅، in = فترة طويلة 🗓️.",
                table = table(
                    listOf("الحرف", "الاستخدام", "أمثلة"),
                    listOf("at", "ساعة / لحظة", "at 5 p.m., at night, at noon"),
                    listOf("on", "يوم / تاريخ", "on Monday, on 3 May, on my birthday"),
                    listOf("in", "شهر / سنة / فصل / جزء من اليوم", "in June, in 2024, in winter, in the morning"),
                ),
                examples = listOf("The class starts [at] 9." means "يبدأ الدرس في التاسعة.", "I was born [in] 1998." means "وُلدت عام 1998."),
                check = q("We have a meeting ___ Tuesday.", "on", "in", "at"),
            ),
            concept(
                "بدون حرف جر",
                "عندما تسبق الكلمةَ next / last / this / every / tomorrow / yesterday، نحذف حرف الجر تماماً.",
                examples = listOf("See you [next Friday]!" means "أراك الجمعة القادمة!", "I go to the gym [every morning]." means "أذهب إلى النادي كل صباح."),
                check = q("I visited my uncle ___ week.", "last", "on last", "in last"),
            ),
        ),
        mistakes = listOf(
            mistake("in Monday", "on Monday", "الأيام مع on."),
            mistake("at the morning", "in the morning", "أجزاء اليوم مع in (ما عدا at night)."),
            mistake("on next week", "next week", "لا حرف جر قبل next."),
        ),
        tip = "ارسم الهرم في دفترك: at في القمة، on في الوسط، in في القاعدة. انظر إليه كلما ترددت.",
    ),
    "b1-u5-l1" to guide(
        hook = "لماذا نقول «I enjoy reading» وليس «I enjoy to read»؟ لا توجد قاعدة منطقية كاملة — الأمر يعتمد على الفعل الأول. لكن هناك «حيلة»: الأفعال التي تتحدث عن المستقبل والنوايا (want, hope, plan) تأخذ غالباً to، لأن to تشبه سهماً يشير إلى الأمام ➡️.",
        goals = listOf("تعرف الأفعال التي تأخذ ing", "تعرف الأفعال التي تأخذ to", "تستخدم ing بعد حروف الجر"),
        concepts = listOf(
            concept(
                "to = سهم نحو المستقبل",
                "want, hope, plan, decide, promise, agree, need, learn — كلها تتحدث عن شيء لم يحدث بعد، فتأخذ to + الفعل.",
                formula = "want / decide / hope + to + الفعل",
                examples = listOf("I [want to travel] the world." means "أريد أن أسافر حول العالم.", "She [promised to call]." means "وعدت أن تتصل."),
                check = q("They plan ___ a new house.", "to buy", "buying", "buy"),
            ),
            concept(
                "ing = نشاط تعيشه",
                "enjoy, finish, avoid, mind, keep, practise, suggest — تتحدث عن نشاط تعيشه أو تتجنبه، فتأخذ ing.",
                formula = "enjoy / finish / avoid + الفعل + ing",
                examples = listOf("I [enjoy cooking]." means "أستمتع بالطبخ.", "He [kept talking]." means "استمر في الكلام."),
                check = q("Have you finished ___ the book?", "reading", "to read", "read"),
            ),
            concept(
                "بعد حرف الجر: ing دائماً",
                "أي فعل يأتي بعد حرف جر (in, at, of, for, about, without) يجب أن يكون ing. هذه قاعدة بلا استثناء!",
                examples = listOf("She's good [at drawing]." means "إنها بارعة في الرسم.", "Thanks [for helping] me." means "شكراً لمساعدتي."),
                check = q("I'm tired of ___ for the bus.", "waiting", "to wait", "wait"),
            ),
        ),
        mistakes = listOf(
            mistake("I enjoy to swim.", "I enjoy swimming.", "enjoy + ing."),
            mistake("I want going home.", "I want to go home.", "want + to."),
            mistake("I'm interested in learn English.", "I'm interested in learning English.", "بعد حرف الجر ing."),
        ),
        tip = "احفظ جملتين «نموذجيتين» تحتويان على أشهر الأفعال: I enjoy reading but I want to travel. كررهما حتى تصبحا تلقائيتين.",
    ),
    "b2-u5-l1" to guide(
        hook = "تخيل أنك تنظر إلى المستقبل من خلال كاميرا: المستقبل المستمر «يلتقط صورة» لك وأنت تفعل شيئاً في لحظة معينة غداً، والمستقبل التام ينظر إلى الوراء من نقطة في المستقبل ويقول: «سأكون قد أنهيت هذا!»",
        goals = listOf("تصف ما ستفعله في لحظة معينة مستقبلاً", "تتحدث عما ستكون أنجزته قبل موعد", "تسأل عن خطط الآخرين بأدب"),
        concepts = listOf(
            concept(
                "المستقبل المستمر: صورة من الغد",
                "حدث سيكون جارياً في لحظة محددة في المستقبل.",
                formula = "will be + الفعل + ing",
                examples = listOf("This time tomorrow, I[’ll be flying] to Dubai." means "في مثل هذا الوقت غداً سأكون طائراً إلى دبي.", "[Will] you [be using] the car tonight?" means "هل ستستخدم السيارة الليلة؟ (سؤال مهذب)"),
                check = q("At 9 p.m. tonight, I ___ my favourite show.", "will be watching", "will have watched", "watch"),
            ),
            concept(
                "المستقبل التام: النظر إلى الوراء من المستقبل",
                "حدث سيكون منتهياً قبل نقطة معينة في المستقبل. كلمة السر: by.",
                formula = "will have + التصريف الثالث",
                examples = listOf("By June, I[’ll have finished] my degree." means "بحلول يونيو سأكون قد أنهيت دراستي.", "She[’ll have left] by the time you arrive." means "ستكون قد غادرت عندما تصل."),
                check = q("By 2030, they ___ the new airport.", "will have built", "will be building", "build"),
            ),
        ),
        mistakes = listOf(
            mistake("By tomorrow I will finish it.", "By tomorrow I will have finished it.", "مع by + وقت نستخدم المستقبل التام."),
            mistake("This time next week I will lie on the beach.", "This time next week I'll be lying on the beach.", "لحظة محددة مستقبلاً ← المستمر."),
        ),
        tip = "تخيل جدول أسبوعك القادم وقل: On Monday at 10, I'll be working. By Friday, I'll have finished my project.",
    ),
    "c1-u5-l1" to guide(
        hook = "الصحفيون والباحثون يتجنبون قول «أنا أعتقد». بدلاً من ذلك يقولون «It is believed that…» ليبدو الكلام موضوعياً. وفي الحياة اليومية نقول «I had my car repaired» عندما يصلحها غيرنا. هذان التركيبان يرفعان مستواك فوراً.",
        goals = listOf("تنقل الآراء بموضوعية", "تستخدم التركيب الشخصي is said to", "تقول إن أحداً قام بعمل لأجلك"),
        concepts = listOf(
            concept(
                "It is said that…",
                "تركيب غير شخصي لنقل ما يقوله الناس دون تحديد من قال.",
                formula = "It is + said / believed / reported + that + جملة",
                examples = listOf("[It is believed that] the city was founded by the Romans." means "يُعتقد أن المدينة أسسها الرومان."),
                check = q("It ___ that the minister will resign.", "is expected", "expects", "is expecting"),
            ),
            concept(
                "He is said to…",
                "تركيب شخصي يبدأ بالشخص نفسه. للحاضر: to + فعل، وللماضي: to have + V3.",
                table = table(
                    listOf("الزمن", "التركيب", "مثال"),
                    listOf("حاضر", "is said to + فعل", "He is said to be rich."),
                    listOf("ماضٍ", "is said to have + V3", "He is said to have lived here."),
                ),
                check = q("She is thought ___ the money last year.", "to have stolen", "to steal", "stealing"),
            ),
            concept(
                "have something done",
                "عندما يقوم شخص آخر (محترف) بالعمل لك. ويُستخدم أيضاً لحدث سيئ وقع لك.",
                formula = "have / get + الشيء + V3",
                examples = listOf("I [had my hair cut]." means "قصصت شعري (عند الحلاق).", "He [had his wallet stolen]." means "سُرقت محفظته."),
                check = q("We're having our kitchen ___.", "redesigned", "redesign", "redesigning"),
            ),
        ),
        mistakes = listOf(
            mistake("I cut my hair at the barber's.", "I had my hair cut at the barber's.", "الحلاق قصّه، لا أنت!"),
            mistake("He is said to leave yesterday.", "He is said to have left yesterday.", "للماضي: to have + V3."),
        ),
        tip = "اقرأ خبراً بالإنجليزية وابحث عن «It is reported» أو «is believed to» — ستجدها في كل مقال إخباري تقريباً.",
    ),
    "c2-u5-l1" to guide(
        hook = "المحامون والدبلوماسيون يحتاجون أدوات شرط أدق من if: «لولا» (but for)، «لنفترض» (supposing)، «وإلا» (otherwise)، «بشرط» (provided). هذه الأدوات تجعل حجتك محكمة ودقيقة.",
        goals = listOf("تستخدم but for بمعنى لولا", "تتخيل مواقف بـ supposing", "تضع شروطاً دقيقة"),
        concepts = listOf(
            concept(
                "But for = لولا",
                "يأتي بعدها اسم (وليس جملة)، وتعادل If it weren't / hadn't been for.",
                formula = "But for + اسم ، would (have) + فعل",
                examples = listOf("[But for] your advice, I'd have made a mistake." means "لولا نصيحتك لارتكبت خطأً."),
                check = q("___ the traffic, we would have arrived on time.", "But for", "Unless", "Otherwise"),
            ),
            concept(
                "Supposing و otherwise",
                "Supposing لتخيّل موقف افتراضي، وotherwise تعني «وإلا» وتأتي بعد جملة أمر أو نصيحة.",
                examples = listOf("[Supposing] it rains, what shall we do?" means "لنفترض أنها أمطرت، ماذا نفعل؟", "Take a map; [otherwise], you'll get lost." means "خذ خريطة، وإلا ستضيع."),
                check = q("Book early; ___, the tickets will sell out.", "otherwise", "supposing", "but for"),
            ),
            concept(
                "شروط دقيقة",
                "provided (that) / as long as / on condition that = بشرط أن. أقوى وأدق من if.",
                examples = listOf("You can go [as long as] you're back by ten." means "يمكنك الذهاب بشرط أن تعود قبل العاشرة."),
                check = q("I'll lend you the money ___ you pay me back next week.", "provided", "otherwise", "but for"),
            ),
        ),
        mistakes = listOf(
            mistake("But for you helped me, …", "But for your help, …", "بعد But for يأتي اسم."),
            mistake("Hurry, unless you'll be late.", "Hurry; otherwise, you'll be late.", "otherwise = وإلا."),
        ),
        tip = "عند كتابة أي حجة، جرّب استبدال if بأداة أدق: provided that للشروط، but for لـ «لولا».",
    ),
)

internal val VocabGuidesU5: Map<String, VocabGuide> = mapOf(
    "a1-u5-l2" to VocabGuide(
        hook = "تجوّل معي في بيت إنجليزي! كل غرفة لها اسم، وكل قطعة أثاث لها كلمة. ستستخدم هذه الكلمات عندما تصف بيتك أو تبحث عن شقة للإيجار.",
        hints = mapOf(
            "kitchen" to "تشبه «كتشن» — من نفس أصل cook (يطبخ).",
            "bedroom" to "bed (سرير) + room (غرفة) = غرفة السرير.",
            "bathroom" to "bath (استحمام) + room. وtoilet للمرحاض فقط.",
            "living room" to "living (العيش) + room = الغرفة التي «نعيش» فيها معاً.",
            "sofa" to "نفس الكلمة بالعربية «صوفا» — أصلها عربي «صُفّة»!",
            "table" to "تشبه «طاولة» في نهايتها (ble = لة).",
            "window" to "wind (رياح) + ow (عين): «عين الرياح» في الجدار.",
            "stairs" to "دائماً بالجمع: the stairs. وupstairs = في الطابق العلوي.",
        ),
        groups = listOf(
            group("الغرف", "There are three bedrooms and one bathroom.", "kitchen", "bedroom", "bathroom", "living room"),
            group("الأثاث وأجزاء البيت", "The sofa is in the living room, next to the window.", "sofa", "table", "window", "stairs"),
        ),
        story = "Welcome to my house! On the left is the [kitchen] — I cook here every day. On the right is the [living room], with a big [sofa] and a small [table]. Go up the [stairs] and you'll find my [bedroom]. It has a big [window]. The [bathroom] is next to it.",
        storyAr = "مرحباً بك في بيتي! على اليسار المطبخ — أطبخ هنا كل يوم. على اليمين غرفة المعيشة، فيها أريكة كبيرة وطاولة صغيرة. اصعد الدرج وستجد غرفة نومي. فيها نافذة كبيرة. والحمّام بجانبها.",
        checks = listOf(
            q("We sleep in the ___.", "bedroom", "kitchen", "living room"),
            q("I cook dinner in the ___.", "kitchen", "bathroom", "bedroom"),
        ),
        mistakes = listOf(
            mistake("I go up the stair.", "I go up the stairs.", "stairs بالجمع دائماً."),
        ),
        tip = "ألصق ورقة صغيرة باسم كل غرفة وقطعة أثاث بالإنجليزية في بيتك لأسبوع واحد!",
    ),
    "a2-u5-l2" to VocabGuide(
        hook = "الإنجليز يتحدثون عن الطقس أكثر من أي موضوع آخر — إنه طريقتهم لبدء المحادثة مع الغرباء! «Lovely weather today, isn't it?» هذه الكلمات تفتح لك أي حوار.",
        hints = mapOf(
            "sunny" to "sun (شمس) + ny = مشمس. القاعدة: sun → sunny, cloud → cloudy, wind → windy!",
            "cloudy" to "cloud (غيمة) + y = غائم.",
            "windy" to "wind (رياح) + y = فيه رياح.",
            "storm" to "تذكر «ستورم» — عاصفة قوية مع رعد ومطر.",
            "temperature" to "تُختصر temp. «It's 30 degrees» = 30 درجة.",
            "forecast" to "fore (قبل) + cast (يرمي): «يرمي» التوقع قبل أن يحدث.",
            "spring" to "spring تعني أيضاً «نابض» — الطبيعة «تقفز» للحياة في الربيع.",
            "autumn" to "بالأمريكية fall لأن الأوراق «تسقط».",
        ),
        groups = listOf(
            group("وصف الطقس", "اسم + y = صفة: sun → sunny، cloud → cloudy، wind → windy.", "sunny", "cloudy", "windy", "storm"),
            group("الحديث عن الطقس", "What's the temperature? What's the forecast for tomorrow?", "temperature", "forecast"),
            group("الفصول", "in spring / in summer / in autumn / in winter", "spring", "autumn"),
        ),
        story = "I love [spring] — it's [sunny] and warm. But today the [forecast] says it will be [cloudy] and [windy]. The [temperature] will drop to 12 degrees, and there might be a [storm] tonight. It feels like [autumn] already!",
        storyAr = "أحب الربيع — إنه مشمس ودافئ. لكن توقعات الطقس اليوم تقول إنه سيكون غائماً وعاصفاً. ستنخفض درجة الحرارة إلى 12 درجة، وقد تحدث عاصفة الليلة. يبدو وكأننا في الخريف بالفعل!",
        checks = listOf(
            q("Let's check the weather ___ before our trip.", "forecast", "temperature", "storm"),
            q("Leaves fall from the trees in ___.", "autumn", "spring", "sunny"),
        ),
        mistakes = listOf(
            mistake("Today is sun.", "Today it's sunny.", "نستخدم الصفة sunny."),
            mistake("The weather is very wind.", "It's very windy.", "الصفة windy."),
        ),
        tip = "كل صباح انظر من النافذة وقل جملة: It's sunny and warm today. — عادة صغيرة تبني لغة كبيرة.",
    ),
    "b1-u5-l2" to VocabGuide(
        hook = "من المدرسة إلى الجامعة والمنح الدراسية — هذه كلمات رحلتك التعليمية. ستحتاجها إذا فكرت في الدراسة بالخارج أو تقديم طلب لمنحة.",
        hints = mapOf(
            "degree" to "تعني أيضاً «درجة» حرارة! a bachelor's / master's degree.",
            "lecture" to "من اللاتينية «قراءة» — الأستاذ «يقرأ» على الطلاب. والمحاضر lecturer.",
            "assignment" to "من assign (يكلّف): المهمة التي كُلّفت بها.",
            "revise" to "re (مرة أخرى) + vise (يرى): ترى الدروس مرة أخرى قبل الامتحان.",
            "scholarship" to "scholar (عالِم/دارس) + ship = منحة للدارسين.",
            "fail" to "عكس pass. fail an exam = يرسب في امتحان.",
            "graduate" to "من grade (درجة): تصعد آخر درجة في سلم التعليم.",
            "subject" to "مادة دراسية: maths, history, science…",
        ),
        groups = listOf(
            group("في الجامعة", "attend a lecture / submit an assignment / study a subject", "lecture", "assignment", "subject"),
            group("الامتحانات", "revise for an exam → pass or fail", "revise", "fail"),
            group("الإنجازات", "get a degree / win a scholarship / graduate from university", "degree", "scholarship", "graduate"),
        ),
        story = "Lina won a [scholarship] to study abroad. Her favourite [subject] was biology. Every morning she attended a [lecture], and every evening she worked on an [assignment]. Before exams, she [revise]d for hours because she was afraid to [fail]. Last summer, she [graduate]d with a [degree] in biology.",
        storyAr = "فازت لينا بمنحة للدراسة في الخارج. كانت مادتها المفضلة الأحياء. كل صباح كانت تحضر محاضرة، وكل مساء تعمل على واجب. قبل الامتحانات كانت تراجع لساعات لأنها كانت تخاف أن ترسب. الصيف الماضي تخرجت بشهادة في الأحياء.",
        checks = listOf(
            q("I have to ___ for my history exam tomorrow.", "revise", "graduate", "fail"),
            q("She didn't pay for university because she got a ___.", "scholarship", "lecture", "subject"),
        ),
        mistakes = listOf(
            mistake("I succeeded the exam.", "I passed the exam.", "النجاح في الامتحان = pass."),
            mistake("I graduated the university.", "I graduated from university.", "graduate from."),
        ),
        tip = "اكتب «سيرة دراسية» قصيرة عن نفسك بهذه الكلمات: I graduated from… My favourite subject was…",
    ),
    "b2-u5-l2" to VocabGuide(
        hook = "أفلام الجريمة والأخبار مليئة بهذه الكلمات. فهمها يساعدك على متابعة الأخبار والمسلسلات الإنجليزية، ويعطيك مفردات قوية لمواضيع الكتابة في الامتحانات.",
        hints = mapOf(
            "witness" to "من wit (معرفة): من «يعرف» ما حدث لأنه رآه.",
            "suspect" to "نطق مختلف: SUS-pect (اسم: مشتبه به) و sus-PECT (فعل: يشتبه).",
            "evidence" to "من evident (واضح): ما يجعل الحقيقة واضحة. غير معدودة: a piece of evidence.",
            "arrest" to "تخيل الشرطة «توقف» (rest) المجرم.",
            "guilty" to "عكسها innocent (بريء). feel guilty = يشعر بالذنب.",
            "sentence" to "تعني أيضاً «جملة»! والقاضي «ينطق» جملة الحكم.",
            "burglary" to "burglar = لص المنازل. robbery = سرقة بالقوة، theft = سرقة عامة.",
            "prevent" to "pre (قبل) + vent (يأتي): توقف الشيء قبل أن يأتي ← تمنعه.",
        ),
        groups = listOf(
            group("الأشخاص", "The witness identified the suspect.", "witness", "suspect"),
            group("الجريمة والتحقيق", "a burglary → evidence → an arrest", "burglary", "evidence", "arrest"),
            group("المحكمة والوقاية", "found guilty / a long sentence / prevent crime", "guilty", "sentence", "prevent"),
        ),
        story = "After a [burglary] in our street, a [witness] described a man in a red cap. Two days later, police [arrest]ed a [suspect]. They found strong [evidence] at his home. In court, he was found [guilty] and received a three-year [sentence]. Now neighbours have installed cameras to [prevent] more crime.",
        storyAr = "بعد سطو في شارعنا، وصف شاهد رجلاً يرتدي قبعة حمراء. بعد يومين اعتقلت الشرطة مشتبهاً به. وجدوا أدلة قوية في منزله. في المحكمة ثبتت إدانته وحُكم عليه بالسجن ثلاث سنوات. الآن ركّب الجيران كاميرات لمنع المزيد من الجرائم.",
        checks = listOf(
            q("The ___ saw everything and told the police.", "witness", "suspect", "sentence"),
            q("There wasn't enough ___ to prove he did it.", "evidence", "burglary", "witness"),
        ),
        mistakes = listOf(
            mistake("The police found many evidences.", "The police found a lot of evidence.", "evidence غير معدودة."),
        ),
        tip = "شاهد حلقة من مسلسل بوليسي بالإنجليزية مع ترجمة إنجليزية — ستسمع هذه الكلمات عشرات المرات.",
    ),
    "c1-u5-l2" to VocabGuide(
        hook = "بدل وصف الناس بـ nice أو bad، يستخدم المتحدث المتقدم كلمات دقيقة تصف الشخصية بعمق. هذه الكلمات مفيدة في المقابلات، والكتابة الإبداعية، ووصف نفسك.",
        hints = mapOf(
            "resilient" to "من اللاتينية «يرتد»: مثل الكرة المطاطية — تسقط وترتد للأعلى.",
            "ambitious" to "ambition = طموح. ambitious = لديه أهداف كبيرة.",
            "outgoing" to "out (خارج) + going: يحب الخروج والناس ← اجتماعي.",
            "reserved" to "reserve = يحجز/يحتفظ: يحتفظ بمشاعره لنفسه.",
            "stubborn" to "عنيد مثل البغل — stubborn as a mule.",
            "empathetic" to "em (داخل) + pathos (شعور): يشعر بما يشعر به غيره.",
            "self-confident" to "self (نفس) + confident (واثق).",
            "impulsive" to "impulse = دافع مفاجئ: يتصرف بدافع اللحظة دون تفكير.",
        ),
        groups = listOf(
            group("صفات إيجابية", "She's resilient, ambitious and self-confident.", "resilient", "ambitious", "self-confident", "empathetic"),
            group("الانطوائي والمنفتح", "outgoing ↔ reserved", "outgoing", "reserved"),
            group("صفات قد تكون سلبية", "Don't be so stubborn! / He's too impulsive with money.", "stubborn", "impulsive"),
        ),
        story = "My two brothers couldn't be more different. Karim is [outgoing], [ambitious] and very [self-confident] — he started his own business at 22. He's also a bit [impulsive] and [stubborn]. Sami is quieter and more [reserved], but he's incredibly [empathetic]. When Karim's business failed, Sami supported him, and Karim proved how [resilient] he was.",
        storyAr = "أخواي لا يمكن أن يكونا أكثر اختلافاً. كريم اجتماعي وطموح وواثق جداً بنفسه — بدأ عمله الخاص في الثانية والعشرين. وهو أيضاً متهور وعنيد قليلاً. سامي أهدأ وأكثر تحفظاً، لكنه متعاطف جداً. عندما فشل عمل كريم دعمه سامي، وأثبت كريم كم هو قادر على النهوض.",
        checks = listOf(
            q("She never changes her mind, even when she's wrong. She's ___.", "stubborn", "empathetic", "outgoing"),
            q("He understands how others feel. He's very ___.", "empathetic", "impulsive", "reserved"),
        ),
        mistakes = listOf(
            mistake("He's very sympathetic person, he loves parties.", "He's very outgoing; he loves parties.", "sympathetic = متعاطف، outgoing = اجتماعي."),
        ),
        tip = "صف نفسك بثلاث صفات من الدرس مع مثال لكل صفة — سؤال شائع جداً في المقابلات!",
    ),
    "c2-u5-l2" to VocabGuide(
        hook = "الناطقون لا يقولون «very cold» في الكتابة الراقية، بل «bitterly cold». هذه «المتلازمات» (ظرف + صفة) ثابتة، واستخدامها الصحيح يجعل إنجليزيتك تبدو طبيعية وأنيقة.",
        hints = mapOf(
            "bitterly cold" to "bitter = مرّ: برد «مرّ» يؤلم.",
            "utterly ridiculous" to "utterly = تماماً — تأتي غالباً مع الصفات السلبية.",
            "highly unlikely" to "highly تأتي مع likely/unlikely/recommended/successful.",
            "deeply concerned" to "deeply مع المشاعر: deeply moved, deeply concerned.",
            "fully aware" to "fully = بالكامل: fully aware, fully booked.",
            "widely regarded" to "widely = على نطاق واسع: widely known, widely accepted.",
            "strongly oppose" to "strongly مع الآراء: strongly believe, strongly oppose.",
            "painfully slow" to "painfully = بشكل مؤلم: painfully shy, painfully slow.",
        ),
        groups = listOf(
            group("الآراء والمواقف", "strongly oppose / fully aware / deeply concerned", "strongly oppose", "fully aware", "deeply concerned"),
            group("الحكم والتقييم", "highly unlikely / utterly ridiculous / widely regarded", "highly unlikely", "utterly ridiculous", "widely regarded"),
            group("الوصف الحسي", "bitterly cold / painfully slow", "bitterly cold", "painfully slow"),
        ),
        story = "It was a [bitterly cold] morning when residents gathered to [strongly oppose] the new motorway. The mayor, [widely regarded] as a reasonable man, said he was [fully aware] of their worries and [deeply concerned] about pollution. Yet progress on alternatives had been [painfully slow]. One resident called the plan [utterly ridiculous]. Most agreed it was [highly unlikely] to be approved.",
        storyAr = "كان صباحاً قارس البرد عندما تجمع السكان ليعارضوا بشدة الطريق السريع الجديد. قال رئيس البلدية، الذي يُعتبر على نطاق واسع رجلاً عاقلاً، إنه مدرك تماماً لمخاوفهم وقلق للغاية بشأن التلوث. لكن التقدم في البدائل كان بطيئاً بشكل مؤلم. وصف أحد السكان الخطة بأنها سخيفة تماماً. واتفق معظمهم على أن الموافقة عليها مستبعدة جداً.",
        checks = listOf(
            q("Most citizens ___ the new tax.", "strongly oppose", "highly oppose", "deeply oppose"),
            q("It's ___ that he'll win — he's never won before.", "highly unlikely", "bitterly unlikely", "painfully unlikely"),
        ),
        mistakes = listOf(
            mistake("It was strongly cold.", "It was bitterly cold.", "التلازم الصحيح مع cold هو bitterly."),
            mistake("I'm highly aware of it.", "I'm fully aware of it.", "التلازم مع aware هو fully."),
        ),
        tip = "عندما تقرأ مقالاً، لاحظ الظرف الذي يسبق كل صفة واكتب التلازمات التي تتكرر في دفتر خاص.",
    ),
)

internal val TextGuidesU5: Map<String, TextGuide> = mapOf(
    "a1-u5-l3" to TextGuide(
        hook = "هدى انتقلت إلى شقة جديدة وتصفها لنا. ستجد كلمات الغرف والأثاث وتركيب has got.",
        predict = q("When people describe their home, they talk about…", "rooms and furniture", "their exam results", "the weather in China"),
        keyWords = listOf(
            w("floor", "طابق", "It's on the third floor."),
            w("bright", "مضيء", "It's very bright."),
            w("favourite", "مفضّل", "The living room is my favourite room."),
            w("neighbours", "جيران", "Our neighbours are friendly."),
        ),
        strategy = "البحث عن معلومة (Scanning): الأسئلة تسأل عن أرقام وألوان وأماكن — ابحث بعينك عنها مباشرة في النص.",
        gist = q("What is the text about?", "Huda describes her new flat", "Huda wants to sell her car", "Huda's job at a school"),
        reflect = "صف بيتك أو غرفتك المفضلة في ثلاث جمل.",
        reflectSample = "My flat has got two bedrooms. My favourite room is the kitchen. It's small but bright.",
    ),
    "a2-u5-l3" to TextGuide(
        hook = "نشرة جوية لعطلة نهاية الأسبوع. ركز على: أي يوم؟ أي مكان؟ وما درجة الحرارة؟",
        predict = q("What information does a weather forecast give?", "Temperature, rain and wind", "Football scores", "Train times"),
        keyWords = listOf(
            w("north", "الشمال", "it will be cloudy in the north"),
            w("degrees", "درجات", "temperatures of around 28 degrees"),
            w("coast", "الساحل", "especially near the coast"),
            w("fall", "تنخفض", "Temperatures will fall to about 18 degrees."),
        ),
        strategy = "في الاستماع الأول ركّز على «من؟ أين؟ ماذا؟» فقط. وفي الاستماع الثاني انتبه للأرقام والأسعار والأوقات — اكتبها على ورقة إن أمكن.",
        gist = q("What is the main purpose of the recording?", "To give the weekend weather forecast", "To advertise a beach hotel", "To report a car accident"),
        reflect = "صف الطقس اليوم في مدينتك كأنك مقدم نشرة جوية.",
        reflectSample = "Today in Amman it's sunny and warm, with temperatures of around 30 degrees.",
    ),
    "b1-u5-l3" to TextGuide(
        hook = "مقال يقارن بين التعلم عن بعد والتعلم في الصف. لاحظ أدوات المقارنة: However و on the other hand.",
        predict = q("An article comparing online and classroom learning will probably…", "discuss advantages and disadvantages", "only praise online learning", "describe a school building"),
        keyWords = listOf(
            w("flexible", "مرن", "Online learning is flexible."),
            w("self-discipline", "الانضباط الذاتي", "it requires a lot of self-discipline"),
            w("isolated", "منعزل", "some students feel isolated"),
            w("survey", "استطلاع", "A recent survey found that…"),
        ),
        strategy = "تتبع المقارنة: ضع عمودين في ذهنك — «إيجابيات» و«سلبيات» لكل طريقة. كلمات مثل However و on the other hand تنقلك من عمود لآخر.",
        gist = q("What does the writer conclude?", "The best method depends on the learner", "Online learning is always best", "Classrooms will disappear"),
        reflect = "أي طريقة تفضل للتعلم ولماذا؟",
        reflectSample = "I prefer blended learning because I enjoy the flexibility of online classes but I need my teacher's support.",
    ),
    "b2-u5-l3" to TextGuide(
        hook = "نداء من الشرطة في الراديو بعد سرقة متجر مجوهرات. استمع للتفاصيل: الوقت، المسروقات، ووصف المشتبه بهم.",
        predict = q("What does a police appeal usually ask for?", "Information from witnesses", "Money from listeners", "New police officers"),
        keyWords = listOf(
            w("appealing", "يناشد", "Police are appealing for witnesses"),
            w("jewellery", "مجوهرات", "a burglary at a jewellery shop"),
            w("van", "شاحنة صغيرة", "leaving in a small white van"),
            w("injured", "مصاب", "No one was injured."),
        ),
        strategy = "في الاستماع الأول ركّز على «من؟ أين؟ ماذا؟» فقط. وفي الاستماع الثاني انتبه للأرقام والأسعار والأوقات — اكتبها على ورقة إن أمكن.",
        gist = q("What is the purpose of the announcement?", "To ask the public for information about a crime", "To advertise a jewellery shop", "To report a traffic jam"),
        reflect = "صف شخصاً (خيالياً) كأنك شاهد تتحدث مع الشرطة.",
        reflectSample = "He was tall, in his twenties, with short dark hair, and he was wearing a grey hoodie.",
    ),
    "c1-u5-l3" to TextGuide(
        hook = "ما سر السعادة؟ دراسة من جامعة هارفارد استمرت أكثر من 80 عاماً لديها إجابة مفاجئة. لاحظ كيف يقدم الكاتب الأدلة.",
        predict = q("According to most research, what matters most for happiness?", "Good relationships", "Being very rich", "Being famous"),
        keyWords = listOf(
            w("predictor", "مؤشر تنبؤي", "the strongest predictor of long-term wellbeing"),
            w("wellbeing", "الرفاهية / جودة الحياة", "long-term wellbeing"),
            w("Loneliness", "الوحدة", "Loneliness, by contrast, was found to be as damaging"),
            w("depth", "عمق", "the depth of those connections"),
        ),
        strategy = "في المقالات العلمية ابحث عن «النتيجة المركزية» (central finding) — غالباً تأتي بعد كلمات مثل found أو concluded. ثم لاحظ الأدلة التي تدعمها.",
        gist = q("What is the main finding of the study?", "Relationships are the key to long-term wellbeing", "Money guarantees happiness", "Smoking causes loneliness"),
        reflect = "من الشخص الذي يجعل حياتك أسعد؟ ولماذا؟",
        reflectSample = "My best friend makes my life happier because she always listens without judging me.",
    ),
    "c2-u5-l3" to TextGuide(
        hook = "محاضرة جامعية تتحدى فكرة شائعة: هل الطرق الجديدة تقلل الزحام؟ المحاضر يستخدم مثال سيول — تتبع حجته.",
        predict = q("Do more roads usually reduce traffic, according to urban research?", "No, they often increase it", "Yes, always", "Roads have no effect"),
        keyWords = listOf(
            w("assumption", "افتراض", "I'd like to challenge an assumption"),
            w("induced demand", "الطلب المستحث", "a phenomenon known as 'induced demand'"),
            w("demolished", "هدم", "the city demolished an elevated highway"),
            w("bold", "جريء", "But for such bold decisions"),
        ),
        strategy = "المحاضرات تتبع هيكلاً: فكرة شائعة ← حجة مضادة ← مثال ← خلاصة. استمع لكلمات التحول مثل In fact و Conversely و Instead.",
        gist = q("What is the lecturer's main argument?", "Cities should prioritise people over cars", "Seoul needs more highways", "Traffic can never be reduced"),
        reflect = "ما التغيير الذي تقترحه لتحسين مدينتك؟ استخدم أداة شرط متقدمة.",
        reflectSample = "Provided that the city invests in public transport, many streets could become pedestrian zones.",
    ),
)
