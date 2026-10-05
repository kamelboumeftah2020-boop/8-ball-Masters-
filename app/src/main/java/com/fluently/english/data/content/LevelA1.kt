package com.fluently.english.data.content

val LevelA1 = level(
    CefrLevel.A1,
    exam = listOf(
        q("My name ___ Sara.", "is", "am", "are", "be"),
        q("___ you a student?", "Are", "Is", "Am", "Do"),
        q("He ___ coffee every morning.", "drinks", "drink", "drinking", "is drink"),
        q("I have ___ apple.", "an", "a", "two", "some a"),
        q("There ___ three books on the table.", "are", "is", "be", "am"),
        q("She doesn't ___ meat.", "eat", "eats", "eating", "ate"),
        q("The opposite of «big» is…", "small", "tall", "long", "old"),
        q("My father's mother is my…", "grandmother", "aunt", "sister", "cousin"),
        q("What time is it? — It's ___ o'clock.", "seven", "seventh", "the seven", "seven's"),
        q("Can you swim? — Yes, I ___.", "can", "do", "am", "swim"),
        order("Where do you live", "أين تسكن؟"),
        listen("I'm from Morocco and I live in Rabat.", "Where does the speaker live?", "In Rabat", "In Morocco only", "In Cairo", "In Paris"),
        listen("The bread costs two dollars.", "How much is the bread?", "$2", "$12", "$20", "$3"),
        type("Write the plural of «child».", "children"),
    ),
) {
    unit("Hello!", "مرحباً! التعارف") {
        grammar(
            "The verb «to be»", "فعل الكينونة am / is / are",
            notes = listOf(
                "فعل to be يعني «يكون» ويُستخدم لوصف الأشخاص والأشياء والتعريف بالنفس. في العربية نقول «أنا طالب» بدون فعل، أما في الإنجليزية فلا بد منه: I am a student.",
                "نستخدم am مع I فقط، وis مع المفرد he / she / it، وare مع you / we / they.",
                "النفي: نضيف not بعد الفعل: I am not / she is not (isn't) / they are not (aren't).",
                "السؤال: نقدّم الفعل على الفاعل: Are you happy? — Is he a teacher?",
            ),
            examples = listOf(
                "I am Ahmed." means "أنا أحمد.",
                "She is a doctor." means "هي طبيبة.",
                "We are friends." means "نحن أصدقاء.",
                "Is it cold today? — No, it isn't." means "هل الجو بارد اليوم؟ — لا.",
            ),
            questions = listOf(
                q("I ___ a student.", "am", "is", "are", explain = "مع I نستخدم am دائماً."),
                q("She ___ from Jordan.", "is", "am", "are", explain = "she مفرد غائب، لذلك is."),
                q("They ___ my friends.", "are", "is", "am", explain = "they جمع، لذلك are."),
                q("___ you ready?", "Are", "Is", "Am", explain = "في السؤال نبدأ بالفعل: Are you…?"),
                q("He ___ not a teacher.", "is", "are", "am"),
                q("Choose the correct short form of «we are».", "we're", "we'is", "wer'e", "we'ar"),
                order("My name is Omar", "اسمي عمر"),
                order("Are you from Egypt", "هل أنت من مصر؟"),
                type("Complete: It ___ a nice day. (اكتب الفعل)", "is"),
            ),
        )
        vocabulary(
            "Greetings & countries", "التحيات والدول",
            listOf(
                w("hello", "مرحباً", "Hello, how are you?"),
                w("goodbye", "مع السلامة", "Goodbye, see you tomorrow."),
                w("name", "اسم", "What is your name?"),
                w("country", "دولة / بلد", "My country is very beautiful."),
                w("friend", "صديق", "She is my best friend."),
                w("morning", "صباح", "Good morning, teacher!"),
                w("thank you", "شكراً لك", "Thank you for your help."),
                w("please", "من فضلك", "Can you help me, please?"),
            ),
        )
        listening(
            "Meeting new people", "لقاء أشخاص جدد",
            script = "Hi! My name is Lina. I am twenty years old. I am from Tunisia, but I live in London now. I am a student at the university. This is my friend Tom. He is from Canada. He is a teacher.",
            questions = listOf(
                q("What is the speaker's name?", "Lina", "Lisa", "Laila", "Tom"),
                q("How old is she?", "20", "12", "22", "30"),
                q("Where is she from?", "Tunisia", "London", "Canada", "Turkey"),
                q("What does Tom do?", "He is a teacher.", "He is a student.", "He is a doctor.", "He is a driver."),
                q("Where does Lina live now?", "In London", "In Tunisia", "In Canada", "In Paris"),
            ),
        )
    }

    unit("My family", "عائلتي وأشيائي") {
        grammar(
            "a / an, plurals, this / that", "أدوات النكرة والجمع وأسماء الإشارة",
            notes = listOf(
                "نستخدم a قبل الكلمة التي تبدأ بصوت ساكن: a book, a car. ونستخدم an قبل صوت متحرك (a, e, i, o, u): an apple, an hour (لأن h لا تُنطق).",
                "الجمع في الغالب بإضافة s: book → books. والكلمات المنتهية بـ s, sh, ch, x نضيف لها es: box → boxes.",
                "جموع شاذة يجب حفظها: man → men, woman → women, child → children, person → people.",
                "this / these للقريب (مفرد / جمع)، وthat / those للبعيد (مفرد / جمع).",
            ),
            examples = listOf(
                "This is an orange." means "هذه برتقالة.",
                "Those are my brothers." means "أولئك إخوتي.",
                "I have two children." means "لدي طفلان.",
                "That is a beautiful house." means "ذلك بيت جميل.",
            ),
            questions = listOf(
                q("I eat ___ egg every day.", "an", "a", "—", explain = "egg تبدأ بصوت متحرك، لذلك an."),
                q("She has ___ cat.", "a", "an", "two"),
                q("The plural of «box» is…", "boxes", "boxs", "boxies", "box"),
                q("The plural of «woman» is…", "women", "womans", "womens", "woman"),
                q("___ are my keys (here, near me).", "These", "This", "That", "Those"),
                q("Look at ___ bird over there!", "that", "these", "this", "those"),
                order("These are my parents", "هؤلاء والداي"),
                type("Write the plural of «person».", "people", "persons"),
            ),
        )
        vocabulary(
            "Family members", "أفراد العائلة",
            listOf(
                w("mother", "أم", "My mother is a nurse."),
                w("father", "أب", "My father works in a bank."),
                w("brother", "أخ", "I have one brother."),
                w("sister", "أخت", "My sister is very funny."),
                w("son", "ابن", "Their son is five years old."),
                w("daughter", "ابنة", "She has a daughter and a son."),
                w("grandfather", "جد", "My grandfather tells great stories."),
                w("husband", "زوج", "Her husband is a chef."),
            ),
        )
        reading(
            "My family", "عائلتي",
            passage = "My name is Karim. I am from Algeria. There are five people in my family. My father's name is Ali. He is an engineer. My mother is Fatima. She is a teacher at a primary school. I have one brother and one sister. My brother, Yacine, is fifteen. My sister, Amira, is ten. She is very clever. We have a small dog. His name is Max. I love my family!",
            questions = listOf(
                q("How many people are in Karim's family?", "Five", "Four", "Six", "Three"),
                q("What is his father's job?", "Engineer", "Teacher", "Doctor", "Driver"),
                q("Where does his mother work?", "At a primary school", "At a hospital", "At a bank", "At home"),
                q("How old is Amira?", "Ten", "Fifteen", "Five", "Twelve"),
                q("«Max» is…", "a dog", "Karim's brother", "a cat", "Karim's friend"),
            ),
        )
    }

    unit("Daily life", "الحياة اليومية") {
        grammar(
            "Present simple", "المضارع البسيط",
            notes = listOf(
                "نستخدم المضارع البسيط للعادات والحقائق والأشياء التي تتكرر: I drink tea every morning.",
                "مع he / she / it نضيف s للفعل: she works, he plays. وإذا انتهى بـ o, sh, ch, x, s نضيف es: he goes, she watches.",
                "النفي: don't + الفعل (مع I / you / we / they) و doesn't + الفعل بدون s (مع he / she / it): She doesn't like fish.",
                "السؤال: Do / Does + الفاعل + الفعل: Do you speak English? — Does he live here?",
                "كلمات مفتاحية: always, usually, often, sometimes, never, every day.",
            ),
            examples = listOf(
                "I get up at 7 o'clock." means "أستيقظ في السابعة.",
                "He goes to work by bus." means "يذهب إلى العمل بالحافلة.",
                "We don't watch TV." means "نحن لا نشاهد التلفاز.",
                "Does she speak French?" means "هل تتحدث الفرنسية؟",
            ),
            questions = listOf(
                q("She ___ in a hospital.", "works", "work", "working", explain = "مع she نضيف s للفعل."),
                q("I ___ coffee. I prefer tea.", "don't like", "doesn't like", "not like"),
                q("___ he play football?", "Does", "Do", "Is"),
                q("My brother ___ TV every evening.", "watches", "watchs", "watch"),
                q("They ___ live in Dubai.", "don't", "doesn't", "aren't"),
                q("Where ___ you work?", "do", "does", "are"),
                q("Choose the correct sentence.", "He never eats meat.", "He eats never meat.", "Never he eats meat.", "He never eat meat."),
                order("I always go to bed early", "أنام مبكراً دائماً"),
                type("Complete with «go»: She ___ to school by bus.", "goes"),
            ),
        )
        vocabulary(
            "Daily routines & time", "الروتين اليومي والوقت",
            listOf(
                w("wake up", "يستيقظ", "I wake up at six every day."),
                w("breakfast", "فطور", "I have breakfast with my family."),
                w("work", "يعمل / عمل", "My parents work in the city."),
                w("lunch", "غداء", "We have lunch at one o'clock."),
                w("evening", "مساء", "I read a book in the evening."),
                w("always", "دائماً", "She always drinks water."),
                w("never", "أبداً", "I never eat fast food."),
                w("weekend", "عطلة نهاية الأسبوع", "We visit our grandparents at the weekend."),
            ),
        )
        reading(
            "A day in my life", "يوم من حياتي",
            passage = "Hello, I'm Nora. I'm a nurse in a big hospital. I usually wake up at 5:30 because I start work at 7:00. I have a quick breakfast — bread, cheese and a cup of tea. I go to work by metro. My work is hard, but I love it. I help a lot of people every day. I finish work at 3 p.m. In the evening, I cook dinner and call my mother. I never go to bed late. At the weekend, I go swimming with my friends.",
            questions = listOf(
                q("What is Nora's job?", "She is a nurse.", "She is a doctor.", "She is a teacher.", "She is a cook."),
                q("What time does she start work?", "7:00", "5:30", "3:00", "8:00"),
                q("How does she go to work?", "By metro", "By bus", "By car", "On foot"),
                q("What does she do in the evening?", "She cooks and calls her mother.", "She goes swimming.", "She works at the hospital.", "She watches films."),
                q("Nora goes to bed late. True or false?", "False", "True", "The text doesn't say"),
            ),
        )
    }

    unit("Places & food", "الأماكن والطعام") {
        grammar(
            "There is / there are, can, prepositions", "يوجد، يستطيع، وحروف الجر",
            notes = listOf(
                "There is + مفرد / There are + جمع للتعبير عن وجود شيء: There is a park near my house. There are two cafés.",
                "can للقدرة والطلب، وبعدها الفعل في صورته الأصلية دائماً: I can swim. Can you help me? والنفي can't.",
                "حروف جر المكان: in (داخل)، on (على)، under (تحت)، next to (بجانب)، between (بين)، behind (خلف)، in front of (أمام).",
            ),
            examples = listOf(
                "There is a bank next to the hotel." means "يوجد بنك بجانب الفندق.",
                "There are some apples in the fridge." means "يوجد بعض التفاح في الثلاجة.",
                "I can't speak Chinese." means "لا أستطيع التحدث بالصينية.",
                "The cat is under the table." means "القطة تحت الطاولة.",
            ),
            questions = listOf(
                q("There ___ a supermarket in my street.", "is", "are", "be"),
                q("There ___ many students in the class.", "are", "is", "am"),
                q("She can ___ the guitar.", "play", "plays", "to play", "playing", explain = "بعد can يأتي الفعل بدون to وبدون s."),
                q("The book is ___ the table. (فوقها)", "on", "in", "under", "between"),
                q("The bank is ___ the café and the school.", "between", "next", "in", "on"),
                q("___ you speak English? — Yes, a little.", "Can", "Are", "Is"),
                order("There is a park near my house", "يوجد حديقة قرب بيتي"),
                type("Complete: I ___ swim. I'm afraid of water. (لا أستطيع)", "can't", "cannot", "can not"),
            ),
        )
        vocabulary(
            "Food & drinks", "الطعام والمشروبات",
            listOf(
                w("water", "ماء", "Can I have a glass of water?"),
                w("bread", "خبز", "We buy fresh bread every morning."),
                w("chicken", "دجاج", "I'd like chicken and rice, please."),
                w("vegetables", "خضروات", "Vegetables are good for you."),
                w("fruit", "فاكهة", "I eat fruit after dinner."),
                w("juice", "عصير", "This orange juice is delicious."),
                w("hungry", "جائع", "I'm hungry. Let's eat!"),
                w("menu", "قائمة الطعام", "Can I see the menu, please?"),
            ),
        )
        listening(
            "At the café", "في المقهى",
            script = "Waiter: Good afternoon. What can I get you? Customer: Hello. Can I have a chicken sandwich and an orange juice, please? Waiter: Of course. Anything else? Customer: Yes, a small salad, please. How much is that? Waiter: That's nine dollars fifty. Customer: Here you are. Thank you!",
            questions = listOf(
                q("What sandwich does the customer order?", "Chicken", "Cheese", "Egg", "Fish"),
                q("What drink does the customer want?", "Orange juice", "Coffee", "Water", "Tea"),
                q("What else does the customer order?", "A small salad", "A cake", "Soup", "Ice cream"),
                q("How much does the customer pay?", "\$9.50", "\$5.90", "\$19.50", "\$9.15"),
                q("When does this conversation happen?", "In the afternoon", "In the morning", "At night", "At midnight"),
            ),
        )
    }

    unit("My home", "بيتي ومدينتي") {
        grammar(
            "Possessives & have got", "الملكية: my / your و 's و have got",
            notes = listOf(
                "صفات الملكية تأتي قبل الاسم: my (لي)، your (لك)، his (له)، her (لها)، its (لشيء)، our (لنا)، their (لهم).",
                "للملكية بالاسم نضيف 's: Sara's car (سيارة سارة)، my brother's room. ومع الجمع المنتهي بـ s نضيف ' فقط: my parents' house.",
                "have got = have (يملك) في الإنجليزية البريطانية: I've got a car / She's got two cats. والنفي: haven't got / hasn't got.",
                "السؤال: Have you got…? — Has he got…?",
            ),
            examples = listOf(
                "This is my flat." means "هذه شقتي.",
                "Is that your phone?" means "هل هذا هاتفك؟",
                "It's Ahmed's bike." means "إنها دراجة أحمد.",
                "She's got a big garden." means "لديها حديقة كبيرة.",
            ),
            questions = listOf(
                q("This is ___ house. (نحن)", "our", "we", "us", "ours"),
                q("Mona and Ali love ___ new car.", "their", "they", "there", "them"),
                q("That's ___ bag. (سارة)", "Sara's", "Saras", "Sara", "of Sara"),
                q("He ___ got a new phone.", "has", "have", "is", "does"),
                q("___ you got any brothers?", "Have", "Has", "Do", "Are"),
                q("The cat is eating ___ food.", "its", "it's", "it", "his"),
                order("My sister has got a small car", "أختي لديها سيارة صغيرة"),
                type("Complete: I ___ got a dog. (نفي — اختصار)", "haven't", "have not"),
            ),
        )
        vocabulary(
            "Rooms & furniture", "الغرف والأثاث",
            listOf(
                w("kitchen", "مطبخ", "We cook dinner in the kitchen."),
                w("bedroom", "غرفة نوم", "My bedroom is small but nice."),
                w("bathroom", "حمّام", "The bathroom is next to my room."),
                w("living room", "غرفة المعيشة", "We watch TV in the living room."),
                w("sofa", "أريكة", "The cat is sleeping on the sofa."),
                w("table", "طاولة", "Put the keys on the table."),
                w("window", "نافذة", "Please open the window."),
                w("stairs", "درج", "The stairs go up to the bedrooms."),
            ),
        )
        reading(
            "My new flat", "شقتي الجديدة",
            passage = "Hi! I'm Huda and this is my new flat. It's on the third floor of a modern building in the city centre. There are two bedrooms: my bedroom and my sister's bedroom. My bedroom has got a big window, so it's very bright. The living room is my favourite room — it's got a blue sofa and a small table. The kitchen is small, but it's got everything we need. There isn't a garden, but there's a park near the building. Our neighbours are friendly. I love my new home!",
            questions = listOf(
                q("Where is Huda's flat?", "In the city centre", "In a village", "Near the sea", "Next to a school"),
                q("How many bedrooms are there?", "Two", "One", "Three", "Four"),
                q("Why is Huda's bedroom bright?", "It has a big window.", "It's on the ground floor.", "It's yellow.", "It has many lamps."),
                q("What colour is the sofa?", "Blue", "Red", "Green", "Black"),
                q("Is there a garden?", "No, but there's a park nearby.", "Yes, a big one.", "Yes, on the roof.", "The text doesn't say."),
            ),
        )
    }
}
