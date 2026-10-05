package com.fluently.english.data.content

// Original graded-reader stories for the advanced levels (C1–C2).

private fun cp(vararg paragraphs: String): String = paragraphs.joinToString("\n\n")

private fun cq(prompt: String, explanation: String, vararg options: String) =
    Question.Choice(prompt = prompt, options = options.toList(), explanation = explanation)

private fun cch(title: String, text: String, glossary: Map<String, String>, vararg questions: Question.Choice) =
    ReaderChapter(title = title, text = text, glossary = glossary, questions = questions.toList())

// ───────────────────────────── C1 ─────────────────────────────

internal val ReadersC1: List<GradedReader> = listOf(
    GradedReader(
        id = "r-c1-1",
        level = CefrLevel.C1,
        title = "The Last Translator",
        titleAr = "المترجمة الأخيرة",
        genreAr = "خيال علمي",
        summaryAr = "في عالم تترجم فيه الآلات كل شيء فوراً، تتلقى مترجمة بشرية عجوز طلباً غريباً لا تستطيع أي آلة تنفيذه.",
        chapters = listOf(
            cch(
                "An Unusual Request",
                cp(
                    "By 2061, hardly anyone remembered that translation had once been a profession. Earpieces rendered every conversation into the listener’s language in real time, and the few human translators who remained were regarded as charming relics, rather like blacksmiths or lighthouse keepers.",
                    "Nadia Saleh, seventy-three and stubbornly unretired, still kept an office above a bakery in Beirut. Most weeks, nobody climbed the stairs. So when a young man in an expensive coat knocked on her door one rainy Tuesday, she assumed he had lost his way.",
                    "“You’re the translator?” he asked, glancing at the shelves of dog-eared dictionaries. He introduced himself as Karim, an engineer at the company that built the earpieces. Then he placed a battered notebook on her desk. “This belonged to my grandmother. She wrote poems in a dialect from a village that no longer exists. Our system can’t make sense of them. It keeps producing nonsense.”",
                    "Nadia turned the pages slowly. The handwriting was cramped but elegant, the vocabulary a tangle of Arabic, Armenian and words she had not heard since childhood. She felt something stir that she had almost forgotten: curiosity, sharp and urgent.",
                    "“Why not hire a linguist?” she asked. Karim hesitated. “Because a linguist would explain the words. I want someone who can tell me what she meant.”"
                ),
                mapOf(
                    "rendered" to "حوّل، ترجم",
                    "relics" to "بقايا من الماضي",
                    "stubbornly" to "بعناد",
                    "dog-eared" to "مثنية الزوايا من كثرة الاستعمال",
                    "battered" to "مهترئ",
                    "a tangle of" to "خليط متشابك من",
                    "stir" to "يتحرك، يستيقظ",
                ),
                cq("Why were human translators rare in 2061?", "سماعات الأذن كانت تترجم كل محادثة فوراً.",
                    "Machines translated conversations instantly.", "Translation was made illegal.", "People stopped learning languages at school.", "Translators earned too little money."),
                cq("Why couldn’t the company’s system translate the notebook?", "القصائد مكتوبة بلهجة قرية لم تعد موجودة.",
                    "It was written in the dialect of a vanished village.", "The handwriting was unreadable.", "The notebook was damaged by rain.", "The poems were in a secret code."),
                cq("What does Karim’s final remark suggest he wants?", "يريد فهم المعنى والمشاعر لا مجرد شرح الكلمات.",
                    "An understanding of meaning, not just words", "A cheaper service than a linguist", "A dictionary of the dialect", "Help to sell the poems"),
            ),
            cch(
                "Between the Lines",
                cp(
                    "For three weeks, Nadia worked on the notebook every morning. She travelled to the mountains to find the handful of elderly people who still remembered the village, recording their stories over endless cups of coffee. Gradually, the poems began to yield their secrets.",
                    "They were not, as Karim had assumed, about love or the landscape. They were about leaving. The grandmother, Siran, had written them in the months before the village was abandoned, and each poem was addressed to an object she would have to leave behind: a cooking pot, a fig tree, the threshold of her house.",
                    "One line troubled Nadia more than any other. The machine had rendered it as “The salt is in the wall.” Technically, this was accurate. Yet an old shepherd explained that, in the village, families hid a little salt inside the walls of a new home as a promise that guests would always be welcome there. To say the salt was in the wall was to say: this house will remember us.",
                    "Nadia sat back and rubbed her eyes. A word-for-word translation was not wrong, exactly; it was simply empty. Meaning, she reflected, lived in the spaces that machines were designed to skip over — in custom, in memory, in what a community took for granted."
                ),
                mapOf(
                    "handful of" to "عدد قليل من",
                    "yield their secrets" to "تبوح بأسرارها",
                    "abandoned" to "مهجور",
                    "threshold" to "عتبة الباب",
                    "Technically" to "من الناحية التقنية",
                    "word-for-word" to "حرفية",
                    "took for granted" to "اعتبره أمراً مسلّماً به",
                ),
                cq("What were the poems really about?", "كانت عن الرحيل، وكل قصيدة موجهة إلى شيء ستتركه.",
                    "Leaving home and the objects left behind", "Romantic love", "The beauty of the mountains", "Life in a big city"),
                cq("What did “The salt is in the wall” actually mean?", "الملح في الجدار وعد بالضيافة، أي أن البيت سيتذكرهم.",
                    "The house will remember the family.", "The walls of the house were damaged.", "The family kept food in the walls.", "Salt was very expensive."),
                cq("What is Nadia’s view of machine translation by the end of the chapter?", "الترجمة الحرفية ليست خاطئة لكنها فارغة من المعنى الثقافي.",
                    "Literal translation misses cultural meaning.", "Machines always make grammar mistakes.", "Machines are better than humans.", "Translation is impossible."),
            ),
            cch(
                "What Remains",
                cp(
                    "When Karim returned, Nadia handed him two documents. The first was a precise translation of every poem. The second, much longer, was what she called a companion: page after page explaining the customs, the people and the losses hidden inside each line.",
                    "He read in silence for nearly an hour. At one point he laughed; at another he had to stop and look out of the window. “She never talked about the village,” he said eventually. “I thought she had simply forgotten it.”",
                    "“People rarely forget,” Nadia replied. “They just stop finding listeners.”",
                    "A month later, Karim came back with an unexpected proposal. His company wanted to build a new feature that would flag words and phrases carrying cultural weight, inviting users to learn the story behind them instead of receiving a flat equivalent. They needed someone to teach the engineers what to look for.",
                    "Nadia laughed at the irony: the machines that had made her obsolete now needed her to make them more human. She agreed on one condition — that the first story they recorded would be Siran’s.",
                    "That evening, she bought a small bag of salt on her way home. Back in her office, she pressed a few grains into a crack in the wall above her desk. It was, she told herself, a perfectly sensible thing for a translator to do."
                ),
                mapOf(
                    "precise" to "دقيق",
                    "companion" to "دليل مرافق",
                    "eventually" to "في النهاية",
                    "proposal" to "اقتراح، عرض",
                    "flag" to "يُعلِّم، يُنبِّه إلى",
                    "a flat equivalent" to "مقابل جاف بلا روح",
                    "the irony" to "المفارقة",
                    "obsolete" to "عفا عليه الزمن",
                ),
                cq("What did the second document Nadia gave Karim contain?", "شرح العادات والناس والخسارات المخفية في كل سطر.",
                    "Explanations of the customs and stories behind each line", "A list of grammar mistakes", "Her invoice for the work", "A dictionary of Armenian words"),
                cq("What did Karim’s company want Nadia to do?", "تعليم المهندسين التعرف على الكلمات ذات الحمولة الثقافية.",
                    "Teach engineers to recognise culturally loaded words", "Translate all their user manuals", "Stop working as a translator", "Sell them her dictionaries"),
                cq("Why does Nadia put salt in the wall at the end?", "تستعير عادة القرية لتعني أن هذا المكان سيحتفظ بالذكرى.",
                    "As a symbolic gesture of memory and welcome", "To repair a crack in the wall", "Because she believes in magic", "To keep insects away"),
            ),
        ),
    ),
    GradedReader(
        id = "r-c1-2",
        level = CefrLevel.C1,
        title = "The Quiet Partner",
        titleAr = "الشريك الصامت",
        genreAr = "تشويق في عالم الأعمال",
        summaryAr = "محاسبة شابة في شركة ناشئة ناجحة تكتشف أرقاماً لا تتطابق، وعليها أن تقرر ما الذي تخاطر به من أجل الحقيقة.",
        chapters = listOf(
            cch(
                "Numbers That Don’t Add Up",
                cp(
                    "Mira Haddad had been at Lumen for barely six months when she noticed the discrepancy. The start-up, which made solar lamps for villages without electricity, was the darling of the investment world, and its founder, Theo Marsh, appeared on magazine covers with reassuring regularity.",
                    "Mira’s job was unglamorous: reconciling the accounts at the end of each quarter. It was tedious work, but she had a talent for spotting patterns, and one pattern refused to go away. Every three months, a payment of exactly 240,000 dollars left the company for a supplier called Northgate Components. Yet when she checked the warehouse records, she could find no trace of anything Northgate had delivered.",
                    "At first, she assumed she had overlooked something. Large companies, she reminded herself, were full of arrangements that made perfect sense to the people who had negotiated them. She mentioned it, casually, to her manager, Daniel.",
                    "His reaction surprised her. He smiled a little too quickly and told her not to worry: Northgate was a long-standing partner, and the paperwork was handled personally by Theo. “Honestly, Mira,” he said, “focus on your own figures. That one’s above our pay grade.”",
                    "She went back to her desk, but she could not concentrate. In her experience, people only told you not to worry when there was something worth worrying about."
                ),
                mapOf(
                    "discrepancy" to "تناقض، عدم تطابق",
                    "the darling of" to "المدلّل لدى",
                    "unglamorous" to "غير لامع، عادي",
                    "reconciling the accounts" to "مطابقة الحسابات",
                    "tedious" to "مُملّ",
                    "overlooked" to "أغفل",
                    "above our pay grade" to "ليس من صلاحياتنا",
                ),
                cq("What did Mira discover?", "دفعات منتظمة لمورّد دون أي دليل على تسليم بضاعة.",
                    "Regular payments to a supplier with no deliveries recorded", "That the lamps did not work", "That Theo was leaving the company", "A mistake in her own salary"),
                cq("How did Daniel respond when Mira mentioned it?", "ابتسم بسرعة زائدة وطلب منها ألا تقلق وأن تركز على أرقامها.",
                    "He told her to ignore it and focus on her own work.", "He thanked her and investigated.", "He reported it to the police.", "He fired her."),
                cq("What does the last sentence suggest about Mira?", "ترى أن الطمأنة المبالغ فيها علامة على وجود مشكلة.",
                    "She became more suspicious, not less.", "She decided to forget the matter.", "She trusted Daniel completely.", "She wanted a promotion."),
            ),
            cch(
                "Following the Money",
                cp(
                    "Over the following weeks, Mira spent her evenings doing what she would later describe as the most nerve-racking research of her life. Northgate Components, she learned, was registered in a small office building in Cyprus that housed more than four hundred companies. It had no website, no employees she could find, and only one director: a woman named Helen Marsh.",
                    "It did not take long to confirm that Helen Marsh was Theo’s sister.",
                    "Mira felt slightly sick. If she was right, the company’s celebrated founder had quietly diverted almost a million dollars a year to a family shell company, money that investors believed was being spent on components for lamps. If she was wrong, she was about to accuse one of the most admired entrepreneurs in the country of fraud.",
                    "She weighed her options. She could resign and walk away with a clear conscience but no influence. She could confront Theo, who would almost certainly deny everything and make sure she never worked in finance again. Or she could take the evidence to the board of directors — the people whose job it was to hold him accountable.",
                    "She spent a sleepless night drafting and redrafting an email. By morning it was barely two hundred words long: factual, unemotional and carefully free of accusations. She attached the spreadsheets, read it one last time and pressed send before she could change her mind."
                ),
                mapOf(
                    "nerve-racking" to "مثير للتوتر",
                    "housed" to "ضمّ، احتوى",
                    "diverted" to "حوّل (بشكل غير مشروع)",
                    "shell company" to "شركة وهمية",
                    "fraud" to "احتيال",
                    "a clear conscience" to "ضمير مرتاح",
                    "hold him accountable" to "محاسبته",
                ),
                cq("Who was the director of Northgate?", "هيلين مارش، أخت مؤسس الشركة ثيو.",
                    "Theo’s sister", "Mira’s manager", "An investor from Cyprus", "Theo himself"),
                cq("Why did Mira hesitate before acting?", "لو كانت مخطئة ستتهم رجل أعمال محترماً بالاحتيال.",
                    "She feared being wrong about a respected founder.", "She did not understand the spreadsheets.", "She was planning to join Northgate.", "She was on holiday."),
                cq("How did Mira write her email to the board?", "مختصر وواقعي وخالٍ من الاتهامات.",
                    "Short, factual and without accusations", "Angry and emotional", "Anonymous and threatening", "Long and full of opinions"),
            ),
            cch(
                "The Board Meeting",
                cp(
                    "For four days, nothing happened. Mira went to work, smiled at Daniel and wondered whether her email had been deleted unread. Then, on Friday afternoon, she was asked to join a video call with the chair of the board, a former judge named Amira Osei.",
                    "Osei did not waste time on pleasantries. The board, she explained, had commissioned an independent audit the moment they received Mira’s message. The auditors had confirmed everything — and found two further companies receiving similar payments. Theo Marsh had been asked to step down with immediate effect.",
                    "“I want you to know,” Osei added, “that what you did took considerable courage. Most people in your position would have looked the other way.”",
                    "The news broke the following Monday. Commentators expressed shock; investors expressed fury. Lumen’s value collapsed overnight, and for a while it seemed the company might not survive. Yet the lamps themselves were real, and so were the villages that depended on them. A new management team was appointed, and slowly, painfully, the company began to rebuild its reputation.",
                    "Mira was offered a promotion, which she accepted, and a book deal, which she turned down. When a journalist asked whether she regretted the trouble she had caused, she thought for a moment. “The trouble was already there,” she said. “I just refused to pretend I hadn’t seen it.”"
                ),
                mapOf(
                    "pleasantries" to "عبارات المجاملة",
                    "commissioned" to "كلّف بإجراء",
                    "audit" to "تدقيق مالي",
                    "step down" to "يتنحّى، يستقيل",
                    "looked the other way" to "تجاهلوا الأمر عمداً",
                    "collapsed overnight" to "انهارت بين ليلة وضحاها",
                    "turned down" to "رفضت",
                ),
                cq("What did the independent audit find?", "أكدت كل شيء ووجدت شركتين أخريين تتلقيان دفعات مماثلة.",
                    "Mira was right, and there were even more suspicious payments.", "There was no problem at all.", "Daniel was the real criminal.", "The lamps were fake."),
                cq("What happened to Lumen after the news broke?", "انهارت قيمتها ثم بدأت تستعيد سمعتها ببطء بإدارة جديدة.",
                    "It lost value but slowly recovered under new management.", "It closed down permanently.", "It became more valuable.", "Theo bought it back."),
                cq("What does Mira’s final answer mean?", "المشكلة لم تأتِ منها، بل كانت موجودة وهي رفضت التظاهر بعدم رؤيتها.",
                    "She exposed a problem rather than creating one.", "She regrets causing trouble.", "She wants to write a book.", "She thinks journalists caused the trouble."),
            ),
        ),
    ),
)

