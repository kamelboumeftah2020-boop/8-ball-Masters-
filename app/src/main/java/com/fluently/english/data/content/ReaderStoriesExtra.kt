package com.fluently.english.data.content

// More original graded readers (fourth A1/A2 story, third C1/C2 story).

private fun ep(vararg paragraphs: String): String = paragraphs.joinToString("\n\n")

private fun eq(prompt: String, explanation: String, vararg options: String) =
    Question.Choice(prompt = prompt, options = options.toList(), explanation = explanation)

private fun ech(title: String, text: String, glossary: Map<String, String>, vararg questions: Question.Choice) =
    ReaderChapter(title = title, text = text, glossary = glossary, questions = questions.toList())

internal val ReadersExtra: List<GradedReader> = listOf(
    // ───────────── A1 ─────────────
    GradedReader(
        id = "r-a1-4",
        level = CefrLevel.A1,
        title = "The Football Match",
        titleAr = "مباراة كرة القدم",
        genreAr = "رياضة",
        summaryAr = "يوسف يحب كرة القدم، لكنه دائماً يجلس على مقعد البدلاء. هل يلعب في المباراة الكبيرة هذا الأسبوع؟",
        chapters = listOf(
            ech(
                "Always on the Bench",
                ep(
                    "Youssef is twelve. He lives in Casablanca with his family. He loves football. He plays every day after school with his friends in the street.",
                    "Youssef is in the school team, but he is not a star. Every Saturday, the team plays a match. And every Saturday, Youssef sits on the bench. He watches the other boys play. He is sad, but he does not stop. He practises every evening.",
                    "On Monday, the coach says, “This Saturday we play the big match against Green Park School. It is the final!” All the boys are excited. Youssef is excited too, but he knows he will be on the bench again."
                ),
                mapOf(
                    "team" to "فريق",
                    "star" to "نجم",
                    "match" to "مباراة",
                    "bench" to "مقعد البدلاء",
                    "practises" to "يتدرّب",
                    "coach" to "المدرّب",
                    "final" to "المباراة النهائية",
                ),
                eq("Where does Youssef live?", "يعيش يوسف في الدار البيضاء.", "In Casablanca", "In Cairo", "In Green Park", "At school"),
                eq("What does Youssef do every Saturday?", "يجلس على مقعد البدلاء ويشاهد المباراة.", "He sits on the bench.", "He scores goals.", "He stays at home.", "He goes to the beach."),
                eq("Why are the boys excited?", "لأن المباراة الكبيرة هذا السبت هي النهائي.", "There is a final on Saturday.", "There is no school.", "They have a new coach.", "It is Youssef’s birthday."),
            ),
            ech(
                "A Big Problem",
                ep(
                    "On Saturday morning, the team is at the stadium. The weather is hot and sunny. There are a lot of people. Youssef’s mother and father are there too.",
                    "The match starts. Green Park School is very good. After twenty minutes, they score a goal. It is one-nil. Then there is a problem. Karim, the best player, falls down. He hurts his leg. He cannot play.",
                    "The coach looks at the bench. He looks at Youssef. “Youssef!” he says. “Are you ready?” Youssef cannot believe it. His heart is beating fast. “Yes,” he says. “I am ready.”"
                ),
                mapOf(
                    "stadium" to "ملعب",
                    "score a goal" to "يسجّلون هدفاً",
                    "falls down" to "يسقط",
                    "hurts his leg" to "يؤذي ساقه",
                    "ready" to "مستعد",
                    "believe" to "يصدّق",
                    "beating fast" to "يدق بسرعة",
                ),
                eq("Who is at the stadium?", "والدا يوسف جاءا لمشاهدة المباراة.", "Youssef’s parents", "Youssef’s teacher", "Nobody", "Only the players"),
                eq("What happens to Karim?", "سقط كريم وآذى ساقه فلم يعد يستطيع اللعب.", "He hurts his leg.", "He scores a goal.", "He goes home early.", "He becomes the coach."),
                eq("How does Youssef feel when the coach calls him?", "قلبه يدق بسرعة ولا يصدّق، أي أنه متحمس ومتوتر.", "Excited and nervous", "Angry", "Bored", "Sleepy"),
            ),
            ech(
                "The Last Minute",
                ep(
                    "Youssef runs onto the field. He is nervous, but he remembers his practice. He passes the ball well. He runs and runs. In the second half, his friend Ali scores. It is one-one!",
                    "There is one minute left. Ali passes the ball to Youssef. Youssef looks at the goal. He kicks the ball hard. The goalkeeper jumps, but the ball goes into the net. Goal! It is two-one!",
                    "The match ends. Youssef’s team wins the final! His friends lift him up. His mother is crying and his father is shouting. The coach smiles and says, “Practice is never a waste of time, Youssef.”"
                ),
                mapOf(
                    "field" to "الملعب، أرض الملعب",
                    "nervous" to "متوتر",
                    "passes the ball" to "يمرّر الكرة",
                    "second half" to "الشوط الثاني",
                    "kicks" to "يركل",
                    "goalkeeper" to "حارس المرمى",
                    "lift him up" to "يرفعونه",
                    "a waste of time" to "مضيعة للوقت",
                ),
                eq("What is the final score?", "فاز فريق يوسف بنتيجة اثنين مقابل واحد.", "Two-one", "One-one", "One-nil", "Three-two"),
                eq("Who scores the winning goal?", "يوسف سجّل الهدف الأخير في الدقيقة الأخيرة.", "Youssef", "Ali", "Karim", "The coach"),
                eq("What is the message of the story?", "التدريب المستمر ليس مضيعة للوقت.", "Practice is important.", "Football is dangerous.", "Coaches are always right.", "Only stars can win."),
            ),
        ),
    ),
    // ───────────── A2 ─────────────
    GradedReader(
        id = "r-a2-4",
        level = CefrLevel.A2,
        title = "The Secret Recipe",
        titleAr = "الوصفة السرية",
        genreAr = "عائلة",
        summaryAr = "جدّة سلمى تصنع أشهر كعك في الحيّ، لكنها لا تكتب الوصفة أبداً. وعندما تمرض الجدة، يجب على سلمى أن تتذكّر كل شيء.",
        chapters = listOf(
            ech(
                "Grandma’s Kitchen",
                ep(
                    "Every Friday, Salma visited her grandmother after school. Grandma Huda lived in an old house with a small garden full of mint and lemon trees. Her kitchen always smelled of cinnamon and fresh bread.",
                    "Grandma Huda was famous in the neighbourhood for her date cakes. People came from other streets to buy them for weddings and holidays. Many people asked for the recipe, but Grandma always laughed and said, “It’s a secret. It’s in my head, not on paper.”",
                    "Salma loved helping her. She washed the bowls, cut the dates and watched carefully. Grandma talked while she worked. “A little more butter,” she said, “and never rush the dough. Good things need time.”"
                ),
                mapOf(
                    "smelled of" to "كانت رائحته",
                    "cinnamon" to "قرفة",
                    "neighbourhood" to "الحيّ",
                    "recipe" to "وصفة",
                    "bowls" to "أوعية",
                    "carefully" to "بانتباه",
                    "rush" to "يستعجل",
                    "dough" to "العجين",
                ),
                eq("Why was Grandma Huda famous?", "كانت مشهورة بكعك التمر في الحيّ.", "For her date cakes", "For her garden", "For her old house", "For her books"),
                eq("Where was the recipe?", "قالت الجدة إن الوصفة في رأسها وليست على ورق.", "Only in Grandma’s head", "In a cookbook", "On the kitchen wall", "On the internet"),
                eq("What did Salma do in the kitchen?", "كانت تغسل الأوعية وتقطّع التمر وتراقب بانتباه.", "She helped and watched carefully.", "She sold the cakes.", "She did her homework.", "She slept."),
            ),
            ech(
                "Bad News",
                ep(
                    "One Friday in winter, Salma arrived at the house, but the kitchen was cold and quiet. Her mother was there. “Grandma is in hospital,” she said. “She fell last night and broke her arm. She’s OK, but she can’t cook for a few weeks.”",
                    "Salma felt terrible. Then her mother remembered something. “Oh no. Mrs Karam’s daughter is getting married on Sunday. Grandma promised to make two hundred cakes for the wedding!”",
                    "Salma thought for a moment. “I can make them,” she said quietly. Her mother looked surprised. “But there isn’t a recipe!” Salma smiled. “There is,” she said. “It’s in my head now too.”"
                ),
                mapOf(
                    "arrived" to "وصلت",
                    "in hospital" to "في المستشفى",
                    "broke her arm" to "كسرت ذراعها",
                    "terrible" to "سيئ جداً",
                    "getting married" to "تتزوج",
                    "promised" to "وعدت",
                    "surprised" to "متفاجئة",
                ),
                eq("Why was Grandma in hospital?", "سقطت وكسرت ذراعها.", "She fell and broke her arm.", "She had a cold.", "She burned her hand.", "She was visiting a friend."),
                eq("What did Grandma promise to do?", "وعدت بصنع مئتي كعكة لعرس ابنة السيدة كرم.", "Make 200 cakes for a wedding", "Visit Mrs Karam", "Write the recipe", "Sell her house"),
                eq("What does Salma mean by “It’s in my head now too”?", "تعلّمت الوصفة من مراقبة جدتها.", "She learned the recipe by watching.", "She has a headache.", "She found the recipe on paper.", "She will ask the neighbours."),
            ),
            ech(
                "Two Hundred Cakes",
                ep(
                    "On Saturday, Salma and her mother worked all day. Salma remembered everything: a little more butter, a pinch of cardamom, and never rush the dough. Her mother was amazed. By midnight, there were two hundred golden cakes on the table.",
                    "At the wedding, the guests loved the cakes. Mrs Karam tasted one and closed her eyes. “These are Huda’s cakes,” she said. “But who made them?” Salma’s mother pointed to her daughter. Everyone clapped.",
                    "On Monday, Salma visited the hospital with a box of cakes. Grandma tasted one and smiled. “A little too much sugar,” she said. Then she laughed and held Salma’s hand. “But it’s perfect. Now the secret has a new home.”"
                ),
                mapOf(
                    "a pinch of" to "رشّة من",
                    "cardamom" to "هيل",
                    "amazed" to "مندهشة",
                    "golden" to "ذهبية اللون",
                    "guests" to "الضيوف",
                    "tasted" to "تذوّقت",
                    "clapped" to "صفّقوا",
                ),
                eq("How many cakes did Salma and her mother make?", "صنعتا مئتي كعكة ذهبية.", "Two hundred", "Twenty", "One hundred", "Two"),
                eq("How did Mrs Karam know the cakes were special?", "عرفت طعم كعك هدى الذي اعتادت عليه.", "They tasted like Huda’s cakes.", "Salma told her.", "They had a label.", "They were very big."),
                eq("What does Grandma mean by “the secret has a new home”?", "الوصفة الآن عند سلمى وستستمر في العائلة.", "Salma now keeps the recipe.", "Grandma is moving house.", "The recipe is lost.", "The cakes will be sold in a shop."),
            ),
        ),
    ),
    // ───────────── C1 ─────────────
    GradedReader(
        id = "r-c1-3",
        level = CefrLevel.C1,
        title = "The Night Shift",
        titleAr = "المناوبة الليلية",
        genreAr = "دراما طبية",
        summaryAr = "طبيبة متدرّبة في أول مناوبة ليلية لها في قسم الطوارئ تواجه قراراً يتحدّى كل ما تعلّمته في الكتب.",
        chapters = listOf(
            ech(
                "Three in the Morning",
                ep(
                    "By three in the morning, Dr Leila Mansour had stopped counting the patients. Her first night shift in the emergency department had begun with a sprained ankle and a child with a fever, and had escalated, hour by hour, into something closer to controlled chaos.",
                    "The senior doctor on duty, a softly spoken man named Dr Okafor, had been called to theatre shortly after midnight. Before leaving, he had squeezed her shoulder and told her to trust her training. It was, she reflected, the sort of advice that sounded far more reassuring in daylight.",
                    "At 3:10, a nurse handed her a new chart. The patient was an elderly man named Mr Brennan, brought in by his neighbour after complaining of indigestion. His observations were unremarkable, his manner apologetic. “I’m sure it’s nothing, Doctor,” he said. “I don’t want to waste your time.”",
                    "Leila glanced at the waiting room, which was overflowing, and then back at Mr Brennan. Something about the grey tinge to his skin nagged at her, though she could not have said precisely what."
                ),
                mapOf(
                    "sprained ankle" to "التواء في الكاحل",
                    "escalated" to "تصاعد",
                    "controlled chaos" to "فوضى منظّمة",
                    "on duty" to "المناوب",
                    "theatre" to "غرفة العمليات",
                    "unremarkable" to "عادية، لا تلفت النظر",
                    "overflowing" to "مكتظّة",
                    "nagged at her" to "ظلّ يقلقها",
                ),
                eq("Why was Leila alone in charge?", "استُدعي الطبيب الأقدم إلى غرفة العمليات بعد منتصف الليل.", "The senior doctor had been called to theatre.", "The other doctors were on strike.", "She had asked to work alone.", "It was a quiet night."),
                eq("How did Mr Brennan describe his problem?", "قال إنها عسر هضم واعتذر عن إضاعة الوقت.", "As indigestion that was probably nothing", "As severe chest pain", "As a broken arm", "As a high fever"),
                eq("What made Leila uneasy?", "اللون الرمادي لبشرته رغم أن مؤشراته عادية.", "The grey colour of his skin", "His rude behaviour", "The neighbour’s story", "The long waiting time"),
            ),
            ech(
                "Instinct",
                ep(
                    "Protocol was clear: a patient with normal observations and mild symptoms could wait while more urgent cases were seen. Leila had memorised the triage guidelines as a student and could recite them in her sleep. Yet medicine, as Dr Okafor liked to say, was not a vending machine. You did not simply insert symptoms and receive a diagnosis.",
                    "She ordered an ECG anyway. The nurse raised an eyebrow but said nothing. When the trace came back, Leila stared at it for a long moment. The changes were subtle — so subtle that a tired eye might easily have missed them — but they were there. Mr Brennan was having a heart attack.",
                    "What followed was a blur of activity. Leila called the cardiology team, started the treatment she had rehearsed a hundred times in simulations, and tried to keep her voice steady as she explained to Mr Brennan what was happening. He took the news with remarkable calm. “Well,” he said, “I suppose it wasn’t indigestion after all.”",
                    "Within forty minutes, he was on his way to the catheter lab. Leila sat down for the first time in six hours and discovered that her hands were trembling."
                ),
                mapOf(
                    "Protocol" to "البروتوكول، الإجراء المعتمد",
                    "triage" to "فرز المرضى حسب الأولوية",
                    "recite" to "يسرد من الذاكرة",
                    "raised an eyebrow" to "رفعت حاجبها (استغراباً)",
                    "subtle" to "دقيقة، خفيّة",
                    "a blur of activity" to "نشاط متسارع",
                    "rehearsed" to "تدرّبت عليه",
                    "trembling" to "ترتجفان",
                ),
                eq("Why was ordering an ECG unusual?", "حسب البروتوكول كان يمكن أن ينتظر لأن مؤشراته عادية.", "Protocol suggested he could wait.", "The machine was broken.", "The patient refused it.", "Only surgeons can order it."),
                eq("What does Dr Okafor mean by “medicine is not a vending machine”?", "التشخيص يحتاج إلى حكم وتفكير لا مجرد تطبيق آلي.", "Diagnosis requires judgement, not just rules.", "Hospitals should sell food.", "Medicine is too expensive.", "Machines are better than doctors."),
                eq("How did Leila feel after the patient left?", "ارتجفت يداها، أي أنها كانت متأثرة بعد التوتر.", "Shaken after the stress", "Bored", "Angry with the nurse", "Disappointed"),
            ),
            ech(
                "Morning",
                ep(
                    "When Dr Okafor emerged from theatre at dawn, he found Leila writing up her notes in the staff room, a cold cup of tea at her elbow. The nurse had already told him about Mr Brennan. He sat down opposite her and said nothing for a while.",
                    "“You broke the rules,” he said eventually.",
                    "Leila’s stomach dropped. “I know. I just had a feeling that—”",
                    "“No,” he interrupted gently. “You didn’t have a feeling. You noticed something your training had taught you to notice, even if you couldn’t yet put it into words. That isn’t breaking the rules. That’s what the rules are for — to support judgement, not replace it.”",
                    "Later that morning, Leila visited the cardiology ward. Mr Brennan was sitting up in bed, pale but cheerful, complaining about the hospital breakfast. When he saw her, he raised his cup of tea in a small salute.",
                    "“My neighbour wants to thank you,” he said. “She says if it weren’t for you, she’d have had to find someone else to feed her cat.” Leila laughed — properly, for the first time in what felt like days — and realised that she was no longer counting the hours until her shift ended. She was already thinking about the next one."
                ),
                mapOf(
                    "emerged" to "خرج",
                    "at dawn" to "عند الفجر",
                    "at her elbow" to "بجانبها مباشرة",
                    "stomach dropped" to "انقبض قلبها خوفاً",
                    "put it into words" to "تعبّر عنه بالكلمات",
                    "in a small salute" to "في تحية صغيرة",
                    "if it weren’t for you" to "لولاك",
                ),
                eq("How did Dr Okafor react to Leila’s decision?", "اعتبر أنها استخدمت حكمها المهني الذي تدعمه القواعد.", "He praised her judgement.", "He reported her to the director.", "He was angry and silent.", "He said she was lucky."),
                eq("According to Dr Okafor, what are rules for?", "القواعد لدعم الحكم المهني لا لتحلّ محلّه.", "To support judgement, not replace it", "To protect doctors from mistakes", "To make work faster", "To be followed without thinking"),
                eq("How has Leila changed by the end?", "صارت متحمسة للمناوبة القادمة بدل عدّ الساعات.", "She now looks forward to her work.", "She wants to leave medicine.", "She is afraid of night shifts.", "She wants to become a cardiologist immediately."),
            ),
        ),
    ),
    // ───────────── C2 ─────────────
    GradedReader(
        id = "r-c2-3",
        level = CefrLevel.C2,
        title = "The Archive of Unsent Letters",
        titleAr = "أرشيف الرسائل التي لم تُرسَل",
        genreAr = "أدب تأملي",
        summaryAr = "أمينة أرشيف في مكتب بريد قديم في بيروت تعثر على صندوق من الرسائل التي لم تصل أبداً، فتقرّر — رغم كل القواعد — أن تُكمل رحلتها.",
        chapters = listOf(
            ech(
                "Dead Letters",
                ep(
                    "Every postal service, Rania had discovered, possessed a room it preferred not to talk about. In Beirut’s central sorting office, it occupied the basement: a long, low-ceilinged space where undeliverable letters came to rest, their addresses smudged beyond recognition or their recipients long since departed. Officially, it was called the Returns Department. Unofficially, everyone called it the graveyard.",
                    "Rania had been transferred there as a quiet punishment, she suspected, for asking too many questions at her previous post. She had expected to loathe it. Instead, within a month, she had grown oddly attached to the place — to its smell of damp paper and the hush that settled over it once the sorting machines upstairs fell silent.",
                    "Regulations stipulated that letters which could not be delivered within a year were to be destroyed. Rania’s predecessor had evidently taken a relaxed view of this rule. Behind a row of filing cabinets she found a tea chest, its lid warped with age, containing several hundred envelopes dating from the late 1970s — the years, she realised with a jolt, when the city had been cut in two."
                ),
                mapOf(
                    "smudged beyond recognition" to "ممسوحة بحيث لا يمكن قراءتها",
                    "long since departed" to "رحلوا منذ زمن بعيد",
                    "loathe" to "يمقت، يكره بشدة",
                    "oddly attached" to "متعلّقة على نحو غريب",
                    "hush" to "سكون",
                    "stipulated" to "نصّت على",
                    "predecessor" to "سلفها، من سبقها في المنصب",
                    "with a jolt" to "بصدمة مفاجئة",
                ),
                eq("Why did people call the Returns Department “the graveyard”?", "لأن الرسائل التي لا يمكن تسليمها تنتهي هناك.", "Undeliverable letters ended up there.", "It was next to a cemetery.", "Workers there were very old.", "It was always dark and cold."),
                eq("How did Rania’s feelings about the department change?", "توقعت أن تكرهه لكنها تعلّقت به.", "She expected to hate it but grew attached to it.", "She loved it at first and then hated it.", "She never stopped hating it.", "She asked to leave immediately."),
                eq("Why was the discovery of the tea chest significant?", "الرسائل تعود إلى سنوات انقسام المدينة في الحرب.", "The letters dated from when the city was divided.", "It contained money.", "It belonged to Rania’s family.", "It was made of rare wood."),
            ),
            ech(
                "Across the Line",
                ep(
                    "She told herself she was merely cataloguing them. That was, after all, her job. But cataloguing required reading the envelopes, and reading the envelopes led, inevitably, to wondering — and by the end of the second week Rania had admitted to herself that she was no longer doing her job so much as excavating a buried city.",
                    "Most of the letters had been posted from one side of the old dividing line to the other, during months when the post simply ceased to cross it. A mother writing to a son who had stayed behind to guard the family shop. A student apologising to a professor for a thesis she would never finish. A man whose handwriting grew progressively less steady, writing to a woman named Mariam at an address in Achrafieh, every week for eleven months.",
                    "Rania did not open them; that, she felt, would be a trespass she could not justify. But she began, in her evenings, to search old directories and municipal records, and to her astonishment discovered that a handful of the intended recipients were still alive. Among them, now eighty-four and living barely a kilometre from the sorting office, was a woman named Mariam Haddad."
                ),
                mapOf(
                    "cataloguing" to "فهرسة",
                    "inevitably" to "حتماً",
                    "excavating" to "تنقّب عن",
                    "ceased" to "توقّف",
                    "progressively" to "تدريجياً",
                    "trespass" to "تعدٍّ، انتهاك",
                    "to her astonishment" to "لدهشتها الكبيرة",
                ),
                eq("Why had these letters never been delivered?", "أُرسلت عبر خط التقسيم في أشهر توقّف فيها البريد عن العبور.", "The post stopped crossing the dividing line.", "The stamps were missing.", "The senders changed their minds.", "The post office burned down."),
                eq("Why did Rania refuse to open the letters?", "رأت أن فتحها انتهاك لا تستطيع تبريره.", "She felt it would be an unjustifiable intrusion.", "It was physically impossible.", "Her manager was watching.", "She could not read the handwriting."),
                eq("What did her research reveal?", "بعض المرسَل إليهم ما زالوا أحياء، ومنهم مريم حداد.", "Some recipients were still alive.", "All the recipients had died.", "The letters were fake.", "The senders were all soldiers."),
            ),
            ech(
                "Delivery",
                ep(
                    "Regulations, Rania reflected on the morning she set out, were remarkably silent on the question of letters delivered forty-five years late. She carried the bundle — fifty-one envelopes, tied with the same faded string — in a canvas bag, and found that her heart was pounding as though she, and not some stranger long ago, had written every word.",
                    "Mariam Haddad lived on the third floor of a building whose balconies overflowed with jasmine. She was small and very upright, with the unhurried courtesy of someone who had long ago stopped expecting surprises. She listened to Rania’s halting explanation without interrupting. Then she held out her hands.",
                    "She did not open the letters either. She turned them over one by one, tracing the handwriting with a fingertip, and when she reached the last — the one in which the script had grown faint and wavering — she pressed it briefly to her lips.",
                    "“He was my fiancé,” she said at last. “We were to be married the spring the fighting began. I was told he had left the country without a word. For forty-five years I believed that he had simply stopped loving me.” She looked up, and her eyes were dry and astonishingly clear. “You have not brought me sorrow, my dear. Sorrow I have had for a long time. You have brought me the truth, which is something else entirely.”",
                    "Rania walked back to the sorting office slowly, through streets that had once been a frontier. In the basement, she took out a fresh notebook and wrote a new heading across the first page. Then she lifted the next envelope from the chest and began, with great care, to read the address."
                ),
                mapOf(
                    "set out" to "انطلقت",
                    "unhurried courtesy" to "لطف هادئ غير متعجّل",
                    "halting" to "متقطّع، متردّد",
                    "tracing" to "تتتبّع (بإصبعها)",
                    "wavering" to "مرتعش",
                    "were to be married" to "كان مقرّراً أن نتزوّج",
                    "sorrow" to "حزن",
                    "frontier" to "خط حدودي",
                ),
                eq("Who had written the letters to Mariam?", "خطيبها الذي ظنّت أنه تخلّى عنها.", "Her fiancé", "Her brother", "Her son", "A stranger"),
                eq("What had Mariam believed for forty-five years?", "أن خطيبها غادر البلد دون كلمة وتوقّف عن حبّها.", "That he had stopped loving her", "That he had died in the war", "That the letters were lost at sea", "That he had married someone else in Beirut"),
                eq("What does Rania’s final action suggest?", "ستواصل إيصال الرسائل الأخرى إلى أصحابها.", "She will try to deliver the other letters too.", "She plans to destroy the letters.", "She will resign from the post office.", "She will write a novel about Mariam."),
            ),
        ),
    ),
)
