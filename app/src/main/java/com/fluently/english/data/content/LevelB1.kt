package com.fluently.english.data.content

val LevelB1 = level(
    CefrLevel.B1,
    exam = listOf(
        q("I ___ to Japan twice.", "have been", "went", "have gone", "was"),
        q("She has worked here ___ 2018.", "since", "for", "from", "ago"),
        q("If it ___ tomorrow, we'll stay at home.", "rains", "will rain", "rained", "is raining"),
        q("You ___ wear a seatbelt. It's the law.", "must", "might", "could", "would"),
        q("You don't ___ come if you're busy.", "have to", "must", "should", "need"),
        q("I ___ TV when the phone rang.", "was watching", "watched", "am watching", "have watched"),
        q("When I was a child, I ___ play in the street every day.", "used to", "use to", "was used to", "am used to"),
        q("Could you ___ the music down? I'm trying to sleep.", "turn", "put", "take", "give"),
        q("We need to ___ plastic bags to protect the environment.", "reduce", "increase", "grow", "rise"),
        q("I've had a terrible headache ___ this morning.", "since", "for", "during", "ago"),
        order("Have you ever eaten Japanese food", "هل أكلت الطعام الياباني من قبل؟"),
        listen("I've lived in this flat for six years, but we're moving next month because we need more space.", "Why is the speaker moving?", "They need more space.", "The flat is too expensive.", "They lost their jobs.", "They don't like the area."),
        listen("If you want to lose weight, you should eat less sugar and walk at least thirty minutes a day.", "What advice does the speaker give?", "Eat less sugar and walk daily", "Stop eating completely", "Go to the gym twice a week", "Drink more coffee"),
        type("Write the past participle of «write».", "written"),
    ),
) {
    unit("Life experiences", "تجارب الحياة") {
        grammar(
            "Present perfect", "المضارع التام",
            notes = listOf(
                "التركيب: have / has + التصريف الثالث للفعل (past participle): I have visited, she has eaten.",
                "نستخدمه للتجارب في الحياة دون ذكر وقت محدد: Have you ever been to London? — I've never tried sushi.",
                "ولحدث ماضٍ له نتيجة في الحاضر: I've lost my keys (ولا زلت لا أجدها). وللأحداث القريبة مع just / already / yet.",
                "for + مدة (for three years) وsince + نقطة بداية (since 2015) لحدث بدأ في الماضي ومستمر حتى الآن.",
                "مهم: إذا ذكرنا وقتاً محدداً منتهياً (yesterday, in 2010) نستخدم الماضي البسيط وليس المضارع التام.",
            ),
            examples = listOf(
                "I have never been to Africa." means "لم أذهب إلى أفريقيا أبداً.",
                "She has just finished her homework." means "انتهت للتو من واجبها.",
                "We've lived here since 2015." means "نسكن هنا منذ 2015.",
                "Have you finished yet? — Not yet." means "هل انتهيت؟ — ليس بعد.",
            ),
            questions = listOf(
                q("Have you ever ___ a camel?", "ridden", "rode", "ride", "riding"),
                q("I ___ my homework. Can I go out now?", "have finished", "finished", "has finished", "am finishing"),
                q("They have been married ___ ten years.", "for", "since", "ago", "during"),
                q("I ___ him yesterday at the station.", "saw", "have seen", "has seen", "see", explain = "yesterday وقت محدد منتهٍ، لذلك الماضي البسيط."),
                q("She hasn't called me ___.", "yet", "already", "just", "ever"),
                q("He has ___ left. You missed him by a minute!", "just", "yet", "ever", "since"),
                q("How long ___ you known each other?", "have", "did", "do", "are"),
                order("I have never seen snow", "لم أرَ الثلج أبداً"),
                type("Complete with «be»: Have you ever ___ to Paris?", "been"),
            ),
        )
        vocabulary(
            "Feelings & experiences", "المشاعر والتجارب",
            listOf(
                w("proud", "فخور", "My parents are proud of me."),
                w("embarrassed", "محرج", "I was so embarrassed when I fell."),
                w("excited", "متحمس", "We're excited about the trip."),
                w("disappointed", "خائب الأمل", "She was disappointed with her results."),
                w("achievement", "إنجاز", "Graduating was my biggest achievement."),
                w("challenge", "تحدٍّ", "Learning a language is a big challenge."),
                w("memorable", "لا يُنسى", "It was a memorable experience."),
                w("nervous", "متوتر", "I always get nervous before exams."),
            ),
        )
        reading(
            "The year I lived abroad", "السنة التي عشت فيها بالخارج",
            passage = "Two years ago, I moved to Germany to do a master's degree. It was the biggest challenge of my life. At first, everything was difficult: I didn't speak German, I didn't know anyone, and the winter was much colder than I had expected. I felt lonely and sometimes I wanted to go home. But slowly things got better. I joined a language exchange group, where I met people from all over the world. Since then, I've made many close friends, I've learned to cook, and I've become much more independent. I've also travelled to eight countries in Europe. Looking back, I'm really proud of myself. Living abroad has changed the way I see the world.",
            questions = listOf(
                q("Why did the writer move to Germany?", "To study", "To work", "To get married", "To learn to cook"),
                q("Which was NOT a difficulty at first?", "Finding a job", "The language", "The cold weather", "Loneliness"),
                q("How did the writer meet new people?", "Through a language exchange group", "At work", "At university parties", "Online"),
                q("How many European countries has the writer visited?", "Eight", "Two", "Ten", "Four"),
                q("What is the main idea of the text?", "Living abroad was hard but changed the writer positively.", "Germany is a cold country.", "Studying a master's degree is easy.", "The writer wants to go home."),
            ),
        )
    }

    unit("Our planet", "كوكبنا") {
        grammar(
            "Zero & first conditionals", "الحالة الشرطية الصفرية والأولى",
            notes = listOf(
                "الشرطية الصفرية للحقائق العامة والعلمية: If + مضارع بسيط، مضارع بسيط: If you heat ice, it melts.",
                "الشرطية الأولى لاحتمال حقيقي في المستقبل: If + مضارع بسيط، will + فعل: If it rains, I'll take an umbrella.",
                "تنبيه: لا نستخدم will بعد if مباشرة: If I will see him ✗ → If I see him ✓.",
                "unless تعني if not: Unless we act now, the problem will get worse.",
                "يمكن تقديم أي من الجملتين؛ إذا بدأنا بـ if نضع فاصلة.",
            ),
            examples = listOf(
                "If you mix red and blue, you get purple." means "إذا خلطت الأحمر والأزرق تحصل على البنفسجي.",
                "If we recycle more, we'll reduce waste." means "إذا أعدنا التدوير أكثر فسنقلل النفايات.",
                "I won't go unless you come with me." means "لن أذهب ما لم تأتِ معي.",
                "What will you do if you miss the bus?" means "ماذا ستفعل إذا فاتتك الحافلة؟",
            ),
            questions = listOf(
                q("If you ___ water to 100°C, it boils.", "heat", "will heat", "heated", "heating"),
                q("If I ___ time tomorrow, I'll help you.", "have", "will have", "had", "having"),
                q("If she studies hard, she ___ the exam.", "will pass", "passes", "passed", "would pass"),
                q("___ you hurry, you'll miss the train.", "Unless", "If", "When", "Because"),
                q("Plants die if they ___ get water.", "don't", "won't", "didn't", "aren't"),
                q("What ___ you do if they cancel the flight?", "will", "do", "did", "are"),
                q("Choose the correct sentence.", "If it's sunny, we'll go to the beach.", "If it will be sunny, we go to the beach.", "If it's sunny, we went to the beach.", "If it will be sunny, we'll go to the beach."),
                order("If we cut down forests animals will lose their homes", "إذا قطعنا الغابات ستفقد الحيوانات موائلها"),
                type("Complete: If you don't sleep, you ___ feel tired. (فعل مساعد)", "will", "'ll"),
            ),
        )
        vocabulary(
            "The environment", "البيئة",
            listOf(
                w("pollution", "تلوث", "Air pollution is a serious problem in big cities."),
                w("recycle", "يعيد التدوير", "We recycle paper and glass."),
                w("climate change", "تغير المناخ", "Climate change affects everyone."),
                w("waste", "نفايات / يهدر", "Don't waste water."),
                w("renewable energy", "طاقة متجددة", "Solar power is a renewable energy source."),
                w("protect", "يحمي", "We must protect wild animals."),
                w("flood", "فيضان", "The flood destroyed many homes."),
                w("drought", "جفاف", "The drought lasted for three years."),
            ),
        )
        listening(
            "A greener school", "مدرسة أكثر خضرة",
            script = "Good morning everyone. As head of the environment club, I want to tell you about our new plan. Every year our school throws away about two tonnes of plastic. If we don't change our habits, this number will grow. So, starting next Monday, the café will stop selling water in plastic bottles. Instead, there will be free water fountains on every floor, so please bring your own bottle. We're also going to put recycling bins in every classroom. And if your class recycles the most this term, you'll win a trip to the national park. Thank you!",
            questions = listOf(
                q("Who is speaking?", "The head of the environment club", "The school director", "A teacher", "A café worker"),
                q("How much plastic does the school throw away each year?", "About two tonnes", "About twenty tonnes", "Two hundred kilos", "Two bottles per student"),
                q("What will change at the café?", "It will stop selling plastic water bottles.", "It will close.", "It will sell cheaper food.", "It will open on Mondays."),
                q("What should students bring?", "Their own bottle", "Plastic bags", "Money for the trip", "Recycling bins"),
                q("What can the best recycling class win?", "A trip to a national park", "Free lunch", "New water fountains", "A day off school"),
            ),
        )
    }

    unit("Health & advice", "الصحة والنصيحة") {
        grammar(
            "Modal verbs: should, must, have to, might", "الأفعال الناقصة",
            notes = listOf(
                "should / shouldn't للنصيحة: You should drink more water.",
                "must للإلزام القوي أو قاعدة يفرضها المتحدث، وmustn't للمنع: You mustn't smoke here.",
                "have to لإلزام خارجي (قانون، عمل): I have to wear a uniform. أما don't have to فتعني «ليس ضرورياً» وليست منعاً: You don't have to come.",
                "might / may / could للاحتمال: It might rain later. — She may be at home.",
                "بعد كل الأفعال الناقصة يأتي الفعل في المصدر بدون to (ما عدا have to).",
            ),
            examples = listOf(
                "You should see a doctor." means "يجب أن (أنصحك أن) ترى طبيباً.",
                "Passengers must show their tickets." means "على الركاب إظهار تذاكرهم.",
                "You don't have to pay. It's free." means "لا داعي أن تدفع. إنه مجاني.",
                "I might go to the gym later." means "ربما أذهب إلى النادي لاحقاً.",
            ),
            questions = listOf(
                q("You look tired. You ___ go to bed early.", "should", "must to", "might", "have"),
                q("You ___ use your phone during the exam. It's forbidden.", "mustn't", "don't have to", "shouldn't to", "might not"),
                q("Tomorrow is a holiday, so I ___ get up early.", "don't have to", "mustn't", "can't", "shouldn't"),
                q("Take an umbrella. It ___ rain.", "might", "must", "should", "has to"),
                q("Doctors ___ work at night sometimes.", "have to", "has to", "must to", "should to"),
                q("You ___ eat so much sugar. It's bad for you.", "shouldn't", "don't have to", "mightn't", "haven't to"),
                q("Choose the correct sentence.", "She has to wear a uniform at work.", "She must to wear a uniform at work.", "She have to wear a uniform at work.", "She should wears a uniform at work."),
                order("You should drink more water", "يجب أن تشرب المزيد من الماء"),
                type("Complete: You ___ smoke in the hospital. (ممنوع — اختصار)", "mustn't", "must not"),
            ),
        )
        vocabulary(
            "Health & body", "الصحة والجسم",
            listOf(
                w("headache", "صداع", "I've got a terrible headache."),
                w("fever", "حُمّى", "The child has a high fever."),
                w("prescription", "وصفة طبية", "The doctor gave me a prescription."),
                w("injury", "إصابة", "He had a knee injury during the match."),
                w("recover", "يتعافى", "She recovered quickly after the operation."),
                w("healthy", "صحي", "Try to eat a healthy diet."),
                w("symptom", "عَرَض", "A cough is a common symptom of a cold."),
                w("appointment", "موعد", "I made an appointment with the dentist."),
            ),
        )
        reading(
            "Sleep: the forgotten medicine", "النوم: الدواء المنسي",
            passage = "Most adults need between seven and nine hours of sleep a night, but studies show that one in three people don't get enough. Lack of sleep doesn't just make you tired. Over time, it can weaken your immune system, increase your risk of heart disease, and affect your memory and mood. So what should you do? Experts say you should go to bed and wake up at the same time every day, even at weekends. You shouldn't drink coffee after 2 p.m., and you should avoid screens for an hour before bed because blue light tells your brain it is still daytime. Your bedroom should be dark, quiet and cool. And if you can't sleep after twenty minutes, you should get up and do something relaxing until you feel sleepy.",
            questions = listOf(
                q("How many adults don't get enough sleep?", "One in three", "Most adults", "Seven out of nine", "Very few"),
                q("Which is NOT mentioned as an effect of poor sleep?", "Weight loss", "A weaker immune system", "Memory problems", "Heart disease"),
                q("Why should you avoid screens before bed?", "Blue light makes the brain think it's day.", "They are too loud.", "They make your eyes red.", "They use a lot of energy."),
                q("What should you do if you can't sleep after 20 minutes?", "Get up and do something relaxing", "Drink coffee", "Look at your phone", "Stay in bed and wait"),
                q("The word «avoid» means…", "stay away from", "enjoy", "look for", "turn on"),
            ),
        )
    }

    unit("Stories from the past", "قصص من الماضي") {
        grammar(
            "Past continuous & used to", "الماضي المستمر و used to",
            notes = listOf(
                "الماضي المستمر (was / were + ing) لحدث كان مستمراً في لحظة معينة في الماضي: At 8 p.m. I was having dinner.",
                "نستخدمه مع الماضي البسيط عندما يقطع حدثٌ قصير حدثاً طويلاً: I was walking home when it started to rain. وغالباً نستخدم while مع المستمر وwhen مع البسيط.",
                "used to + فعل: لعادات أو حالات في الماضي لم تعد موجودة: I used to live in Cairo (لم أعد أسكن هناك).",
                "النفي والسؤال: didn't use to / Did you use to…? (بدون d).",
            ),
            examples = listOf(
                "I was sleeping when you called." means "كنت نائماً عندما اتصلت.",
                "While she was cooking, the lights went out." means "بينما كانت تطبخ انقطعت الكهرباء.",
                "He used to be very shy." means "كان خجولاً جداً (في الماضي).",
                "Did you use to play outside as a child?" means "هل كنت تلعب في الخارج عندما كنت طفلاً؟",
            ),
            questions = listOf(
                q("I ___ a shower when the phone rang.", "was having", "had", "have", "am having"),
                q("While they were playing, it ___ to snow.", "started", "was starting", "starts", "start"),
                q("What ___ you doing at 10 last night?", "were", "did", "was", "are"),
                q("I ___ live in a village, but now I live in the city.", "used to", "use to", "was used", "using to"),
                q("Did you ___ have long hair?", "use to", "used to", "using to", "uses to"),
                q("She ___ when the teacher came in.", "was talking", "talked", "talks", "has talked"),
                q("There ___ be a cinema here, but they closed it.", "used to", "uses to", "was used to", "is used to"),
                order("We were watching a film when the lights went out", "كنا نشاهد فيلماً عندما انقطعت الكهرباء"),
                type("Complete with «drive»: He was ___ too fast when the police stopped him.", "driving"),
            ),
        )
        vocabulary(
            "Common phrasal verbs", "الأفعال المركبة الشائعة",
            listOf(
                w("give up", "يستسلم / يقلع عن", "Don't give up! You can do it."),
                w("look after", "يعتني بـ", "She looks after her little brother."),
                w("find out", "يكتشف / يعرف", "I found out the truth yesterday."),
                w("turn down", "يرفض / يخفض الصوت", "He turned down the job offer."),
                w("get on with", "ينسجم مع", "I get on well with my colleagues."),
                w("set off", "ينطلق (في رحلة)", "We set off early in the morning."),
                w("run out of", "ينفد منه", "We've run out of milk."),
                w("put off", "يؤجل", "Never put off until tomorrow what you can do today."),
            ),
        )
        listening(
            "A strange night", "ليلة غريبة",
            script = "This happened when I was a university student. I used to work at a hotel at night to pay for my studies. One night in winter, at about three in the morning, I was reading a book at the reception desk when I heard a noise. Someone was knocking on the front door. It was snowing heavily outside. I opened the door and saw an old man. He was wearing a summer shirt and he was shaking with cold. He said his car had broken down. I gave him a hot drink and a room for the night. In the morning, he left before I woke up, but he left a note and a gold coin on the desk. The note said: 'Kindness always comes back.' I still have that coin today.",
            questions = listOf(
                q("Why did the speaker work at the hotel?", "To pay for university", "To meet people", "To learn about hotels", "Because he liked the night"),
                q("What was the speaker doing when he heard the noise?", "Reading a book", "Sleeping", "Watching TV", "Cleaning"),
                q("What was strange about the old man?", "He was wearing a summer shirt in winter.", "He had no car.", "He was very young.", "He didn't speak."),
                q("What did the old man leave?", "A note and a gold coin", "His car keys", "Money for the room", "A book"),
                q("What is the message of the story?", "Being kind is rewarded.", "Hotels are dangerous at night.", "Always check your car.", "Gold is valuable."),
            ),
        )
    }
}
