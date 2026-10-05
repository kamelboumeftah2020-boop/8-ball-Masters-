package com.fluently.english.data.content

/** Step-by-step explanations for the A1 and A2 grammar lessons. */
internal val GuidesA: Map<String, Guide> = mapOf(

    // ---------------- A1 ----------------

    "a1-u1-l1" to guide(
        hook = "في العربية نقول «أنا طالب» بدون فعل، والجملة مفهومة تماماً. أما الإنجليزية فلا تقبل جملة بدون فعل أبداً! لذلك تحتاج «جسراً» يربط الشخص بصفته. هذا الجسر هو فعل الكينونة to be، وله ثلاثة أشكال فقط: am و is و are.",
        goals = listOf(
            "تعرّف بنفسك: I am Sara.",
            "تختار بين am و is و are بدون تفكير",
            "تكوّن النفي والسؤال",
        ),
        concepts = listOf(
            concept(
                "الجسر بين الشخص وصفته",
                "تخيّل الجملة كجسر: على اليمين شخص، وعلى اليسار وصفه (اسمه، عمله، بلده، شعوره). فعل to be هو الجسر بينهما. بدونه تسقط الجملة!",
                formula = "الشخص + am / is / are + الوصف",
                examples = listOf(
                    "I [am] a student." means "أنا (أكون) طالب.",
                    "Cairo [is] big." means "القاهرة كبيرة.",
                    "They [are] happy." means "هم سعداء.",
                ),
                check = q("My name ___ Omar.", "is", "am", "are", explain = "«my name» شيء مفرد مثل it، لذلك is."),
            ),
            concept(
                "أي جسر أختار؟",
                "القاعدة سهلة جداً: am خاص بـ I وحدها. is لشخص أو شيء واحد غيرك وغير المخاطَب (هو، هي، هذا الشيء). are للمخاطب ولأي مجموعة.",
                table = table(
                    listOf("الضمير", "الفعل", "مثال"),
                    listOf("I", "am", "I am tired."),
                    listOf("he / she / it", "is", "She is a nurse."),
                    listOf("you / we / they", "are", "We are ready."),
                ),
                examples = listOf(
                    "He [is] my brother." means "هو أخي.",
                    "You [are] very kind." means "أنت لطيف جداً.",
                ),
                check = q("We ___ from Libya.", "are", "is", "am"),
            ),
            concept(
                "النفي والسؤال",
                "للنفي: أضف not بعد الجسر مباشرة (isn't = is not، aren't = are not). للسؤال: اقلب الترتيب وضع الجسر في البداية، تماماً كأنك ترفع صوتك بالسؤال.",
                formula = "Am / Is / Are + الشخص + الوصف ?",
                examples = listOf(
                    "She [is not] at home." means "هي ليست في البيت.",
                    "[Are] you hungry?" means "هل أنت جائع؟",
                    "[Is] he a doctor? — Yes, he is." means "هل هو طبيب؟ — نعم.",
                ),
                check = q("___ they at school?", "Are", "Is", "Am"),
            ),
        ),
        mistakes = listOf(
            mistake("I student.", "I am a student.", "لا توجد جملة إنجليزية بدون فعل."),
            mistake("She are nice.", "She is nice.", "she شخص واحد، لذلك is."),
            mistake("You is my friend.", "You are my friend.", "you دائماً معها are حتى لو كان شخصاً واحداً."),
        ),
        tip = "احفظها كأغنية: «I am — he, she, it is — you, we, they are». كررها ثلاث مرات بصوت عالٍ وستبقى معك للأبد.",
    ),

    "a1-u2-l1" to guide(
        hook = "عندما تشير إلى شيء في السوق تقول «هذا» أو «ذلك»، وعندما تطلب «تفاحة» أو «تفاحتين». الإنجليزية تفعل الشيء نفسه بكلمات صغيرة جداً: a / an للواحد، s للجمع، وthis / that للإشارة.",
        goals = listOf(
            "تعرف متى تقول a ومتى an",
            "تحوّل الاسم إلى جمع",
            "تشير إلى القريب والبعيد",
        ),
        concepts = listOf(
            concept(
                "a أم an؟ استمع للصوت!",
                "السر ليس في الحرف المكتوب بل في الصوت الأول عند النطق. إذا بدأت الكلمة بصوت حرف متحرك (a, e, i, o, u) نقول an لأن النطق أسهل: an apple. وإلا نقول a: a book.",
                table = table(
                    listOf("a + صوت ساكن", "an + صوت متحرك"),
                    listOf("a car", "an egg"),
                    listOf("a dog", "an orange"),
                    listOf("a university (يو)", "an hour (h صامتة)"),
                ),
                examples = listOf(
                    "I have [an] idea!" means "لدي فكرة!",
                    "She is [a] teacher." means "هي معلمة.",
                ),
                check = q("He eats ___ apple every day.", "an", "a", explain = "apple تبدأ بصوت متحرك."),
            ),
            concept(
                "من واحد إلى كثير",
                "أغلب الكلمات نضيف لها s فقط. الكلمات التي تنتهي بصوت «صفير» (s, sh, ch, x) نضيف لها es لأن نطقها يحتاج مقطعاً إضافياً. وبعض الكلمات «متمردة» تتغير كلياً.",
                table = table(
                    listOf("القاعدة", "مفرد", "جمع"),
                    listOf("+ s", "book", "books"),
                    listOf("+ es", "box", "boxes"),
                    listOf("شاذ", "child", "children"),
                    listOf("شاذ", "man", "men"),
                ),
                examples = listOf(
                    "Two [buses] are coming." means "حافلتان قادمتان.",
                    "Many [people] live here." means "يسكن هنا أناس كثيرون.",
                ),
                check = q("The plural of «watch» is…", "watches", "watchs", "watchies"),
            ),
            concept(
                "هنا وهناك",
                "تخيل يدك: ما تستطيع لمسه هو this (مفرد) أو these (جمع). وما تشير إليه بإصبعك بعيداً هو that (مفرد) أو those (جمع).",
                table = table(
                    listOf("", "قريب ✋", "بعيد 👉"),
                    listOf("مفرد", "this", "that"),
                    listOf("جمع", "these", "those"),
                ),
                examples = listOf(
                    "[This] is my phone." means "هذا هاتفي.",
                    "[Those] mountains are beautiful." means "تلك الجبال جميلة.",
                ),
                check = q("___ shoes (in my hands) are new.", "These", "This", "That"),
            ),
        ),
        mistakes = listOf(
            mistake("a apple", "an apple", "apple تبدأ بصوت متحرك."),
            mistake("two childs", "two children", "child جمعها شاذ."),
            mistake("This books are mine.", "These books are mine.", "books جمع، فنحتاج these."),
        ),
        tip = "this و these كلاهما فيهما «ي» عند النطق ويعنيان قريب. that و those يبدآن بـ «ذ» مثل «ذلك» البعيد في العربية!",
    ),

    "a1-u3-l1" to guide(
        hook = "ما الأشياء التي تفعلها كل يوم؟ تستيقظ، تشرب القهوة، تذهب للعمل… المضارع البسيط هو زمن «العادات والروتين والحقائق». وفيه سر صغير واحد يجب أن تنتبه له: حرف s مع he و she و it.",
        goals = listOf(
            "تتحدث عن روتينك اليومي",
            "تضيف s للفعل في الوقت الصحيح",
            "تنفي وتسأل باستخدام do / does",
        ),
        concepts = listOf(
            concept(
                "زمن العادات",
                "نستخدمه لما يتكرر دائماً أو للحقائق الثابتة. الفعل يبقى كما هو في القاموس مع I / you / we / they.",
                formula = "الفاعل + الفعل + الباقي",
                examples = listOf(
                    "I [drink] tea every morning." means "أشرب الشاي كل صباح.",
                    "The sun [rises] in the east." means "تشرق الشمس من الشرق.",
                ),
                check = q("We ___ football on Fridays.", "play", "plays", "playing"),
            ),
            concept(
                "سر حرف s",
                "عندما يكون الفاعل شخصاً واحداً غائباً (he / she / it) يأخذ الفعل s. تخيل أن s «جائزة» يأخذها الشخص الوحيد. الأفعال المنتهية بـ o, sh, ch, x تأخذ es.",
                table = table(
                    listOf("I / you / we / they", "he / she / it"),
                    listOf("work", "works"),
                    listOf("go", "goes"),
                    listOf("watch", "watches"),
                    listOf("study", "studies"),
                ),
                examples = listOf(
                    "She [works] in a bank." means "هي تعمل في بنك.",
                    "My father [goes] to the mosque." means "أبي يذهب إلى المسجد.",
                ),
                check = q("He ___ coffee.", "likes", "like", "liking"),
            ),
            concept(
                "النفي والسؤال بمساعد",
                "الفعل العادي لا يستطيع أن ينفي أو يسأل وحده، فيحتاج مساعداً: do (أو does مع he/she/it). وانتبه: عندما يظهر does تنتقل s إليه ويعود الفعل لأصله.",
                formula = "Do / Does + الفاعل + الفعل الأصلي ?",
                examples = listOf(
                    "I [don't] eat meat." means "لا آكل اللحم.",
                    "She [doesn't] like cats." means "هي لا تحب القطط.",
                    "[Does] he speak English?" means "هل يتحدث الإنجليزية؟",
                ),
                check = q("___ your sister live in Dubai?", "Does", "Do", "Is"),
            ),
        ),
        mistakes = listOf(
            mistake("She work here.", "She works here.", "مع she نضيف s."),
            mistake("He doesn't works.", "He doesn't work.", "s انتقلت إلى does، فلا نكررها."),
            mistake("Do she like tea?", "Does she like tea?", "مع she نستخدم does."),
        ),
        tip = "قاعدة «s واحدة فقط»: في كل جملة مع he/she/it توجد s واحدة. إما في الفعل (she works) أو في المساعد (she doesn't work) — أبداً ليس في الاثنين.",
    ),

    "a1-u4-l1" to guide(
        hook = "تخيل أنك سائح في مدينة جديدة: تريد أن تسأل «هل يوجد بنك هنا؟» و«هل تستطيع مساعدتي؟» و«أين المحطة؟». هذا الدرس يعطيك هذه الأدوات الثلاث: there is / are، وcan، وحروف المكان.",
        goals = listOf(
            "تصف ما يوجد في مكان ما",
            "تتحدث عن قدراتك وتطلب المساعدة",
            "تحدد مكان الأشياء",
        ),
        concepts = listOf(
            concept(
                "يوجد… There is / There are",
                "عندما تريد أن تقول «يوجد»، ابدأ بـ There ثم is للمفرد وare للجمع. كأنك تقدم شيئاً للمستمع لأول مرة.",
                table = table(
                    listOf("", "مفرد", "جمع"),
                    listOf("إثبات", "There is a bank.", "There are two banks."),
                    listOf("نفي", "There isn't a bank.", "There aren't any banks."),
                    listOf("سؤال", "Is there a bank?", "Are there any banks?"),
                ),
                examples = listOf(
                    "[There is] a café near here." means "يوجد مقهى قريب من هنا.",
                    "[Are there] any shops?" means "هل توجد أي محلات؟",
                ),
                check = q("There ___ three rooms in my flat.", "are", "is", "be"),
            ),
            concept(
                "can: القدرة والطلب",
                "can كلمة سحرية لا تتغير أبداً: لا s ولا to بعدها. نستخدمها للقدرة (أستطيع) ولطلب الأشياء بلطف.",
                formula = "الفاعل + can / can't + الفعل الأصلي",
                examples = listOf(
                    "She [can] speak three languages." means "تستطيع التحدث بثلاث لغات.",
                    "I [can't] drive." means "لا أستطيع القيادة.",
                    "[Can] you help me, please?" means "هل تستطيع مساعدتي من فضلك؟",
                ),
                check = q("He can ___ very fast.", "run", "runs", "to run"),
            ),
            concept(
                "أين هو؟ حروف المكان",
                "تخيل قطة وصندوقاً: القطة in (داخل) الصندوق، on (فوق) الصندوق، under (تحت) الصندوق، next to (بجانب) الصندوق، behind (خلف) الصندوق، وin front of (أمام) الصندوق.",
                table = table(
                    listOf("الحرف", "المعنى"),
                    listOf("in", "داخل"),
                    listOf("on", "على / فوق (ملامس)"),
                    listOf("under", "تحت"),
                    listOf("next to", "بجانب"),
                    listOf("between", "بين"),
                ),
                examples = listOf(
                    "The keys are [on] the table." means "المفاتيح على الطاولة.",
                    "The pharmacy is [between] the bank and the school." means "الصيدلية بين البنك والمدرسة.",
                ),
                check = q("The milk is ___ the fridge.", "in", "on", "between"),
            ),
        ),
        mistakes = listOf(
            mistake("There is two cars.", "There are two cars.", "two cars جمع، لذلك are."),
            mistake("I can to swim.", "I can swim.", "لا نضع to بعد can."),
            mistake("She cans cook.", "She can cook.", "can لا تأخذ s أبداً."),
        ),
        tip = "تذكر صورة «القطة والصندوق» عند كل حرف جر. ارسمها مرة واحدة على ورقة وستحفظ الحروف كلها.",
    ),

    // ---------------- A2 ----------------

    "a2-u1-l1" to guide(
        hook = "كل قصة تحكيها تبدأ في الماضي: «أمس ذهبت… رأيت… أكلت…». الماضي البسيط هو زمن القصص. والخبر السعيد: الفعل له شكل واحد مع كل الضمائر، لا s ولا تعقيد!",
        goals = listOf(
            "تحكي ما فعلته أمس أو في عطلتك",
            "تعرف الأفعال المنتظمة والشاذة",
            "تنفي وتسأل باستخدام did",
        ),
        concepts = listOf(
            concept(
                "أضف ed وانتقل إلى الماضي",
                "معظم الأفعال «منتظمة»: تضيف لها ed فقط. وهذا الشكل نفسه مع I و he و they وكل الضمائر.",
                formula = "الفاعل + الفعل + ed + وقت في الماضي",
                examples = listOf(
                    "I [visited] my grandmother yesterday." means "زرت جدتي أمس.",
                    "They [watched] a film last night." means "شاهدوا فيلماً ليلة أمس.",
                ),
                check = q("She ___ the door five minutes ago.", "opened", "opens", "open"),
            ),
            concept(
                "الأفعال المتمردة",
                "بعض الأفعال الأكثر استخداماً «شاذة» وتتغير بطريقتها الخاصة. لا قاعدة لها — تُحفظ كالكلمات الجديدة. ابدأ بالعشرة الأشهر:",
                table = table(
                    listOf("الفعل", "الماضي", "المعنى"),
                    listOf("go", "went", "ذهب"),
                    listOf("see", "saw", "رأى"),
                    listOf("eat", "ate", "أكل"),
                    listOf("have", "had", "امتلك / تناول"),
                    listOf("buy", "bought", "اشترى"),
                    listOf("take", "took", "أخذ"),
                ),
                examples = listOf(
                    "We [went] to the beach." means "ذهبنا إلى الشاطئ.",
                    "I [bought] a new phone." means "اشتريت هاتفاً جديداً.",
                ),
                check = q("He ___ a big pizza yesterday.", "ate", "eated", "eat"),
            ),
            concept(
                "did: مساعد الماضي",
                "للنفي والسؤال نستخدم did. وهنا الحيلة: did «تحمل» الماضي، فيعود الفعل إلى شكله الأصلي.",
                formula = "Did + الفاعل + الفعل الأصلي ?",
                examples = listOf(
                    "I [didn't go] to work." means "لم أذهب إلى العمل.",
                    "[Did] you [see] the match?" means "هل شاهدت المباراة؟",
                ),
                check = q("Did you ___ the email?", "read", "readed", "reads"),
            ),
        ),
        mistakes = listOf(
            mistake("I goed to school.", "I went to school.", "go فعل شاذ: went."),
            mistake("She didn't came.", "She didn't come.", "بعد didn't يعود الفعل لأصله."),
            mistake("Did you went?", "Did you go?", "did تحمل الماضي، فلا نكرره."),
        ),
        tip = "تخيل did «حقيبة الماضي»: إذا حملت الحقيبة (did/didn't) فإن الفعل يرتاح ويعود لأصله. إذا لم توجد حقيبة، يحمل الفعل الماضي بنفسه (went, played).",
    ),

    "a2-u2-l1" to guide(
        hook = "أيهما أكبر: القاهرة أم دبي؟ ما أطول برج في العالم؟ نحن نقارن طوال الوقت! في الإنجليزية الأمر يعتمد على طول الصفة: الصفة القصيرة تأخذ نهاية، والطويلة تأخذ كلمة قبلها.",
        goals = listOf(
            "تقارن بين شيئين",
            "تقول «الأفضل» و«الأكبر» بين مجموعة",
            "تحفظ الصفات الشاذة",
        ),
        concepts = listOf(
            concept(
                "المقارنة بين اثنين",
                "صفة قصيرة (مقطع واحد): أضف er ثم than. صفة طويلة (3 مقاطع أو أكثر): ضع more قبلها. تخيل أن الصفة الطويلة «ثقيلة» فلا تستطيع حمل er، فتستعين بـ more.",
                table = table(
                    listOf("النوع", "الصفة", "المقارنة"),
                    listOf("قصيرة", "tall", "taller than"),
                    listOf("تنتهي بـ y", "happy", "happier than"),
                    listOf("حرف مضاعف", "big", "bigger than"),
                    listOf("طويلة", "expensive", "more expensive than"),
                ),
                examples = listOf(
                    "My brother is [taller than] me." means "أخي أطول مني.",
                    "Gold is [more expensive than] silver." means "الذهب أغلى من الفضة.",
                ),
                check = q("Summer is ___ than winter.", "hotter", "more hot", "hotest"),
            ),
            concept(
                "الأفضل بين الجميع",
                "للتفضيل نستخدم the دائماً لأنه شيء واحد مميز. قصيرة: the + est. طويلة: the most.",
                formula = "the + صفة est  /  the most + صفة طويلة",
                examples = listOf(
                    "Burj Khalifa is [the tallest] building." means "برج خليفة هو أطول مبنى.",
                    "This is [the most beautiful] city." means "هذه أجمل مدينة.",
                ),
                check = q("Mount Everest is ___ mountain in the world.", "the highest", "the higher", "highest"),
            ),
            concept(
                "الصفات الشاذة",
                "ثلاث صفات مشهورة لا تتبع القواعد، ولكنها من أكثر الكلمات استخداماً:",
                table = table(
                    listOf("الصفة", "مقارنة", "تفضيل"),
                    listOf("good", "better", "the best"),
                    listOf("bad", "worse", "the worst"),
                    listOf("far", "further", "the furthest"),
                ),
                examples = listOf(
                    "Your English is [better] now." means "إنجليزيتك أفضل الآن.",
                    "It was [the worst] day ever." means "كان أسوأ يوم على الإطلاق.",
                ),
                check = q("This is the ___ pizza in town!", "best", "goodest", "better"),
            ),
        ),
        mistakes = listOf(
            mistake("more bigger", "bigger", "لا نجمع more مع er."),
            mistake("She is taller that me.", "She is taller than me.", "المقارنة تأتي مع than."),
            mistake("gooder", "better", "good صفة شاذة."),
        ),
        tip = "عدّ المقاطع بالتصفيق: «tall» تصفيقة واحدة ← taller. «ex-pen-sive» ثلاث تصفيقات ← more expensive.",
    ),

    "a2-u3-l1" to guide(
        hook = "هل الفرق بين «سأسافر» المخطط لها منذ شهر و«سأفتح الباب» التي قررتها الآن مهم؟ في الإنجليزية نعم! going to للخطط المسبقة، وwill للقرارات اللحظية والتوقعات.",
        goals = listOf(
            "تتحدث عن خططك",
            "تتخذ قرارات وتقدم وعوداً",
            "تفرق بين will و going to",
        ),
        concepts = listOf(
            concept(
                "going to: الخطة جاهزة",
                "استخدمه عندما تكون قد قررت مسبقاً، أو عندما ترى دليلاً أمامك. كأن الخطة مكتوبة في دفترك.",
                formula = "الفاعل + am / is / are + going to + الفعل",
                examples = listOf(
                    "I'm [going to] study medicine." means "سأدرس الطب (قررت ذلك).",
                    "Look at the clouds! It's [going to] rain." means "انظر إلى الغيوم! ستمطر.",
                ),
                check = q("We ___ going to visit Paris next month.", "are", "will", "is"),
            ),
            concept(
                "will: القرار في لحظته",
                "استخدمه للقرار الذي تتخذه في نفس لحظة الكلام، وللوعود والعروض، وللتوقعات الشخصية (I think…). will لا تتغير مع أي ضمير.",
                formula = "الفاعل + will / won't + الفعل",
                examples = listOf(
                    "The phone is ringing. — I[’ll] get it!" means "الهاتف يرن. — سأجيب أنا!",
                    "I promise I [won't] tell anyone." means "أعدك ألا أخبر أحداً.",
                    "I think it [will] be sunny." means "أظن أن الجو سيكون مشمساً.",
                ),
                check = q("I'm cold. — I ___ close the window.", "'ll", "'m going to", "am"),
            ),
        ),
        mistakes = listOf(
            mistake("I will to go.", "I will go.", "لا to بعد will."),
            mistake("She going to travel.", "She is going to travel.", "going to تحتاج is / am / are."),
            mistake("It wills rain.", "It will rain.", "will لا تتغير."),
        ),
        tip = "going to = «الخطة في الجيب» 📝، وwill = «الفكرة الآن» 💡. اسأل نفسك: هل قررت قبل أن أتكلم؟",
    ),

    "a2-u4-l1" to guide(
        hook = "هناك فرق بين «أنا أعمل في بنك» (عادة) و«أنا أعمل الآن على تقرير» (يحدث هذه اللحظة). الإنجليزية تفرق بينهما بزمنين. وفي هذا الدرس أيضاً: كيف تقول «بعض» و«كثير».",
        goals = listOf(
            "تصف ما يحدث الآن",
            "تفرق بين العادة واللحظة",
            "تستخدم some / any و much / many",
        ),
        concepts = listOf(
            concept(
                "المضارع المستمر: لقطة الكاميرا",
                "تخيل أنك تلتقط صورة الآن: ماذا يفعل الناس في الصورة؟ هذا هو المضارع المستمر.",
                formula = "الفاعل + am / is / are + الفعل + ing",
                examples = listOf(
                    "I [am reading] a book now." means "أقرأ كتاباً الآن.",
                    "The children [are playing] outside." means "الأطفال يلعبون في الخارج.",
                ),
                check = q("Listen! The baby ___.", "is crying", "cries", "cry"),
            ),
            concept(
                "العادة أم اللحظة؟",
                "كلمات مثل usually / every day تشير للمضارع البسيط. وكلمات مثل now / at the moment / Look! تشير للمستمر.",
                table = table(
                    listOf("المضارع البسيط (عادة)", "المضارع المستمر (الآن)"),
                    listOf("I usually wear jeans.", "Today I'm wearing a suit."),
                    listOf("She works in Cairo.", "She's working from home this week."),
                ),
                check = q("He usually ___ the bus, but today he's walking.", "takes", "is taking", "take"),
            ),
            concept(
                "كم؟ some / any / much / many",
                "some في الجمل المثبتة، any في النفي والسؤال. many مع ما يُعد (تفاحات، أشخاص)، much مع ما لا يُعد (ماء، مال، وقت).",
                table = table(
                    listOf("", "يُعد (books)", "لا يُعد (water)"),
                    listOf("سؤال الكمية", "How many?", "How much?"),
                    listOf("مثبت", "some books", "some water"),
                    listOf("نفي / سؤال", "any books", "any water"),
                ),
                examples = listOf(
                    "How [much] money do you have?" means "كم من المال لديك؟",
                    "There aren't [any] eggs." means "لا يوجد أي بيض.",
                ),
                check = q("How ___ brothers do you have?", "many", "much", "any"),
            ),
        ),
        mistakes = listOf(
            mistake("I reading now.", "I am reading now.", "المستمر يحتاج am / is / are."),
            mistake("How much people?", "How many people?", "people يُعد."),
            mistake("I don't have some money.", "I don't have any money.", "في النفي نستخدم any."),
        ),
        tip = "اسأل: هل أستطيع العد؟ «واحد، اثنان…» إذا نعم ← many. إذا لا (ماء، رمل) ← much.",
    ),
)
