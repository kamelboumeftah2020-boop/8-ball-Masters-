package com.fluently.english.data.content

val LevelA2 = level(
    CefrLevel.A2,
    exam = listOf(
        q("Last summer we ___ to Turkey.", "went", "go", "goed", "have gone"),
        q("___ you see the match yesterday?", "Did", "Do", "Were", "Have"),
        q("This hotel is ___ than the other one.", "cheaper", "more cheap", "cheapest", "the cheaper"),
        q("It's the ___ building in the city.", "tallest", "taller", "most tall", "more tall"),
        q("Look at the clouds! It ___ rain.", "is going to", "will to", "goes to", "is raining to"),
        q("I'm sorry, I ___ dinner now. Can I call you later?", "am having", "have", "has", "having"),
        q("How ___ sugar do you want?", "much", "many", "lot", "few"),
        q("There aren't ___ eggs in the fridge.", "any", "some", "much", "a"),
        q("A person who flies planes is a…", "pilot", "farmer", "mechanic", "waiter"),
        q("I'd like to ___ on this jacket. Where is the changing room?", "try", "put", "take", "wear"),
        order("She didn't go to work yesterday", "لم تذهب إلى العمل أمس"),
        listen("The train to Manchester leaves at a quarter past ten from platform four.", "When does the train leave?", "10:15", "10:45", "4:10", "10:04"),
        listen("I'll probably stay at home this weekend because I have an exam on Monday.", "Why will the speaker stay at home?", "Because of an exam", "Because it's raining", "Because he is ill", "Because of work"),
        type("Write the past simple of «buy».", "bought"),
    ),
) {
    unit("Past events", "أحداث الماضي") {
        grammar(
            "Past simple", "الماضي البسيط",
            notes = listOf(
                "نستخدم الماضي البسيط لحدث بدأ وانتهى في وقت محدد في الماضي: yesterday, last week, in 2010, two days ago.",
                "الأفعال المنتظمة نضيف لها ed: play → played, visit → visited. والأفعال الشاذة لها صيغ خاصة يجب حفظها: go → went, see → saw, eat → ate, have → had.",
                "النفي: didn't + الفعل في المصدر: I didn't see him (وليس didn't saw).",
                "السؤال: Did + الفاعل + المصدر: Did you enjoy the film?",
                "فعل to be في الماضي: was مع I / he / she / it، وwere مع you / we / they.",
            ),
            examples = listOf(
                "I visited my uncle last weekend." means "زرت عمي في نهاية الأسبوع الماضي.",
                "We went to the beach two days ago." means "ذهبنا إلى الشاطئ قبل يومين.",
                "She didn't call me yesterday." means "لم تتصل بي أمس.",
                "Were you at home last night?" means "هل كنت في البيت ليلة أمس؟",
            ),
            questions = listOf(
                q("I ___ a great film last night.", "saw", "see", "seed", "seen"),
                q("They ___ football yesterday.", "played", "play", "plays", "playing"),
                q("She didn't ___ breakfast this morning.", "have", "had", "has", "having", explain = "بعد didn't يأتي الفعل في المصدر."),
                q("___ you go out last Saturday?", "Did", "Do", "Were", "Was"),
                q("The weather ___ terrible yesterday.", "was", "were", "is", "did"),
                q("We ___ in Paris for a week in 2019.", "were", "was", "are", "did"),
                q("Choose the correct past forms: eat / write / buy", "ate / wrote / bought", "eated / writed / buyed", "ate / writed / bought", "eat / wrote / buyed"),
                order("Where did you go last summer", "أين ذهبت الصيف الماضي؟"),
                type("Write the past simple of «go».", "went"),
            ),
        )
        vocabulary(
            "Travel & holidays", "السفر والعطلات",
            listOf(
                w("trip", "رحلة", "We had a wonderful trip to Egypt."),
                w("passport", "جواز سفر", "Don't forget your passport!"),
                w("ticket", "تذكرة", "I bought a train ticket online."),
                w("luggage", "أمتعة", "My luggage is very heavy."),
                w("abroad", "في الخارج", "She wants to study abroad."),
                w("sightseeing", "مشاهدة المعالم", "We went sightseeing in Rome."),
                w("souvenir", "تذكار", "I bought a souvenir for my mother."),
                w("book", "يحجز", "We booked a room by the sea."),
            ),
        )
        reading(
            "My last holiday", "عطلتي الأخيرة",
            passage = "Last August, my family and I went to Istanbul for ten days. We flew from Amman and the flight took about two hours. Our hotel was small but very clean, and it was close to the old city. On the first day, we visited the Blue Mosque and Hagia Sophia. They were amazing! The next day, we took a boat trip on the Bosphorus. My little brother was a bit scared, but he enjoyed it in the end. The food was delicious — I tried many new dishes. The only problem was the weather: it was very hot. I bought some souvenirs for my friends, and we came home tired but happy.",
            questions = listOf(
                q("How long did the family stay in Istanbul?", "Ten days", "Two days", "Two weeks", "One month"),
                q("How did they travel to Istanbul?", "By plane", "By boat", "By car", "By train"),
                q("What did they do on the second day?", "They took a boat trip.", "They visited a mosque.", "They went shopping.", "They stayed at the hotel."),
                q("What was the problem during the holiday?", "The heat", "The hotel", "The food", "The flight"),
                q("The word «scared» means…", "afraid", "happy", "tired", "hungry"),
            ),
        )
    }

    unit("Comparing things", "المقارنة") {
        grammar(
            "Comparatives & superlatives", "صيغ المقارنة والتفضيل",
            notes = listOf(
                "للمقارنة بين شيئين: الصفات القصيرة نضيف لها er + than: tall → taller than. والصفات الطويلة نضع قبلها more: more expensive than.",
                "للتفضيل (الأفضل بين مجموعة): the + est للقصيرة: the tallest، وthe most للطويلة: the most beautiful.",
                "الصفات المنتهية بـ y تتحول إلى ier / iest: happy → happier → the happiest. وبعض الصفات القصيرة نضاعف حرفها الأخير: big → bigger.",
                "صفات شاذة: good → better → the best / bad → worse → the worst / far → further → the furthest.",
                "للمساواة: as + صفة + as: She is as tall as her mother.",
            ),
            examples = listOf(
                "Cairo is bigger than Alexandria." means "القاهرة أكبر من الإسكندرية.",
                "This phone is more expensive than that one." means "هذا الهاتف أغلى من ذلك.",
                "It was the best day of my life." means "كان أفضل يوم في حياتي.",
                "My car isn't as fast as yours." means "سيارتي ليست بسرعة سيارتك.",
            ),
            questions = listOf(
                q("An elephant is ___ than a horse.", "bigger", "biger", "more big", "biggest"),
                q("This is ___ book I've ever read.", "the most interesting", "the more interesting", "most interesting", "the interestingest"),
                q("Today is ___ than yesterday.", "hotter", "more hot", "hoter", "hottest"),
                q("He is the ___ player in the team.", "best", "goodest", "better", "most good"),
                q("My English is ___ than last year. I practise every day.", "better", "gooder", "best", "more good"),
                q("Tom is ___ tall ___ his brother.", "as / as", "so / than", "more / as", "as / than"),
                q("Russia is ___ country in the world.", "the largest", "the larger", "largest", "the most large"),
                order("Gold is more expensive than silver", "الذهب أغلى من الفضة"),
                type("Write the comparative of «happy».", "happier"),
            ),
        )
        vocabulary(
            "Describing people & places", "وصف الأشخاص والأماكن",
            listOf(
                w("crowded", "مزدحم", "The market is always crowded on Fridays."),
                w("quiet", "هادئ", "I live in a quiet village."),
                w("modern", "حديث / عصري", "Dubai has many modern buildings."),
                w("friendly", "ودود", "The people here are very friendly."),
                w("lazy", "كسول", "My cat is fat and lazy."),
                w("generous", "كريم", "My uncle is very generous."),
                w("dangerous", "خطير", "This road is dangerous at night."),
                w("cheap", "رخيص", "The food in this restaurant is cheap."),
            ),
        )
        listening(
            "Which city?", "أي مدينة؟",
            script = "I lived in two cities: Riyadh and Jeddah. Riyadh is bigger than Jeddah and it has more modern buildings. But for me, Jeddah is more relaxing because it's by the sea. The weather in Riyadh is drier, and in summer it's hotter. In Jeddah it's more humid. People in both cities are very friendly. If you like shopping, Riyadh has the biggest malls. But if you want the best seafood, go to Jeddah!",
            questions = listOf(
                q("Which city is bigger?", "Riyadh", "Jeddah", "They are the same size."),
                q("Why is Jeddah more relaxing for the speaker?", "It's by the sea.", "It's smaller.", "It has big malls.", "It's drier."),
                q("Which city is more humid?", "Jeddah", "Riyadh", "Both"),
                q("Where can you find the biggest malls?", "Riyadh", "Jeddah", "In neither city"),
                q("What does the speaker recommend in Jeddah?", "Seafood", "Shopping", "Modern buildings", "Desert trips"),
            ),
        )
    }

    unit("Future plans", "خطط المستقبل") {
        grammar(
            "be going to & will", "التعبير عن المستقبل",
            notes = listOf(
                "be going to + الفعل: لخطة أو نية قررناها مسبقاً: I'm going to study medicine. ولتوقع مبني على دليل نراه: Look at those clouds! It's going to rain.",
                "will + الفعل: لقرار نتخذه لحظة الكلام: I'm thirsty. — I'll get you some water. ولتوقعات عامة ورأي: I think it will be a great year. وللوعود والعروض: I'll help you.",
                "النفي: won't (will not) و am / is / are not going to.",
                "نستخدم أيضاً المضارع المستمر لترتيبات مؤكدة في المستقبل القريب: I'm meeting Sara tomorrow at 5.",
            ),
            examples = listOf(
                "We're going to buy a new car next month." means "سنشتري سيارة جديدة الشهر القادم (خطة).",
                "The phone is ringing. — I'll answer it." means "الهاتف يرن. — سأجيب أنا (قرار فوري).",
                "I promise I won't be late." means "أعدك ألا أتأخر.",
                "She's flying to Paris on Friday." means "ستسافر إلى باريس يوم الجمعة (ترتيب مؤكد).",
            ),
            questions = listOf(
                q("I've decided. I ___ learn to drive this year.", "am going to", "will to", "going to", "am go to"),
                q("It's cold in here. — OK, I ___ close the window.", "'ll", "'m going to", "am", "going to", explain = "قرار لحظي، لذلك will."),
                q("Be careful! You ___ fall!", "are going to", "will to", "are", "go to"),
                q("I think robots ___ do most jobs in the future.", "will", "are going", "going to", "are"),
                q("Don't worry. I ___ tell anyone your secret.", "won't", "don't", "am not", "willn't"),
                q("What ___ you going to do after university?", "are", "will", "do", "is"),
                order("I am going to visit my grandmother", "سأزور جدتي"),
                type("Complete with will (negative short form): I ___ forget your birthday.", "won't"),
            ),
        )
        vocabulary(
            "Jobs & work", "المهن والعمل",
            listOf(
                w("job", "وظيفة", "She has a new job at a bank."),
                w("salary", "راتب", "The salary is good, but the hours are long."),
                w("boss", "مدير / رئيس", "My boss is very kind."),
                w("office", "مكتب", "I work in an office in the city centre."),
                w("interview", "مقابلة", "I have a job interview tomorrow."),
                w("lawyer", "محامٍ", "Her father is a famous lawyer."),
                w("engineer", "مهندس", "He wants to be an engineer."),
                w("experience", "خبرة", "Do you have any experience in sales?"),
            ),
        )
        reading(
            "Plans for the summer", "خطط الصيف",
            passage = "Hi Jake, thanks for your email! I finished my exams last week and now I'm free. I have big plans for the summer. First, I'm going to work in my uncle's restaurant for a month because I want to save some money. Then, in August, I'm going to travel to Spain with two friends. We're staying in a small apartment in Valencia — we booked it yesterday! I think it will be really hot, but I don't mind. I'm also going to take a short Spanish course before the trip. What about you? Are you going to visit us this summer? Write soon, Adam",
            questions = listOf(
                q("Why is Adam going to work in the restaurant?", "To save money", "To learn cooking", "To help his friends", "Because he failed his exams"),
                q("Who is Adam going to Spain with?", "Two friends", "His uncle", "Jake", "His family"),
                q("Where are they staying?", "In an apartment", "In a hotel", "With a family", "In a tent"),
                q("What is Adam going to do before the trip?", "Take a Spanish course", "Visit Jake", "Book a hotel", "Finish his exams"),
                q("«I don't mind» means…", "It's not a problem for me.", "I don't know.", "I'm very angry.", "I forgot."),
            ),
        )
    }

    unit("Shopping & now", "التسوق واللحظة الحالية") {
        grammar(
            "Present continuous; some / any; much / many", "المضارع المستمر والكميات",
            notes = listOf(
                "المضارع المستمر (am / is / are + ing) لحدث يحدث الآن أو هذه الفترة: I'm reading a great book these days.",
                "الفرق: المضارع البسيط للعادات (I usually wear jeans) والمستمر للآن (Today I'm wearing a suit).",
                "some في الجمل المثبتة والعروض: I have some money. Would you like some tea? وany في النفي والسؤال: Do you have any brothers? I don't have any time.",
                "many مع المعدود (many books)، وmuch مع غير المعدود (much water, much money). وa lot of مع النوعين.",
            ),
            examples = listOf(
                "Shh! The baby is sleeping." means "صه! الطفل نائم.",
                "Are you looking for something?" means "هل تبحث عن شيء؟",
                "How much is this shirt?" means "بكم هذا القميص؟",
                "There isn't any milk left." means "لم يتبقَّ أي حليب.",
            ),
            questions = listOf(
                q("Listen! Someone ___ at the door.", "is knocking", "knocks", "knock", "knocking"),
                q("She usually ___ to work, but today she's taking a taxi.", "walks", "is walking", "walk", "walking"),
                q("Can I have ___ water, please?", "some", "any", "many", "a"),
                q("We don't have ___ bread. Let's buy some.", "any", "some", "many", "no"),
                q("How ___ people came to the party?", "many", "much", "lot", "any"),
                q("I don't have ___ money this month.", "much", "many", "few", "a lot"),
                q("What ___ you doing right now?", "are", "do", "is", "does"),
                order("I am looking for a blue jacket", "أبحث عن سترة زرقاء"),
                type("Complete with the verb «wear»: Today she is ___ a red dress.", "wearing"),
            ),
        )
        vocabulary(
            "Shopping & clothes", "التسوق والملابس",
            listOf(
                w("price", "سعر", "What's the price of this bag?"),
                w("size", "مقاس", "Do you have this in a bigger size?"),
                w("try on", "يجرّب (ملابس)", "Can I try on these shoes?"),
                w("discount", "خصم", "There's a 20% discount today."),
                w("receipt", "إيصال", "Keep the receipt if you want to return it."),
                w("cash", "نقداً", "Can I pay in cash?"),
                w("expensive", "غالٍ", "This watch is too expensive."),
                w("fit", "يناسب (مقاساً)", "These jeans don't fit me."),
            ),
        )
        listening(
            "In a shop", "في المتجر",
            script = "Assistant: Hi, can I help you? Customer: Yes, I'm looking for a jacket for winter. Assistant: What size are you? Customer: Medium, I think. Assistant: Here's a nice black one. It's sixty pounds, but today there's a twenty percent discount. Customer: Great. Can I try it on? Assistant: Sure, the changing rooms are over there. Customer: Hmm, it's a bit small. Do you have it in large? Assistant: Yes, here you are. Customer: Perfect, it fits. I'll take it. Can I pay by card? Assistant: Of course.",
            questions = listOf(
                q("What is the customer looking for?", "A winter jacket", "A pair of jeans", "Winter boots", "A black shirt"),
                q("What is the original price?", "£60", "£20", "£48", "£80"),
                q("Why does the customer ask for another size?", "The first one is too small.", "The first one is too big.", "He doesn't like the colour.", "It's too expensive."),
                q("Which size does the customer buy?", "Large", "Medium", "Small", "Extra large"),
                q("How does the customer pay?", "By card", "In cash", "By cheque", "He doesn't pay."),
            ),
        )
    }

    unit("Time & weather", "الوقت والطقس") {
        grammar(
            "Prepositions of time: in, on, at", "حروف جر الزمن in / on / at",
            notes = listOf(
                "at للوقت الدقيق واللحظات: at 7 o'clock, at noon, at night, at the weekend.",
                "on للأيام والتواريخ: on Monday, on 5 May, on my birthday, on Friday morning.",
                "in للفترات الأطول: in the morning, in July, in summer, in 2024.",
                "لا نستخدم حرف جر قبل: this / next / last / every: next week, last Monday, every day.",
            ),
            examples = listOf(
                "The film starts at 8 p.m." means "يبدأ الفيلم في الثامنة مساءً.",
                "I was born on 12 March." means "وُلدت في 12 مارس.",
                "It's very hot in August." means "الجو حار جداً في أغسطس.",
                "See you next week!" means "أراك الأسبوع القادم!",
            ),
            questions = listOf(
                q("The meeting is ___ 10 o'clock.", "at", "on", "in"),
                q("My birthday is ___ June.", "in", "on", "at"),
                q("We don't work ___ Fridays.", "on", "in", "at"),
                q("I always drink coffee ___ the morning.", "in", "on", "at"),
                q("They got married ___ 2015.", "in", "on", "at"),
                q("I'll call you ___ week. (القادم)", "next", "in next", "at next", "on next"),
                q("Shops are closed ___ Christmas Day.", "on", "in", "at"),
                order("The shop opens at nine in the morning", "يفتح المتجر في التاسعة صباحاً"),
                type("Complete: I usually sleep late ___ the weekend. (حرف جر)", "at", "on"),
            ),
        )
        vocabulary(
            "Weather & seasons", "الطقس والفصول",
            listOf(
                w("sunny", "مشمس", "It's sunny today — let's go to the beach."),
                w("cloudy", "غائم", "The sky is grey and cloudy."),
                w("windy", "عاصف / فيه رياح", "It's too windy to play tennis."),
                w("storm", "عاصفة", "There was a big storm last night."),
                w("temperature", "درجة الحرارة", "The temperature is 35 degrees."),
                w("forecast", "توقعات الطقس", "The forecast says it will rain."),
                w("spring", "الربيع", "Flowers grow in spring."),
                w("autumn", "الخريف", "The leaves fall in autumn."),
            ),
        )
        listening(
            "The weather forecast", "النشرة الجوية",
            script = "Good evening, and here's the weather forecast for the weekend. On Saturday morning it will be cloudy in the north, with some rain in the afternoon. In the south, it will be sunny and warm, with temperatures of around 28 degrees. On Sunday, a storm is coming from the west, so it will be very windy, especially near the coast. Temperatures will fall to about 18 degrees. If you're planning a trip to the beach, Saturday is the better day. Have a great weekend!",
            questions = listOf(
                q("What will the weather be like on Saturday morning in the north?", "Cloudy", "Sunny", "Snowy", "Stormy"),
                q("What temperature is expected in the south on Saturday?", "About 28 degrees", "About 18 degrees", "About 38 degrees", "About 8 degrees"),
                q("Where is the storm coming from?", "The west", "The east", "The north", "The south"),
                q("Where will it be especially windy on Sunday?", "Near the coast", "In the mountains", "In the city centre", "In the desert"),
                q("Which day is better for the beach?", "Saturday", "Sunday", "Both days", "Neither day"),
            ),
        )
    }
}
