package com.fluently.english.data.content

val LevelC2 = level(
    CefrLevel.C2,
    exam = listOf(
        q("The board insisted that the CEO ___ immediately.", "resign", "resigns", "resigned", "would resign"),
        q("It's high time we ___ a decision.", "made", "make", "will make", "have made"),
        q("So complex ___ the problem that no one could solve it.", "was", "it was", "were", "had"),
        q("I'd rather you ___ mention this to anyone.", "didn't", "don't", "won't", "not"),
        q("He's not one to ___ words — he says exactly what he thinks.", "mince", "chop", "cut", "slice"),
        q("The new policy has been met with ___ criticism.", "scathing", "scalding", "scorching", "sweltering"),
        q("Her argument, ___ persuasive, contained several flaws.", "albeit", "despite", "although being", "however"),
        q("The data would ___ to suggest a link between the two.", "appear", "seem as", "look", "show"),
        q("A formal synonym of «get rid of» is…", "eliminate", "kick out", "chuck", "drop off"),
        q("«To bite the bullet» means…", "to face something unpleasant bravely", "to be very angry", "to eat quickly", "to avoid a problem"),
        order("Were it not for your help I would have given up", "لولا مساعدتك لكنت استسلمت"),
        listen("Notwithstanding the committee's reservations, the proposal was ratified by an overwhelming majority.", "What happened to the proposal?", "It was approved by most members.", "It was rejected.", "It was postponed.", "The committee withdrew it."),
        listen("The novelist's prose, though ostensibly simple, conceals a remarkable depth of irony.", "How does the speaker describe the novelist's writing?", "Seemingly simple but deeply ironic", "Complex and difficult", "Simple and shallow", "Humorous but careless"),
        type("Complete: Were it ___ for the rain, we would have won. (كلمة واحدة)", "not"),
    ),
) {
    unit("Formal English", "الإنجليزية الرسمية") {
        grammar(
            "The subjunctive & unreal past", "صيغة الشرط الافتراضية والماضي غير الحقيقي",
            notes = listOf(
                "صيغة subjunctive تستخدم المصدر بدون to ولجميع الضمائر بعد أفعال وصفات الاقتراح والطلب والضرورة: suggest, recommend, insist, demand, essential, vital: It is essential that he be present. — I recommend that she apply early.",
                "تُستخدم في الإنجليزية البريطانية أيضاً should + فعل: I suggest that he should apply.",
                "الماضي غير الحقيقي بعد: It's (high) time, would rather, as if / as though, suppose: It's time we left. — I'd rather you didn't smoke. — He talks as if he were the boss.",
                "تعابير ثابتة من الصيغة الافتراضية: Be that as it may…, Come what may…, Suffice it to say…, God save the King.",
            ),
            examples = listOf(
                "The doctor insisted that he stop smoking." means "أصر الطبيب على أن يتوقف عن التدخين.",
                "It is vital that every student be informed." means "من الضروري أن يُبلَّغ كل طالب.",
                "It's high time you found a job." means "آن الأوان أن تجد عملاً.",
                "Come what may, I'll stand by you." means "مهما حدث، سأقف بجانبك.",
            ),
            questions = listOf(
                q("The committee recommended that the law ___ changed.", "be", "is", "was", "will be"),
                q("It is essential that she ___ the contract before Monday.", "sign", "signs", "signed", "will sign"),
                q("It's high time the government ___ action.", "took", "take", "takes", "has taken"),
                q("I'd rather you ___ here tomorrow.", "came", "come", "will come", "coming"),
                q("He behaves as though he ___ the owner.", "were", "is being", "be", "has been"),
                q("___ that as it may, we still need a solution.", "Be", "Being", "Is", "Was"),
                q("They demanded that the manager ___ fired.", "be", "is", "was", "being"),
                order("Suffice it to say that the meeting did not go well", "يكفي القول إن الاجتماع لم يسر على ما يرام"),
                type("Complete: I suggest that he ___ (take) a break.", "take", "should take"),
            ),
        )
        vocabulary(
            "Nuanced synonyms", "المترادفات الدقيقة",
            listOf(
                w("meticulous", "دقيق للغاية", "She is meticulous in her research."),
                w("ubiquitous", "منتشر في كل مكان", "Smartphones have become ubiquitous."),
                w("ephemeral", "زائل / سريع الانقضاء", "Fame on social media is often ephemeral."),
                w("pragmatic", "عملي / واقعي", "We need a pragmatic approach to this issue."),
                w("eloquent", "فصيح / بليغ", "He gave an eloquent speech."),
                w("ambiguous", "غامض / ملتبس", "The wording of the contract is ambiguous."),
                w("candid", "صريح", "Thank you for your candid feedback."),
                w("scrutinise", "يدقق / يفحص بعناية", "Experts will scrutinise the data."),
            ),
        )
        reading(
            "On the virtue of boredom", "في فضيلة الملل",
            passage = "We live in an age that regards boredom as a malfunction to be eliminated. The moment a queue stalls or a conversation lulls, a screen is summoned to fill the void. Yet it is worth asking whether, in our zeal to abolish tedium, we have discarded something of value. Psychologists have long observed that boredom, far from being a merely aversive state, functions as a signal: it alerts us that our current activity is failing to engage us and impels us to seek something more meaningful. Experiments in which participants were first given a deliberately dull task — copying numbers from a telephone directory, for instance — found that they subsequently generated more creative ideas than a control group. The mind, denied external stimulation, turns inward and begins to wander, forging unexpected connections. Were we to tolerate a little more idleness, we might rediscover the fertile, if uncomfortable, territory from which original thought so often springs.",
            questions = listOf(
                q("What is the writer's attitude towards boredom?", "It can be valuable and productive.", "It should be eliminated.", "It is a dangerous illness.", "It only affects lazy people."),
                q("According to psychologists, boredom functions as…", "a signal to seek something more meaningful", "a sign of low intelligence", "a result of technology", "a way to rest the body"),
                q("What did the experiments show?", "Bored participants later had more creative ideas.", "Bored participants performed worse.", "Copying numbers improves memory.", "The control group was more creative."),
                q("The phrase «in our zeal to abolish tedium» suggests that we…", "are very eager to get rid of boredom", "secretly enjoy boredom", "are unaware of boredom", "have failed to create boredom"),
                q("«Fertile, if uncomfortable, territory» means the state is…", "productive although unpleasant", "uncomfortable and useless", "comfortable and fertile", "dangerous and forbidden"),
            ),
        )
    }

    unit("Elegant structures", "التراكيب البليغة") {
        grammar(
            "Advanced inversion, ellipsis & substitution", "القلب المتقدم والحذف والاستبدال",
            notes = listOf(
                "القلب مع so / such للتوكيد البلاغي: So great was the demand that tickets sold out in minutes. — Such was his anger that he couldn't speak.",
                "القلب في الشرط بدون if: Were it not for… / Had it not been for… = لولا: Had it not been for the doctor, she would have died.",
                "الحذف (ellipsis): نحذف ما يُفهم من السياق لتجنب التكرار: She can speak French, and I can too. — I wanted to go but I couldn't (go).",
                "الاستبدال (substitution): نستخدم so / not / one / do so بدلاً من تكرار جملة كاملة: Will it rain? — I hope not. / I think so. — I was asked to leave, and I did so.",
            ),
            examples = listOf(
                "So beautiful was the view that we forgot the time." means "كان المنظر جميلاً لدرجة أننا نسينا الوقت.",
                "Had it not been for her, I would never have succeeded." means "لولاها لما نجحت أبداً.",
                "Is he coming? — I'm afraid not." means "هل سيأتي؟ — أخشى أنه لن يأتي.",
                "He promised to call, but he didn't." means "وعد أن يتصل لكنه لم يفعل.",
            ),
            questions = listOf(
                q("So loud ___ that we couldn't hear each other.", "was the music", "the music was", "the music", "did the music"),
                q("Such ___ her talent that she won every competition.", "was", "is being", "has", "did"),
                q("___ it not been for the GPS, we would have got lost.", "Had", "If", "Were", "Should"),
                q("___ it not for your support, I'd have quit.", "Were", "Had", "Was", "Did"),
                q("Do you think she'll pass? — I hope ___.", "so", "it", "that", "yes"),
                q("Will they cancel the event? — I suspect ___.", "not", "no", "don't", "it not"),
                q("You were told to submit the report, and you should have ___.", "done so", "made so", "done it so", "so done"),
                order("So successful was the campaign that it was extended", "كانت الحملة ناجحة لدرجة أنه تم تمديدها"),
                type("Complete: Had it not ___ for the storm, the flight would have landed.", "been"),
            ),
        )
        vocabulary(
            "Advanced idioms & expressions", "تعابير متقدمة",
            listOf(
                w("a double-edged sword", "سلاح ذو حدين", "Fame can be a double-edged sword."),
                w("to cut corners", "يتهاون / يختصر على حساب الجودة", "The builders cut corners to save money."),
                w("a moot point", "مسألة خلافية / بلا أهمية عملية", "Whether he knew is now a moot point."),
                w("to pay lip service", "يؤيد بالكلام فقط", "They pay lip service to equality."),
                w("the tip of the iceberg", "رأس جبل الجليد", "These problems are just the tip of the iceberg."),
                w("to take something with a pinch of salt", "يتعامل مع الشيء بحذر وتشكك", "Take his promises with a pinch of salt."),
                w("a Pyrrhic victory", "نصر باهظ الثمن", "Winning the lawsuit was a Pyrrhic victory."),
                w("to play devil's advocate", "يتبنى الرأي المعاكس للنقاش", "Let me play devil's advocate for a moment."),
            ),
        )
        listening(
            "Podcast: the power of language", "بودكاست: قوة اللغة",
            script = "Host: Welcome back. My guest today is a linguist who argues that the language we speak subtly shapes the way we think. Guest: That's right, although I'd add a caveat: it shapes, rather than determines, thought. Take the Kuuk Thaayorre, an Aboriginal community in Australia. They don't use words like 'left' and 'right'; instead, they use compass directions — north, south, east, west — even for tiny distances. 'There's an ant on your south-west leg', they might say. Consequently, speakers maintain an astonishing sense of orientation at all times. Host: So their language trains their attention? Guest: Precisely. Had they spoken English, they would almost certainly not have developed that skill to the same degree. That said, we should be wary of exaggerating such effects. People can, and do, think about concepts for which their language lacks a word.",
            questions = listOf(
                q("What is the guest's main claim?", "Language influences, but doesn't fully determine, thought.", "Language completely controls thought.", "Language has no effect on thought.", "English is the best language for thinking."),
                q("What is special about the Kuuk Thaayorre language?", "It uses compass directions instead of left and right.", "It has no words for animals.", "It is spoken only by children.", "It has no grammar."),
                q("What skill do Kuuk Thaayorre speakers have?", "An excellent sense of orientation", "Perfect memory", "Fast reading", "Musical talent"),
                q("What does the guest mean by «I'd add a caveat»?", "He wants to add a warning or limitation.", "He completely agrees.", "He wants to change the subject.", "He disagrees with himself."),
                q("What does the guest warn against at the end?", "Exaggerating the effect of language", "Learning Aboriginal languages", "Using compass directions", "Speaking English"),
            ),
        )
    }

    unit("Academic writing", "الكتابة الأكاديمية") {
        grammar(
            "Nominalisation & hedging", "التحويل إلى أسماء والتحوّط في الكتابة",
            notes = listOf(
                "التحويل إلى أسماء (nominalisation): تحويل الأفعال والصفات إلى أسماء يجعل الأسلوب أكاديمياً وموضوعياً: Prices rose sharply, which worried investors → The sharp rise in prices caused concern among investors.",
                "التحوّط (hedging): يتجنب الكاتب الأكاديمي الجزم المطلق باستخدام أدوات تخفيف: may, might, could, appear to, seem to, tend to, suggest, it is likely that, to some extent, arguably.",
                "مقارنة: Social media causes depression (جزم غير علمي) → The findings suggest that heavy social media use may contribute to depression (تحوّط أكاديمي).",
                "صيغ موضوعية غير شخصية: It is widely believed that… / It has been argued that… / There is evidence to suggest…",
            ),
            examples = listOf(
                "The government's failure to act led to widespread criticism." means "أدى إخفاق الحكومة في التحرك إلى انتقادات واسعة.",
                "These results would appear to support the theory." means "يبدو أن هذه النتائج تدعم النظرية.",
                "It is likely that costs will continue to rise." means "من المرجح أن تستمر التكاليف في الارتفاع.",
                "This is, arguably, the most important discovery of the century." means "يمكن القول إن هذا أهم اكتشاف في القرن.",
            ),
            questions = listOf(
                q("Choose the most academic version.", "The rapid growth of cities has led to an increase in pollution.", "Cities grew really fast so there's way more pollution.", "Pollution is up because cities got big.", "Cities are growing and polluting a lot."),
                q("Nominalise: «The company expanded» → The company's ___", "expansion", "expanding", "expand", "expanded"),
                q("Which sentence is appropriately hedged?", "The data suggest that diet may play a role.", "The data prove that diet is the only cause.", "Diet definitely causes it.", "Everyone knows diet is the reason."),
                q("The results ___ to indicate a correlation.", "appear", "appears to", "are appear", "appearing"),
                q("It ___ argued that the policy was ineffective.", "has been", "has", "is being argue", "did"),
                q("Nominalise: «Researchers analysed the data carefully» → A careful ___ of the data", "analysis", "analyse", "analysing", "analytic"),
                q("Which word is NOT a hedging device?", "undoubtedly", "arguably", "tend to", "possibly"),
                order("There is evidence to suggest that sleep improves memory", "هناك أدلة تشير إلى أن النوم يحسّن الذاكرة"),
                type("Nominalise «decide»: The ___ was made after a long debate.", "decision"),
            ),
        )
        vocabulary(
            "Register: formal vs informal", "مستوى اللغة: الرسمي وغير الرسمي",
            listOf(
                w("commence", "يبدأ (رسمي = start)", "The ceremony will commence at noon."),
                w("terminate", "يُنهي (رسمي = end)", "The contract was terminated early."),
                w("ascertain", "يتحقق من (رسمي = find out)", "We need to ascertain the facts."),
                w("endeavour", "يسعى (رسمي = try)", "We will endeavour to reply within 24 hours."),
                w("sufficient", "كافٍ (رسمي = enough)", "There is insufficient evidence."),
                w("purchase", "يشتري (رسمي = buy)", "Tickets can be purchased online."),
                w("assist", "يساعد (رسمي = help)", "Our staff will assist you."),
                w("reside", "يقيم (رسمي = live)", "She currently resides in Geneva."),
            ),
        )
        reading(
            "The microbiome and the mind", "الميكروبيوم والعقل",
            passage = "Over the past two decades, research into the human microbiome — the trillions of microorganisms inhabiting the gut — has undergone a remarkable expansion. Of particular interest is the so-called 'gut–brain axis', the bidirectional communication between intestinal bacteria and the central nervous system. A growing body of evidence suggests that the composition of gut bacteria may influence mood, stress responses and even cognitive function. In one frequently cited study, germ-free mice exhibited exaggerated stress reactions, which were partially reversed following the introduction of specific bacterial strains. Nevertheless, considerable caution is warranted in extrapolating such findings to humans. Much of the existing research is correlational, and the mechanisms involved remain poorly understood. It would therefore be premature to conclude that probiotic supplements constitute an effective treatment for depression. What can be stated with reasonable confidence is that the relationship between diet, gut health and mental well-being merits further rigorous investigation.",
            questions = listOf(
                q("What is the 'gut–brain axis'?", "Two-way communication between gut bacteria and the nervous system", "A part of the brain", "A type of probiotic", "A disease of the stomach"),
                q("What happened to the germ-free mice?", "They showed extreme stress reactions.", "They became more intelligent.", "They lost weight.", "They had no reaction."),
                q("Why does the writer urge caution?", "Most research is correlational and mechanisms are unclear.", "The mice studies were fake.", "Probiotics are dangerous.", "Scientists are not interested."),
                q("The word «premature» in the text means…", "too early", "too late", "very accurate", "unlikely"),
                q("What is the writer's conclusion?", "The topic deserves more rigorous research.", "Probiotics cure depression.", "Diet has no effect on the mind.", "The microbiome is not important."),
            ),
        )
    }

    unit("The art of rhetoric", "فن الخطابة") {
        grammar(
            "Rhetorical devices & fronting", "الأساليب البلاغية والتقديم",
            notes = listOf(
                "التقديم (fronting): وضع عنصر في بداية الجملة لإبرازه أو للربط مع ما قبله: Strange as it may seem, I enjoyed the exam. — Much as I admire him, I can't agree.",
                "التركيب: صفة / ظرف + as + فاعل + فعل = على الرغم من: Tired as she was, she kept working.",
                "الثلاثيات (tricolon): ذكر ثلاثة عناصر متوازية لقوة الإيقاع: «Government of the people, by the people, for the people».",
                "التوازي والتضاد (antithesis): «Ask not what your country can do for you — ask what you can do for your country».",
                "السؤال البلاغي (rhetorical question) لإشراك المستمع دون انتظار إجابة: Who among us has not made mistakes?",
            ),
            examples = listOf(
                "Try as he might, he couldn't open the door." means "مهما حاول لم يستطع فتح الباب.",
                "Much as I'd love to come, I'm too busy." means "على الرغم من رغبتي الشديدة في الحضور، أنا مشغول جداً.",
                "Into the room walked a tall stranger." means "دخل إلى الغرفة غريب طويل القامة.",
                "We came, we saw, we conquered." means "جئنا، رأينا، انتصرنا.",
            ),
            questions = listOf(
                q("___ as it may seem, the plan worked.", "Strange", "Strangely", "Strangeness", "How strange"),
                q("Much ___ I respect her, I think she's wrong.", "as", "though", "so", "like"),
                q("Try as she ___, she couldn't remember his name.", "might", "can", "did", "would try"),
                q("Exhausted ___ he was, he finished the race.", "though", "despite", "even", "however"),
                q("Which is an example of a tricolon?", "Faster, stronger, smarter.", "Fast and strong.", "He was very fast.", "Faster than ever."),
                q("«Who doesn't want to be happy?» is…", "a rhetorical question", "an antithesis", "a tricolon", "a cleft sentence"),
                q("Choose the sentence with antithesis.", "It was the best of times, it was the worst of times.", "It was a very good time.", "Time flies when you're happy.", "It was the time of my life."),
                order("Much as I admire his work I cannot support his views", "مع إعجابي الكبير بعمله لا أستطيع تأييد آرائه"),
                type("Complete: Hard ___ he tried, he couldn't win. (كلمة واحدة)", "as", "though"),
            ),
        )
        vocabulary(
            "Advanced phrasal verbs", "أفعال مركبة متقدمة",
            listOf(
                w("brush up on", "يُنعش معرفته بـ", "I need to brush up on my French."),
                w("come across as", "يبدو / يترك انطباعاً بأنه", "He comes across as arrogant."),
                w("gloss over", "يتجاوز بسطحية", "The report glosses over the real problems."),
                w("iron out", "يحل (مشكلات بسيطة)", "We need to iron out a few details."),
                w("bank on", "يعتمد على", "Don't bank on getting a pay rise."),
                w("factor in", "يأخذ في الحسبان", "Remember to factor in the cost of travel."),
                w("rule out", "يستبعد", "The police have ruled out murder."),
                w("live up to", "يرقى إلى مستوى", "The film didn't live up to my expectations."),
            ),
        )
        listening(
            "A graduation speech", "خطاب تخرج",
            script = "Graduates, families, distinguished guests. Today, you stand at a threshold. Behind you lie years of effort, of sacrifice, of sleepless nights. Before you lies a world that is uncertain, unequal, and, yes, unforgiving. Daunting as that may sound, I would ask you this: when has progress ever come from certainty? Every great discovery, every movement for justice, began with someone who refused to accept the world as it was. So do not measure your success by what you earn, but by what you give. Do not ask how far you can rise, but how many you can lift with you. Fail, if you must — but fail forward. The future will not be handed to you. It will be built by you.",
            questions = listOf(
                q("Who is the speech addressed to mainly?", "Graduates", "Teachers", "Politicians", "Children"),
                q("How does the speaker describe the world?", "Uncertain, unequal and unforgiving", "Safe and fair", "Easy and certain", "Boring and predictable"),
                q("«When has progress ever come from certainty?» is an example of…", "a rhetorical question", "a passive sentence", "a conditional", "an idiom"),
                q("According to the speaker, how should graduates measure success?", "By what they give", "By how much they earn", "By how famous they become", "By how high they rise"),
                q("What does «fail forward» mean?", "Learn and progress from failure", "Avoid failure at all costs", "Fail many times on purpose", "Give up after failing"),
            ),
        )
    }

    unit("Precision in argument", "الدقة في الحجة") {
        grammar(
            "Advanced conditionals: but for, supposing, otherwise", "الشرط المتقدم",
            notes = listOf(
                "But for + اسم = لولا: But for your help, I would have failed. (= If it hadn't been for your help…)",
                "Supposing / Suppose / Imagine تُستخدم لتخيّل موقف: Supposing you lost your job, what would you do?",
                "otherwise = وإلا: Leave now; otherwise, you'll miss the train.",
                "provided / providing (that) و as long as و on condition that = بشرط أن: You can borrow it as long as you return it.",
                "في الرسمية: If + should للاحتمال البعيد: If you should see him, tell him to call me. أو بالقلب: Should you see him…",
            ),
            examples = listOf(
                "But for the rain, the match would have been perfect." means "لولا المطر لكانت المباراة مثالية.",
                "Supposing you won the lottery, what would you buy?" means "لنفترض أنك ربحت اليانصيب، ماذا ستشتري؟",
                "Save your work; otherwise, you might lose it." means "احفظ عملك، وإلا فقد تفقده.",
                "You can stay, on condition that you're quiet." means "يمكنك البقاء بشرط أن تكون هادئاً.",
            ),
            questions = listOf(
                q("___ his quick thinking, there would have been an accident.", "But for", "Unless", "Otherwise", "Providing"),
                q("___ you were offered the job, would you accept?", "Supposing", "Otherwise", "But for", "Unless that"),
                q("Wear a coat; ___, you'll catch a cold.", "otherwise", "unless", "provided", "supposing"),
                q("You may use my car ___ you drive carefully.", "as long as", "otherwise", "but for", "unless"),
                q("___ you need any further information, do not hesitate to contact us.", "Should", "Would", "Had", "Were"),
                q("We'll go ahead ___ that everyone agrees.", "on condition", "otherwise", "but for", "supposing"),
                order("But for the map we would have got lost", "لولا الخريطة لضللنا الطريق"),
                type("Complete: Hurry up; ___, we'll be late. (كلمة واحدة)", "otherwise"),
            ),
        )
        vocabulary(
            "Strong collocations", "المتلازمات القوية",
            listOf(
                w("bitterly cold", "بارد جداً / قارس", "It was bitterly cold in the mountains."),
                w("utterly ridiculous", "سخيف تماماً", "That idea is utterly ridiculous."),
                w("highly unlikely", "مستبعد جداً", "It's highly unlikely to rain today."),
                w("deeply concerned", "قلق للغاية", "We are deeply concerned about the situation."),
                w("fully aware", "مدرك تماماً", "I'm fully aware of the risks."),
                w("widely regarded", "يُعتبر على نطاق واسع", "She is widely regarded as the best in her field."),
                w("strongly oppose", "يعارض بشدة", "Many residents strongly oppose the plan."),
                w("painfully slow", "بطيء بشكل مؤلم", "Progress has been painfully slow."),
            ),
        )
        listening(
            "A lecture on urban design", "محاضرة عن تصميم المدن",
            script = "Good afternoon. Today I'd like to challenge an assumption that has shaped our cities for decades: that more roads mean less traffic. In fact, the evidence suggests the opposite. When a city builds a new motorway, traffic typically increases to fill it within a few years — a phenomenon known as 'induced demand'. Conversely, when roads are removed, traffic often doesn't simply move elsewhere; much of it disappears, as people change their habits. Seoul offers a striking example. In 2005, the city demolished an elevated highway and restored the stream beneath it. Critics predicted chaos. Instead, traffic adapted, air quality improved, and the area became one of the most popular public spaces in the city. But for such bold decisions, many cities would still be designed around cars rather than people.",
            questions = listOf(
                q("What assumption does the lecturer challenge?", "That more roads reduce traffic", "That cities need more parks", "That cars are cheap", "That traffic is decreasing"),
                q("What is 'induced demand'?", "New roads attracting more traffic", "People wanting more parks", "Higher petrol prices", "Fewer cars in cities"),
                q("What happened in Seoul in 2005?", "A highway was removed and a stream restored.", "A new motorway was built.", "Cars were banned completely.", "A metro line was closed."),
                q("What did critics predict in Seoul?", "Chaos", "Cleaner air", "More tourists", "Lower prices"),
                q("What is the lecturer's overall view?", "Cities should be designed for people, not cars.", "More motorways are needed.", "Traffic problems cannot be solved.", "Seoul made a mistake."),
            ),
        )
    }
}
