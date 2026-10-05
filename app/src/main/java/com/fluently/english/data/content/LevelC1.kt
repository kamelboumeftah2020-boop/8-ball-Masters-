package com.fluently.english.data.content

val LevelC1 = level(
    CefrLevel.C1,
    exam = listOf(
        q("Not only ___ late, but he also forgot the documents.", "was he", "he was", "he is", "did he be"),
        q("Rarely ___ such a talented young player.", "have I seen", "I have seen", "I saw", "did I saw"),
        q("You ___ told me earlier! Now it's too late.", "should have", "must have", "would", "should"),
        q("She ___ left already — her coat isn't here.", "must have", "should have", "can't have", "would have"),
        q("If I had taken that job, I ___ in London now.", "would be living", "would have lived", "will live", "lived"),
        q("___ by the noise, the baby started crying.", "Woken", "Waking", "Woke", "Having woke"),
        q("It was the manager ___ made the final decision.", "who", "which", "what", "whom"),
        q("What I need ___ a long holiday.", "is", "are", "be", "it is"),
        q("The results were ___ — they didn't support either theory.", "inconclusive", "inclusive", "concluded", "including"),
        q("Let's not beat about the ___. The project has failed.", "bush", "tree", "field", "garden"),
        order("Under no circumstances should you open this door", "لا يجب أن تفتح هذا الباب تحت أي ظرف"),
        listen("Contrary to popular belief, the study found that multitasking actually reduces productivity by up to forty percent.", "What did the study find about multitasking?", "It reduces productivity.", "It increases productivity by 40%.", "It has no effect.", "Most people can't do it."),
        listen("Having reviewed all the applications, the committee has decided to postpone its decision pending further interviews.", "What has the committee decided?", "To delay the decision", "To reject all applicants", "To accept the first candidate", "To cancel the interviews"),
        type("Complete the idiom: «It's not my cup of ___.»", "tea"),
    ),
) {
    unit("Emphasis & style", "التوكيد والأسلوب") {
        grammar(
            "Inversion for emphasis", "القلب (التقديم والتأخير) للتوكيد",
            notes = listOf(
                "في الإنجليزية الرسمية والأدبية نبدأ الجملة أحياناً بظرف نفي أو تقييد لتوكيد المعنى، ثم نعكس ترتيب الفاعل والفعل المساعد كما في السؤال.",
                "أشهر التعابير: Never (before), Rarely, Seldom, Hardly … when, No sooner … than, Not only … but also, Under no circumstances, Little, Only then / Only after.",
                "مثال: I have never seen such beauty → Never have I seen such beauty.",
                "إذا لم يكن في الجملة فعل مساعد نستخدم do / does / did: He rarely smiles → Rarely does he smile.",
                "الانعكاس في الشرطية الرسمية: Had I known… = If I had known… / Should you need help… = If you need help… / Were I you… = If I were you…",
            ),
            examples = listOf(
                "Not only is she intelligent, but she's also very kind." means "ليست ذكية فحسب، بل لطيفة جداً أيضاً.",
                "No sooner had we arrived than it started to rain." means "ما إن وصلنا حتى بدأ المطر.",
                "Little did he know what was waiting for him." means "لم يكن يعلم ما الذي ينتظره.",
                "Should you have any questions, please contact us." means "إذا كانت لديك أي أسئلة فلا تتردد في التواصل معنا.",
            ),
            questions = listOf(
                q("Never ___ such a beautiful sunset.", "have I seen", "I have seen", "I saw", "seen I have"),
                q("Hardly had I sat down ___ the phone rang.", "when", "than", "that", "then"),
                q("No sooner had she left ___ he arrived.", "than", "when", "then", "that"),
                q("Seldom ___ he admit his mistakes.", "does", "do", "is", "has"),
                q("___ I known about the problem, I would have acted.", "Had", "If", "Have", "Did"),
                q("Only after the exam ___ how easy it was.", "did I realise", "I realised", "I did realise", "realised I"),
                q("Under no circumstances ___ leave the building.", "should you", "you should", "you must", "must"),
                order("Little did they know about the danger", "لم يكونوا يعلمون شيئاً عن الخطر"),
                type("Complete: Not only ___ he lose his job, but he also lost his house. (فعل مساعد)", "did"),
            ),
        )
        vocabulary(
            "Academic vocabulary", "المفردات الأكاديمية",
            listOf(
                w("hypothesis", "فرضية", "The results support our original hypothesis."),
                w("significant", "ملحوظ / ذو دلالة", "There was a significant increase in sales."),
                w("phenomenon", "ظاهرة", "Migration is a global phenomenon."),
                w("comprehensive", "شامل", "The report gives a comprehensive overview."),
                w("subsequently", "لاحقاً / بعد ذلك", "He was arrested and subsequently released."),
                w("advocate", "يؤيد / يدافع عن", "She advocates equal access to education."),
                w("undermine", "يقوّض", "These rumours could undermine public trust."),
                w("inevitable", "حتمي", "Change is inevitable."),
            ),
        )
        reading(
            "The myth of multitasking", "خرافة تعدد المهام",
            passage = "For decades, the ability to multitask has been regarded as a valuable skill, frequently listed in job advertisements and praised in performance reviews. Yet a substantial body of research suggests that what we call multitasking is largely an illusion. Neuroscientists have demonstrated that the human brain cannot genuinely focus on two cognitively demanding tasks at once; instead, it rapidly switches between them. Each switch carries a cost — known as 'switching cost' — in time and accuracy. Some estimates suggest that habitual task-switching can reduce productivity by as much as 40%. Paradoxically, those who consider themselves the best multitaskers often perform worst in controlled experiments, being more easily distracted by irrelevant information. Not only does chronic multitasking impair performance, but it may also increase stress levels. Consequently, a growing number of organisations are now encouraging 'deep work': extended periods of uninterrupted concentration on a single task.",
            questions = listOf(
                q("What is the writer's main argument?", "Multitasking is mostly an illusion that harms performance.", "Multitasking is an essential skill.", "Only some people can multitask.", "Companies should hire multitaskers."),
                q("According to neuroscientists, what does the brain actually do?", "It switches quickly between tasks.", "It does two tasks simultaneously.", "It ignores the second task.", "It works faster under pressure."),
                q("What is surprising about self-described 'good multitaskers'?", "They often perform the worst.", "They have bigger brains.", "They are less stressed.", "They are the most productive."),
                q("The word «impair» is closest in meaning to…", "damage", "improve", "measure", "replace"),
                q("What is 'deep work'?", "Long periods of focus on one task", "Working late at night", "Doing many tasks quickly", "Working underground"),
            ),
        )
    }

    unit("Speculation & regret", "التخمين والندم") {
        grammar(
            "Past modals & mixed conditionals", "الأفعال الناقصة في الماضي والشرطية المختلطة",
            notes = listOf(
                "must have + V3: استنتاج شبه مؤكد عن الماضي: The streets are wet. It must have rained.",
                "can't / couldn't have + V3: استنتاج بأن شيئاً مستحيل حدوثه: She can't have seen me — she didn't say hello.",
                "might / may / could have + V3: احتمال في الماضي: He might have missed the bus.",
                "should have + V3: ندم أو لوم على شيء لم يحدث: You should have told me. وneedn't have + V3: فعلت شيئاً لم يكن ضرورياً.",
                "الشرطية المختلطة تربط الماضي بالحاضر: If I had studied medicine (ماضٍ), I would be a doctor now (حاضر). أو العكس: If I weren't afraid of flying, I would have visited you last year.",
            ),
            examples = listOf(
                "He must have forgotten our meeting." means "لا بد أنه نسي اجتماعنا.",
                "They can't have finished already!" means "مستحيل أن يكونوا قد انتهوا بالفعل!",
                "I should have listened to your advice." means "كان يجب أن أستمع إلى نصيحتك.",
                "If I had saved money, I wouldn't be in debt now." means "لو كنت ادخرت المال لما كنت مديناً الآن.",
            ),
            questions = listOf(
                q("The lights are off. They ___ gone to bed.", "must have", "should have", "can't have", "needn't have"),
                q("She ___ stolen the money — she was with me all day.", "can't have", "must have", "should have", "might"),
                q("I'm not sure where he is. He ___ gone home.", "might have", "must", "can't have", "should"),
                q("You ___ studied harder. Then you'd have passed.", "should have", "must have", "can't have", "might"),
                q("We ___ taken a taxi. The hotel was only five minutes away.", "needn't have", "mustn't have", "can't have", "shouldn't"),
                q("If she had taken the job in Dubai, she ___ there now.", "would be living", "would have lived", "will live", "lives"),
                q("If I ___ so shy, I would have spoken to her at the party.", "weren't", "hadn't been", "am not", "wouldn't be", explain = "شرطية مختلطة: صفة دائمة في الحاضر أدت إلى نتيجة في الماضي."),
                order("You should have asked for help", "كان يجب أن تطلب المساعدة"),
                type("Complete: She isn't answering. She ___ have left her phone at home. (احتمال)", "might", "may", "could", "must"),
            ),
        )
        vocabulary(
            "Idioms", "التعابير الاصطلاحية",
            listOf(
                w("break the ice", "يكسر الجمود", "He told a joke to break the ice."),
                w("hit the nail on the head", "يصيب كبد الحقيقة", "You've hit the nail on the head."),
                w("a blessing in disguise", "رُبّ ضارة نافعة", "Losing that job was a blessing in disguise."),
                w("cost an arm and a leg", "يكلف ثروة", "That car cost an arm and a leg."),
                w("on the fence", "متردد / محايد", "I'm still on the fence about moving."),
                w("burn the midnight oil", "يسهر للعمل أو الدراسة", "She burned the midnight oil before the exam."),
                w("the last straw", "القشة التي قصمت ظهر البعير", "His rude comment was the last straw."),
                w("once in a blue moon", "نادراً جداً", "We only eat out once in a blue moon."),
            ),
        )
        listening(
            "The mystery of the missing painting", "لغز اللوحة المفقودة",
            script = "Detective: So, the painting was here at eight last night, and by six this morning it was gone. The alarm didn't go off, so the thief must have known the security code. And look — there's no sign of forced entry. That means whoever did it must have had a key. Assistant: Could it have been the cleaner? Detective: He can't have done it — he was on holiday in Spain all week. The manager might have taken it, but she would have needed help; that painting weighs over fifty kilos. Assistant: What about the night guard? Detective: He should have been watching the cameras, but he says he fell asleep. If he had stayed awake, we would know exactly what happened. Either way, he's got some explaining to do.",
            questions = listOf(
                q("Why does the detective think the thief knew the code?", "The alarm didn't go off.", "The door was broken.", "The cleaner told him.", "There was a camera recording."),
                q("Why can't the cleaner be the thief?", "He was abroad.", "He doesn't have a key.", "He is too weak.", "He was asleep."),
                q("Why would the manager have needed help?", "The painting is very heavy.", "She doesn't know the code.", "She was in Spain.", "The door was locked."),
                q("What did the guard do wrong?", "He fell asleep instead of watching the cameras.", "He stole the painting.", "He turned off the alarm.", "He lost his key."),
                q("«He's got some explaining to do» means…", "He needs to justify his actions.", "He is a good teacher.", "He has explained everything.", "He is innocent."),
            ),
        )
    }

    unit("Writing with cohesion", "الكتابة المترابطة") {
        grammar(
            "Participle clauses", "جمل اسم الفاعل واسم المفعول",
            notes = listOf(
                "تختصر جمل الصلة والجمل الظرفية وتجعل الأسلوب أكثر رسمية وإيجازاً، بشرط أن يكون الفاعل واحداً في الجملتين.",
                "اسم الفاعل (ing) لمعنى مبني للمعلوم أو حدث متزامن: Walking down the street, I saw an old friend (= While I was walking…).",
                "اسم المفعول (V3) لمعنى مبني للمجهول: Built in 1890, the bridge is still in use (= which was built…).",
                "Having + V3 لحدث سابق تماماً للحدث الرئيسي: Having finished the report, she went home.",
                "تحذير من «الفاعل المعلّق»: Walking home, the rain started ✗ (المطر لا يمشي!) → Walking home, I got caught in the rain ✓.",
            ),
            examples = listOf(
                "Feeling tired, he went to bed early." means "لأنه كان يشعر بالتعب نام مبكراً.",
                "Written in simple language, the book is easy to read." means "الكتاب سهل القراءة لأنه مكتوب بلغة بسيطة.",
                "Having lived abroad, she understands different cultures." means "بما أنها عاشت في الخارج فهي تفهم ثقافات مختلفة.",
                "People living near the airport complain about the noise." means "يشتكي الناس الذين يسكنون قرب المطار من الضجيج.",
            ),
            questions = listOf(
                q("___ the door, she heard a strange noise.", "Opening", "Opened", "Having opened by", "To opened"),
                q("___ in 1931, the Empire State Building was once the tallest in the world.", "Completed", "Completing", "Having completing", "Complete"),
                q("___ his homework, he went out to play.", "Having finished", "Finished", "Being finish", "Having finish"),
                q("The man ___ next to me on the plane was a famous writer.", "sitting", "sat", "was sitting", "who sitting"),
                q("Most of the goods ___ in this factory are exported.", "made", "making", "are made", "have made"),
                q("___ by the news, she couldn't say a word.", "Shocked", "Shocking", "Having shocking", "Shock"),
                q("Which sentence is correct?", "Walking home, I saw a fox.", "Walking home, a fox crossed in front of me.", "Walked home, I saw a fox.", "Walking home, the fox was seen by me suddenly running."),
                order("Having lost his keys he couldn't get into the house", "بعد أن فقد مفاتيحه لم يستطع دخول المنزل"),
                type("Complete with «know»: Not ___ what to do, I called my father.", "knowing"),
            ),
        )
        vocabulary(
            "Linking words", "أدوات الربط",
            listOf(
                w("nevertheless", "ومع ذلك", "It was raining; nevertheless, we went out."),
                w("whereas", "في حين أن", "He likes tea, whereas she prefers coffee."),
                w("furthermore", "علاوة على ذلك", "The plan is cheap. Furthermore, it's effective."),
                w("consequently", "وبالتالي", "He didn't study and consequently failed."),
                w("in spite of", "على الرغم من", "In spite of the rain, the match continued."),
                w("provided that", "شريطة أن", "You can go, provided that you finish your work."),
                w("thereby", "وبذلك", "They cut costs, thereby increasing profits."),
                w("notwithstanding", "بصرف النظر عن", "Notwithstanding the risks, they continued."),
            ),
        )
        reading(
            "Should university be free?", "هل يجب أن يكون التعليم الجامعي مجانياً؟",
            passage = "Few issues divide public opinion as sharply as the question of whether higher education should be free. Advocates argue that education is a public good: a well-educated population drives innovation, raises tax revenues and strengthens democracy. Furthermore, they contend that tuition fees deter talented students from poorer backgrounds, thereby reinforcing social inequality. Opponents, however, point out that 'free' education is never truly free — it is funded by taxpayers, many of whom never attended university themselves. They also argue that graduates, who typically earn more over their lifetimes, should contribute to the cost of the education that benefits them. Having examined both positions, many economists propose a middle ground: income-contingent loans, whereby graduates repay their fees only once their earnings exceed a certain threshold. Such a system, they claim, preserves access while ensuring that the financial burden is shared fairly.",
            questions = listOf(
                q("According to advocates, why should university be free?", "Education benefits society as a whole.", "Universities have too much money.", "Graduates earn less.", "Taxes are too low."),
                q("What concern do opponents raise?", "Taxpayers who didn't attend university pay for it.", "Free education lowers quality.", "Students will stop working.", "There are too many universities."),
                q("What does «deter» mean in the text?", "discourage", "encourage", "attract", "require"),
                q("How do income-contingent loans work?", "Graduates repay only when they earn above a certain level.", "The government pays everything.", "Students pay before they start.", "Universities lend money to banks."),
                q("What is the tone of the text?", "Balanced and analytical", "Angry and emotional", "Humorous", "Strongly against free education"),
            ),
        )
    }

    unit("Business & persuasion", "الأعمال والإقناع") {
        grammar(
            "Cleft sentences", "الجمل المشطورة للتوكيد",
            notes = listOf(
                "الجمل المشطورة تقسّم الجملة إلى جزأين لإبراز معلومة معينة، وهي شائعة جداً في الكلام والكتابة المقنعة.",
                "It is / was + الجزء المؤكد + who / that…: It was Sara who solved the problem (ليس شخصاً آخر).",
                "What + جملة + is / was…: What I need is a long holiday. — What surprised me was his reaction.",
                "All (that)… is / was: All I want is some peace and quiet. وThe thing / reason / place…: The reason I called is to apologise.",
                "What … do is / was + (to) فعل: What she did was (to) call the police.",
            ),
            examples = listOf(
                "It was in Cairo that they first met." means "في القاهرة تحديداً التقيا أول مرة.",
                "What I love about this city is its energy." means "ما أحبه في هذه المدينة هو حيويتها.",
                "All you have to do is sign here." means "كل ما عليك فعله هو التوقيع هنا.",
                "What we need to do is reduce costs." means "ما نحتاج فعله هو تقليل التكاليف.",
            ),
            questions = listOf(
                q("It was my brother ___ broke the window, not me.", "who", "which", "what", "whom he"),
                q("___ I really need is a cup of coffee.", "What", "That", "Which", "It"),
                q("It's the price ___ worries me, not the quality.", "that", "what", "who", "where"),
                q("All I want ___ to be left alone.", "is", "are", "be", "it is"),
                q("What she did ___ resign immediately.", "was", "is that", "did", "has"),
                q("The reason ___ I'm calling is to confirm the meeting.", "why", "because", "what", "which"),
                q("Choose the cleft sentence that emphasises «the customer service».", "What impressed me was the customer service.", "The customer service impressed me.", "I was impressed by the customer service.", "Impressed me the customer service."),
                order("What we need is a clear strategy", "ما نحتاجه هو استراتيجية واضحة"),
                type("Complete: ___ was in 2015 that the company was founded.", "It"),
            ),
        )
        vocabulary(
            "Business & negotiation", "الأعمال والتفاوض",
            listOf(
                w("stakeholder", "صاحب مصلحة", "We need to consult all stakeholders."),
                w("leverage", "يستفيد من / نفوذ", "We can leverage our experience in this market."),
                w("compromise", "حل وسط", "Both sides reached a compromise."),
                w("revenue", "إيرادات", "Revenue rose by 15% last year."),
                w("merger", "اندماج", "The merger created the largest bank in the region."),
                w("feasible", "ممكن التنفيذ", "Is the plan financially feasible?"),
                w("concession", "تنازل", "We're not willing to make any more concessions."),
                w("win-win", "مربح للطرفين", "We're looking for a win-win solution."),
            ),
        )
        listening(
            "The negotiation", "التفاوض",
            script = "Supplier: We appreciate your interest, but I'm afraid our price of twelve dollars per unit is already very competitive. Buyer: I understand. However, what we're proposing is a three-year contract for fifty thousand units a year. Surely an order of that size justifies a discount. Supplier: It's true that volume matters. If you could commit to paying within thirty days rather than ninety, we might be able to go down to eleven. Buyer: That's a step in the right direction. What concerns us, though, is delivery. It was delays last year that cost us our biggest client. Supplier: Then let's include a penalty clause: for every week of delay, we'll reduce the price by two percent. Buyer: Now that sounds like a win-win. Let's put it in writing.",
            questions = listOf(
                q("What is the supplier's original price?", "\$12 per unit", "\$11 per unit", "\$50 per unit", "\$2 per unit"),
                q("What does the buyer offer in return for a discount?", "A large, long-term contract", "Cash payment today", "A new client", "Free delivery"),
                q("What condition does the supplier set for the \$11 price?", "Payment within 30 days", "A five-year contract", "Paying in advance", "Ordering 100,000 units"),
                q("Why is delivery important to the buyer?", "Delays lost them a major client.", "They need it tomorrow.", "Their warehouse is small.", "They had a penalty last year."),
                q("What is the final agreement about delays?", "A 2% price cut for every week of delay", "No delays are allowed", "The buyer pays extra for fast delivery", "The contract will be cancelled"),
            ),
        )
    }
}
