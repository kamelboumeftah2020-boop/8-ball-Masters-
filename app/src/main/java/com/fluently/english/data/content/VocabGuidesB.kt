package com.fluently.english.data.content

/** Vocabulary teaching for B1 and B2. */
internal val VocabGuidesB: Map<String, VocabGuide> = mapOf(

    "b1-u1-l2" to VocabGuide(
        hook = "كيف تصف شعورك عندما تنجح؟ أو عندما تقع أمام الناس؟ كلمات المشاعر تجعل قصصك حيّة وتساعدك أن تعبّر عن نفسك بدقة بدل «good» و«bad» فقط.",
        hints = mapOf(
            "proud" to "proud of + شيء: I'm proud of you. تخيل طاووساً فخوراً بريشه 🦚.",
            "embarrassed" to "حرفا r و s مضاعفان — «محرج» لدرجة أن الحروف تتكرر!",
            "excited" to "excited about = متحمس لـ. وexciting = مثير (الشيء نفسه).",
            "disappointed" to "dis (عكس) + appointed: كأن موعدك أُلغي ← خيبة أمل.",
            "achievement" to "من achieve (يحقق): achievement = ما حققته.",
            "challenge" to "مثل «تشالنج» الشائعة على وسائل التواصل!",
            "memorable" to "memory (ذاكرة) + able = يمكن تذكره ← لا يُنسى.",
            "nervous" to "nerves = الأعصاب ← متوتر الأعصاب قبل الامتحان.",
        ),
        groups = listOf(
            group("مشاعر إيجابية", "I felt so proud! / I'm excited about the trip.", "proud", "excited"),
            group("مشاعر سلبية", "-ed للشعور: I'm embarrassed. و -ing للسبب: It's embarrassing.", "embarrassed", "disappointed", "nervous"),
            group("التجارب", "a big challenge / a great achievement / a memorable day", "achievement", "challenge", "memorable"),
        ),
        story = "My first job interview was a real [challenge]. I was so [nervous] that my hands were shaking! Then I spilled water on the table — I was really [embarrassed]. But I got the job! My parents were [proud] of me. It was a [memorable] day and my biggest [achievement]. I was [excited], not [disappointed] at all.",
        storyAr = "كانت أول مقابلة عمل لي تحدياً حقيقياً. كنت متوتراً لدرجة أن يديّ كانتا ترتجفان! ثم سكبت الماء على الطاولة — كنت محرجاً جداً. لكنني حصلت على الوظيفة! كان والداي فخورين بي. كان يوماً لا يُنسى وأكبر إنجاز لي. كنت متحمساً ولم أكن خائب الأمل أبداً.",
        checks = listOf(
            q("I failed the test. I'm really ___.", "disappointed", "proud", "memorable"),
            q("I have an exam tomorrow and I feel ___.", "nervous", "achievement", "challenge"),
        ),
        mistakes = listOf(
            mistake("I'm very exciting about the trip.", "I'm very excited about the trip.", "excited للشعور، exciting لوصف الشيء."),
            mistake("I'm proud on you.", "I'm proud of you.", "proud تأتي مع of."),
        ),
        tip = "ربط الكلمة بذكرى شخصية يجعلها لا تُنسى: تذكر موقفاً شعرت فيه بالإحراج وقل: I was embarrassed when…",
    ),

    "b1-u2-l2" to VocabGuide(
        hook = "تغير المناخ من أكثر المواضيع حضوراً في الأخبار والامتحانات الدولية (IELTS يسأل عنه كثيراً!). هذه الكلمات تساعدك أن تفهم الأخبار وتعبر عن رأيك.",
        hints = mapOf(
            "pollution" to "pollute (يلوّث) + tion. air / water pollution.",
            "recycle" to "re (مرة أخرى) + cycle (دورة) = يعيد إلى الدورة.",
            "climate change" to "climate = المناخ (طويل المدى)، weather = الطقس (اليوم).",
            "waste" to "اسم: نفايات. وفعل: يهدر — Don't waste time/water.",
            "renewable energy" to "re + new: طاقة «تتجدد» مثل الشمس والرياح.",
            "protect" to "pro (أمام) + tect (يغطي): يقف أمامه ليحميه 🛡️.",
            "flood" to "oo تُنطق «أ» هنا: «فلَد».",
            "drought" to "عكس flood: ماء كثير جداً ↔ لا ماء. gh صامتة: «دراوت».",
        ),
        groups = listOf(
            group("المشكلات", "cause pollution / suffer from drought / be hit by floods", "pollution", "climate change", "flood", "drought", "waste"),
            group("الحلول", "We should recycle more and use renewable energy.", "recycle", "renewable energy", "protect"),
        ),
        story = "[Climate change] is a real problem. Some countries suffer from [drought], while others have terrible [flood]s. Air [pollution] makes people ill. What can we do? We can [recycle] plastic, stop [waste]-ing water, use [renewable energy] like solar power, and [protect] our forests.",
        storyAr = "تغير المناخ مشكلة حقيقية. بعض الدول تعاني من الجفاف، بينما تعاني دول أخرى من فيضانات رهيبة. تلوث الهواء يُمرض الناس. ماذا نستطيع أن نفعل؟ نستطيع إعادة تدوير البلاستيك، والتوقف عن هدر الماء، واستخدام الطاقة المتجددة مثل الطاقة الشمسية، وحماية غاباتنا.",
        checks = listOf(
            q("It hasn't rained for two years. There's a terrible ___.", "drought", "flood", "recycle"),
            q("Solar and wind power are types of ___.", "renewable energy", "pollution", "waste"),
        ),
        mistakes = listOf(
            mistake("The climate today is sunny.", "The weather today is sunny.", "climate للمدى الطويل، weather لليوم."),
        ),
        tip = "اقرأ عنواناً إخبارياً واحداً عن البيئة بالإنجليزية كل يوم — ستجد هذه الكلمات تتكرر.",
    ),

    "b1-u3-l2" to VocabGuide(
        hook = "زيارة الطبيب في بلد أجنبي قد تكون مخيفة إذا لم تعرف الكلمات. تعلم كيف تصف ما تشعر به وتفهم ما يقوله الطبيب.",
        hints = mapOf(
            "headache" to "head (رأس) + ache (ألم). وبالمثل: toothache, stomachache.",
            "fever" to "have a fever / a high fever. تُنطق «فيفر».",
            "prescription" to "pre (قبل) + script (كتابة): يكتبها الطبيب قبل أن تأخذ الدواء.",
            "injury" to "من injure (يجرح/يصيب). sports injury = إصابة رياضية.",
            "recover" to "re + cover: «يستعيد» صحته. recover from an illness.",
            "healthy" to "health (صحة) + y = صحي. healthy food / a healthy lifestyle.",
            "symptom" to "p صامتة تقريباً: «سِمتوم». العرَض الذي يدل على المرض.",
            "appointment" to "make an appointment = يحجز موعداً (عند الطبيب أو المحامي).",
        ),
        groups = listOf(
            group("الأعراض والمشكلات", "I've got a headache / a fever. — What are your symptoms?", "headache", "fever", "injury", "symptom"),
            group("العلاج", "make an appointment → get a prescription → recover", "appointment", "prescription", "recover", "healthy"),
        ),
        story = "Last week I felt terrible. I had a bad [headache] and a high [fever]. I made an [appointment] with my doctor. She asked about my [symptom]s and gave me a [prescription]. She said, «Eat [healthy] food and rest.» Now I've [recover]ed completely. My brother wasn't so lucky — he had a knee [injury] playing football!",
        storyAr = "الأسبوع الماضي شعرت بتعب شديد. كان لدي صداع قوي وحمى مرتفعة. حجزت موعداً مع طبيبتي. سألت عن أعراضي وأعطتني وصفة طبية. قالت: «تناول طعاماً صحياً وارتح». الآن تعافيت تماماً. أخي لم يكن محظوظاً — أصيب في ركبته وهو يلعب كرة القدم!",
        checks = listOf(
            q("The doctor wrote a ___ for some medicine.", "prescription", "symptom", "fever"),
            q("A cough and a sore throat are ___ of a cold.", "symptoms", "appointments", "injuries"),
        ),
        mistakes = listOf(
            mistake("I have pain in my head.", "I have a headache.", "الأكثر طبيعية: headache."),
            mistake("I took an appointment.", "I made an appointment.", "التلازم الصحيح: make an appointment."),
        ),
        tip = "تعلم «عائلة ache»: headache, toothache, backache, stomachache — كلمة واحدة تعطيك أربع!",
    ),

    "b1-u4-l2" to VocabGuide(
        hook = "الأفعال المركبة (phrasal verbs) هي «سر» الإنجليزية اليومية: الناطقون يقولون give up بدل surrender، وfind out بدل discover. معناها غالباً لا يُفهم من أجزائها — لذلك نتعلمها بالصور والقصص.",
        hints = mapOf(
            "give up" to "تخيل شخصاً يرفع يديه للأعلى (up) مستسلماً.",
            "look after" to "تنظر (look) خلف (after) الطفل لتحميه ← يعتني.",
            "find out" to "تجد (find) المعلومة وتخرجها (out) للضوء ← يكتشف.",
            "turn down" to "تدير (turn) زر الصوت للأسفل (down)، أو «تخفض» العرض ← يرفض.",
            "get on with" to "تركبان (get on) نفس القطار بسلام ← تنسجمان.",
            "set off" to "تنطلق من نقطة البداية مثل السهم.",
            "run out of" to "الشيء «يركض خارجاً» حتى لا يتبقى منه شيء ← ينفد.",
            "put off" to "تضع (put) المهمة بعيداً (off) ← تؤجلها.",
        ),
        groups = listOf(
            group("مع الناس", "I get on well with my boss. / She looks after her grandmother.", "look after", "get on with", "turn down"),
            group("المهام والقرارات", "Don't give up! / Don't put off your homework.", "give up", "put off", "find out"),
            group("الرحلات والأشياء", "We set off at 6. / We've run out of petrol.", "set off", "run out of"),
        ),
        story = "We [set off] early for the mountains. Halfway there, we [ran out of] petrol! I wanted to [give up] and go home, but my friend said, «Never [put off] an adventure!» We walked to a village, where an old man [looked after] us. We [found out] that he was a famous painter. We [got on] really well [with] him. He even offered us dinner — and we didn't [turn] it [down]!",
        storyAr = "انطلقنا مبكراً إلى الجبال. في منتصف الطريق نفد منا البنزين! أردت أن أستسلم وأعود للبيت، لكن صديقي قال: «لا تؤجل مغامرة أبداً!» مشينا إلى قرية حيث اعتنى بنا رجل عجوز. اكتشفنا أنه رسام مشهور. انسجمنا معه كثيراً. حتى أنه عرض علينا العشاء — ولم نرفضه!",
        checks = listOf(
            q("We have no milk. We've ___ milk.", "run out of", "given up", "set off"),
            q("I won't do it today; I'll ___ it until tomorrow.", "put off", "look after", "find out"),
        ),
        mistakes = listOf(
            mistake("He turned down it.", "He turned it down.", "الضمير بين الفعل وحرف الجر."),
            mistake("I give up smoke.", "I gave up smoking.", "بعد give up يأتي فعل + ing."),
        ),
        tip = "لا تحفظ phrasal verbs منفردة — احفظها في جملة كاملة من حياتك: I need to give up sugar!",
    ),

    "b2-u1-l2" to VocabGuide(
        hook = "التكنولوجيا تتغير كل يوم، وكلماتها تدخل لغتنا العربية أيضاً (تحديث، تحميل، ذكاء اصطناعي). في هذا الدرس كلمات تحتاجها لقراءة الأخبار التقنية والحديث عنها.",
        hints = mapOf(
            "device" to "أي جهاز إلكتروني: phones, tablets and other devices.",
            "update" to "up (لأعلى) + date (تاريخ): تجعله بآخر تاريخ.",
            "download" to "down (للأسفل) من الإنترنت إليك. وعكسها upload.",
            "artificial intelligence" to "artificial = صناعي (عكس طبيعي). اختصارها AI.",
            "privacy" to "من private (خاص). privacy settings = إعدادات الخصوصية.",
            "cutting-edge" to "«الحافة القاطعة» للسكين = الجزء الأمامي ← الأحدث والأكثر تطوراً.",
            "breakthrough" to "break (يكسر) + through (عبر): يكسر الحاجز ويعبر ← اكتشاف كبير.",
            "obsolete" to "من «أوبسوليت» — قديم لدرجة أنه لم يعد يُستخدم مثل الفاكس.",
        ),
        groups = listOf(
            group("الاستخدام اليومي", "update your phone / download an app / turn off your device", "device", "update", "download"),
            group("التقنية المتقدمة", "a major breakthrough in AI / cutting-edge technology", "artificial intelligence", "cutting-edge", "breakthrough"),
            group("القضايا", "privacy concerns / obsolete technology", "privacy", "obsolete"),
        ),
        story = "Ten years ago, my phone was [cutting-edge]. Today it's almost [obsolete] — I can't even [download] new apps or [update] the system! Modern [device]s use [artificial intelligence] to answer questions. Scientists call it a [breakthrough], but many people worry about their [privacy].",
        storyAr = "قبل عشر سنوات كان هاتفي من أحدث التقنيات. اليوم أصبح قديماً تقريباً — لا أستطيع حتى تنزيل تطبيقات جديدة أو تحديث النظام! الأجهزة الحديثة تستخدم الذكاء الاصطناعي للإجابة عن الأسئلة. يسميه العلماء إنجازاً كبيراً، لكن كثيرين قلقون على خصوصيتهم.",
        checks = listOf(
            q("Nobody uses fax machines now. They're ___.", "obsolete", "cutting-edge", "updated"),
            q("I don't want apps to collect my data. I care about my ___.", "privacy", "breakthrough", "device"),
        ),
        mistakes = listOf(
            mistake("I downloaded my photo to Instagram.", "I uploaded my photo to Instagram.", "upload = ترفع، download = تنزّل."),
        ),
        tip = "غيّر لغة هاتفك إلى الإنجليزية لأسبوع — ستتعلم كلمات التقنية من الاستخدام اليومي.",
    ),

    "b2-u2-l2" to VocabGuide(
        hook = "لماذا نقول make a mistake وليس do a mistake؟ لا توجد قاعدة 100%، لكن هناك فكرة تساعدك: make = تصنع شيئاً جديداً أو نتيجة، do = تنفذ نشاطاً أو عملاً. هذه «المتلازمات» تجعل إنجليزيتك تبدو طبيعية.",
        hints = mapOf(
            "make a decision" to "تصنع (make) قراراً جديداً لم يكن موجوداً.",
            "make progress" to "تصنع تقدماً — نتيجة جديدة لجهدك.",
            "make a mistake" to "الخطأ «تصنعه» — لم يكن موجوداً قبل أن ترتكبه!",
            "do research" to "البحث نشاط وعمل تقوم به ← do.",
            "do your best" to "تنفذ أفضل ما عندك ← do.",
            "do business" to "الأعمال نشاط ← do business with someone.",
            "make an effort" to "تصنع جهداً يدفعك للأمام.",
            "do someone a favour" to "تؤدي خدمة لشخص ← do.",
        ),
        groups = listOf(
            group("make = تصنع نتيجة", "make a decision / progress / a mistake / an effort / money / noise", "make a decision", "make progress", "make a mistake", "make an effort"),
            group("do = تنفذ نشاطاً", "do research / your best / business / a favour / homework / exercise", "do research", "do your best", "do business", "do someone a favour"),
        ),
        story = "Starting a company isn't easy. First, I [did research] for months. Then I had to [make a decision]: should I [do business] alone or with a partner? I [made a mistake] at first, but I [made an effort] to learn from it. My friend [did] me [a favour] and lent me money. Now we're [making progress] — I always [do my best]!",
        storyAr = "بدء شركة ليس سهلاً. أولاً أجريت بحثاً لشهور. ثم كان علي أن أتخذ قراراً: هل أمارس الأعمال وحدي أم مع شريك؟ ارتكبت خطأً في البداية، لكنني بذلت جهداً للتعلم منه. أسدى لي صديقي معروفاً وأقرضني مالاً. الآن نحرز تقدماً — دائماً أبذل قصارى جهدي!",
        checks = listOf(
            q("I'm learning fast. I'm ___ good progress.", "making", "doing", "taking"),
            q("Scientists ___ research on new medicines.", "do", "make", "give"),
        ),
        mistakes = listOf(
            mistake("I did a mistake.", "I made a mistake.", "الخطأ مع make."),
            mistake("Can you make me a favour?", "Can you do me a favour?", "الخدمة مع do."),
        ),
        tip = "اصنع جدولين في دفترك: make و do، وكلما سمعت تلازماً جديداً أضفه إلى عموده.",
    ),

    "b2-u3-l2" to VocabGuide(
        hook = "في عصر الأخبار الكاذبة، فهم لغة الإعلام مهارة ضرورية. هذه الكلمات تساعدك أن تقرأ الأخبار بعين ناقدة وتميّز المصدر الموثوق من المتحيز.",
        hints = mapOf(
            "headline" to "head (رأس) + line (سطر) = السطر الذي على رأس الخبر.",
            "journalist" to "من journal (صحيفة/مجلة) + ist (الشخص).",
            "reliable" to "rely (يعتمد) + able = يمكن الاعتماد عليه.",
            "biased" to "bias = ميل لطرف. biased = يميل لجهة ولا يعرض الصورة كاملة.",
            "fake news" to "fake = مزيف (fake watch = ساعة مقلدة).",
            "broadcast" to "broad (واسع) + cast (يرمي): يرمي الخبر لجمهور واسع.",
            "coverage" to "من cover (يغطي): تغطية إعلامية.",
            "claim" to "يقول شيئاً دون إثبات بالضرورة. «He claims he's innocent.»",
        ),
        groups = listOf(
            group("صناعة الخبر", "The journalist wrote the headline. The match was broadcast live.", "headline", "journalist", "broadcast", "coverage"),
            group("الحكم على الخبر", "a reliable source / a biased report / spread fake news / make a claim", "reliable", "biased", "fake news", "claim"),
        ),
        story = "Yesterday, a shocking [headline] spread online: «Chocolate cures colds!» The website [claim]ed it was based on science. But a [journalist] checked the facts and found it was [fake news]. The real study was small and [biased]. That evening, a [reliable] TV channel [broadcast] the truth, and the story got wide [coverage].",
        storyAr = "أمس انتشر عنوان صادم على الإنترنت: «الشوكولاتة تعالج الزكام!» زعم الموقع أنه مبني على العلم. لكن صحفياً تحقق من الحقائق ووجد أنه خبر كاذب. كانت الدراسة الحقيقية صغيرة ومتحيزة. في ذلك المساء بثت قناة موثوقة الحقيقة، وحظيت القصة بتغطية واسعة.",
        checks = listOf(
            q("This newspaper only shows one side. It's ___.", "biased", "reliable", "broadcast"),
            q("You can trust this website. It's very ___.", "reliable", "fake", "biased"),
        ),
        mistakes = listOf(
            mistake("The news are good.", "The news is good.", "news مفرد رغم s في نهايته."),
        ),
        tip = "عندما تقرأ خبراً بالإنجليزية، اسأل: Is the source reliable? Is it biased? — ستتعلم اللغة والتفكير النقدي معاً.",
    ),

    "b2-u4-l2" to VocabGuide(
        hook = "من كتابة السيرة الذاتية إلى التفاوض على الراتب — هذه كلمات مسيرتك المهنية. ستحتاجها في المقابلات والاجتماعات وبيئة العمل الدولية.",
        hints = mapOf(
            "promotion" to "pro (للأمام) + mote (يتحرك): تتحرك للأمام في عملك ← ترقية.",
            "resign" to "re + sign: «توقّع» على ورقة المغادرة ← يستقيل. g صامتة: «رِزاين».",
            "deadline" to "dead (ميت) + line (خط): إذا تجاوزت الخط «مات» المشروع!",
            "colleague" to "تُنطق «كولِيغ». زميل في العمل (وليس في الدراسة = classmate).",
            "negotiate" to "تذكر «نقاش»: تتناقش حتى تصل لاتفاق.",
            "skills" to "soft skills (التواصل) و hard skills (البرمجة).",
            "workload" to "work + load (حمولة) = حمولة العمل.",
            "CV" to "اختصار لاتيني curriculum vitae = «مسار الحياة». بالأمريكية résumé.",
        ),
        groups = listOf(
            group("التقدم الوظيفي", "send your CV → show your skills → get a promotion", "CV", "skills", "promotion"),
            group("الحياة اليومية في العمل", "meet a deadline / a heavy workload / get on with colleagues", "deadline", "workload", "colleague"),
            group("قرارات كبيرة", "negotiate a salary / resign from a job", "negotiate", "resign"),
        ),
        story = "Maya updated her [CV] to show her new [skills]. At work, her [workload] was huge and every [deadline] was tight. Her [colleague]s were great, but she wanted more. When her boss offered a [promotion], she [negotiate]d a better salary. A year later, she decided to [resign] and start her own company.",
        storyAr = "حدّثت مايا سيرتها الذاتية لتُظهر مهاراتها الجديدة. في العمل كان عبء العمل ضخماً وكل موعد نهائي ضيقاً. كان زملاؤها رائعين، لكنها أرادت المزيد. عندما عرض عليها مديرها ترقية، تفاوضت على راتب أفضل. بعد عام قررت أن تستقيل وتبدأ شركتها الخاصة.",
        checks = listOf(
            q("I have too much to do this week. My ___ is huge.", "workload", "deadline", "promotion"),
            q("The report must be finished by Friday — that's the ___.", "deadline", "CV", "colleague"),
        ),
        mistakes = listOf(
            mistake("She resigned her work.", "She resigned from her job.", "resign from + الوظيفة."),
        ),
        tip = "اكتب سيرتك الذاتية بالإنجليزية الآن — ولو في سطرين. ستستخدم نصف كلمات هذا الدرس!",
    ),
)