// ───────────────────────────── C2 ─────────────────────────────

internal val ReadersC2: List<GradedReader> = listOf(
    GradedReader(
        id = "r-c2-1",
        level = CefrLevel.C2,
        title = "The Cartographer’s Daughter",
        titleAr = "ابنة رسّام الخرائط",
        genreAr = "أدب تاريخي",
        summaryAr = "في القرن التاسع عشر، ترث امرأة شابة ورشة والدها لرسم الخرائط في الإسكندرية، ومعها خريطة لم تكتمل تخفي سراً عائلياً.",
        chapters = listOf(
            cch(
                "An Inheritance of Ink",
                cp(
                    "When Yusuf Adly died in the spring of 1868, he left his daughter Zeinab a workshop in Alexandria, a modest sum of money and a reputation that was, by any reasonable measure, disproportionate to both. Merchants from Marseille to Bombay swore by his charts; captains claimed, only half in jest, that an Adly map could find a harbour in fog.",
                    "Zeinab had grown up among his instruments — the brass dividers, the inks ground from oak galls, the vellum stretched taut as a drum — and had absorbed his craft as other children absorb a mother tongue: without ever quite deciding to. Yet the city’s guild of mapmakers regarded her inheritance with a mixture of condescension and barely concealed appetite. A woman, they implied, might keep the shop tidy until a suitable buyer could be found.",
                    "She let them believe it. It was, she had learned from her father, often wiser to be underestimated than admired.",
                    "On the evening after the funeral, sorting through his papers, she came upon a chart she had never seen. It depicted the coast of the Red Sea with Yusuf’s customary precision, save for one stretch near Suakin, which had been left conspicuously blank. In the margin, in his small, slanting hand, he had written a single line: “Not until she is ready.”"
                ),
                mapOf(
                    "disproportionate to" to "لا يتناسب مع",
                    "swore by" to "يثقون ثقة مطلقة بـ",
                    "in jest" to "على سبيل المزاح",
                    "taut" to "مشدود",
                    "condescension" to "تعالٍ، استعلاء",
                    "underestimated" to "يُستهان به",
                    "conspicuously" to "بشكل لافت",
                ),
                cq("How did sailors regard Yusuf Adly’s maps?", "كانوا يثقون بها كثيراً، حتى قيل إنها تجد الميناء في الضباب.",
                    "As exceptionally reliable", "As beautiful but inaccurate", "As too expensive", "As old-fashioned"),
                cq("Why did Zeinab allow the guild to underestimate her?", "تعلمت من أبيها أن الاستهانة بك أحياناً أذكى من الإعجاب.",
                    "It gave her a strategic advantage.", "She planned to sell the shop.", "She lacked confidence in her skills.", "She did not understand their intentions."),
                cq("What was unusual about the chart she found?", "جزء قرب سواكن ترك فارغاً عمداً مع ملاحظة غامضة.",
                    "A section had been deliberately left blank.", "It was drawn by Zeinab as a child.", "It showed an imaginary island.", "It had been torn in half."),
            ),
            cch(
                "The Blank Coast",
                cp(
                    "It took Zeinab the better part of a year to understand the omission. Her father’s ledgers, kept in a private shorthand she had to decipher line by line, revealed that in 1851 he had sailed south with a surveying expedition financed by a consortium of European merchants. Officially, its purpose was to chart safe anchorages for the steamships that would soon pour through the canal then being dreamed of in Paris.",
                    "Unofficially, as the ledgers made painfully clear, the consortium had wanted something else: the precise location of the wells and seasonal grazing grounds of the Beja tribes who lived along that coast. Such knowledge would allow whoever possessed it to control the caravan routes — and, in time, the people who depended on them.",
                    "Yusuf had surveyed the coast faithfully. He had also, it seemed, been taken in by a Beja family after falling ill with fever, and had spent seven weeks in their care. When he returned to Alexandria, he submitted his charts to the consortium with a single, deliberate error: the wells were placed forty miles from where they truly lay. The real survey he kept, unfinished, in a drawer for seventeen years.",
                    "Zeinab read the final entry several times. Her father, whom she had always thought of as a man of almost pedantic exactness, had chosen — at considerable risk to his livelihood — to be wrong. The blank coast was not a lapse of craftsmanship. It was an act of conscience rendered in the only language he fully trusted: ink."
                ),
                mapOf(
                    "the better part of" to "معظم",
                    "omission" to "الحذف، الإغفال",
                    "decipher" to "يفك رموز",
                    "consortium" to "تحالف شركات",
                    "anchorages" to "مراسٍ للسفن",
                    "taken in by" to "استضافته (عائلة)",
                    "pedantic" to "متشدد في التفاصيل",
                    "lapse" to "زلّة، هفوة",
                ),
                cq("What did the consortium secretly want?", "مواقع آبار ومراعي قبائل البجا للسيطرة على طرق القوافل.",
                    "To control the routes by locating the tribes’ wells", "To build a new harbour", "To find gold in the desert", "To chart the canal in Paris"),
                cq("Why did Yusuf deliberately falsify the map?", "عاش مع عائلة من البجا اعتنت به، فحمى الآبار بنقلها على الخريطة.",
                    "To protect the people who had cared for him", "Because he was paid more", "Because he was ill and confused", "To embarrass his rivals"),
                cq("How does Zeinab reinterpret the blank coast?", "لم يكن تقصيراً مهنياً بل موقفاً أخلاقياً.",
                    "As a moral choice rather than a professional failure", "As proof her father was careless", "As an unfinished joke", "As a mistake she must correct quickly"),
            ),
            cch(
                "Not Until She Is Ready",
                cp(
                    "The test of her readiness, when it came, arrived in the form of an elegant Englishman named Mr Ashcombe, who appeared at the workshop one autumn morning in 1869, the very week the canal was opened with fireworks and visiting royalty. He represented, he said, certain interests that had long admired her father’s work. They wished to complete a survey of the Red Sea coast, and had reason to believe that Yusuf Adly had left materials of particular value.",
                    "He named a sum that would have bought the workshop three times over.",
                    "Zeinab served him coffee and listened with every appearance of attentiveness. When he had finished, she brought out a chart — not the real survey, which was by then sewn into the lining of her mother’s old travelling chest, but a copy she had spent the summer preparing. It was exquisite, faithful in every respect to the contours of the coast, and wrong in precisely the way her father had been wrong.",
                    "Ashcombe examined it for a long time. “Remarkable,” he said at last. “Your father’s hand, unmistakably.”",
                    "“He taught me everything I know,” Zeinab replied, which was entirely true.",
                    "Years later, when the Adly workshop had become the most respected in the Levant and Zeinab trained apprentices of her own, she would tell them that a map is never merely a picture of the world. It is an argument about what the world should become — and a cartographer must decide, every time she lifts her pen, which argument she is willing to make."
                ),
                mapOf(
                    "with every appearance of" to "متظاهرة تماماً بـ",
                    "attentiveness" to "الإصغاء والاهتمام",
                    "lining" to "البطانة",
                    "exquisite" to "رائع الإتقان",
                    "contours" to "خطوط الشكل، التضاريس",
                    "unmistakably" to "بلا أدنى شك",
                    "apprentices" to "متدربون",
                ),
                cq("What did Mr Ashcombe want from Zeinab?", "أراد مواد أبيها عن ساحل البحر الأحمر مقابل مبلغ كبير.",
                    "Her father’s survey materials of the Red Sea coast", "To marry her", "To buy coffee from Alexandria", "To hire her as a teacher"),
                cq("How did Zeinab respond to his offer?", "أعطته نسخة جميلة لكنها تحمل الخطأ المقصود نفسه.",
                    "She gave him a beautiful but deliberately misleading copy.", "She sold him the real survey.", "She refused to meet him.", "She reported him to the guild."),
                cq("Why is “He taught me everything I know” an ironic answer?", "هي صادقة حرفياً، لكنها تعني أنه علّمها أيضاً فن الخطأ المقصود.",
                    "It is literally true but hides that he also taught her to deceive.", "It is a complete lie.", "She is criticising her father.", "She wants Ashcombe to hire her father."),
            ),
        ),
    ),
    GradedReader(
        id = "r-c2-2",
        level = CefrLevel.C2,
        title = "A Theory of Small Kindnesses",
        titleAr = "نظرية في اللطف الصغير",
        genreAr = "أدب نفسي",
        summaryAr = "عالم اقتصاد مشهور يؤمن بأن الناس يتصرفون دائماً بدافع المصلحة، حتى تضعه سلسلة من المصادفات الصغيرة أمام اختبار لا تفسّره نظرياته.",
        chapters = listOf(
            cch(
                "The Rational Man",
                cp(
                    "Professor Edmund Hale had built a distinguished career on a single, unfashionable premise: that every human act, however altruistic it might appear, could ultimately be traced to self-interest. The man who donated to charity purchased a flattering image of himself; the stranger who returned a lost wallet was insuring himself against guilt. Kindness, in Hale’s lectures, was simply a transaction whose currency happened to be invisible.",
                    "His students found him exhilarating and faintly chilling in roughly equal measure. His colleagues found him insufferable, though they conceded, with some reluctance, that his models predicted behaviour with irritating accuracy.",
                    "Hale himself lived accordingly. He was scrupulously polite, because rudeness was inefficient, and scrupulously solitary, because other people were expensive. He had not been married, had no pets, and regarded his houseplants as an experiment in delayed gratification that he was, on balance, losing.",
                    "So it was with a certain academic detachment that he observed what happened on the morning his car broke down on a country road forty miles from the university, in a downpour of almost biblical proportions, with his phone — through a lapse he would later find impossible to model — entirely out of battery."
                ),
                mapOf(
                    "premise" to "فرضية أساسية",
                    "altruistic" to "إيثاري",
                    "self-interest" to "المصلحة الذاتية",
                    "exhilarating" to "مثير ومنعش للعقل",
                    "insufferable" to "لا يُطاق",
                    "scrupulously" to "بدقة وحرص شديدين",
                    "detachment" to "انفصال وحياد عاطفي",
                    "downpour" to "مطر غزير",
                ),
                cq("What is Professor Hale’s central belief?", "كل فعل بشري، مهما بدا إيثارياً، دافعه المصلحة الذاتية.",
                    "All human behaviour is driven by self-interest.", "People are naturally kind.", "Charity should be banned.", "Economics cannot predict behaviour."),
                cq("Why was Hale polite but solitary?", "التهذيب فعّال، والناس مكلفون بحسب منطقه.",
                    "He applied his cost-benefit logic to his own life.", "He was shy as a child.", "His doctor advised it.", "He disliked his students."),
                cq("What is the tone of the description of his houseplants?", "سخرية خفيفة: يعامل النباتات كتجربة يخسرها.",
                    "Gently ironic", "Tragic", "Angry", "Romantic"),
            ),
            cch(
                "Strangers in the Rain",
                cp(
                    "Within ten minutes, an elderly farmer in a mud-spattered truck had stopped, towed the car to his barn, and refused, with an obstinacy Hale found almost offensive, to accept any payment. His wife fed the professor soup and lent him a cardigan of startling ugliness. Their teenage granddaughter, home from school with a cold, spent an hour on the phone tracking down a mechanic willing to come out on a Sunday.",
                    "Hale spent the afternoon attempting, with diminishing confidence, to account for their behaviour. The farmer might anticipate future reciprocity — but Hale lived forty miles away and would almost certainly never return. The wife might be seeking social approval — but there was no one present to approve. The granddaughter might simply be bored, a hypothesis he was obliged to abandon when she cheerfully admitted that she had an exam the next day and ought to be revising.",
                    "When he finally asked, as tactfully as he could manage, why they had gone to so much trouble, the farmer looked at him with genuine puzzlement. “You were wet,” he said, as though that settled the matter.",
                    "For a man who had spent thirty years explaining the world, it was a disconcerting experience to be handed an explanation so complete and so entirely useless. Driving home that night, in a cardigan he had somehow failed to return, Hale found himself turning the farmer’s three words over and over, like a stone whose shape he could feel but not describe."
                ),
                mapOf(
                    "mud-spattered" to "ملطّخة بالطين",
                    "obstinacy" to "عناد",
                    "with diminishing confidence" to "بثقة متناقصة",
                    "reciprocity" to "المعاملة بالمثل",
                    "hypothesis" to "فرضية",
                    "tactfully" to "بلباقة",
                    "settled the matter" to "حسم المسألة",
                    "disconcerting" to "مُربِك، مُقلق",
                ),
                cq("Why couldn’t Hale explain the family’s kindness with his theory?", "لم يكن هناك مقابل متوقع ولا جمهور يمدحهم، والفتاة كان لديها امتحان.",
                    "None of his usual self-interest explanations fitted.", "They asked for too much money.", "They were secretly his students.", "They wanted to buy his car."),
                cq("What does the farmer’s answer “You were wet” show?", "اللطف عنده رد فعل طبيعي بسيط لا يحتاج إلى حساب.",
                    "His kindness was a simple, natural response.", "He was making fun of Hale.", "He wanted Hale to leave quickly.", "He did not understand English."),
                cq("What does the simile of the stone suggest?", "شيء يشعر به هيل بوضوح لكنه لا يستطيع صياغته بنظرياته.",
                    "Hale senses something real that he cannot put into words.", "Hale wants to throw something away.", "The farmer gave Hale a stone.", "Hale is physically injured."),
            ),
            cch(
                "Revisions",
                cp(
                    "Hale’s next lecture series began, as always, with his celebrated proof that altruism was an illusion. He delivered it with his customary precision, and his students took notes with their customary unease. Then, to the evident bewilderment of the front row, he paused.",
                    "“I should add,” he said, “that this model has recently encountered a data point it cannot accommodate.” He described the farmer, the soup and — after a moment’s hesitation — the cardigan. “It is possible,” he went on, choosing his words with unusual care, “that what I have been calling self-interest is too narrow a concept. That some people experience another person’s discomfort not as information, but as something approaching their own. If so, helping is not a transaction. It is closer to relief.”",
                    "The lecture hall was very quiet. One student, a young woman who had argued with him relentlessly all the previous year, raised her hand. “So you were wrong?”",
                    "Hale considered the question with the seriousness it deserved. “I was incomplete,” he said. “Which, in my field, is the most interesting thing one can be.”",
                    "That spring, he drove forty miles on a Sunday to return a hideous cardigan, accompanied by a box of chocolates he had chosen with a care quite out of proportion to its cost. He told himself it was an experiment in reciprocity. The farmer’s wife, accepting it at the door, seemed to know better — and, for once, Edmund Hale did not feel the need to correct her."
                ),
                mapOf(
                    "bewilderment" to "حيرة شديدة",
                    "accommodate" to "يستوعب",
                    "approaching" to "يقترب من",
                    "relentlessly" to "بلا هوادة",
                    "incomplete" to "ناقص، غير مكتمل",
                    "hideous" to "قبيح جداً",
                    "out of proportion to" to "لا يتناسب مع",
                ),
                cq("How does Hale revise his theory in the lecture?", "اقترح أن بعض الناس يشعرون بانزعاج الآخر كأنه انزعاجهم، فالمساعدة راحة لا صفقة.",
                    "Helping may be a response to shared discomfort, not a trade.", "He abandons economics completely.", "He says the farmer was lying.", "He repeats his old theory unchanged."),
                cq("What does Hale mean by “I was incomplete”?", "نظريته لم تكن خاطئة تماماً لكنها تحتاج إلى توسيع، وهذا مثير علمياً.",
                    "His theory needs expanding, which he finds intellectually exciting.", "He did not finish his lecture.", "He admits he is a bad teacher.", "He never completed his degree."),
                cq("What does the ending imply about Hale?", "تغيّر قليلاً، وصار يقبل أن فعله نابع من مشاعر لا من حساب.",
                    "He has changed and quietly accepts his own kindness.", "He still believes everything is a transaction.", "He wants to buy the farm.", "He is angry with the farmer’s wife."),
            ),
        ),
    ),
)
