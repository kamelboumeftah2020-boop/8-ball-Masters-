package com.fluently.english.data.content

/** Step-by-step explanations for the C1 and C2 grammar lessons. */
internal val GuidesC: Map<String, Guide> = mapOf(

    // ---------------- C1 ----------------

    "c1-u1-l1" to guide(
        hook = "في العربية نقول «ما رأيت قط مثل هذا الجمال» لنؤكد بقوة. الإنجليزية تفعل شيئاً مشابهاً: تبدأ الجملة بكلمة نفي أو تقييد، ثم «تقلب» ترتيب الفاعل والفعل كأنها سؤال. النتيجة: جملة درامية ومؤثرة تسمعها في الخطب والروايات.",
        goals = listOf(
            "تفهم لماذا ينقلب الترتيب",
            "تستخدم Never / Rarely / Not only في بداية الجملة",
            "تستخدم الشرط الرسمي بدون if",
        ),
        concepts = listOf(
            concept(
                "الآلية: ترتيب السؤال في جملة خبرية",
                "عندما تتقدم كلمة نفي إلى البداية، يأتي الفعل المساعد قبل الفاعل. إذا لم يوجد مساعد نستعين بـ do / does / did.",
                formula = "كلمة النفي + المساعد + الفاعل + الفعل",
                examples = listOf(
                    "I have never seen this. → [Never have I] seen this." means "لم أرَ هذا قط.",
                    "He rarely smiles. → [Rarely does he] smile." means "نادراً ما يبتسم.",
                ),
                check = q("Never ___ such a beautiful view.", "have I seen", "I have seen", "I saw"),
            ),
            concept(
                "أشهر الكلمات التي تقلب الجملة",
                "احفظ هذه العائلة وستتعرف عليها فوراً عند القراءة:",
                table = table(
                    listOf("التعبير", "المعنى"),
                    listOf("Never / Rarely / Seldom", "أبداً / نادراً"),
                    listOf("Not only … but also", "ليس فقط … بل أيضاً"),
                    listOf("No sooner … than", "ما إن … حتى"),
                    listOf("Hardly … when", "بالكاد … حتى"),
                    listOf("Under no circumstances", "تحت أي ظرف لا"),
                    listOf("Little", "لم يكن يعلم إطلاقاً"),
                ),
                examples = listOf(
                    "[Not only is she] smart, but she's also kind." means "ليست ذكية فحسب بل لطيفة أيضاً.",
                    "[No sooner had we] arrived than it rained." means "ما إن وصلنا حتى أمطرت.",
                ),
                check = q("No sooner had he left ___ she arrived.", "than", "when", "then"),
            ),
            concept(
                "الشرط الرسمي بدون if",
                "في الرسائل الرسمية تُحذف if ويتقدم الفعل: Had = If … had، Should = If، Were = If … were.",
                examples = listOf(
                    "[Had I known], I would have come." means "لو كنت أعلم لأتيت.",
                    "[Should you need] help, call us." means "إذا احتجت المساعدة اتصل بنا.",
                ),
                check = q("___ I known about it, I would have helped.", "Had", "If", "Did"),
            ),
        ),
        mistakes = listOf(
            mistake("Never I have seen…", "Never have I seen…", "بعد Never يتقدم المساعد."),
            mistake("Rarely he smiles.", "Rarely does he smile.", "لا يوجد مساعد ← نستخدم does."),
        ),
        tip = "عندما تبدأ بكلمة «سلبية»، تخيل أن الجملة «تتعجب» فتأخذ شكل السؤال — لكن بدون علامة استفهام.",
    ),

    "c1-u2-l1" to guide(
        hook = "المحقق في الأفلام يقول: «لا بد أن اللص دخل من النافذة» و«مستحيل أن يكون الحارس هو الفاعل». هذه استنتاجات عن الماضي. الإنجليزية تصنعها بمعادلة واحدة: فعل ناقص + have + التصريف الثالث.",
        goals = listOf(
            "تستنتج ما حدث في الماضي",
            "تعبر عن الندم واللوم",
            "تربط الماضي بالحاضر بالشرطية المختلطة",
        ),
        concepts = listOf(
            concept(
                "مقياس اليقين عن الماضي",
                "كلها بنفس التركيب، والفرق في درجة التأكد:",
                formula = "must / might / can't + have + V3",
                table = table(
                    listOf("التعبير", "درجة اليقين", "المعنى"),
                    listOf("must have done", "95% ✅", "لا بد أنه فعل"),
                    listOf("might / could have done", "50% 🤔", "ربما فعل"),
                    listOf("can't have done", "0% ❌", "مستحيل أن يكون فعل"),
                ),
                examples = listOf(
                    "The street is wet. It [must have rained]." means "الشارع مبلل. لا بد أنها أمطرت.",
                    "She [can't have seen] me." means "مستحيل أن تكون رأتني.",
                ),
                check = q("He's not answering. He ___ his phone.", "might have lost", "must lose", "can't lose"),
            ),
            concept(
                "الندم واللوم",
                "should have + V3 = كان يجب (ولم يحدث). needn't have + V3 = فعلت شيئاً لم يكن ضرورياً.",
                examples = listOf(
                    "You [should have told] me!" means "كان يجب أن تخبرني!",
                    "I [needn't have cooked]; they'd already eaten." means "لم يكن داعٍ أن أطبخ، فقد أكلوا.",
                ),
                check = q("I failed. I ___ studied harder.", "should have", "must have", "can't have"),
            ),
            concept(
                "الشرطية المختلطة",
                "تخلط بين زمنين: شرط في الماضي ونتيجته الآن، أو العكس.",
                formula = "If + had + V3 ، would + الفعل (now)",
                examples = listOf(
                    "If I [had studied] medicine, I [would be] a doctor now." means "لو درست الطب لكنت طبيباً الآن.",
                ),
                check = q("If she had taken the job, she ___ in London now.", "would be living", "would have lived", "lives"),
            ),
        ),
        mistakes = listOf(
            mistake("He must went.", "He must have gone.", "الاستنتاج عن الماضي: must have + V3."),
            mistake("You should told me.", "You should have told me.", "نحتاج have."),
        ),
        tip = "تخيل نفسك «شرلوك هولمز»: كل استنتاج عن الماضي = (must / might / can't) + have + V3.",
    ),

    "c1-u3-l1" to guide(
        hook = "الكتّاب المحترفون يختصرون جملتين في جملة واحدة أنيقة. بدل «كنت أمشي في الشارع، ورأيت صديقي» يكتبون «Walking down the street, I saw a friend». هذه هي جمل اسم الفاعل والمفعول.",
        goals = listOf(
            "تختصر الجمل بـ ing و V3",
            "تستخدم Having + V3 لحدث سابق",
            "تتجنب خطأ «الفاعل المعلّق»",
        ),
        concepts = listOf(
            concept(
                "ing للمعلوم و V3 للمجهول",
                "إذا كان الفاعل يفعل الشيء بنفسه ← ing. إذا وقع عليه الفعل ← التصريف الثالث.",
                table = table(
                    listOf("الجملة الطويلة", "المختصرة"),
                    listOf("While I was walking home, I…", "Walking home, I…"),
                    listOf("The bridge, which was built in 1890, …", "Built in 1890, the bridge…"),
                    listOf("Because she felt tired, she…", "Feeling tired, she…"),
                ),
                check = q("___ in 1931, the building is still famous.", "Completed", "Completing", "Complete"),
            ),
            concept(
                "Having + V3: حدث انتهى أولاً",
                "عندما ينتهي حدث تماماً قبل الآخر، استخدم Having + V3.",
                examples = listOf(
                    "[Having finished] the report, she went home." means "بعد أن أنهت التقرير ذهبت إلى البيت.",
                ),
                check = q("___ his keys, he couldn't get in.", "Having lost", "Lost", "Losing having"),
            ),
            concept(
                "احذر: الفاعل المعلّق",
                "الجزء المختصر يعود دائماً على فاعل الجملة الرئيسية. «Walking home, the rain started» تعني أن المطر كان يمشي! 😄",
                examples = listOf(
                    "✗ Walking home, the rain started." means "(المطر يمشي؟!)",
                    "✓ Walking home, [I] got caught in the rain." means "أثناء عودتي إلى البيت فاجأني المطر.",
                ),
                check = q("Which sentence is correct?", "Opening the door, I saw a cat.", "Opening the door, a cat appeared.", "Opened the door, I saw a cat."),
            ),
        ),
        mistakes = listOf(
            mistake("Arriving late, the meeting had started.", "Arriving late, I found the meeting had started.", "الاجتماع لم يصل متأخراً!"),
        ),
        tip = "اسأل بعد كل جملة مختصرة: «من الذي…؟» يجب أن يكون الجواب هو أول اسم بعد الفاصلة.",
    ),

    "c1-u4-l1" to guide(
        hook = "عندما تريد أن تقول «سارة هي التي حلت المشكلة (وليس أحمد)»، تكسر الجملة إلى جزأين لتسلط الضوء على معلومة واحدة. هذه الجمل المشطورة سلاح المتحدث المقنع.",
        goals = listOf(
            "تبرز معلومة بـ It is … that",
            "تستخدم What … is",
            "تكتب جملاً مقنعة",
        ),
        concepts = listOf(
            concept(
                "It is … who / that",
                "ضع المعلومة التي تريد إبرازها بين It was و that / who، كأنك تضع عليها ضوء المسرح 🔦.",
                formula = "It is / was + المعلومة المهمة + who / that + الباقي",
                examples = listOf(
                    "[It was Sara who] solved the problem." means "سارة هي التي حلت المشكلة.",
                    "[It was in 2015 that] we met." means "في عام 2015 تحديداً التقينا.",
                ),
                check = q("It was my sister ___ called you.", "who", "which", "what"),
            ),
            concept(
                "What … is",
                "ابدأ بـ What لتشويق المستمع، ثم اكشف المعلومة بعد is.",
                formula = "What + جملة + is / was + المعلومة",
                examples = listOf(
                    "[What I need is] a holiday." means "ما أحتاجه هو عطلة.",
                    "[What surprised me was] his reaction." means "ما فاجأني هو ردة فعله.",
                    "[All you have to do is] sign here." means "كل ما عليك هو التوقيع هنا.",
                ),
                check = q("___ I love about Cairo is its energy.", "What", "That", "Which"),
            ),
        ),
        mistakes = listOf(
            mistake("What I need it is…", "What I need is…", "لا نكرر it."),
            mistake("It was him which…", "It was him who…", "للأشخاص نستخدم who."),
        ),
        tip = "الجملة المشطورة مثل «إعلان»: الجزء الأول يشوّق (What I want…) والثاني يكشف المفاجأة (…is you!).",
    ),

    // ---------------- C2 ----------------

    "c2-u1-l1" to guide(
        hook = "في القوانين والعقود والخطابات الرسمية ستجد جملاً مثل «It is essential that he be present» — لماذا be وليس is؟ هذه صيغة الشرط الافتراضية (subjunctive)، وهي علامة على لغة رفيعة المستوى.",
        goals = listOf(
            "تستخدم المصدر بعد suggest / insist / essential",
            "تستخدم الماضي غير الحقيقي بعد It's time و would rather",
            "تتعرف على التعابير الثابتة",
        ),
        concepts = listOf(
            concept(
                "المصدر لكل الضمائر",
                "بعد أفعال وصفات الطلب والضرورة، نستخدم الفعل في أصله بدون s وبدون زمن، مع كل الضمائر.",
                formula = "suggest / insist / essential + that + الفاعل + المصدر",
                examples = listOf(
                    "The doctor insisted that he [stop] smoking." means "أصر الطبيب أن يتوقف عن التدخين.",
                    "It is vital that she [be] informed." means "من الضروري أن تُبلَّغ.",
                ),
                check = q("They recommended that the law ___ changed.", "be", "is", "was"),
            ),
            concept(
                "الماضي غير الحقيقي",
                "بعد هذه التعابير نستخدم الماضي رغم أن المعنى حاضر أو مستقبل:",
                table = table(
                    listOf("التعبير", "مثال"),
                    listOf("It's (high) time", "It's time we left."),
                    listOf("would rather + شخص", "I'd rather you stayed."),
                    listOf("as if / as though", "He acts as if he were king."),
                ),
                check = q("It's high time you ___ a job.", "found", "find", "will find"),
            ),
            concept(
                "تعابير ثابتة",
                "عبارات قديمة بقيت في اللغة الرسمية: Be that as it may (مهما يكن)، Come what may (مهما حدث)، Suffice it to say (يكفي القول).",
                examples = listOf(
                    "[Come what may], I'll support you." means "مهما حدث سأدعمك.",
                ),
                check = q("___ that as it may, we must decide.", "Be", "Being", "Is"),
            ),
        ),
        mistakes = listOf(
            mistake("I suggest that he goes.", "I suggest that he go.", "في الإنجليزية الرسمية نستخدم المصدر."),
            mistake("It's time we leave.", "It's time we left.", "بعد It's time نستخدم الماضي."),
        ),
        tip = "تخيل أن الـ subjunctive «يتحدث عن عالم مطلوب لا موجود»: ما زال طلباً، لذلك لا زمن له — مجرد فعل خام.",
    ),

    "c2-u2-l1" to guide(
        hook = "المتحدث البارع لا يكرر نفسه. يقول «Will it rain? — I hope not» بدل «I hope it will not rain». ويبهر سامعيه بـ «So great was the demand that…». هذا الدرس عن الإيجاز والبلاغة.",
        goals = listOf(
            "تستخدم القلب مع so و such",
            "تستخدم Were it not for / Had it not been for",
            "تتجنب التكرار بالحذف والاستبدال",
        ),
        concepts = listOf(
            concept(
                "So … that / Such … that مقلوبة",
                "لإظهار شدة شيء أدى إلى نتيجة: ضع So + صفة أو Such في البداية، ثم اقلب الفعل والفاعل.",
                examples = listOf(
                    "[So loud was the music] that we couldn't talk." means "كانت الموسيقى عالية لدرجة أننا لم نستطع الكلام.",
                    "[Such was his anger] that he left." means "بلغ غضبه حداً جعله يغادر.",
                ),
                check = q("So beautiful ___ that we stopped the car.", "was the view", "the view was", "the view"),
            ),
            concept(
                "لولا…",
                "Were it not for (لولا — للحاضر) وHad it not been for (لولا — للماضي).",
                examples = listOf(
                    "[Were it not for] you, I'd give up." means "لولاك لاستسلمت.",
                    "[Had it not been for] the GPS, we'd have got lost." means "لولا نظام الملاحة لضعنا.",
                ),
                check = q("___ it not been for her help, I would have failed.", "Had", "Were", "If"),
            ),
            concept(
                "الحذف والاستبدال",
                "بدل تكرار جملة كاملة، استخدم so / not / do so، أو احذف ما يُفهم من السياق.",
                table = table(
                    listOf("بدلاً من", "قل"),
                    listOf("I think it will rain.", "I think so."),
                    listOf("I hope it won't rain.", "I hope not."),
                    listOf("I was asked to leave and I left.", "…and I did so."),
                ),
                check = q("Is he coming? — I'm afraid ___.", "not", "no", "don't"),
            ),
        ),
        mistakes = listOf(
            mistake("So loud the music was that…", "So loud was the music that…", "بعد So + صفة يتقدم الفعل."),
            mistake("I hope no.", "I hope not.", "الاستبدال بالنفي يكون بـ not."),
        ),
        tip = "قبل أن تكرر جملة، اسأل: هل يكفي so أو not؟ الإيجاز علامة الإتقان.",
    ),

    "c2-u3-l1" to guide(
        hook = "لماذا تبدو الأبحاث العلمية «رسمية»؟ لسببين: تحويل الأفعال إلى أسماء (nominalisation)، والحذر في الأحكام (hedging). الباحث لا يقول «القهوة تسبب الأرق»، بل «تشير النتائج إلى أن القهوة قد تسهم في الأرق».",
        goals = listOf(
            "تحول الأفعال إلى أسماء",
            "تخفف الأحكام بأدوات التحوط",
            "تكتب بأسلوب أكاديمي موضوعي",
        ),
        concepts = listOf(
            concept(
                "من فعل إلى اسم",
                "الأسلوب اليومي يعتمد على الأفعال، والأكاديمي يعتمد على الأسماء. هذا يجعل الجملة أكثر كثافة وموضوعية.",
                table = table(
                    listOf("الفعل / الصفة", "الاسم"),
                    listOf("decide", "decision"),
                    listOf("analyse", "analysis"),
                    listOf("increase", "an increase"),
                    listOf("fail", "failure"),
                ),
                examples = listOf(
                    "Prices rose sharply. → [The sharp rise in prices]…" means "الارتفاع الحاد في الأسعار…",
                ),
                check = q("«The company expanded» → The company's ___", "expansion", "expanding", "expand"),
            ),
            concept(
                "التحوّط: لا تجزم",
                "الكاتب الأكاديمي يترك مساحة للشك العلمي باستخدام كلمات تخفيف:",
                table = table(
                    listOf("جازم ❌", "متحوّط ✅"),
                    listOf("X causes Y.", "X may contribute to Y."),
                    listOf("This proves…", "This suggests…"),
                    listOf("Everyone agrees…", "It is widely believed…"),
                ),
                check = q("Which sentence is hedged?", "The data suggest a possible link.", "The data prove the link.", "There is definitely a link."),
            ),
        ),
        mistakes = listOf(
            mistake("This proves that diet causes cancer.", "This suggests that diet may play a role.", "العلم لا يجزم من دراسة واحدة."),
        ),
        tip = "عند الكتابة الأكاديمية اسأل: «هل أستطيع إثبات هذا 100%؟» إذا لا ← أضف may / suggest / appear to.",
    ),

    "c2-u4-l1" to guide(
        hook = "خطب مارتن لوثر كينغ وتشرشل وكينيدي تبقى في الذاكرة لأنها تستخدم أساليب بلاغية: الثلاثيات، والتضاد، والتقديم، والأسئلة البلاغية. في هذا الدرس تتعلم أسرارهم.",
        goals = listOf(
            "تستخدم التقديم: Strange as it may seem",
            "تتعرف على الثلاثية والتضاد",
            "تستخدم السؤال البلاغي",
        ),
        concepts = listOf(
            concept(
                "صفة + as + فاعل + فعل",
                "طريقة أنيقة لقول «على الرغم من»: تقدم الصفة ثم as.",
                formula = "صفة / ظرف + as + الفاعل + الفعل ، الجملة الرئيسية",
                examples = listOf(
                    "[Tired as she was], she kept working." means "رغم تعبها استمرت في العمل.",
                    "[Much as I admire] him, I disagree." means "رغم إعجابي به لا أتفق معه.",
                ),
                check = q("Strange ___ it may seem, I enjoyed the exam.", "as", "though it", "so"),
            ),
            concept(
                "أدوات الخطيب",
                "ثلاث أدوات تجعل أي كلام مؤثراً:",
                table = table(
                    listOf("الأداة", "مثال"),
                    listOf("الثلاثية", "We came, we saw, we conquered."),
                    listOf("التضاد", "Ask not what your country can do for you…"),
                    listOf("السؤال البلاغي", "Who among us has never failed?"),
                ),
                check = q("«Faster, stronger, smarter» is an example of…", "a tricolon", "an antithesis", "a question"),
            ),
        ),
        mistakes = listOf(
            mistake("Strange as it seems may…", "Strange as it may seem…", "الترتيب: صفة + as + فاعل + فعل."),
        ),
        tip = "عند كتابة أي خطاب: ابدأ بسؤال بلاغي، وضع فكرتك الرئيسية في ثلاثية، واختم بتضاد. ستلاحظ الفرق فوراً!",
    ),
)

/** All grammar explanations, keyed by lesson id. */
val Guides: Map<String, Guide> = GuidesA + GuidesB + GuidesC + GuidesU5
