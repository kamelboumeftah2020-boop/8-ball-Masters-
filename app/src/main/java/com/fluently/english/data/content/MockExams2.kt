package com.fluently.english.data.content

/** Second practice test of each kind (same formats as MockExams.kt, new content). */
internal val MockExamsSet2: List<MockExam> = listOf(

    // ======================== IELTS ACADEMIC ========================
    MockExam(
        id = "mock-ielts-2",
        kind = MockKind.IELTS,
        title = "IELTS Academic — Practice Test 2",
        titleAr = "اختبار IELTS الأكاديمي التجريبي 2",
        level = CefrLevel.B2,
        descriptionAr = "اختبار تجريبي ثانٍ بنفس أقسام IELTS الأكاديمي: الاستماع (جولة سياحية مع مخطط المكان ونقاش بين طالبين)، القراءة (TRUE / FALSE / NOT GIVEN وإكمال الجمل)، الكتابة Task 2 من نوع «الأسباب والحلول» والمحادثة بأجزائها الثلاثة. النتيجة تقدير لدرجة Band من 9.",
        realFormatAr = "الاختبار الحقيقي: استماع 30 دقيقة (40 سؤالاً)، قراءة 60 دقيقة (40 سؤالاً)، كتابة 60 دقيقة (مهمتان)، محادثة 11–14 دقيقة. هذه نسخة مختصرة بنفس الأنماط.",
        sections = listOf(
            MockSection(
                SectionType.LISTENING, "Listening", 12,
                "ستسمع تسجيلين. في الاختبار الحقيقي تسمع كل تسجيل مرة واحدة فقط — اقرأ الأسئلة قبل الاستماع وحاول أن تلتزم بذلك.",
                parts = listOf(
                    MockPart(
                        "Part 2 — Guided tour (plan labelling)",
                        "استمع إلى مرشدة سياحية تشرح مخطط مصنع قديم. اكتب كلمة واحدة و/أو رقماً (NO MORE THAN ONE WORD AND/OR A NUMBER) أو اختر الإجابة الصحيحة (A, B أو C). انتبه لكلمات المكان: next to, behind, opposite, on the left.",
                        text = "Good morning, everyone, and welcome to Thornbury Mill. My name's Grace and I'll be your guide this morning. The mill was built in 1824, and for almost a hundred and fifty years it produced cotton cloth that was sold all over the world. Let me give you a quick idea of the layout. We're standing in the entrance hall, and the ticket office is just behind me. If you look at your plan, the main weaving hall is straight ahead, through the large double doors. The gift shop is on the right of the weaving hall, and the café is right next to the gift shop, so you can have a drink when you've finished shopping. The famous water wheel is behind the main building, beside the river — you can reach it by following the path from the café. I'm afraid the engine room, on the left of the entrance hall, is closed today for repairs, so we won't be able to go in there. The highlight of the tour is the weaving demonstration, when our volunteers operate one of the original machines. That starts at half past eleven in the weaving hall, so please make sure you're there in good time.",
                        questions = listOf(
                            Question.Typing("Year the mill was built: ________", listOf("1824", "eighteen twenty-four", "eighteen twenty four"), explanation = "«The mill was built in 1824»."),
                            Question.Choice("On the plan, where is the café?", listOf("Next to the gift shop", "Opposite the ticket office", "On the left of the entrance hall"), explanation = "«the café is right next to the gift shop»."),
                            Question.Choice("How can visitors reach the water wheel?", listOf("By following the path from the café", "By going through the engine room", "By walking through the ticket office"), explanation = "«you can reach it by following the path from the café»."),
                            Question.Choice("Which part of the mill is closed today?", listOf("The engine room", "The weaving hall", "The gift shop"), explanation = "«the engine room … is closed today for repairs»."),
                            Question.Typing("Time of the weaving demonstration: ________", listOf("11.30", "11:30", "half past eleven", "eleven thirty", "11.30 am", "11:30 am", "11.30am", "11:30am"), explanation = "«That starts at half past eleven» = 11:30."),
                        ),
                    ),
                    MockPart(
                        "Part 3 — Students discussing an assignment",
                        "استمع إلى طالبين جامعيين يخططان لعرض تقديمي، واختر الإجابة الصحيحة (A, B أو C). في هذا الجزء انتبه لمن يقترح الفكرة ومن يوافق عليها.",
                        text = "Lina: Hi Mark. Have you had a chance to think about our presentation for the environmental studies module? Mark: Yes. I know we talked about air quality, but I think noise pollution would be more original — nobody else in the group has chosen it. Lina: Good point. Let's go with noise pollution. Now, how should we collect data? I suggested a questionnaire last week. Mark: I'm not sure. When we did a questionnaire last term, hardly anyone replied — we only got twelve responses. Lina: True. So what's the alternative? Mark: I could measure noise levels myself using an app on my phone. I'd record at three locations: the bus station, the park and the main shopping street. Lina: That sounds much more reliable. And I'll do the background research. Dr Patel said she wants us to use sources published in the last five years, not old textbooks. Mark: Right. And she said the presentation should last fifteen minutes, including questions. Lina: OK. When shall we meet to put it all together? Mark: How about Thursday afternoon in the library? Lina: Thursday afternoon's fine. See you then.",
                        questions = listOf(
                            Question.Choice("Why does Mark prefer noise pollution as a topic?", listOf("Nobody else in the group has chosen it", "It is easier to research than air quality", "Their tutor suggested it")),
                            Question.Choice("Why do they decide not to use a questionnaire?", listOf("Very few people responded last time", "It would take too long to design", "Their tutor does not allow questionnaires")),
                            Question.Choice("How will Mark collect data?", listOf("By measuring noise at three locations with a phone app", "By interviewing people at the bus station", "By reading old textbooks in the library")),
                            Question.Choice("What does Dr Patel require?", listOf("Sources published in the last five years", "A presentation lasting at least thirty minutes", "Data from five different locations")),
                            Question.Choice("When will the students next meet?", listOf("On Thursday afternoon", "On Thursday morning", "On Friday afternoon")),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.READING, "Reading", 25,
                "اقرأ النصين وأجب عن الأسئلة. لا تعتمد على معلوماتك الخاصة — الإجابة يجب أن تكون من النص فقط.",
                parts = listOf(
                    MockPart(
                        "Passage 1 — The Long Road to the Modern Bicycle",
                        "Questions 1–5: هل العبارات تتفق مع النص؟ TRUE = تتفق، FALSE = تتعارض مع النص، NOT GIVEN = النص لا يذكر هذه المعلومة. Questions 6–8: اختر الإجابة الصحيحة.",
                        text = "The bicycle is so familiar today that it is easy to forget how recently it was invented. Its first recognisable ancestor appeared in 1817, when the German inventor Karl Drais demonstrated his 'running machine' in Mannheim. It had two wheels in line and a steerable front wheel, but no pedals: riders pushed themselves along with their feet. Some historians believe Drais was motivated by a shortage of horses following a failed harvest, although this theory remains disputed.\n\nPedals arrived in the 1860s, when French makers attached them directly to the front wheel. These machines, nicknamed 'boneshakers' because of their iron-rimmed wooden wheels, were extremely uncomfortable on cobbled streets. To travel further with each turn of the pedals, manufacturers enlarged the front wheel, producing the high-wheeler, or 'penny-farthing', of the 1870s. Its front wheel could exceed 1.5 metres in diameter. Although fast, it was dangerous: a rider who hit a stone could be thrown forward over the handlebars, and it was used mainly by wealthy young men.\n\nThe real turning point came in 1885 with John Kemp Starley's Rover 'safety bicycle'. With two wheels of similar size and a chain driving the rear wheel, it was far easier to mount and control. Three years later, John Boyd Dunlop developed a practical air-filled tyre, which made riding dramatically smoother. Within a decade, cycling had become a craze across Europe and North America.\n\nThe social effects were significant. For the first time, ordinary workers could travel several kilometres to a job without paying for a horse or a train ticket. The bicycle also played a part in the changing position of women. Cycling required lighter, less restrictive clothing, and the American campaigner Susan B. Anthony famously remarked that the bicycle had done more to emancipate women than anything else in the world.\n\nIn the twentieth century, the car pushed the bicycle to the margins in many countries. Yet in recent years, concern about congestion, pollution and public health has led cities such as Copenhagen and Paris to invest heavily in cycle lanes, suggesting that this two-hundred-year-old machine may have an important future as well as a remarkable past.",
                        questions = listOf(
                            tfng("Drais's running machine had no pedals.", "TRUE", "«but no pedals: riders pushed themselves along with their feet»."),
                            tfng("Historians agree that a shortage of horses led Drais to build his machine.", "FALSE", "«this theory remains disputed» = المؤرخون غير متفقين."),
                            tfng("Boneshakers were more expensive than running machines.", "NOT GIVEN", "النص لا يقارن بين أسعار الدراجتين."),
                            tfng("The penny-farthing was popular with people from all social classes.", "FALSE", "«it was used mainly by wealthy young men» = الأغنياء فقط تقريباً."),
                            tfng("The safety bicycle was introduced before the air-filled tyre.", "TRUE", "الدراجة الآمنة 1885، والإطار الهوائي بعدها بثلاث سنوات (1888)."),
                            Question.Choice("Why did manufacturers make the front wheel larger?", listOf("To travel further with each turn of the pedals", "To make the bicycle safer to ride", "To make it easier to get on the bicycle", "To reduce the cost of production"), explanation = "«To travel further with each turn of the pedals, manufacturers enlarged the front wheel»."),
                            Question.Choice("According to the passage, the bicycle helped ordinary workers by…", listOf("allowing them to reach jobs without paying for transport", "giving them new jobs in bicycle factories", "replacing trains in most cities", "making horses cheaper to buy"), explanation = "«ordinary workers could travel several kilometres to a job without paying for a horse or a train ticket»."),
                            Question.Choice("What does the writer suggest in the final paragraph?", listOf("The bicycle may become important again", "Cars will soon disappear from cities", "Cycle lanes have failed in Paris", "The bicycle has no future"), explanation = "«may have an important future as well as a remarkable past»."),
                        ),
                    ),
                    MockPart(
                        "Passage 2 — The Language of the Honeybee",
                        "Questions 9–12: أكمل الجمل بكلمة واحدة فقط من النص (ONE WORD ONLY). Question 13: اختر الإجابة الصحيحة.",
                        text = "When a honeybee discovers a rich source of nectar, it does not keep the information to itself. Returning to the hive, it performs a series of movements that tell its sisters exactly where the flowers are. The meaning of this behaviour was decoded by the Austrian scientist Karl von Frisch, whose work earned him a share of the Nobel Prize in 1973.\n\nVon Frisch identified two main types of movement. If food is close to the hive — generally within about 50 metres — the forager performs a 'round dance', circling first one way and then the other. This tells other bees that food is nearby but gives no information about direction. For more distant sources, the bee performs the 'waggle dance'. It runs in a straight line while shaking its body from side to side, then loops back to the starting point and repeats the run.\n\nThe waggle dance contains remarkably precise information. The angle of the straight run, measured from the vertical on the honeycomb, matches the angle between the food source and the sun. A run pointing straight up, for example, means 'fly towards the sun'. Meanwhile, the duration of the waggle indicates distance: the longer the run lasts, the farther away the food is. Because the sun moves across the sky during the day, dancers gradually adjust their angle to compensate — an ability that implies bees possess an internal clock.\n\nNot every scientist accepted these findings at first. In the 1960s, some researchers argued that bees located food mainly by smell, and that the dance was of little importance. The debate was largely settled in 2005, when scientists attached tiny radar transponders to bees and tracked their flights. Recruits that had followed a dance flew directly towards the area it indicated, confirming that they had used the information it contained.\n\nToday, the waggle dance is regarded as one of the most sophisticated examples of communication in the animal kingdom. Researchers are also putting it to practical use: by 'reading' dances filmed inside hives, they can map where colonies are finding food and identify landscapes that offer bees too few flowers.",
                        questions = listOf(
                            Question.Typing("If food is very near the hive, the bee performs a ________ dance.", listOf("round"), explanation = "«the forager performs a 'round dance'»"),
                            Question.Typing("The angle of the straight run shows where the food is in relation to the ________.", listOf("sun", "the sun"), explanation = "«the angle between the food source and the sun»"),
                            Question.Typing("How long the bee waggles tells other bees the ________ to the food.", listOf("distance"), explanation = "«the duration of the waggle indicates distance»"),
                            Question.Typing("In 2005, researchers used ________ to follow the bees' flights.", listOf("radar"), explanation = "«attached tiny radar transponders to bees and tracked their flights»"),
                            Question.Choice("Why does the writer mention that dancers adjust their angle during the day?", listOf("It suggests that bees have an internal clock", "It proves that bees find food by smell", "It shows that the round dance is inaccurate", "It explains why bees dance only in the morning"), explanation = "«an ability that implies bees possess an internal clock»."),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.WRITING, "Writing — Task 2", 40,
                "اكتب مقالاً لا يقل عن 250 كلمة. في الاختبار الحقيقي لديك 40 دقيقة لهذه المهمة.",
                writing = WritingTask(
                    prompt = "In many cities around the world, traffic congestion is becoming a serious problem. What are the main causes of this problem, and what measures could be taken to solve it?",
                    promptAr = "في كثير من مدن العالم أصبح الازدحام المروري مشكلة خطيرة. ما الأسباب الرئيسية لهذه المشكلة، وما الإجراءات التي يمكن اتخاذها لحلها؟",
                    minWords = 250,
                    modelAnswer = "Traffic congestion has become a defining feature of modern city life, costing commuters hours each week and contributing significantly to air pollution. This essay will examine the main causes of the problem and propose several measures that governments, employers and individuals could adopt.\n\nThe most obvious cause is the rapid growth in private car ownership. As incomes rise, more families can afford a vehicle, and many regard the car as a symbol of status and independence rather than simply a means of transport. A second factor is poor urban planning. In many cities, residential areas have expanded far from business districts, forcing people to make long journeys to work, often along a handful of main roads. Finally, public transport in numerous places is unreliable, overcrowded or poorly connected, so residents feel they have little alternative to driving.\n\nTackling these causes requires a combination of investment and regulation. First and foremost, authorities should make public transport faster and more attractive than the car. Dedicated bus lanes, frequent metro services and integrated ticketing have transformed commuting in cities such as Seoul and Madrid. Secondly, congestion charges, which require drivers to pay to enter the city centre at peak times, have proved effective in London and Stockholm, reducing traffic while raising money for transport improvements. In the longer term, planners should design mixed-use neighbourhoods where homes, shops and offices are close together, allowing more people to walk or cycle. Employers can also play a part by offering flexible working hours or the option to work from home on certain days.\n\nIn conclusion, traffic congestion stems mainly from rising car ownership, scattered urban development and inadequate public transport. Although no single solution will be sufficient, a coordinated approach that combines better alternatives with financial incentives offers the most realistic way of keeping our cities moving.",
                    tipsAr = listOf(
                        "هيكل مقال «الأسباب والحلول»: مقدمة (إعادة صياغة المشكلة + ما سيناقشه المقال) ← فقرة للأسباب ← فقرة للحلول ← خاتمة تلخص الاثنين.",
                        "اربط كل حل بسبب ذكرته: ضعف النقل العام ← تحسين النقل العام، كثرة السيارات ← رسوم الازدحام.",
                        "استخدم أدوات الترتيب والسببية: The most obvious cause is…, A second factor is…, First and foremost, Secondly, As a result.",
                        "أعطِ أمثلة محددة (مدن أو تجارب حقيقية) فهي تقوّي الحجة وترفع درجة Task Response.",
                    ),
                ),
            ),
            MockSection(
                SectionType.SPEAKING, "Speaking", 12,
                "سيقرأ عليك الممتحن الأسئلة بصوته. أجب بصوت عالٍ كأنك في الاختبار الحقيقي — حاول أن تتحدث بجمل كاملة وتوسع إجاباتك.",
                speaking = listOf(
                    SpeakingPart(
                        "Part 1 — Introduction",
                        "أسئلة قصيرة عن نفسك. أجب في جملتين أو ثلاث لكل سؤال.",
                        listOf(
                            "Can you describe the neighbourhood where you live?",
                            "What kind of weather do you like best? Why?",
                            "Did you enjoy reading when you were a child?",
                            "How do you usually get to work or to your place of study?",
                        ),
                        sample = "I live in a fairly quiet neighbourhood on the edge of the city. It's mostly residential, but there's a small market and a park nearby, so it's very convenient. The best thing is that the neighbours are friendly — we often chat outside in the evenings.",
                    ),
                    SpeakingPart(
                        "Part 2 — Long turn",
                        "لديك دقيقة للتحضير (يمكنك كتابة ملاحظات)، ثم تحدث لمدة دقيقتين عن الموضوع.",
                        listOf("Describe a journey that you remember well."),
                        cueCard = listOf("where you went", "how you travelled", "who you were with", "and explain why you remember this journey so well"),
                        prepSeconds = 60,
                        talkSeconds = 120,
                        sample = "I'd like to describe a train journey I took from Cairo to Aswan with two of my cousins a few years ago. We travelled overnight on a sleeper train, which was a completely new experience for me. The journey took about twelve hours, and I remember lying in the narrow bed, listening to the rhythm of the wheels and watching the lights of small villages pass by. In the morning, we woke up to the most incredible view of the Nile with palm trees on both sides. I think I remember it so well because it was the first time I'd travelled without my parents, so I felt very independent. It also brought me much closer to my cousins — we still talk about that trip whenever we meet.",
                    ),
                    SpeakingPart(
                        "Part 3 — Discussion",
                        "أسئلة أعمق مرتبطة بموضوع الجزء الثاني. أعطِ رأيك مع أسباب وأمثلة.",
                        listOf(
                            "Why do you think some people prefer to travel by car rather than by public transport?",
                            "How has tourism changed in your country over the last twenty years?",
                            "Do you think people will travel more or less in the future? Why?",
                        ),
                        sample = "I think many people choose the car simply because it gives them more freedom. They can leave whenever they like and go directly to their destination. However, in big cities this advantage is disappearing because of traffic, so I expect more people will switch to public transport if it becomes faster and more reliable.",
                    ),
                ),
            ),
        ),
    ),

    // ======================== CAMBRIDGE B1 PRELIMINARY ========================
    MockExam(
        id = "mock-pet-2",
        kind = MockKind.CAMBRIDGE_B1,
        title = "B1 Preliminary — Practice Test 2",
        titleAr = "اختبار Cambridge B1 Preliminary التجريبي 2",
        level = CefrLevel.B1,
        descriptionAr = "اختبار تجريبي ثانٍ بأنماط امتحان كامبريدج للمستوى B1 (PET): قراءة الإعلانات والرسائل القصيرة، اختيار الكلمة المناسبة في نص، الفراغات المفتوحة، الاستماع، كتابة قصة قصيرة، والمحادثة. النتيجة تقدير على مقياس Cambridge English Scale.",
        realFormatAr = "الاختبار الحقيقي: قراءة 45 دقيقة (6 أجزاء)، كتابة 45 دقيقة (جزءان)، استماع 30 دقيقة (4 أجزاء)، محادثة 12–17 دقيقة.",
        sections = listOf(
            MockSection(
                SectionType.READING, "Reading", 15,
                "ثلاثة أجزاء من قسم القراءة بنفس نمط الامتحان.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Short texts",
                        "اقرأ كل رسالة أو إعلان قصير واختر المعنى الصحيح.",
                        questions = listOf(
                            Question.Choice("LIBRARY SIGN: «Books may be borrowed for two weeks. Late returns: 20p per day.»\nWhat does the sign say?", listOf("You pay if you return a book late.", "You can keep books for as long as you like.", "Borrowing a book costs 20p a day."), explanation = "Late returns = إرجاع الكتاب متأخراً، وعليه غرامة 20 بنساً يومياً."),
                            Question.Choice("TEXT MESSAGE: «Mum, I've missed the 5.15 bus. The next one's at 5.45, so I'll still be home for dinner, just a bit late. — Yara»\nWhy has Yara sent this message?", listOf("To say she will arrive later than planned", "To ask her mother to collect her", "To say she won't be home for dinner"), explanation = "«I'll still be home for dinner, just a bit late» = ستتأخر قليلاً فقط."),
                            Question.Choice("NOTICE on a classroom door: «Art Club: this week's meeting will be in Room 4 instead of the hall.»\nWhat does the notice say?", listOf("The Art Club is meeting in a different place this week.", "This week's Art Club meeting has been cancelled.", "The Art Club will meet in the hall from now on."), explanation = "instead of = بدلاً من — المكان تغيّر هذا الأسبوع فقط."),
                        ),
                    ),
                    MockPart(
                        "Part 5 — Multiple-choice cloze",
                        "اقرأ النص واختر الكلمة المناسبة لكل فراغ.",
                        text = "Helping at the animal shelter\n\nLast summer I decided to (1)___ some voluntary work at an animal shelter near my home. I had always loved animals, but I didn't (2)___ how much work they need. Every morning we fed the dogs and cleaned their cages, and in the afternoon we (3)___ them for long walks. The hardest part was saying goodbye when a family (4)___ to give one of the dogs a new home. However, I felt very proud when the manager said I had been a great (5)___ to the team.",
                        questions = listOf(
                            gap(1, "do", "make", "have", "take", explain = "do voluntary work = يقوم بعمل تطوعي."),
                            gap(2, "realise", "remind", "explain", "admit", explain = "realise = يدرك."),
                            gap(3, "took", "brought", "carried", "led", explain = "take a dog for a walk = يأخذ الكلب في نزهة."),
                            gap(4, "decided", "considered", "suggested", "enjoyed", explain = "decide to + فعل. أما consider و suggest و enjoy فتأتي بعدها -ing."),
                            gap(5, "help", "advice", "use", "favour", explain = "be a great help to someone = يكون عوناً كبيراً."),
                        ),
                    ),
                    MockPart(
                        "Part 6 — Open cloze",
                        "اكتب كلمة واحدة فقط في كل فراغ.",
                        text = "Hi Jack,\nI'm sorry I didn't reply (1)___ your message sooner. I've been really busy (2)___ I started my new course. It's much more interesting (3)___ I expected, and the teacher is great. Are you free (4)___ Saturday? We could go to the new sports centre together.\nBest wishes, Dana",
                        questions = listOf(
                            openGap(1, "to", explain = "reply to a message"),
                            openGap(2, "since", explain = "since + بداية الحدث مع المضارع التام (I've been)."),
                            openGap(3, "than", explain = "more interesting than = أكثر إثارة من (مقارنة)."),
                            openGap(4, "on", "this", "next", explain = "on Saturday / this Saturday"),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.LISTENING, "Listening", 8,
                "ستسمع أربعة مقاطع قصيرة. اختر الإجابة الصحيحة لكل سؤال. في الامتحان الحقيقي تسمع كل مقطع مرتين.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Short extracts",
                        "استمع واختر الإجابة الصحيحة.",
                        questions = listOf(
                            audioQ("Man: Shall we get Lily a book for her birthday? Woman: She's got hundreds already! What about a concert ticket? Man: That's too expensive. Let's get her a board game — she loves those.", "What will they buy for Lily?", "A board game", "A book", "A concert ticket"),
                            audioQ("Here's the weather for tomorrow. It'll be a cloudy start in the morning, but heavy rain will arrive by lunchtime and continue all afternoon. It should be dry again by the evening.", "What will the weather be like tomorrow afternoon?", "Rainy", "Cloudy but dry", "Sunny"),
                            audioQ("Boy: How was the school trip? Girl: The bus journey was long and boring, and the museum was really crowded. But the guide was brilliant — she made everything so interesting.", "What did the girl enjoy about the trip?", "The guide", "The bus journey", "The crowds at the museum"),
                            audioQ("Shop assistant: Can I help you? Woman: Yes, I bought these jeans here yesterday, but they're too small. I'd like to change them for a bigger size, please. I don't want my money back.", "What does the woman want to do?", "Change the jeans for a bigger size", "Get her money back", "Buy a second pair of jeans"),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.WRITING, "Writing — Part 2 (Story)", 20,
                "اكتب قصة من حوالي 100 كلمة تبدأ بالجملة المعطاة. في الامتحان الحقيقي تختار بين قصة ومقال.",
                writing = WritingTask(
                    prompt = "Your English teacher has asked you to write a story.\n\nYour story must begin with this sentence:\n\n«As soon as I opened the door, I knew something was different.»\n\nWrite your story in about 100 words.",
                    promptAr = "اكتب قصة من حوالي 100 كلمة تبدأ بالجملة: «بمجرد أن فتحت الباب، عرفت أن شيئاً ما مختلف.» اجعل للقصة بداية ووسطاً ونهاية واضحة.",
                    minWords = 100,
                    modelAnswer = "As soon as I opened the door, I knew something was different. The living room was completely dark, and there was a strange silence in the house. Normally my little brother would be watching cartoons at full volume, but that evening I couldn't hear a thing.\n\n'Hello? Is anybody home?' I called nervously. Nobody answered.\n\nI reached for the light switch, but before I could touch it, the lights came on and twenty people jumped out from behind the sofa. 'Surprise!' they shouted.\n\nI couldn't believe my eyes. My whole family was there, together with my best friends from school. I had been so busy with exams that I had completely forgotten it was my birthday!\n\nWe spent the evening eating cake, dancing and laughing. It was the best surprise I have ever had.",
                    tipsAr = listOf(
                        "ابدأ بالجملة المعطاة حرفياً — لا تغيّرها — واجعل القصة متصلة بها.",
                        "استخدم أزمنة الماضي: الماضي البسيط للأحداث (opened, jumped)، الماضي المستمر للخلفية، والماضي التام لما حدث قبلها (I had forgotten).",
                        "أضف حواراً قصيراً ومشاعر (nervously, I couldn't believe my eyes) لتصبح القصة حيّة.",
                        "اختم بنهاية واضحة — مفاجأة أو شعور أو درس تعلمته.",
                    ),
                ),
            ),
            MockSection(
                SectionType.SPEAKING, "Speaking", 8,
                "في امتحان كامبريدج تتحدث مع ممتحن ومع طالب آخر. هنا تدرّب على أسئلة الممتحن.",
                speaking = listOf(
                    SpeakingPart(
                        "Part 1 — Interview",
                        "أجب عن أسئلة شخصية قصيرة.",
                        listOf("Where are you from?", "What do you like doing with your friends?", "What's your favourite day of the week? Why?", "Tell us about something you're planning to do next month."),
                        sample = "My favourite day of the week is Friday, because it's the start of the weekend. After work I usually meet my friends for dinner, and I don't have to wake up early the next morning, so I feel really relaxed.",
                    ),
                    SpeakingPart(
                        "Part 3 — Collaborative task",
                        "تخيّل أنك تناقش هذه الأفكار مع طالب آخر. تحدث عن كل خيار واقترح الأفضل مع السبب.",
                        listOf("A town wants to give young people more things to do at weekends. Here are some ideas: a sports centre, a cinema, a music school, a youth café, and a park with a skate area. Talk about each idea and decide which one would be best."),
                        sample = "I think a sports centre would be a great idea, because young people could stay fit and meet new friends there. A cinema is fun, but it's quite expensive to go every weekend. What do you think about the youth café? I'd choose the sports centre, because everyone can use it, whatever their interests.",
                    ),
                    SpeakingPart(
                        "Part 4 — Discussion",
                        "تحدث عن رأيك في الموضوع مع أسباب.",
                        listOf("What do young people in your town usually do at the weekend?", "Is it better to spend free time indoors or outdoors? Why?"),
                        sample = "In my town, most young people go to cafés or shopping centres at the weekend. Personally, I think it's better to spend free time outdoors, because we spend all week sitting in classrooms or offices. Going for a walk or playing football helps me feel more energetic.",
                    ),
                ),
            ),
        ),
    ),

    // ======================== CAMBRIDGE B2 FIRST ========================
    MockExam(
        id = "mock-fce-2",
        kind = MockKind.CAMBRIDGE_B2,
        title = "B2 First — Practice Test 2",
        titleAr = "اختبار Cambridge B2 First التجريبي 2",
        level = CefrLevel.B2,
        descriptionAr = "اختبار تجريبي ثانٍ بأنماط امتحان B2 First (FCE): الأجزاء الأربعة لاستخدام اللغة (الاختيار من متعدد، الفراغات المفتوحة، تكوين الكلمات، إعادة الصياغة بكلمة مفتاحية)، القراءة، الاستماع، كتابة مراجعة (Review) والمحادثة.",
        realFormatAr = "الاختبار الحقيقي: القراءة واستخدام اللغة 75 دقيقة (7 أجزاء)، الكتابة 80 دقيقة، الاستماع 40 دقيقة، المحادثة 14 دقيقة.",
        sections = listOf(
            MockSection(
                SectionType.USE_OF_ENGLISH, "Reading & Use of English", 20,
                "الأجزاء الأربعة لاستخدام اللغة في امتحان B2 First.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Multiple-choice cloze",
                        "اختر الكلمة الأنسب لكل فراغ — انتبه للتلازمات اللفظية وحروف الجر بعد الكلمة.",
                        text = "Citizen science\n\nYou don't need a laboratory to (1)___ a contribution to science. Thousands of volunteers around the world now (2)___ part in research projects, from counting birds in their gardens to classifying galaxies online. Scientists (3)___ on this help because there is simply too much data for professionals to analyse alone. Participants, (4)___ turn, gain a deeper understanding of the natural world. Some projects have even (5)___ to genuine discoveries, such as previously unknown planets.",
                        questions = listOf(
                            gap(1, "make", "do", "give", "have", explain = "make a contribution = يُسهم."),
                            gap(2, "take", "have", "get", "hold", explain = "take part in = يشارك في."),
                            gap(3, "rely", "trust", "insist", "base", explain = "rely on = يعتمد على."),
                            gap(4, "in", "by", "on", "at", explain = "in turn = بدورهم / في المقابل."),
                            gap(5, "led", "resulted", "caused", "brought", explain = "lead to = يؤدي إلى (أما result فتأخذ in)."),
                        ),
                    ),
                    MockPart(
                        "Part 2 — Open cloze",
                        "اكتب كلمة واحدة فقط في كل فراغ.",
                        text = "Why we procrastinate\n\nMost of us have put (1)___ an important task at some point, choosing to tidy our desk or check our phone instead. Psychologists say this has little to do (2)___ laziness. Instead, it is a way of avoiding the unpleasant feelings that a difficult task creates. (3)___ we understand this, we can start to deal with the problem, for example by breaking a large task (4)___ smaller, more manageable steps.",
                        questions = listOf(
                            openGap(1, "off", explain = "put off = يؤجّل."),
                            openGap(2, "with", explain = "have little to do with = لا علاقة كبيرة له بـ."),
                            openGap(3, "once", "if", "when", explain = "Once / When / If we understand this…"),
                            openGap(4, "into", explain = "break something into parts = يقسّم إلى أجزاء."),
                        ),
                    ),
                    MockPart(
                        "Part 3 — Word formation",
                        "استخدم الكلمة المكتوبة بأحرف كبيرة لتكوين كلمة مناسبة للجملة.",
                        questions = listOf(
                            Question.Typing("There has been a significant ________ in the number of tourists this year. (GROW)", listOf("growth"), explanation = "grow → growth (اسم)."),
                            Question.Typing("The instructions were so ________ that nobody understood them. (CONFUSE)", listOf("confusing"), explanation = "confuse → confusing (صفة تصف الشيء المسبب للحيرة)."),
                            Question.Typing("The team's ________ was the result of hard work and a little luck. (SUCCEED)", listOf("success"), explanation = "succeed → success (اسم)."),
                            Question.Typing("It's ________ that he'll arrive on time — he's always late. (LIKELY)", listOf("unlikely"), explanation = "likely → unlikely (عكس)."),
                            Question.Typing("The volunteers worked ________ to finish the project before winter. (TIRE)", listOf("tirelessly"), explanation = "tire → tireless → tirelessly (ظرف بمعنى دون كلل)."),
                        ),
                    ),
                    MockPart(
                        "Part 4 — Key word transformation",
                        "أكمل الجملة الثانية لتعطي نفس معنى الأولى باستخدام الكلمة المعطاة (من كلمتين إلى خمس كلمات). اكتب الكلمات الناقصة فقط.",
                        questions = listOf(
                            Question.Typing("«Shall I carry your bag?» he said to me. (OFFERED)\nHe ________ my bag.", listOf("offered to carry"), explanation = "He offered to carry my bag."),
                            Question.Typing("«Perhaps she missed the train.» (MAY)\nShe ________ the train.", listOf("may have missed"), explanation = "She may have missed the train. (may have + التصريف الثالث للتخمين عن الماضي)"),
                            Question.Typing("«I started playing the piano ten years ago.» (BEEN)\nI ________ the piano for ten years.", listOf("have been playing", "'ve been playing"), explanation = "I have been playing the piano for ten years."),
                            Question.Typing("«They didn't go out because of the rain.» (PREVENTED)\nThe rain ________ out.", listOf("prevented them from going", "prevented them going"), explanation = "The rain prevented them from going out."),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.READING, "Reading — Part 5", 12,
                "اقرأ المقال واختر الإجابة الأنسب لكل سؤال.",
                parts = listOf(
                    MockPart(
                        "Part 5 — Multiple choice",
                        "أسئلة عن الفكرة والتفاصيل ورأي الكاتب ومعنى الكلمات.",
                        text = "For most of my adult life, I was convinced I couldn't sing. A primary school teacher had once asked me to 'just mouth the words' during a concert, and that single comment stayed with me for thirty years. So when my neighbour invited me to join the community choir she ran, my first instinct was to refuse politely.\n\nShe was persistent, though. 'Nobody auditions,' she told me every time we met. 'If you can talk, you can sing.' Eventually I agreed to come once, mainly so that she would stop asking. I stood at the very back, hoping nobody would hear me.\n\nWhat struck me that first evening was not the quality of the singing, which was mixed, but the atmosphere. There were retired teachers, students, a bus driver and a surgeon, all laughing at their own mistakes. Nobody seemed to care whether they hit every note. For two hours, I forgot about work completely.\n\nI won't pretend I improved overnight. For weeks I struggled to read the music and kept losing my place. But gradually my voice grew stronger, and so did my confidence. Six months later, I sang a short solo at our winter concert. My hands were shaking, but I didn't mouth a single word.",
                        questions = listOf(
                            Question.Choice("Why did the writer want to refuse the invitation at first?", listOf("A comment from childhood had made the writer believe they couldn't sing", "The writer was too busy with work", "The writer didn't get on with the neighbour", "The writer had failed an audition"), explanation = "تعليق المعلمة في المدرسة الابتدائية بقي معه ثلاثين عاماً."),
                            Question.Choice("What does 'persistent' mean in paragraph 2?", listOf("She kept asking despite being refused", "She was extremely polite", "She was impatient and rude", "She was a talented singer"), explanation = "«she told me every time we met» = استمرت في الإلحاح."),
                            Question.Choice("Why did the writer finally agree to go?", listOf("To stop the neighbour asking", "To improve their singing quickly", "To meet people from work", "To prepare for a solo"), explanation = "«mainly so that she would stop asking»."),
                            Question.Choice("What impressed the writer most on the first evening?", listOf("The relaxed, friendly atmosphere", "The high quality of the singing", "The famous people in the choir", "The difficulty of the music")),
                            Question.Choice("What does the final sentence suggest?", listOf("The writer had overcome an old fear", "The writer forgot the words of the song", "The writer will never sing alone again", "The writer's teacher was in the audience"), explanation = "عكس ما طلبته المعلمة قديماً — لم يعد يكتفي بتحريك شفتيه."),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.LISTENING, "Listening — Part 1", 8,
                "ستسمع أربعة مقاطع لا علاقة بينها. اختر الإجابة الصحيحة لكل مقطع.",
                parts = listOf(
                    MockPart(
                        "Part 1 — Short extracts",
                        "استمع واختر الإجابة الصحيحة.",
                        questions = listOf(
                            audioQ("I'd expected the cookery course to be mostly theory, but to my surprise we spent almost every session in the kitchen actually cooking. The tutor was strict, but that was what I'd been told to expect, and I learned more in six weeks than in years of watching cooking shows.", "What surprised the speaker about the course?", "How practical it was", "How strict the tutor was", "How short it was"),
                            audioQ("Man: Did you finally buy that laptop? Woman: I was about to, but then I read some reviews saying the battery barely lasts three hours. The screen's great and the price was fine, but I'm on trains a lot, so that's a deal-breaker.", "Why didn't the woman buy the laptop?", "The battery life was too short", "The screen was poor", "It was too expensive"),
                            audioQ("Everyone told me moving abroad would be lonely at first, and they were right. But what I hadn't expected was how much I'd miss simple things — the bread from my local bakery, the sound of my own language in the street.", "What did the speaker find unexpected about moving abroad?", "Missing ordinary everyday things", "Feeling lonely at first", "Having to learn a new language"),
                            audioQ("Woman: Are you coming to the match on Saturday? Man: I'd love to, but my sister's flying in that morning and I promised to pick her up from the airport. Maybe next time.", "Why can't the man go to the match?", "He's collecting his sister from the airport", "He has to work on Saturday", "He isn't interested in football"),
                        ),
                    ),
                ),
            ),
            MockSection(
                SectionType.WRITING, "Writing — Part 2 (Review)", 35,
                "اكتب مراجعة (Review) من 140 إلى 190 كلمة. صِف المكان وقيّمه وانصح القارئ.",
                writing = WritingTask(
                    prompt = "You see this announcement on an English-language website.\n\nREVIEWS WANTED — A place to eat\nHave you recently eaten at a café or restaurant that you would recommend? Write a review describing the food, the atmosphere and the service, and say whether you would recommend it to other people.\n\nWrite your review.",
                    promptAr = "اكتب مراجعة لمقهى أو مطعم أكلت فيه مؤخراً لموقع إلكتروني: صِف الطعام والأجواء والخدمة، وقل هل تنصح الآخرين به.",
                    minWords = 140,
                    modelAnswer = "The Olive Tree, Harbour Street\n\nIf you're looking for somewhere relaxed to enjoy a meal with friends, The Olive Tree is well worth a visit. This small family-run restaurant opened last spring, and it has quickly become one of my favourite places to eat.\n\nThe menu focuses on Mediterranean dishes made with fresh, local ingredients. I strongly recommend the grilled fish with lemon and herbs, which was perfectly cooked, and the homemade bread is simply delicious. Vegetarians are well catered for too, with several tasty options. Prices are reasonable, especially at lunchtime, when there is a set menu.\n\nThe atmosphere is warm and welcoming. The walls are decorated with old photographs of the harbour, and soft music plays in the background, so you can actually have a conversation without shouting.\n\nThe only disappointment was the service, which was rather slow on a busy Saturday evening. However, the staff were friendly and apologised for the wait.\n\nOverall, I would definitely recommend The Olive Tree, particularly for a weekday lunch when it is quieter. Just remember to book in advance!",
                    tipsAr = listOf(
                        "أعطِ المراجعة عنواناً (اسم المكان) وابدأ بجملة تجذب القارئ.",
                        "غطِّ النقاط المطلوبة كلها: الطعام، الأجواء، الخدمة، ثم التوصية في الخاتمة.",
                        "استخدم صفات قوية ومتنوعة: delicious, welcoming, reasonable, well worth a visit — وتجنب تكرار good و nice.",
                        "اذكر إيجابية وسلبية واحدة على الأقل لتكون المراجعة متوازنة ومقنعة (The only disappointment was…).",
                    ),
                ),
            ),
            MockSection(
                SectionType.SPEAKING, "Speaking", 8,
                "تدرّب على أسئلة الممتحن في الجزأين الأول والرابع.",
                speaking = listOf(
                    SpeakingPart(
                        "Part 1 — Interview",
                        "أسئلة عامة عن حياتك واهتماماتك.",
                        listOf("Do you prefer spending time with a big group of friends or just one or two people?", "What kind of food do you enjoy cooking or eating?", "Tell us about a hobby you'd like to try in the future."),
                        sample = "I'd really like to try rock climbing. A friend of mine goes to an indoor climbing centre every week, and she says it's great exercise for both your body and your mind, because you have to plan every move. I'm a little afraid of heights, so it would be a real challenge for me.",
                    ),
                    SpeakingPart(
                        "Part 4 — Discussion",
                        "أسئلة نقاشية — أعطِ رأيك وبرره، واذكر وجهات نظر أخرى.",
                        listOf("Do you think online reviews are a reliable way to choose a restaurant or hotel?", "Some people say that it's important to try new experiences regularly. Do you agree?"),
                        sample = "I think online reviews can be useful, but you have to read them carefully. Some reviews are written by people who had one bad experience, while others might even be fake. So I usually look at the overall pattern rather than a single comment, and I also ask friends for recommendations.",
                    ),
                ),
            ),
        ),
    ),
)
