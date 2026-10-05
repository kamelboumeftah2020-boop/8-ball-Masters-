package com.fluently.english.data.content

val LevelB2 = level(
    CefrLevel.B2,
    exam = listOf(
        q("The new bridge ___ next year.", "will be built", "will build", "is building", "builds"),
        q("If I ___ more money, I would travel the world.", "had", "have", "would have", "will have"),
        q("If she had left earlier, she ___ the train.", "wouldn't have missed", "won't miss", "didn't miss", "wouldn't miss"),
        q("He told me that he ___ tired.", "was", "is", "be", "has"),
        q("She asked me where I ___.", "lived", "do live", "did I live", "am living"),
        q("The woman ___ car was stolen called the police.", "whose", "who", "which", "that's"),
        q("I ___ for you for over an hour! Where have you been?", "have been waiting", "am waiting", "waited", "was waiting"),
        q("I wish I ___ speak Chinese.", "could", "can", "will", "would can"),
        q("Can you ___ me a favour?", "do", "make", "give", "take"),
        q("The company is looking to ___ its profits this year.", "boost", "raise up", "rise", "grow up"),
        order("The report was written by the manager", "كُتب التقرير من قِبل المدير"),
        listen("Had I known about the traffic, I would have taken the metro instead of driving.", "What does the speaker regret?", "Driving instead of taking the metro", "Taking the metro", "Being late for the metro", "Not buying a car"),
        listen("The meeting, which was supposed to start at nine, has been postponed until Thursday afternoon.", "When will the meeting take place?", "Thursday afternoon", "Today at nine", "Thursday at nine", "It has been cancelled."),
        type("Rewrite in reported speech: «I am happy» → She said she ___ happy.", "was"),
    ),
) {
    unit("The digital world", "العالم الرقمي") {
        grammar(
            "The passive voice", "المبني للمجهول",
            notes = listOf(
                "نستخدم المبني للمجهول عندما يكون الحدث أو الشيء أهم من الفاعل، أو عندما يكون الفاعل مجهولاً: My bike was stolen.",
                "التركيب: be (بالزمن المناسب) + التصريف الثالث: is made / was built / has been sold / will be opened / is being repaired.",
                "لذكر الفاعل نستخدم by: The book was written by Naguib Mahfouz.",
                "الأفعال الناقصة: modal + be + V3: It must be done today. — The rules can't be changed.",
                "يُستخدم كثيراً في الأخبار والكتابة العلمية والرسمية.",
            ),
            examples = listOf(
                "English is spoken all over the world." means "تُتحدث الإنجليزية في كل أنحاء العالم.",
                "The first iPhone was released in 2007." means "صدر أول آيفون عام 2007.",
                "Your order has been shipped." means "تم شحن طلبك.",
                "The road is being repaired at the moment." means "يتم إصلاح الطريق حالياً.",
            ),
            questions = listOf(
                q("Millions of emails ___ every day.", "are sent", "send", "are sending", "is sent"),
                q("The Pyramids ___ thousands of years ago.", "were built", "built", "are built", "have built"),
                q("Your car ___ at the moment. It'll be ready at 5.", "is being repaired", "is repairing", "repairs", "has repaired"),
                q("The results ___ next week.", "will be announced", "will announce", "are announcing", "announce"),
                q("This film ___ by Christopher Nolan.", "was directed", "directed", "was directing", "is directing"),
                q("The documents must ___ before Friday.", "be signed", "signed", "sign", "being signed"),
                q("Choose the passive form of «They have cancelled the concert».", "The concert has been cancelled.", "The concert was cancelling.", "The concert has cancelled.", "The concert is cancelled by they."),
                order("The data is stored in the cloud", "يتم تخزين البيانات في السحابة"),
                type("Complete: The window ___ broken yesterday. (فعل مساعد)", "was"),
            ),
        )
        vocabulary(
            "Technology", "التكنولوجيا",
            listOf(
                w("device", "جهاز", "Please turn off all electronic devices."),
                w("update", "تحديث / يحدّث", "You need to update the app."),
                w("download", "يُنزّل / تحميل", "I downloaded the file yesterday."),
                w("artificial intelligence", "الذكاء الاصطناعي", "Artificial intelligence is changing many industries."),
                w("privacy", "الخصوصية", "Social media raises serious privacy concerns."),
                w("cutting-edge", "أحدث ما توصلت إليه التقنية", "The lab uses cutting-edge equipment."),
                w("breakthrough", "اختراق / إنجاز علمي كبير", "Scientists have made a major breakthrough."),
                w("obsolete", "عفا عليه الزمن", "Fax machines are almost obsolete."),
            ),
        )
        reading(
            "Are we addicted to our phones?", "هل نحن مدمنون على هواتفنا؟",
            passage = "It is estimated that the average person checks their phone more than ninety times a day. Smartphones were designed to make our lives easier, and in many ways they have: maps, banking and communication are all available at the touch of a screen. However, a growing number of psychologists warn that many apps are deliberately designed to be addictive. Features such as endless scrolling and push notifications are built to keep users engaged for as long as possible, because more attention means more advertising revenue. Research has linked heavy phone use to poor sleep, shorter attention spans and anxiety, especially among teenagers. Some people have responded by taking a 'digital detox' — a period of time without devices. Others use settings that limit screen time. Experts suggest that the goal should not be to give up technology entirely, but to use it more consciously.",
            questions = listOf(
                q("According to the text, why are many apps designed to be addictive?", "To earn more from advertising", "To help people sleep", "To improve attention spans", "To protect teenagers"),
                q("Which feature is mentioned as addictive?", "Endless scrolling", "Online banking", "Digital maps", "Phone calls"),
                q("Heavy phone use has been linked to all of these EXCEPT…", "better memory", "poor sleep", "anxiety", "shorter attention spans"),
                q("What is a «digital detox»?", "A period without devices", "A new phone app", "A type of screen", "An advertising method"),
                q("What do experts recommend?", "Using technology more consciously", "Giving up phones completely", "Buying newer phones", "Using more notifications"),
            ),
        )
    }

    unit("What if…?", "ماذا لو…؟") {
        grammar(
            "Second & third conditionals; wish", "الحالة الشرطية الثانية والثالثة والتمني",
            notes = listOf(
                "الشرطية الثانية لموقف خيالي أو غير محتمل في الحاضر/المستقبل: If + ماضٍ بسيط، would + فعل: If I won the lottery, I would buy a house. ومع to be نستخدم were للجميع غالباً: If I were you, I'd accept.",
                "الشرطية الثالثة لموقف خيالي في الماضي (ندم أو تخيّل نتيجة مختلفة): If + had + V3، would have + V3: If I had studied, I would have passed.",
                "wish + ماضٍ بسيط للتمني في الحاضر: I wish I had a car. وwish + had + V3 للندم على الماضي: I wish I hadn't said that.",
                "if only تشبه wish لكنها أقوى عاطفياً: If only I had more time!",
            ),
            examples = listOf(
                "If I were rich, I would travel the world." means "لو كنت غنياً لسافرت حول العالم.",
                "If you had told me, I would have helped you." means "لو أخبرتني لكنت ساعدتك.",
                "I wish I lived closer to the sea." means "أتمنى لو كنت أسكن أقرب إلى البحر.",
                "She wishes she hadn't sold her house." means "تتمنى لو أنها لم تبع بيتها.",
            ),
            questions = listOf(
                q("If I ___ you, I would apologise.", "were", "am", "will be", "had"),
                q("If we had left earlier, we ___ the flight.", "wouldn't have missed", "wouldn't miss", "didn't miss", "won't miss"),
                q("What would you do if you ___ a million dollars?", "found", "find", "will find", "had found"),
                q("I wish I ___ play the piano.", "could", "can", "will", "would"),
                q("If she ___ harder, she would have got the job.", "had tried", "tried", "would try", "has tried"),
                q("I wish I ___ eaten so much. I feel sick.", "hadn't", "didn't", "haven't", "wouldn't"),
                q("Which sentence talks about the past?", "If I had seen him, I would have said hello.", "If I saw him, I would say hello.", "If I see him, I'll say hello.", "When I see him, I say hello."),
                order("If I had more time I would learn Spanish", "لو كان لدي وقت أكثر لتعلمت الإسبانية"),
                type("Complete: If it ___ rained, we would have played outside. (نفي)", "hadn't", "had not"),
            ),
        )
        vocabulary(
            "Collocations: make & do", "المتلازمات اللفظية: make و do",
            listOf(
                w("make a decision", "يتخذ قراراً", "It's time to make a decision."),
                w("make progress", "يحرز تقدماً", "You're making great progress in English."),
                w("make a mistake", "يرتكب خطأً", "Everyone makes mistakes."),
                w("do research", "يجري بحثاً", "She's doing research on climate change."),
                w("do your best", "يبذل قصارى جهده", "Just do your best in the exam."),
                w("do business", "يمارس الأعمال التجارية", "It's a pleasure to do business with you."),
                w("make an effort", "يبذل جهداً", "He made an effort to be polite."),
                w("do someone a favour", "يسدي معروفاً", "Could you do me a favour?"),
            ),
        )
        listening(
            "Regrets", "الندم",
            script = "Interviewer: Looking back on your career, do you have any regrets? Musician: Of course. When I was nineteen, a big record company offered me a contract, and I turned it down because I wanted to finish university. If I had accepted, I would probably have become famous much earlier. But honestly, I don't wish I'd done things differently. If I hadn't studied, I wouldn't have met my wife, and I wouldn't have learned so much about music theory. My only real regret is that I didn't spend more time with my father before he passed away. I wish I had called him more often.",
            questions = listOf(
                q("What did the musician do when he was nineteen?", "He refused a record contract.", "He signed a record contract.", "He left university.", "He became famous."),
                q("Why did he make that decision?", "He wanted to finish university.", "The money wasn't good.", "His father told him to.", "He wanted to get married."),
                q("Does he regret that decision?", "No, not really.", "Yes, very much.", "He doesn't say."),
                q("What would NOT have happened if he hadn't studied?", "He wouldn't have met his wife.", "He wouldn't have become a musician.", "He wouldn't have had a father.", "He wouldn't have got a contract."),
                q("What is his real regret?", "Not spending more time with his father", "Not becoming famous earlier", "Studying music theory", "Getting married young"),
            ),
        )
    }

    unit("In the news", "في الأخبار") {
        grammar(
            "Reported speech", "الكلام المنقول",
            notes = listOf(
                "عند نقل كلام شخص بعد فعل ماضٍ (said, told) نرجع الزمن خطوة للوراء: am → was، present simple → past simple، will → would، can → could، past simple / present perfect → past perfect.",
                "said بدون مفعول (He said that…) وtold مع مفعول (He told me that…).",
                "الأسئلة المنقولة تأخذ ترتيب الجملة الخبرية بدون do / did: «Where do you live?» → She asked me where I lived. وأسئلة نعم/لا نستخدم if / whether: He asked if I was ready.",
                "الأوامر: tell / ask + someone + to + فعل: «Sit down» → He told me to sit down.",
                "تتغير الظروف أيضاً: today → that day، tomorrow → the next day، here → there.",
            ),
            examples = listOf(
                "\"I'm tired.\" → She said she was tired." means "قالت إنها متعبة.",
                "\"We will win.\" → They said they would win." means "قالوا إنهم سيفوزون.",
                "\"Did you see it?\" → He asked if I had seen it." means "سألني إن كنت قد رأيته.",
                "\"Don't be late.\" → She told us not to be late." means "طلبت منا ألا نتأخر.",
            ),
            questions = listOf(
                q("«I love this song.» → He said he ___ that song.", "loved", "loves", "has loved", "is loving"),
                q("«I will call you.» → She said she ___ call me.", "would", "will", "can", "is going"),
                q("«Where is the station?» → He asked me where the station ___.", "was", "is it", "was it", "did be"),
                q("«Are you hungry?» → She asked ___ I was hungry.", "if", "that", "what", "do"),
                q("She ___ me that she was leaving.", "told", "said", "asked", "spoke"),
                q("«Close the door.» → He told me ___ the door.", "to close", "close", "closing", "that I close"),
                q("«I saw him yesterday.» → She said she had seen him ___.", "the day before", "yesterday", "tomorrow", "the next day"),
                order("She asked me where I worked", "سألتني أين أعمل"),
                type("«I can swim.» → He said he ___ swim.", "could"),
            ),
        )
        vocabulary(
            "Media & news", "الإعلام والأخبار",
            listOf(
                w("headline", "عنوان رئيسي", "The story made headlines around the world."),
                w("journalist", "صحفي", "The journalist interviewed the president."),
                w("reliable", "موثوق", "Always check if your source is reliable."),
                w("biased", "متحيز", "The article was clearly biased."),
                w("fake news", "أخبار كاذبة", "Fake news spreads quickly online."),
                w("broadcast", "يبث / بث", "The match will be broadcast live."),
                w("coverage", "تغطية إعلامية", "The election received wide coverage."),
                w("claim", "يزعم / ادعاء", "The company claims its product is safe."),
            ),
        )
        reading(
            "How fake news spreads", "كيف تنتشر الأخبار الكاذبة",
            passage = "A study carried out by researchers at the Massachusetts Institute of Technology found that false stories spread significantly faster and further on social media than true ones. The researchers analysed around 126,000 stories shared by about three million people. They reported that false news was 70% more likely to be shared than the truth. Interestingly, the main cause was not automated 'bots', but humans. The researchers explained that false stories tend to be more surprising and emotional, which makes people more eager to share them. One of the authors said that people who share new information are often seen as being 'in the know'. To reduce the problem, experts advise readers to check the source, look for the same story on reliable news websites, and pause before sharing — especially when a headline makes them feel angry or shocked.",
            questions = listOf(
                q("What did the study find?", "False stories spread faster than true ones.", "True stories spread faster.", "Bots share most stories.", "People rarely share news."),
                q("How much more likely was false news to be shared?", "70%", "17%", "126%", "3 times"),
                q("Who was mainly responsible for spreading false news?", "Humans", "Bots", "Journalists", "Researchers"),
                q("Why do people share false stories, according to the text?", "They are surprising and emotional.", "They are longer.", "They are from reliable websites.", "They are paid to share them."),
                q("Which advice is NOT given in the text?", "Delete your social media accounts", "Check the source", "Pause before sharing", "Look for the story on reliable websites"),
            ),
        )
    }

    unit("Careers", "المسيرة المهنية") {
        grammar(
            "Relative clauses & present perfect continuous", "جمل الصلة والمضارع التام المستمر",
            notes = listOf(
                "ضمائر الصلة: who للأشخاص، which للأشياء، that للاثنين (في الجمل المحددة)، whose للملكية، where للمكان، when للزمن.",
                "الجملة المحددة (defining) ضرورية للمعنى ولا تأخذ فواصل: The man who called you is my boss. أما غير المحددة (non-defining) فتضيف معلومة إضافية بين فاصلتين ولا نستخدم فيها that: My brother, who lives in Paris, is a chef.",
                "المضارع التام المستمر (have / has been + ing) لنشاط بدأ في الماضي ومستمر حتى الآن مع التركيز على المدة: I've been working here for five years. أو لنشاط حديث له أثر ظاهر: You look tired. — I've been running.",
                "الأفعال الحالية (know, like, believe, own) لا تُستخدم عادة بصيغة المستمر: I've known him for years (وليس I've been knowing).",
            ),
            examples = listOf(
                "That's the company where I did my internship." means "تلك هي الشركة التي تدربت فيها.",
                "She's the colleague whose idea won the award." means "إنها الزميلة التي فازت فكرتها بالجائزة.",
                "Dubai, which hosted Expo 2020, is a global hub." means "دبي، التي استضافت إكسبو 2020، مركز عالمي.",
                "I've been learning English for two years." means "أتعلم الإنجليزية منذ سنتين.",
            ),
            questions = listOf(
                q("The person ___ interviewed me was very professional.", "who", "which", "whose", "where"),
                q("This is the office ___ I spent ten years of my life.", "where", "which", "who", "when"),
                q("The manager, ___ name I forget, was very helpful.", "whose", "who", "which", "that"),
                q("My laptop, ___ I bought last year, has stopped working.", "which", "that", "who", "what", explain = "جملة غير محددة بين فاصلتين، لا نستخدم that."),
                q("She ___ for a new job since January.", "has been looking", "is looking", "looks", "was looking"),
                q("Your hands are dirty. What ___?", "have you been doing", "did you do", "are you doing", "do you do"),
                q("I ___ my best friend since primary school.", "have known", "have been knowing", "know", "am knowing"),
                order("The candidate who got the job has three years of experience", "المرشح الذي حصل على الوظيفة لديه ثلاث سنوات خبرة"),
                type("Complete: How long have you been ___ (wait) here?", "waiting"),
            ),
        )
        vocabulary(
            "Work & career", "العمل والمسيرة المهنية",
            listOf(
                w("promotion", "ترقية", "She got a promotion after two years."),
                w("resign", "يستقيل", "He resigned from his job last month."),
                w("deadline", "موعد نهائي", "We must meet the deadline."),
                w("colleague", "زميل عمل", "My colleagues are very supportive."),
                w("negotiate", "يتفاوض", "She negotiated a higher salary."),
                w("skills", "مهارات", "Communication skills are essential."),
                w("workload", "عبء العمل", "My workload has doubled this month."),
                w("CV", "سيرة ذاتية", "Send your CV to the HR department."),
            ),
        )
        listening(
            "A job interview", "مقابلة عمل",
            script = "Interviewer: Thank you for coming, Ms Haddad. Could you tell us a little about yourself? Candidate: Certainly. I graduated in computer science four years ago, and since then I've been working as a software developer at a start-up in Beirut, where I lead a small team of three people. Interviewer: Why do you want to leave your current job? Candidate: I've learned a lot there, but I'm looking for a company where I can work on larger projects and develop my management skills. Interviewer: What would you say is your main weakness? Candidate: I sometimes find it hard to say no to extra work, which can increase my workload. But I've been using a planning app that helps me prioritise. Interviewer: Excellent. Do you have any questions for us? Candidate: Yes — what opportunities are there for training?",
            questions = listOf(
                q("What does the candidate do now?", "She's a software developer.", "She's a university student.", "She's a manager at a big company.", "She's unemployed."),
                q("How many people does she lead?", "Three", "Four", "Thirteen", "Nobody"),
                q("Why does she want to change jobs?", "To work on bigger projects and develop management skills", "Because the salary is low", "Because she doesn't like her team", "Because she wants to move to Beirut"),
                q("What is her weakness?", "She finds it hard to refuse extra work.", "She is often late.", "She can't work in a team.", "She doesn't like computers."),
                q("What does she ask at the end?", "About training opportunities", "About the salary", "About holidays", "About the office location"),
            ),
        )
    }

    unit("Crime & society", "الجريمة والمجتمع") {
        grammar(
            "Future continuous & future perfect", "المستقبل المستمر والمستقبل التام",
            notes = listOf(
                "المستقبل المستمر (will be + ing) لحدث سيكون جارياً في لحظة معينة في المستقبل: This time tomorrow, I'll be flying to Paris.",
                "ويُستخدم أيضاً لسؤال مهذب عن خطط شخص: Will you be using the car tonight?",
                "المستقبل التام (will have + V3) لحدث سيكون قد انتهى قبل وقت معين في المستقبل: By 2030, I'll have finished my PhD.",
                "كلمات مفتاحية: this time next week / at 8 p.m. tomorrow (للمستمر)، by + وقت / by the time (للتام).",
            ),
            examples = listOf(
                "At 10 a.m. tomorrow, I'll be taking my exam." means "في العاشرة صباح الغد سأكون أؤدي امتحاني.",
                "By the end of the year, we'll have saved enough money." means "بنهاية العام سنكون قد ادخرنا مالاً كافياً.",
                "Don't call at 9 — I'll be sleeping." means "لا تتصل في التاسعة — سأكون نائماً.",
                "She'll have left by the time you arrive." means "ستكون قد غادرت عندما تصل.",
            ),
            questions = listOf(
                q("This time next week, we ___ on the beach.", "will be lying", "will have lain", "lie", "are lain"),
                q("By 2030, scientists ___ a cure.", "will have found", "will be finding", "find", "found"),
                q("Don't phone at 8 — I ___ dinner.", "will be having", "will have had", "have", "had"),
                q("By the time you get home, I ___ the report.", "will have finished", "will be finishing", "finish", "finished"),
                q("___ you be using the laptop this evening?", "Will", "Are", "Have", "Do"),
                q("In ten years, she ___ here for two decades.", "will have worked", "will be work", "works", "is working"),
                order("By next summer I will have graduated", "بحلول الصيف القادم سأكون قد تخرجت"),
                type("Complete: At midnight, I'll be ___ (sleep).", "sleeping"),
            ),
        )
        vocabulary(
            "Crime & law", "الجريمة والقانون",
            listOf(
                w("witness", "شاهد", "The witness saw the thief's face."),
                w("suspect", "مشتبه به", "The police arrested a suspect."),
                w("evidence", "دليل / أدلة", "There isn't enough evidence."),
                w("arrest", "يعتقل", "The police arrested two men."),
                w("guilty", "مذنب", "The jury found him guilty."),
                w("sentence", "حكم (قضائي)", "He received a five-year sentence."),
                w("burglary", "سطو (على منزل)", "There was a burglary in our street."),
                w("prevent", "يمنع", "Street lights can help prevent crime."),
            ),
        )
        listening(
            "A police appeal", "نداء من الشرطة",
            script = "Police are appealing for witnesses after a burglary at a jewellery shop on King Street late last night. At around 11:30 p.m., two men broke into the shop through a back window and stole watches worth over fifty thousand pounds. One suspect is described as tall, in his thirties, wearing a black jacket. The other was shorter and was seen leaving in a small white van. No one was injured. Detective Sarah Moore said, 'We believe someone will have seen the van in the area. Any information, however small, could be important evidence.' Anyone with information should call 0800 555 0199.",
            questions = listOf(
                q("What kind of shop was burgled?", "A jewellery shop", "A clothes shop", "A bank", "A supermarket"),
                q("How did the men get into the shop?", "Through a back window", "Through the front door", "Through the roof", "With a key"),
                q("What was stolen?", "Watches", "Cash", "Phones", "Paintings"),
                q("How did the second suspect leave?", "In a small white van", "On a motorbike", "On foot", "In a black car"),
                q("Why does the detective think people can help?", "Someone probably saw the van.", "The thieves were caught on camera.", "The shop owner knows the thieves.", "There was a big reward."),
            ),
        )
    }
}
