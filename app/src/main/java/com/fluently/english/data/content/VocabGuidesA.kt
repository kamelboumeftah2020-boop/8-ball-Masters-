package com.fluently.english.data.content

/** Vocabulary teaching for A1 and A2. */
internal val VocabGuidesA: Map<String, VocabGuide> = mapOf(

    "a1-u1-l2" to VocabGuide(
        hook = "أول دقيقة في أي لقاء تحدد الانطباع كله. بثماني كلمات فقط تستطيع أن تحيي شخصاً، وتعرّف بنفسك، وتشكره، وتودعه بأدب. هذه الكلمات ستستخدمها كل يوم طوال حياتك!",
        hints = mapOf(
            "hello" to "تشبه «هلا» العربية — سهلة الحفظ!",
            "goodbye" to "أصلها God be with you (الله معك) — مثل «في أمان الله».",
            "name" to "تشبه «نام» — تخيل أن اسمك مكتوب على سريرك عندما تنام.",
            "country" to "تذكر country music: موسيقى الريف والوطن.",
            "friend" to "ابدأ بـ fri مثل Friday: الجمعة يوم نلتقي فيه بالأصدقاء.",
            "morning" to "Good morning تُقال حتى الظهر (12:00) فقط.",
            "thank you" to "you هنا مهمة: thank you وليس thanks you. وthanks وحدها أقل رسمية.",
            "please" to "تُوضع في البداية أو النهاية: Please sit. / Sit, please.",
        ),
        groups = listOf(
            group("التحية والوداع", "hello في أي وقت، good morning صباحاً، goodbye عند المغادرة.", "hello", "morning", "goodbye"),
            group("كلمات الأدب السحرية", "الإنجليز يستخدمونها أكثر منا كثيراً — استخدمها في كل طلب.", "please", "thank you"),
            group("التعريف بالنفس", "My name is… / I'm from (country)… / This is my friend…", "name", "country", "friend"),
        ),
        story = "Good [morning]! [Hello], my [name] is Ali. I'm from Egypt — it's a beautiful [country]. This is my [friend] Sam. Sam, can you open the window, [please]? [Thank you]! OK, [goodbye] everyone!",
        storyAr = "صباح الخير! مرحباً، اسمي علي. أنا من مصر — إنها بلد جميل. هذا صديقي سام. سام، هل تفتح النافذة من فضلك؟ شكراً لك! حسناً، مع السلامة للجميع!",
        checks = listOf(
            q("Someone gives you a coffee. You say: «___!»", "Thank you", "Goodbye", "Morning"),
            q("It's 8 a.m. You meet your teacher: «Good ___!»", "morning", "evening", "name"),
        ),
        mistakes = listOf(
            mistake("Thanks you.", "Thank you. / Thanks.", "لا نجمع thanks مع you."),
            mistake("Good morning (at 6 p.m.)", "Good evening.", "morning تنتهي عند الظهر."),
        ),
        tip = "تحدّ نفسك اليوم: قل hello و thank you و please بصوت عالٍ عشر مرات لنفسك في المرآة!",
    ),

    "a1-u2-l2" to VocabGuide(
        hook = "العائلة أهم موضوع في أي محادثة أولى بين الناس. «هل لديك إخوة؟» سؤال ستسمعه كثيراً. لنرسم شجرة عائلتك بالإنجليزية!",
        hints = mapOf(
            "mother" to "تشبه «ماما» — mom / mum في الكلام اليومي.",
            "father" to "تشبه «بابا» في نهايتها — dad في الكلام اليومي.",
            "brother" to "brother و mother و father كلها تنتهي بـ ther — عائلة واحدة!",
            "sister" to "sis للاختصار بين الإخوة.",
            "son" to "تُنطق مثل sun (الشمس) — ابنك هو شمس حياتك ☀️.",
            "daughter" to "gh صامتة: تُنطق «دوتر».",
            "grandfather" to "grand تعني «كبير»: الأب الكبير = الجد.",
            "husband" to "wife = زوجة، husband = زوج.",
        ),
        groups = listOf(
            group("الوالدان", "parents = الوالدان معاً.", "mother", "father"),
            group("الإخوة والأبناء", "siblings = الإخوة والأخوات معاً، children = الأبناء.", "brother", "sister", "son", "daughter"),
            group("الأجداد والزواج", "grand + أي كلمة = جيل أعلى: grandmother, grandson…", "grandfather", "husband"),
        ),
        story = "This is my family photo. My [father] is a doctor and my [mother] is a teacher. I have one [brother] and two [sister]s. My [grandfather] lives with us. My aunt is here too with her [husband], her [son] and her little [daughter].",
        storyAr = "هذه صورة عائلتي. أبي طبيب وأمي معلمة. لدي أخ واحد وأختان. جدي يعيش معنا. خالتي هنا أيضاً مع زوجها وابنها وابنتها الصغيرة.",
        checks = listOf(
            q("My mother's father is my ___.", "grandfather", "brother", "husband"),
            q("My parents have a girl. She is their ___.", "daughter", "son", "sister"),
        ),
        mistakes = listOf(
            mistake("My father's mother is my mother.", "My father's mother is my grandmother.", "أم الأب = الجدة."),
            mistake("I have two brother.", "I have two brothers.", "الجمع يحتاج s."),
        ),
        tip = "ارسم شجرة عائلتك على ورقة واكتب الكلمة الإنجليزية بجانب كل شخص. ستحفظها في دقائق!",
    ),

    "a1-u3-l2" to VocabGuide(
        hook = "صف يومك بالإنجليزية من الاستيقاظ حتى النوم — هذه أفضل طريقة لتمرين المضارع البسيط. كلمات اليوم هي «ساعة» يومك.",
        hints = mapOf(
            "wake up" to "wake = يوقظ، up = للأعلى: تخيل نفسك تقفز من السرير للأعلى!",
            "breakfast" to "break (يكسر) + fast (الصيام): كسر صيام الليل = الفطور. مثل «الإفطار» تماماً!",
            "work" to "فعل واسم معاً: I work (أعمل) / my work (عملي).",
            "lunch" to "وجبة منتصف النهار — lunch box صندوق الغداء.",
            "evening" to "من حوالي 6 مساءً حتى النوم. Good evening تحية مسائية.",
            "always" to "all ways = كل الطرق ← كل مرة ← دائماً.",
            "never" to "n + ever = ليس أبداً. عكس always تماماً.",
            "weekend" to "week (أسبوع) + end (نهاية).",
        ),
        groups = listOf(
            group("أوقات اليوم والوجبات", "ترتيب اليوم: morning → breakfast → lunch → evening.", "breakfast", "lunch", "evening", "weekend"),
            group("الأفعال اليومية", "مع he/she نضيف s: she wakes up, he works.", "wake up", "work"),
            group("كم مرة؟", "توضع قبل الفعل: I always…, She never…", "always", "never"),
        ),
        story = "I [always] [wake up] at six. I have [breakfast] with my family, then I go to [work]. I have [lunch] at one o'clock. In the [evening], I read. I [never] watch TV late. At the [weekend], I visit my friends.",
        storyAr = "أستيقظ دائماً في السادسة. أتناول الفطور مع عائلتي ثم أذهب إلى العمل. أتغدى في الواحدة. في المساء أقرأ. لا أشاهد التلفاز متأخراً أبداً. في عطلة نهاية الأسبوع أزور أصدقائي.",
        checks = listOf(
            q("Saturday and Sunday are the ___.", "weekend", "evening", "lunch"),
            q("I drink coffee every morning. I ___ drink coffee.", "always", "never", "wake up"),
        ),
        mistakes = listOf(
            mistake("I wake at 7.", "I wake up at 7.", "الأكثر طبيعية: wake up."),
            mistake("I go always to work.", "I always go to work.", "always قبل الفعل الرئيسي."),
        ),
        tip = "قبل النوم، قل لنفسك ثلاث جمل عن يومك بالإنجليزية: I woke up at… I had… I worked…",
    ),

    "a1-u4-l2" to VocabGuide(
        hook = "الطعام لغة عالمية! في أي سفر ستحتاج أن تطلب طعامك وتقرأ قائمة المطعم. هذه الكلمات ستنقذك من الجوع 😄.",
        hints = mapOf(
            "water" to "تُنطق بالأمريكية «وارر» وبالبريطانية «ووتا».",
            "bread" to "ea تُنطق «إ» قصيرة: «بريد».",
            "chicken" to "تذكر الأكلة الشهيرة fried chicken.",
            "vegetables" to "تُنطق «فِجْتَبِلز» (3 مقاطع فقط!) واختصارها veggies.",
            "fruit" to "ui تُنطق «و» طويلة: «فروت». ولا تأخذ s عادة: I eat fruit.",
            "juice" to "مثل «جوس» — orange juice عصير برتقال.",
            "hungry" to "لا تخلطها مع angry (غاضب)! hungry + angry = hangry (غاضب من الجوع) 😄",
            "menu" to "نفس الكلمة في العربية: «منيو».",
        ),
        groups = listOf(
            group("مأكولات", "chicken و bread لا تأخذ a عادة: I'd like some bread.", "bread", "chicken", "vegetables", "fruit"),
            group("مشروبات", "a glass of water / a cup of tea / a bottle of juice", "water", "juice"),
            group("في المطعم", "I'm hungry! Can I see the menu, please?", "hungry", "menu"),
        ),
        story = "I'm very [hungry]! Let's go to a restaurant. Can I see the [menu], please? I'd like [chicken] with [vegetables] and some [bread]. To drink, an orange [juice] and a glass of [water]. For dessert, fresh [fruit]. Delicious!",
        storyAr = "أنا جائع جداً! لنذهب إلى مطعم. هل يمكنني رؤية قائمة الطعام؟ أريد دجاجاً مع خضروات وبعض الخبز. للشرب، عصير برتقال وكوب ماء. وللحلوى فاكهة طازجة. لذيذ!",
        checks = listOf(
            q("Apples and bananas are ___.", "fruit", "vegetables", "bread"),
            q("I didn't eat all day. I'm very ___.", "hungry", "angry", "menu"),
        ),
        mistakes = listOf(
            mistake("I want a bread.", "I want some bread.", "bread غير معدودة."),
            mistake("I am angry, let's eat!", "I am hungry, let's eat!", "angry = غاضب، hungry = جائع."),
        ),
        tip = "في المرة القادمة التي تأكل فيها، سمِّ كل شيء أمامك بالإنجليزية بصوت منخفض.",
    ),

    "a2-u1-l2" to VocabGuide(
        hook = "تخطط لرحلة؟ من حجز التذكرة إلى شراء التذكارات، هذه الكلمات ترافقك في كل خطوة: قبل السفر، في المطار، وفي المدينة الجديدة.",
        hints = mapOf(
            "trip" to "رحلة قصيرة. a business trip = رحلة عمل. وtravel فعل: I love to travel.",
            "passport" to "pass (يعبر) + port (ميناء): الورقة التي تعبر بها الموانئ!",
            "ticket" to "تشبه «تكت» — ticket office شباك التذاكر.",
            "luggage" to "غير معدودة: my luggage is heavy (وليس luggages). مرادفها baggage.",
            "abroad" to "لا نضع قبلها to: go abroad (وليس go to abroad).",
            "sightseeing" to "sight (منظر) + seeing (رؤية) = رؤية المعالم.",
            "souvenir" to "كلمة فرنسية تعني «أتذكر» — هدية تذكرك بالرحلة.",
            "book" to "كتاب، لكنها أيضاً فعل «يحجز»: book a hotel. تخيل أنك تكتب اسمك في دفتر الحجوزات.",
        ),
        groups = listOf(
            group("قبل السفر", "Book early to get cheap tickets!", "book", "ticket", "passport", "luggage"),
            group("في الرحلة", "go on a trip / go sightseeing / buy souvenirs", "trip", "abroad", "sightseeing", "souvenir"),
        ),
        story = "Last month, I went [abroad] for the first time. I [book]ed my [ticket] online and checked my [passport] twice! My [luggage] was very heavy. The [trip] was amazing — we went [sightseeing] every day, and I bought a small [souvenir] for my mother.",
        storyAr = "الشهر الماضي سافرت إلى الخارج لأول مرة. حجزت تذكرتي عبر الإنترنت وتحققت من جواز سفري مرتين! كانت أمتعتي ثقيلة جداً. كانت الرحلة رائعة — كنا نشاهد المعالم كل يوم، واشتريت تذكاراً صغيراً لأمي.",
        checks = listOf(
            q("You need a ___ to travel to another country.", "passport", "souvenir", "menu"),
            q("I want to ___ a room for two nights.", "book", "trip", "luggage"),
        ),
        mistakes = listOf(
            mistake("I have three luggages.", "I have three bags. / I have a lot of luggage.", "luggage غير معدودة."),
            mistake("I want to go to abroad.", "I want to go abroad.", "abroad لا تحتاج to."),
        ),
        tip = "عندما تخطط لرحلتك القادمة، اكتب قائمة التحضير بالإنجليزية: passport ✓ ticket ✓ luggage ✓",
    ),

    "a2-u2-l2" to VocabGuide(
        hook = "الصفات تلوّن كلامك! بدل «المدينة جيدة» تستطيع أن تقول «مزدحمة لكن ودودة». الصفات تأتي غالباً في أزواج متضادة — احفظها معاً.",
        hints = mapOf(
            "crowded" to "crowd = حشد من الناس. crowded = مليء بالحشود.",
            "quiet" to "لا تخلطها مع quite (تماماً). quiet عكس noisy.",
            "modern" to "مثل «مودرن» بالعربية — عكسها old / traditional.",
            "friendly" to "friend + ly = مثل الصديق ← ودود.",
            "lazy" to "تخيل قطة «ليزي» نائمة طوال اليوم 🐈.",
            "generous" to "كلمة لاتينية الأصل، عكسها mean / stingy (بخيل).",
            "dangerous" to "danger = خطر. dangerous = فيه خطر.",
            "cheap" to "تُنطق «تشيب» — عكسها expensive (غالٍ).",
        ),
        groups = listOf(
            group("لوصف الأماكن", "crowded ↔ quiet، cheap ↔ expensive، safe ↔ dangerous", "crowded", "quiet", "modern", "dangerous", "cheap"),
            group("لوصف الأشخاص", "friendly ↔ unfriendly، lazy ↔ hard-working، generous ↔ mean", "friendly", "lazy", "generous"),
        ),
        story = "I live in a big, [modern] city. The streets are [crowded] in the day, but my street is [quiet] at night. People here are [friendly] and [generous]. Food is [cheap] in the old market. But be careful: some roads are [dangerous]. My cat? He's very [lazy] — he sleeps all day!",
        storyAr = "أعيش في مدينة كبيرة حديثة. الشوارع مزدحمة نهاراً، لكن شارعي هادئ ليلاً. الناس هنا ودودون وكرماء. الطعام رخيص في السوق القديم. لكن احذر: بعض الطرق خطيرة. قطتي؟ كسولة جداً — تنام طوال اليوم!",
        checks = listOf(
            q("There are too many people in this shop. It's very ___.", "crowded", "quiet", "lazy"),
            q("He always helps people and gives gifts. He's ___.", "generous", "dangerous", "cheap"),
        ),
        mistakes = listOf(
            mistake("The library is very quite.", "The library is very quiet.", "quiet = هادئ، quite = تماماً."),
            mistake("a city modern", "a modern city", "الصفة قبل الاسم في الإنجليزية."),
        ),
        tip = "احفظ الصفات كأزواج متضادة: crowded/quiet، cheap/expensive. كل كلمة تذكّرك بأختها.",
    ),

    "a2-u3-l2" to VocabGuide(
        hook = "«ماذا تعمل؟» من أول الأسئلة في أي تعارف، ومن أهمها في البحث عن وظيفة. هذه الكلمات تساعدك أن تتحدث عن عملك أو حلمك المهني.",
        hints = mapOf(
            "job" to "وظيفة محددة (a job). أما work فعامة وغير معدودة.",
            "salary" to "من salt (ملح): كان الجنود الرومان يُعطون ملحاً كأجر!",
            "boss" to "مثل «البوس» المستخدمة عربياً = المدير.",
            "office" to "مكتب (المكان). أما desk فهو الطاولة.",
            "interview" to "inter (بين) + view (نظر): نظرة متبادلة بين شخصين.",
            "lawyer" to "law = قانون + er = الشخص ← رجل القانون.",
            "engineer" to "تذكر engine (محرك): المهندس يصنع المحركات. انتبه: an engineer.",
            "experience" to "غير معدودة بمعنى الخبرة: work experience.",
        ),
        groups = listOf(
            group("مهن", "مع المهن نستخدم a/an: She's a lawyer. He's an engineer.", "lawyer", "engineer"),
            group("في مكان العمل", "work in an office / my boss / a good salary", "boss", "office", "salary"),
            group("البحث عن عمل", "apply for a job → have an interview → get the job!", "job", "interview", "experience"),
        ),
        story = "My sister is an [engineer]. She found a new [job] last month. She had an [interview] with the [boss], and they liked her [experience]. Now she works in a big [office] in the city centre. The [salary] is good! Her husband is a [lawyer].",
        storyAr = "أختي مهندسة. وجدت وظيفة جديدة الشهر الماضي. أجرت مقابلة مع المدير، وأعجبتهم خبرتها. الآن تعمل في مكتب كبير في وسط المدينة. الراتب جيد! وزوجها محامٍ.",
        checks = listOf(
            q("Before you get a job, you usually have an ___.", "interview", "office", "salary"),
            q("She works in a court and helps people with the law. She's a ___.", "lawyer", "engineer", "boss"),
        ),
        mistakes = listOf(
            mistake("I have a new work.", "I have a new job.", "work غير معدودة؛ نقول a job."),
            mistake("She is engineer.", "She is an engineer.", "المهنة تحتاج a/an."),
        ),
        tip = "اكتب جملتين عن عملك أو عن عملك الذي تحلم به: I'm a… / I want to be a…",
    ),

    "a2-u4-l2" to VocabGuide(
        hook = "في المتجر تحتاج أن تسأل عن السعر والمقاس، وأن تجرب الملابس وتطلب خصماً. هذه الكلمات تجعل التسوق في الخارج سهلاً.",
        hints = mapOf(
            "price" to "price = سعر الشيء. وprize = جائزة! حرف واحد يغير المعنى.",
            "size" to "S, M, L, XL = small, medium, large, extra large.",
            "try on" to "try = يجرب + on = على جسمك. «Can I try it on?»",
            "discount" to "dis (إزالة) + count (عدّ): إزالة جزء من السعر.",
            "receipt" to "حرف p صامت! تُنطق «ريسيت».",
            "cash" to "مثل «كاش» بالعربية تماماً. عكسها by card.",
            "expensive" to "تذكر «اكسبنسف» تشبه «إكس» = علامة رفض للسعر المرتفع ❌.",
            "fit" to "مثل «فِت» = مناسب المقاس. The shoes fit me.",
        ),
        groups = listOf(
            group("المال", "pay in cash / pay by card / get a discount", "price", "discount", "cash", "expensive", "receipt"),
            group("الملابس", "What size are you? Can I try it on? It doesn't fit.", "size", "try on", "fit"),
        ),
        story = "I saw a nice jacket, but the [price] was high — very [expensive]! Luckily, there was a 30% [discount]. I asked for my [size] and went to [try] it [on]. It didn't [fit], so I took a bigger one. I paid in [cash] and kept the [receipt].",
        storyAr = "رأيت سترة جميلة لكن سعرها مرتفع — غالية جداً! لحسن الحظ كان هناك خصم 30%. طلبت مقاسي وذهبت لأجربها. لم تناسبني فأخذت أكبر منها. دفعت نقداً واحتفظت بالإيصال.",
        checks = listOf(
            q("Keep the ___ if you want to return the item.", "receipt", "size", "discount"),
            q("These shoes are too small. They don't ___.", "fit", "try on", "cash"),
        ),
        mistakes = listOf(
            mistake("How much is the prize?", "How much is the price?", "prize = جائزة."),
            mistake("Can I try on it?", "Can I try it on?", "الضمير يأتي بين try و on."),
        ),
        tip = "في المرة القادمة التي تتسوق فيها، تخيل الحوار بالإنجليزية: How much is it? Can I try it on?",
    ),
)
