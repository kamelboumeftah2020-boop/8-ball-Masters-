package com.fluently.english.data.content

/** Step-by-step explanations for the B1 and B2 grammar lessons. */
internal val GuidesB: Map<String, Guide> = mapOf(

    // ---------------- B1 ----------------

    "b1-u1-l1" to guide(
        hook = "المضارع التام هو الزمن الذي يحيّر العرب أكثر من أي زمن آخر، لأنه لا يوجد في العربية! تخيله كجسر يربط الماضي بالحاضر: حدث في الماضي، لكن ما يهمنا هو أثره أو علاقته باللحظة الحالية.",
        goals = listOf(
            "تتحدث عن تجاربك في الحياة",
            "تستخدم for و since بشكل صحيح",
            "تعرف متى تستخدمه بدل الماضي البسيط",
        ),
        concepts = listOf(
            concept(
                "التركيب: have + التصريف الثالث",
                "التصريف الثالث (past participle) هو العمود الثالث في جدول الأفعال الشاذة: go – went – gone. مع he / she / it نستخدم has.",
                formula = "الفاعل + have / has + التصريف الثالث",
                table = table(
                    listOf("الفعل", "الماضي", "التصريف الثالث"),
                    listOf("see", "saw", "seen"),
                    listOf("eat", "ate", "eaten"),
                    listOf("be", "was / were", "been"),
                    listOf("visit", "visited", "visited"),
                ),
                check = q("She has ___ that film three times.", "seen", "saw", "see"),
            ),
            concept(
                "تجارب الحياة: «هل سبق لك…؟»",
                "عندما يهمنا «هل حدث في حياتك أم لا؟» وليس «متى؟»، نستخدم المضارع التام مع ever (في السؤال) وnever (في النفي).",
                examples = listOf(
                    "[Have] you [ever been] to Japan?" means "هل سبق أن زرت اليابان؟",
                    "I've [never eaten] sushi." means "لم آكل السوشي أبداً.",
                ),
                check = q("Have you ever ___ a horse?", "ridden", "rode", "ride"),
            ),
            concept(
                "for و since: منذ متى؟",
                "لحدث بدأ في الماضي ومازال مستمراً. for + المدة (كم من الوقت؟). since + نقطة البداية (منذ متى بالضبط؟).",
                table = table(
                    listOf("for + مدة ⏳", "since + نقطة بداية 📍"),
                    listOf("for three years", "since 2020"),
                    listOf("for two hours", "since Monday"),
                    listOf("for a long time", "since I was a child"),
                ),
                examples = listOf(
                    "I've lived here [for] ten years." means "أسكن هنا منذ عشر سنوات.",
                    "She has worked here [since] January." means "تعمل هنا منذ يناير.",
                ),
                check = q("We've known each other ___ 2015.", "since", "for", "from"),
            ),
            concept(
                "المضارع التام أم الماضي البسيط؟",
                "القاعدة الذهبية: إذا ذكرت وقتاً منتهياً محدداً (yesterday, last year, in 2010) فاستخدم الماضي البسيط. إذا لم تذكر الوقت أو كان مفتوحاً حتى الآن فاستخدم المضارع التام.",
                table = table(
                    listOf("المضارع التام", "الماضي البسيط"),
                    listOf("I've lost my keys.", "I lost my keys yesterday."),
                    listOf("She has visited Rome.", "She visited Rome in 2019."),
                ),
                check = q("I ___ him last week.", "saw", "have seen", "has seen"),
            ),
        ),
        mistakes = listOf(
            mistake("I have seen him yesterday.", "I saw him yesterday.", "yesterday وقت منتهٍ ← ماضٍ بسيط."),
            mistake("I live here since 2010.", "I've lived here since 2010.", "حدث مستمر حتى الآن ← مضارع تام."),
            mistake("for 2015", "since 2015", "2015 نقطة بداية وليست مدة."),
        ),
        tip = "اسأل سؤالين: «هل ذكرت متى بالضبط في الماضي؟» إذا نعم ← ماضٍ بسيط. «هل الأمر مازال مهماً أو مستمراً الآن؟» إذا نعم ← مضارع تام.",
    ),

    "b1-u2-l1" to guide(
        hook = "«إذا سخّنت الماء يغلي» و«إذا أمطرت سأبقى في البيت». الأولى حقيقة علمية دائمة، والثانية احتمال حقيقي في المستقبل. الإنجليزية تعطي كل واحدة منهما تركيباً خاصاً.",
        goals = listOf(
            "تعبر عن الحقائق العامة بالشرطية الصفرية",
            "تتحدث عن احتمالات المستقبل بالشرطية الأولى",
            "تستخدم unless",
        ),
        concepts = listOf(
            concept(
                "الشرطية الصفرية: الحقائق الدائمة",
                "عندما تكون النتيجة صحيحة دائماً (قوانين الطبيعة، العادات الثابتة)، نستخدم المضارع البسيط في الجزأين. if هنا تعني تقريباً «كلما».",
                formula = "If + مضارع بسيط ، مضارع بسيط",
                examples = listOf(
                    "If you [heat] ice, it [melts]." means "إذا سخنت الثلج يذوب.",
                    "If I [drink] coffee at night, I [can't] sleep." means "إذا شربت القهوة ليلاً لا أستطيع النوم.",
                ),
                check = q("If you mix blue and yellow, you ___ green.", "get", "will get", "got"),
            ),
            concept(
                "الشرطية الأولى: احتمال حقيقي",
                "لموقف ممكن الحدوث في المستقبل. الجزء الأول (الشرط) بالمضارع البسيط، والنتيجة بـ will. انتبه: لا نضع will بعد if أبداً!",
                formula = "If + مضارع بسيط ، will + الفعل",
                examples = listOf(
                    "If it [rains], I[’ll stay] at home." means "إذا أمطرت سأبقى في البيت.",
                    "If you [study], you[’ll pass]." means "إذا درست ستنجح.",
                ),
                check = q("If she ___ early, she'll catch the train.", "leaves", "will leave", "left"),
            ),
            concept(
                "unless = if not",
                "unless تعني «ما لم» أو «إلا إذا»، وهي تحمل النفي بداخلها، فلا نضيف not بعدها.",
                examples = listOf(
                    "[Unless] you hurry, you'll be late." means "ما لم تسرع ستتأخر.",
                    "I won't go [unless] you come too." means "لن أذهب إلا إذا أتيت أنت أيضاً.",
                ),
                check = q("___ we leave now, we'll miss the plane.", "Unless", "If", "When"),
            ),
        ),
        mistakes = listOf(
            mistake("If it will rain, I'll stay.", "If it rains, I'll stay.", "لا will بعد if."),
            mistake("Unless you don't study, you'll fail.", "Unless you study, you'll fail.", "unless فيها نفي مسبقاً."),
        ),
        tip = "تخيل if «باب» لا تدخله will: ما بعد if يبقى في المضارع، وwill تنتظر في الجزء الثاني.",
    ),

    "b1-u3-l1" to guide(
        hook = "كيف تنصح صديقاً؟ كيف تقول «ممنوع» أو «ليس ضرورياً» أو «ربما»؟ الأفعال الناقصة (modal verbs) هي أدوات صغيرة تغير «نبرة» الجملة كلها.",
        goals = listOf(
            "تنصح باستخدام should",
            "تفرق بين mustn't و don't have to",
            "تعبر عن الاحتمال بـ might",
        ),
        concepts = listOf(
            concept(
                "مقياس القوة",
                "تخيل مقياساً من «نصيحة لطيفة» إلى «إلزام قوي». كل فعل ناقص له مكانه على المقياس. وكلها يأتي بعدها الفعل الأصلي بدون to (ما عدا have to).",
                table = table(
                    listOf("الفعل", "المعنى", "مثال"),
                    listOf("should", "نصيحة 💡", "You should rest."),
                    listOf("have to", "إلزام خارجي 📋", "I have to wear a uniform."),
                    listOf("must", "إلزام قوي ❗", "You must stop here."),
                    listOf("might", "احتمال 🤔", "It might rain."),
                ),
                check = q("You look tired. You ___ go to bed.", "should", "might", "have"),
            ),
            concept(
                "الفخ الأكبر: mustn't ≠ don't have to",
                "رغم التشابه، معناهما مختلف تماماً! mustn't تعني «ممنوع» 🚫. don't have to تعني «ليس ضرورياً، أنت حر» 🙂.",
                table = table(
                    listOf("mustn't 🚫 ممنوع", "don't have to 🙂 غير ضروري"),
                    listOf("You mustn't smoke here.", "You don't have to come."),
                    listOf("You mustn't park here.", "You don't have to pay. It's free."),
                ),
                check = q("It's Saturday, so I ___ get up early.", "don't have to", "mustn't", "can't"),
            ),
            concept(
                "might: ربما",
                "عندما لا تكون متأكداً، استخدم might أو may. نسبة الاحتمال حوالي 50%.",
                examples = listOf(
                    "I [might] go to the party." means "ربما أذهب إلى الحفلة.",
                    "Take a coat. It [might] be cold." means "خذ معطفاً. قد يكون الجو بارداً.",
                ),
                check = q("I'm not sure. She ___ be at home.", "might", "must", "should"),
            ),
        ),
        mistakes = listOf(
            mistake("You should to rest.", "You should rest.", "لا to بعد should."),
            mistake("She musts go.", "She must go.", "الأفعال الناقصة لا تأخذ s."),
            mistake("You mustn't pay, it's free.", "You don't have to pay, it's free.", "المقصود «غير ضروري» وليس «ممنوع»."),
        ),
        tip = "تخيل لافتات الطريق: mustn't = لافتة حمراء ممنوع 🚫، should = لافتة زرقاء إرشادية، don't have to = لا توجد لافتة، أنت حر!",
    ),

    "b1-u4-l1" to guide(
        hook = "كل قصة جيدة لها «خلفية» و«حدث». «كنت أمشي في الشارع (خلفية)… عندما رأيت صديقي (حدث)». الماضي المستمر يرسم الخلفية، والماضي البسيط يرسم الحدث. وused to تحكي عن عادات قديمة انتهت.",
        goals = listOf(
            "ترسم خلفية القصة بالماضي المستمر",
            "تربط بين حدثين بـ when و while",
            "تتحدث عن عادات الماضي بـ used to",
        ),
        concepts = listOf(
            concept(
                "الماضي المستمر: فيلم في الخلفية",
                "حدث كان مستمراً في لحظة معينة في الماضي، كأنك ضغطت «إيقاف مؤقت» على فيلم قديم.",
                formula = "الفاعل + was / were + الفعل + ing",
                examples = listOf(
                    "At 8 p.m. I [was having] dinner." means "في الثامنة مساءً كنت أتناول العشاء.",
                    "They [were sleeping] when I called." means "كانوا نائمين عندما اتصلت.",
                ),
                check = q("What ___ you doing at midnight?", "were", "was", "did"),
            ),
            concept(
                "الحدث الطويل يقطعه حدث قصير",
                "الحدث الطويل (الخلفية) ← ماضٍ مستمر مع while. الحدث القصير المفاجئ ← ماضٍ بسيط مع when.",
                formula = "while + ماضٍ مستمر ، ماضٍ بسيط",
                examples = listOf(
                    "I [was walking] home [when] it [started] to rain." means "كنت أمشي إلى البيت عندما بدأ المطر.",
                    "[While] she [was cooking], the phone [rang]." means "بينما كانت تطبخ رن الهاتف.",
                ),
                check = q("I was reading when the lights ___ out.", "went", "were going", "go"),
            ),
            concept(
                "used to: عادات انتهت",
                "لشيء كنت تفعله بانتظام في الماضي ولم تعد تفعله الآن. في النفي والسؤال تصبح use to (بدون d).",
                examples = listOf(
                    "I [used to] play football every day." means "كنت ألعب كرة القدم كل يوم (لم أعد).",
                    "She [didn't use to] like coffee." means "لم تكن تحب القهوة.",
                ),
                check = q("There ___ be a park here, but now it's a mall.", "used to", "use to", "was used"),
            ),
        ),
        mistakes = listOf(
            mistake("I was watch TV.", "I was watching TV.", "الماضي المستمر يحتاج ing."),
            mistake("Did you used to…?", "Did you use to…?", "بعد did نحذف d."),
        ),
        tip = "ارسم خطاً طويلاً ~~~~ للماضي المستمر، وعلامة ⚡ للماضي البسيط الذي يقطعه. كل قصة = خط طويل + برق!",
    ),

    // ---------------- B2 ----------------

    "b2-u1-l1" to guide(
        hook = "في الأخبار تقرأ «تم افتتاح الجسر» لا «فلان افتتح الجسر». عندما يكون الحدث أهم من الفاعل، أو الفاعل مجهول، نستخدم المبني للمجهول. إنه أسلوب الأخبار والعلم والكتابة الرسمية.",
        goals = listOf(
            "تحول الجملة إلى المبني للمجهول",
            "تستخدمه في كل الأزمنة",
            "تعرف متى تذكر الفاعل بـ by",
        ),
        concepts = listOf(
            concept(
                "قلب الجملة",
                "المفعول به يتقدم ليصبح في البداية، والفعل يتحول إلى be + التصريف الثالث. والفاعل الأصلي يختفي أو يأتي بعد by.",
                formula = "المفعول + be + التصريف الثالث + (by الفاعل)",
                examples = listOf(
                    "Someone stole my bike. → My bike [was stolen]." means "سُرقت دراجتي.",
                    "Mahfouz wrote this novel. → This novel [was written by] Mahfouz." means "كُتبت هذه الرواية بقلم محفوظ.",
                ),
                check = q("The Pyramids ___ thousands of years ago.", "were built", "built", "are built"),
            ),
            concept(
                "be تحمل الزمن",
                "التصريف الثالث لا يتغير أبداً. ما يتغير هو be حسب الزمن. هذا كل السر!",
                table = table(
                    listOf("الزمن", "المبني للمجهول"),
                    listOf("مضارع", "is made"),
                    listOf("ماضٍ", "was made"),
                    listOf("مضارع تام", "has been made"),
                    listOf("مستقبل", "will be made"),
                    listOf("مستمر", "is being made"),
                    listOf("فعل ناقص", "must be made"),
                ),
                check = q("Your order ___ shipped. It will arrive tomorrow.", "has been", "has", "is being been"),
            ),
        ),
        mistakes = listOf(
            mistake("The car was repair.", "The car was repaired.", "نحتاج التصريف الثالث."),
            mistake("English speaks here.", "English is spoken here.", "الإنجليزية لا «تتكلم» بنفسها."),
        ),
        tip = "اسأل: «من فعل؟» إذا كان الجواب غير مهم أو مجهول ← مبني للمجهول. وتذكر المعادلة: be + V3.",
    ),

    "b2-u2-l1" to guide(
        hook = "«لو كنت مليونيراً…» و«لو أنني درست أكثر…». هذه جمل الخيال والندم. الإنجليزية «ترجع خطوة للوراء» في الزمن لتقول إن الأمر غير حقيقي.",
        goals = listOf(
            "تتخيل مواقف غير حقيقية في الحاضر",
            "تعبر عن الندم على الماضي",
            "تستخدم wish بشكل صحيح",
        ),
        concepts = listOf(
            concept(
                "الشرطية الثانية: خيال في الحاضر",
                "لشيء غير حقيقي أو مستبعد الآن. نستخدم الماضي البسيط بعد if رغم أننا نتحدث عن الحاضر — الماضي هنا يعني «بعيد عن الواقع».",
                formula = "If + ماضٍ بسيط ، would + الفعل",
                examples = listOf(
                    "If I [won] the lottery, I [would buy] a house." means "لو ربحت اليانصيب لاشتريت بيتاً.",
                    "If I [were] you, I[’d accept]." means "لو كنت مكانك لقبلت.",
                ),
                check = q("If I ___ more time, I'd learn the piano.", "had", "have", "will have"),
            ),
            concept(
                "الشرطية الثالثة: الندم",
                "لشيء لم يحدث في الماضي، ونتخيل نتيجة مختلفة. فات الأوان — لا يمكن تغييره.",
                formula = "If + had + V3 ، would have + V3",
                examples = listOf(
                    "If I [had studied], I [would have passed]." means "لو كنت درست لنجحت (لكنني لم أدرس).",
                    "If you [had told] me, I [would have helped]." means "لو أخبرتني لساعدتك.",
                ),
                check = q("If we had left earlier, we ___ the bus.", "wouldn't have missed", "wouldn't miss", "didn't miss"),
            ),
            concept(
                "wish: أتمنى لو",
                "نرجع خطوة للوراء أيضاً: للحاضر نستخدم الماضي البسيط، وللماضي نستخدم الماضي التام.",
                table = table(
                    listOf("أتمنى عن…", "التركيب", "مثال"),
                    listOf("الحاضر", "wish + ماضٍ بسيط", "I wish I had a car."),
                    listOf("الماضي", "wish + had + V3", "I wish I hadn't said that."),
                ),
                check = q("I wish I ___ speak French.", "could", "can", "will"),
            ),
        ),
        mistakes = listOf(
            mistake("If I would have money…", "If I had money…", "لا would بعد if."),
            mistake("If I had known, I would told you.", "If I had known, I would have told you.", "الثالثة: would have + V3."),
        ),
        tip = "قاعدة «خطوة للوراء»: كلما ابتعد الكلام عن الواقع، رجع الزمن خطوة. حاضر حقيقي ← ماضٍ خيالي. ماضٍ حقيقي ← ماضٍ تام خيالي.",
    ),

    "b2-u3-l1" to guide(
        hook = "عندما تنقل كلام شخص آخر، فأنت تتحدث بعد أن قيل الكلام. لذلك ترجع الأزمنة «خطوة للخلف» كأن الكلام أصبح ذكرى. وتتغير الضمائر وكلمات الزمن أيضاً.",
        goals = listOf(
            "تنقل الجمل الخبرية",
            "تنقل الأسئلة والأوامر",
            "تفرق بين say و tell",
        ),
        concepts = listOf(
            concept(
                "خطوة للخلف",
                "كل زمن يتراجع خطوة واحدة عند النقل بعد said / told:",
                table = table(
                    listOf("الكلام الأصلي", "الكلام المنقول"),
                    listOf("am / is", "was"),
                    listOf("do / does", "did"),
                    listOf("will", "would"),
                    listOf("can", "could"),
                    listOf("did / have done", "had done"),
                ),
                examples = listOf(
                    "\"I [am] tired.\" → She said she [was] tired." means "قالت إنها متعبة.",
                    "\"We [will] win.\" → They said they [would] win." means "قالوا إنهم سيفوزون.",
                ),
                check = q("\"I can swim.\" → He said he ___ swim.", "could", "can", "will"),
            ),
            concept(
                "الأسئلة: لم تعد أسئلة",
                "السؤال المنقول يصبح جملة خبرية: لا do/does، ولا علامة استفهام، والفاعل قبل الفعل. أسئلة نعم/لا تأخذ if.",
                examples = listOf(
                    "\"Where do you live?\" → She asked [where I lived]." means "سألتني أين أسكن.",
                    "\"Are you ready?\" → He asked [if I was] ready." means "سألني إن كنت مستعداً.",
                ),
                check = q("He asked me where I ___.", "worked", "do work", "did I work"),
            ),
            concept(
                "say أم tell؟ والأوامر",
                "tell تحتاج دائماً شخصاً بعدها (tell me)، أما say فلا (said that). والأوامر تُنقل بـ told + شخص + to + فعل.",
                examples = listOf(
                    "She [told me] the truth." means "أخبرتني الحقيقة.",
                    "\"Sit down.\" → He [told us to sit] down." means "طلب منا أن نجلس.",
                ),
                check = q("She ___ me she was busy.", "told", "said", "spoke"),
            ),
        ),
        mistakes = listOf(
            mistake("He said me that…", "He told me that…", "مع الشخص نستخدم tell."),
            mistake("She asked where did I go.", "She asked where I went.", "السؤال المنقول بترتيب الجملة الخبرية."),
        ),
        tip = "تخيل أن الكلام المنقول «صورة قديمة» بالأبيض والأسود: كل شيء فيها يرجع خطوة إلى الماضي.",
    ),

    "b2-u4-l1" to guide(
        hook = "بدل أن تقول جملتين قصيرتين «هذا رجل. هو مديري»، تقول جملة واحدة أنيقة «هذا هو الرجل الذي هو مديري». جمل الصلة تجعل كلامك أكثر طلاقة. وسنتعلم أيضاً كيف نقول «أفعل هذا منذ مدة».",
        goals = listOf(
            "تربط الجمل بـ who / which / whose / where",
            "تفرق بين الجملة المحددة وغير المحددة",
            "تستخدم المضارع التام المستمر",
        ),
        concepts = listOf(
            concept(
                "ضمائر الوصل",
                "كل ضمير له «تخصص»:",
                table = table(
                    listOf("الضمير", "يُستخدم لـ", "مثال"),
                    listOf("who", "الأشخاص", "the man who called"),
                    listOf("which", "الأشياء", "the car which I bought"),
                    listOf("whose", "الملكية", "the girl whose bag…"),
                    listOf("where", "الأماكن", "the city where I live"),
                    listOf("that", "أشخاص وأشياء", "the book that I read"),
                ),
                check = q("This is the hotel ___ we stayed.", "where", "which", "who"),
            ),
            concept(
                "محددة أم إضافية؟",
                "المحددة ضرورية لنعرف من نقصد (بدون فواصل). غير المحددة معلومة إضافية بين فاصلتين، يمكن حذفها، ولا نستخدم فيها that.",
                examples = listOf(
                    "The man [who lives next door] is a doctor." means "الرجل الذي يسكن بجانبنا طبيب (أي رجل؟ هذا).",
                    "My father, [who is 60], still works." means "أبي، الذي عمره 60، مازال يعمل (معلومة إضافية).",
                ),
                check = q("Dubai, ___ is in the UAE, is very modern.", "which", "that", "who"),
            ),
            concept(
                "المضارع التام المستمر: منذ متى وأنت…؟",
                "لنشاط بدأ في الماضي ومازال مستمراً، مع التركيز على المدة والاستمرار.",
                formula = "have / has + been + الفعل + ing",
                examples = listOf(
                    "I[’ve been learning] English for two years." means "أتعلم الإنجليزية منذ سنتين.",
                    "You look tired. — I[’ve been running]." means "تبدو متعباً. — كنت أركض.",
                ),
                check = q("She ___ for an hour. She's still waiting.", "has been waiting", "waits", "is waiting"),
            ),
        ),
        mistakes = listOf(
            mistake("The man which helped me…", "The man who helped me…", "للأشخاص نستخدم who."),
            mistake("My car, that is red, …", "My car, which is red, …", "لا that بين فاصلتين."),
            mistake("I've been knowing him for years.", "I've known him for years.", "know لا تُستخدم بصيغة ing."),
        ),
        tip = "الفاصلتان مثل «قوسين»: ما بداخلهما إضافة يمكن حذفها. وthat لا تحب الأقواس!",
    ),
)
