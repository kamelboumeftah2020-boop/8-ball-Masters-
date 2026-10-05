package com.fluently.english.data.content

/** Vocabulary teaching for C1 and C2. */
internal val VocabGuidesC: Map<String, VocabGuide> = mapOf(

    "c1-u1-l2" to VocabGuide(
        hook = "الكلمات الأكاديمية هي «مفتاح» الجامعة واختبار IELTS والمقالات الرسمية. أغلبها من أصل لاتيني أو يوناني، لذلك تعلم «جذورها» يساعدك على فهم عشرات الكلمات الأخرى.",
        hints = mapOf(
            "hypothesis" to "hypo (تحت) + thesis (فكرة): فكرة «تحت» الاختبار لم تُثبت بعد.",
            "significant" to "من sign (علامة): شيء يترك علامة واضحة ← مهم وملحوظ.",
            "phenomenon" to "يوناني: «ما يظهر». الجمع phenomena!",
            "comprehensive" to "com (كل) + prehend (يمسك): يمسك بكل شيء ← شامل.",
            "subsequently" to "sub (بعد/تحت) + sequent (يتبع): ما يتبع ← لاحقاً.",
            "advocate" to "من voc (صوت): يرفع صوته لصالح فكرة ← يدافع عنها.",
            "undermine" to "under (تحت) + mine (يحفر): يحفر تحت الجدار حتى يسقط ← يقوّض.",
            "inevitable" to "in (لا) + evitable (يمكن تجنبه) = لا يمكن تجنبه.",
        ),
        groups = listOf(
            group("لغة البحث", "test a hypothesis / a significant increase / a comprehensive review", "hypothesis", "significant", "phenomenon", "comprehensive"),
            group("لغة الحجة", "advocate a policy / undermine trust / an inevitable result", "advocate", "undermine", "inevitable"),
            group("أدوات الربط الزمني", "تُستخدم في وصف الأحداث المتتالية في الكتابة الرسمية.", "subsequently"),
        ),
        story = "The researchers tested a [hypothesis] about social media and sleep. Their [comprehensive] study found a [significant] link between late-night scrolling and poor sleep — a [phenomenon] now seen worldwide. [Subsequently], doctors began to [advocate] screen-free evenings. Critics argued this could [undermine] personal freedom, but some change seems [inevitable].",
        storyAr = "اختبر الباحثون فرضية حول وسائل التواصل والنوم. وجدت دراستهم الشاملة صلة ملحوظة بين التصفح ليلاً وسوء النوم — وهي ظاهرة تُرى الآن في كل العالم. لاحقاً بدأ الأطباء يدعون إلى أمسيات بلا شاشات. جادل المنتقدون بأن هذا قد يقوّض الحرية الشخصية، لكن بعض التغيير يبدو حتمياً.",
        checks = listOf(
            q("The scientists' ___ was proved correct by the experiment.", "hypothesis", "phenomenon", "advocate"),
            q("Death and taxes are ___.", "inevitable", "comprehensive", "significant"),
        ),
        mistakes = listOf(
            mistake("These phenomenons are rare.", "These phenomena are rare.", "جمع phenomenon شاذ: phenomena."),
        ),
        tip = "تعلم الجذور: sign (علامة) تعطيك signal, significant, signature, design — كلها مرتبطة!",
    ),

    "c1-u2-l2" to VocabGuide(
        hook = "التعابير الاصطلاحية (idioms) لا تُفهم من كلماتها: «cost an arm and a leg» لا تعني أنك ستدفع ذراعك! هي مثل أمثالنا العربية تماماً، وكثير منها له مقابل عربي جميل.",
        hints = mapOf(
            "break the ice" to "تخيل جليداً بين شخصين غريبين، والنكتة تكسره ❄️.",
            "hit the nail on the head" to "ضرب المسمار على رأسه تماماً ← أصاب الهدف بدقة.",
            "a blessing in disguise" to "نعمة متنكرة في شكل مصيبة = «رُبّ ضارة نافعة».",
            "cost an arm and a leg" to "غالٍ لدرجة أنك تدفع أعضاءك!",
            "on the fence" to "تجلس على السور بين حديقتين — لم تختر أي جهة.",
            "burn the midnight oil" to "قديماً كانوا يحرقون زيت المصباح ليلاً للدراسة.",
            "the last straw" to "القشة الأخيرة التي كسرت ظهر الجمل — نفس المثل العربي!",
            "once in a blue moon" to "القمر الأزرق ظاهرة نادرة جداً ← نادراً جداً.",
        ),
        groups = listOf(
            group("مواقف اجتماعية", "A joke can break the ice. / You hit the nail on the head!", "break the ice", "hit the nail on the head", "on the fence"),
            group("المال والعمل", "That car cost an arm and a leg. / She burned the midnight oil.", "cost an arm and a leg", "burn the midnight oil"),
            group("الحياة والأحداث", "Losing that job was a blessing in disguise.", "a blessing in disguise", "the last straw", "once in a blue moon"),
        ),
        story = "When Karim lost his job, his boss's rude email was [the last straw]. But it was [a blessing in disguise]. He [burned the midnight oil] to start his own café. Rent [cost an arm and a leg], and his family was [on the fence] about the idea. On opening day, he told a joke to [break the ice] with customers. Now he only takes a day off [once in a blue moon]! His friend said, «Following your dream — you [hit the nail on the head]!»",
        storyAr = "عندما فقد كريم وظيفته، كانت رسالة مديره الفظة القشة الأخيرة. لكنها كانت رُبّ ضارة نافعة. سهر الليالي ليبدأ مقهاه الخاص. كان الإيجار يكلف ثروة، وكانت عائلته مترددة بشأن الفكرة. في يوم الافتتاح ألقى نكتة ليكسر الجمود مع الزبائن. الآن نادراً جداً ما يأخذ يوم عطلة! قال صديقه: «اتباع حلمك — أصبت كبد الحقيقة!»",
        checks = listOf(
            q("I can't decide which university to choose. I'm ___.", "on the fence", "breaking the ice", "the last straw"),
            q("We only go to the cinema ___. Maybe once a year.", "once in a blue moon", "on the fence", "at the last straw"),
        ),
        mistakes = listOf(
            mistake("It cost an arm and leg.", "It cost an arm and a leg.", "التعابير ثابتة — لا تحذف كلمة."),
        ),
        tip = "ابحث عن المقابل العربي لكل تعبير: the last straw = القشة التي قصمت ظهر البعير. الربط بالعربية يثبتها فوراً.",
    ),

    "c1-u3-l2" to VocabGuide(
        hook = "أدوات الربط هي «الغراء» الذي يجعل كتابتك مترابطة ومقنعة. في اختبار IELTS جزء من الدرجة مخصص لـ cohesion — أي كيف تربط أفكارك.",
        hints = mapOf(
            "nevertheless" to "never + the + less = «لم يقلّ ذلك» ← ومع ذلك.",
            "whereas" to "للمقارنة بين شيئين متقابلين: A is…, whereas B is…",
            "furthermore" to "further (أبعد) + more = تذهب أبعد بإضافة فكرة.",
            "consequently" to "من consequence (نتيجة) ← وبالتالي.",
            "in spite of" to "يأتي بعدها اسم أو ing: in spite of the rain / of being tired.",
            "provided that" to "«بشرط أن» — أقوى وأكثر رسمية من if.",
            "thereby" to "there + by = «بذلك» — تربط فعلاً بنتيجته.",
            "notwithstanding" to "not + withstanding (مقاومة): ما لا يمنع ← بصرف النظر عن.",
        ),
        groups = listOf(
            group("للإضافة والنتيجة", "Furthermore, … / Consequently, … / …, thereby…", "furthermore", "consequently", "thereby"),
            group("للتضاد والتنازل", "Nevertheless, … / whereas … / In spite of … / Notwithstanding …", "nevertheless", "whereas", "in spite of", "notwithstanding"),
            group("للشرط", "You may leave early provided that you finish.", "provided that"),
        ),
        story = "Online learning is flexible, [whereas] classroom learning offers social contact. [Furthermore], online courses are often cheaper. [Nevertheless], many students miss their friends. [In spite of] this, numbers keep rising; [consequently], universities are investing more, [thereby] reaching new learners. [Notwithstanding] its limits, online study works well, [provided that] students stay motivated.",
        storyAr = "التعلم عبر الإنترنت مرن، في حين يوفر التعلم في الصف تواصلاً اجتماعياً. علاوة على ذلك، الدورات الإلكترونية غالباً أرخص. ومع ذلك يفتقد كثير من الطلاب أصدقاءهم. على الرغم من هذا تستمر الأعداد في الارتفاع؛ وبالتالي تستثمر الجامعات أكثر، وبذلك تصل إلى متعلمين جدد. بصرف النظر عن حدوده، الدراسة الإلكترونية تنجح، شريطة أن يبقى الطلاب متحمسين.",
        checks = listOf(
            q("He studied hard; ___, he passed with high marks.", "consequently", "whereas", "in spite of"),
            q("___ the bad weather, the event was a success.", "In spite of", "Furthermore", "Provided that"),
        ),
        mistakes = listOf(
            mistake("In spite of it was raining, …", "In spite of the rain, … / Although it was raining, …", "in spite of لا يأتي بعدها جملة كاملة."),
        ),
        tip = "في كل فقرة تكتبها، استخدم أداة ربط واحدة على الأقل من هذه القائمة — وستلاحظ الفرق في جودة كتابتك.",
    ),

    "c1-u4-l2" to VocabGuide(
        hook = "لغة الأعمال لها مفرداتها الخاصة. في الاجتماعات والمفاوضات الدولية، هذه الكلمات تجعلك تبدو محترفاً وتفهم ما يدور حولك.",
        hints = mapOf(
            "stakeholder" to "stake = حصة. stakeholder = كل من له «حصة» أو مصلحة في المشروع.",
            "leverage" to "lever = رافعة: تستخدم ما لديك كرافعة لتحقيق المزيد.",
            "compromise" to "com (معاً) + promise (وعد): الطرفان يعدان بالتنازل قليلاً.",
            "revenue" to "re + venue (يأتي): المال الذي «يعود» للشركة ← إيرادات.",
            "merger" to "merge = يدمج. merger = اندماج شركتين.",
            "feasible" to "من الفرنسية «faire» (يفعل): يمكن فعله ← ممكن التنفيذ.",
            "concession" to "من concede (يتنازل/يعترف). make a concession.",
            "win-win" to "الطرفان يربحان ← حل مربح للجميع.",
        ),
        groups = listOf(
            group("في الشركة", "increase revenue / consult stakeholders / announce a merger", "stakeholder", "revenue", "merger"),
            group("في التفاوض", "reach a compromise / make a concession / a win-win deal", "compromise", "concession", "win-win", "leverage"),
            group("التقييم", "Is the plan financially feasible?", "feasible"),
        ),
        story = "Two tech companies discussed a [merger]. Both wanted to grow [revenue], but they disagreed on price. Each side tried to [leverage] its strengths. After long talks, they reached a [compromise]: one side made a [concession] on price, the other on management. All [stakeholder]s agreed the plan was [feasible] — a true [win-win].",
        storyAr = "ناقشت شركتا تقنية الاندماج. أرادت كلتاهما زيادة الإيرادات، لكنهما اختلفتا على السعر. حاول كل طرف الاستفادة من نقاط قوته. بعد محادثات طويلة توصلتا إلى حل وسط: قدم طرف تنازلاً في السعر والآخر في الإدارة. وافق جميع أصحاب المصلحة على أن الخطة ممكنة التنفيذ — حل مربح للطرفين حقاً.",
        checks = listOf(
            q("Both sides gave up something to agree. They reached a ___.", "compromise", "revenue", "merger"),
            q("The plan is too expensive. It isn't ___.", "feasible", "win-win", "stakeholder"),
        ),
        mistakes = listOf(
            mistake("We did a compromise.", "We reached / made a compromise.", "التلازم: reach a compromise."),
        ),
        tip = "تابع عناوين الأخبار الاقتصادية بالإنجليزية (BBC Business مثلاً) — ستجد merger و revenue يومياً.",
    ),

    "c2-u1-l2" to VocabGuide(
        hook = "في المستوى C2 لا يكفي أن تقول «good» أو «clear» — المتحدث المحترف يختار الكلمة الدقيقة. هذه كلمات «راقية» تجعل كلامك وكتابتك أكثر دقة وأناقة.",
        hints = mapOf(
            "meticulous" to "أكثر من careful: دقيق في أصغر التفاصيل.",
            "ubiquitous" to "من اللاتينية ubique = في كل مكان.",
            "ephemeral" to "يونانية: «ليوم واحد» — مثل الفراشة التي تعيش يوماً.",
            "pragmatic" to "يركز على ما ينجح عملياً وليس على المثاليات.",
            "eloquent" to "e (خارج) + loqu (يتكلم): الكلام يخرج بسلاسة وجمال.",
            "ambiguous" to "ambi = اثنان (مثل ambidextrous): يحتمل معنيين ← غامض.",
            "candid" to "من اللاتينية «أبيض/نقي»: صريح بلا تجميل. candid photo = صورة عفوية.",
            "scrutinise" to "من «scruta» (القمامة): البحث في كل قطعة صغيرة ← فحص دقيق.",
        ),
        groups = listOf(
            group("صفات الأشخاص", "a meticulous researcher / an eloquent speaker / a candid answer / a pragmatic approach", "meticulous", "eloquent", "candid", "pragmatic"),
            group("صفات الأشياء والأفكار", "ubiquitous smartphones / ephemeral fame / ambiguous wording", "ubiquitous", "ephemeral", "ambiguous"),
            group("الفعل", "Experts will scrutinise the data.", "scrutinise"),
        ),
        story = "Smartphones are now [ubiquitous], and online fame is often [ephemeral]. A [meticulous] journalist decided to [scrutinise] a viral post whose wording was deliberately [ambiguous]. In an [eloquent] article, she gave a [candid] verdict, recommending a [pragmatic] approach: check before you share.",
        storyAr = "أصبحت الهواتف الذكية منتشرة في كل مكان، والشهرة على الإنترنت غالباً زائلة. قررت صحفية دقيقة للغاية أن تفحص بعناية منشوراً واسع الانتشار كانت صياغته غامضة عمداً. في مقال بليغ قدمت حكماً صريحاً، وأوصت بنهج عملي: تحقق قبل أن تشارك.",
        checks = listOf(
            q("The contract can be read in two ways. It's ___.", "ambiguous", "candid", "meticulous"),
            q("Coffee shops are everywhere in this city. They're ___.", "ubiquitous", "ephemeral", "pragmatic"),
        ),
        mistakes = listOf(
            mistake("He is a meticulous of details.", "He is meticulous about details.", "meticulous about / in."),
        ),
        tip = "بدل very careful قل meticulous، وبدل very clear speaker قل eloquent. استبدل «very + صفة» بكلمة واحدة دقيقة.",
    ),

    "c2-u2-l2" to VocabGuide(
        hook = "تعابير المستوى C2 تجدها في افتتاحيات الصحف والخطابات السياسية. استخدامها في الوقت المناسب يُظهر أنك تفهم ثقافة اللغة، لا كلماتها فقط.",
        hints = mapOf(
            "a double-edged sword" to "سيف بحدين يجرح صاحبه أيضاً = «سلاح ذو حدين» تماماً كالعربية!",
            "to cut corners" to "من يقطع زوايا الطريق ليصل أسرع — لكن بشكل غير صحيح.",
            "a moot point" to "moot = نقاش قديم في المحاكم: مسألة لم تعد مهمة عملياً.",
            "to pay lip service" to "يدفع «بالشفاه» فقط — كلام بلا فعل.",
            "the tip of the iceberg" to "90% من جبل الجليد مخفي تحت الماء.",
            "to take something with a pinch of salt" to "قديماً: الطعام المسموم «يُبلع» مع الملح ← تشكك قبل أن تصدق.",
            "a Pyrrhic victory" to "الملك بيروس انتصر على الرومان لكنه خسر معظم جيشه.",
            "to play devil's advocate" to "في الفاتيكان كان شخص يُعيَّن ليجادل ضد تطويب القديسين.",
        ),
        groups = listOf(
            group("التقييم والحكم", "Social media is a double-edged sword. / Winning was a Pyrrhic victory.", "a double-edged sword", "a Pyrrhic victory", "the tip of the iceberg"),
            group("السلوك", "Don't cut corners. / They only pay lip service to equality.", "to cut corners", "to pay lip service"),
            group("في النقاش", "Let me play devil's advocate. / Take it with a pinch of salt. / That's a moot point.", "to play devil's advocate", "to take something with a pinch of salt", "a moot point"),
        ),
        story = "The company won the lawsuit, but it was [a Pyrrhic victory] — the legal costs were enormous. Investigators found that managers had [cut corners] on safety, and this was just [the tip of the iceberg]. The CEO [paid lip service] to reform. «Let me [play devil's advocate]», said one analyst: «technology is [a double-edged sword].» Whether he knew is now [a moot point]. Take his promises [with a pinch of salt].",
        storyAr = "ربحت الشركة القضية، لكنه كان نصراً باهظ الثمن — فالتكاليف القانونية كانت ضخمة. وجد المحققون أن المديرين تهاونوا في السلامة، وكان هذا رأس جبل الجليد فقط. أيّد الرئيس التنفيذي الإصلاح بالكلام فقط. قال أحد المحللين: «دعوني أتبنى الرأي المعاكس: التكنولوجيا سلاح ذو حدين». سواء كان يعلم أم لا فهي مسألة بلا أهمية الآن. تعامل مع وعوده بحذر.",
        checks = listOf(
            q("We discovered a few problems, but they're only ___.", "the tip of the iceberg", "a moot point", "a double-edged sword"),
            q("The builders used cheap materials to save time. They ___.", "cut corners", "paid lip service", "played devil's advocate"),
        ),
        mistakes = listOf(
            mistake("It's a mute point.", "It's a moot point.", "moot وليس mute (صامت)."),
        ),
        tip = "عند قراءة مقال رأي بالإنجليزية، ابحث عن تعبير واحد على الأقل من هذه القائمة — ستجدها أكثر مما تتوقع.",
    ),

    "c2-u3-l2" to VocabGuide(
        hook = "الكلمة نفسها قد تكون مناسبة في رسالة لصديق ومحرجة في رسالة رسمية. «Let's start» مع صديق، و«We shall commence» في حفل رسمي. هذا الدرس عن اختيار مستوى اللغة المناسب.",
        hints = mapOf(
            "commence" to "تشبه «commencement» = حفل التخرج الرسمي.",
            "terminate" to "تذكر فيلم Terminator: «المُنهي».",
            "ascertain" to "as + certain (متأكد) = يصبح متأكداً ← يتحقق.",
            "endeavour" to "en + devoir (الواجب بالفرنسية): يبذل واجبه ← يسعى.",
            "sufficient" to "عكسها insufficient. أكثر رسمية من enough.",
            "purchase" to "تكثر في المواقع والإعلانات الرسمية: purchase online.",
            "assist" to "assistant = مساعد. أكثر رسمية من help.",
            "reside" to "residence = مكان الإقامة. أكثر رسمية من live.",
        ),
        groups = listOf(
            group("أفعال رسمية", "start→commence، end→terminate، find out→ascertain، try→endeavour", "commence", "terminate", "ascertain", "endeavour"),
            group("في الخدمات والمستندات", "buy→purchase، help→assist، live→reside، enough→sufficient", "purchase", "assist", "reside", "sufficient"),
        ),
        story = "Dear Customer, we wish to inform you that your contract will [terminate] on 30 June. Should you wish to [purchase] a new plan, our staff will be happy to [assist] you. Please [ascertain] that you [reside] at the address on file, as [sufficient] proof of address is required. We [endeavour] to reply within 24 hours. The new service will [commence] on 1 July.",
        storyAr = "عزيزنا العميل، نود إعلامك بأن عقدك سينتهي في 30 يونيو. إذا رغبت في شراء باقة جديدة فسيسعد موظفونا بمساعدتك. يرجى التحقق من أنك تقيم في العنوان المسجل، إذ يلزم إثبات كافٍ للعنوان. نسعى للرد خلال 24 ساعة. ستبدأ الخدمة الجديدة في 1 يوليو.",
        checks = listOf(
            q("Formal version of «Tickets can be bought online»: Tickets can be ___ online.", "purchased", "commenced", "resided"),
            q("Formal version of «The meeting will start at 9»: The meeting will ___ at 9.", "commence", "terminate", "assist"),
        ),
        mistakes = listOf(
            mistake("Hey mate, I wish to ascertain if you're coming tonight.", "Hey, can you find out if you're coming tonight?", "الكلمة الرسمية في رسالة غير رسمية تبدو غريبة."),
        ),
        tip = "قبل الكتابة اسأل: لمن أكتب؟ صديق ← كلمات بسيطة. جهة رسمية ← purchase / assist / commence.",
    ),

    "c2-u4-l2" to VocabGuide(
        hook = "الأفعال المركبة المتقدمة هي ما يميّز المتحدث الطليق عن الكتابي. الناطقون يقولون «Don't bank on it» بدل «Don't rely on it». تعلّمها بالصور يجعلها سهلة.",
        hints = mapOf(
            "brush up on" to "تمسح الغبار بالفرشاة عن معلومات قديمة ← تُنعشها.",
            "come across as" to "الانطباع الذي «يعبر» منك إلى الآخرين.",
            "gloss over" to "gloss = لمعان: تلمّع السطح لتخفي ما تحته.",
            "iron out" to "تكوي (iron) القميص لتزيل التجاعيد ← تحل المشكلات الصغيرة.",
            "bank on" to "تضع أملك في «البنك» كأنه مضمون ← تعتمد عليه.",
            "factor in" to "factor = عامل: تُدخل العامل في المعادلة ← تأخذه في الحسبان.",
            "rule out" to "تضع الاحتمال خارج (out) الخط بالمسطرة (rule) ← تستبعده.",
            "live up to" to "تعيش (live) على مستوى (up to) التوقعات.",
        ),
        groups = listOf(
            group("في العمل", "iron out details / factor in costs / rule out an option", "iron out", "factor in", "rule out"),
            group("الانطباع والتوقعات", "He comes across as arrogant. / The film didn't live up to the hype.", "come across as", "live up to", "gloss over"),
            group("الاستعداد والثقة", "brush up on your French / don't bank on it", "brush up on", "bank on"),
        ),
        story = "Before the interview, Lina [brushed up on] her technical skills. She didn't want to [come across as] unprepared. The manager didn't [gloss over] the company's problems: «We still need to [iron out] some issues.» Lina asked if they had [factored in] remote work. «We haven't [ruled] it [out], but don't [bank on] it.» The job [lived up to] her expectations.",
        storyAr = "قبل المقابلة أنعشت لينا مهاراتها التقنية. لم تكن تريد أن تبدو غير مستعدة. لم يتجاوز المدير مشكلات الشركة بسطحية: «ما زلنا بحاجة لحل بعض المشكلات». سألت لينا إن كانوا أخذوا العمل عن بعد في الحسبان. «لم نستبعده، لكن لا تعتمدي عليه». وكانت الوظيفة على مستوى توقعاتها.",
        checks = listOf(
            q("The police have ___ the possibility of murder. It was an accident.", "ruled out", "banked on", "brushed up on"),
            q("The hotel didn't ___ the photos on its website. It was disappointing.", "live up to", "iron out", "factor in"),
        ),
        mistakes = listOf(
            mistake("I need to brush up my English on.", "I need to brush up on my English.", "brush up on + الموضوع."),
        ),
        tip = "اختر فعلاً مركباً واحداً كل يوم واستخدمه في رسالة أو منشور حقيقي — الاستخدام الحقيقي يثبت الكلمة.",
    ),
)

/** All vocabulary teaching, keyed by lesson id. */
val VocabGuides: Map<String, VocabGuide> = VocabGuidesA + VocabGuidesB + VocabGuidesC + VocabGuidesU5
