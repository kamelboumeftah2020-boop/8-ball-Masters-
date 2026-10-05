package com.fluently.english.data.content

private fun best(en: String, feedback: String) = Reply(en, ReplyQuality.BEST, feedback)
private fun ok(en: String, feedback: String) = Reply(en, ReplyQuality.OK, feedback)
private fun bad(en: String, feedback: String) = Reply(en, ReplyQuality.WRONG, feedback)
private fun turn(line: String, lineAr: String, vararg replies: Reply) = Turn(line, lineAr, replies.toList())

internal val ScenariosExtra: List<Scenario> = listOf(
    Scenario(
        "conv-a1-directions", CefrLevel.A1, "Asking for directions", "السؤال عن الطريق",
        "أنت سائح في مدينة جديدة وتبحث عن محطة القطار.", "Tom", "أحد المارّة",
        listOf(
            turn(
                "Hello! Can I help you?", "مرحباً! هل أستطيع مساعدتك؟",
                best("Yes, please. Where is the train station?", "ممتاز! «Where is …?» أبسط طريقة للسؤال عن مكان."),
                ok("Train station?", "مفهومة، لكن جملة كاملة أكثر تهذيباً: Where is the train station?"),
                bad("Yes, I am a train.", "هذه تعني «أنا قطار»! اسأل: Where is the train station?"),
            ),
            turn(
                "Go straight and turn left at the bank.", "امشِ مستقيماً وانعطف يساراً عند البنك.",
                best("Turn left at the bank? OK.", "رائع — تكرار المعلومة للتأكد عادة ذكية في المحادثة."),
                ok("OK.", "مقبول، لكن تكرار الاتجاه يساعدك على التذكر."),
                bad("I like the bank.", "لا علاقة لها بالسؤال؛ كرّر الاتجاه: Turn left at the bank?"),
            ),
            turn(
                "Yes. Then it's on your right.", "نعم. ثم ستجدها على يمينك.",
                best("Is it far?", "سؤال مهم ومناسب: هل هي بعيدة؟"),
                ok("Right.", "صحيح، لكن يمكنك أن تسأل سؤالاً مفيداً مثل Is it far?"),
                bad("Left, please.", "قال لك على يمينك؛ لا تطلب اتجاهاً آخر."),
            ),
            turn(
                "No, it's about five minutes on foot.", "لا، حوالي خمس دقائق مشياً.",
                best("Great, thank you very much!", "ممتاز — شكر واضح ومهذب."),
                ok("Thanks.", "جيد، ويمكنك إضافة very much لمزيد من اللطف."),
                bad("Five minutes ago.", "ago تعني «منذ» للماضي؛ هنا يكفي أن تشكره."),
            ),
            turn(
                "You're welcome. Have a good trip!", "على الرحب. رحلة سعيدة!",
                best("Thanks, bye!", "خاتمة طبيعية وودودة."),
                ok("Bye.", "مقبولة، وThanks قبلها ألطف."),
                bad("Good morning.", "تحية بداية وليست وداعاً؛ قل Thanks, bye!"),
            ),
        ),
        "Enjoy the city!", "استمتع بالمدينة!",
    ),
    Scenario(
        "conv-a2-airport", CefrLevel.A2, "Checking in at the airport", "تسجيل الوصول في المطار",
        "أنت في مكتب شركة الطيران قبل رحلتك إلى إسطنبول.", "Ms Green", "موظفة شركة الطيران",
        listOf(
            turn(
                "Good morning. Where are you flying to today?", "صباح الخير. إلى أين تسافر اليوم؟",
                best("Good morning. I'm flying to Istanbul.", "ممتاز! fly to + المدينة، مع رد التحية."),
                ok("Istanbul.", "مفهومة، لكن الجملة الكاملة مع التحية أفضل."),
                bad("I flew to Istanbul yesterday.", "هذا ماضٍ! أنت مسافر اليوم: I'm flying to Istanbul."),
            ),
            turn(
                "Can I see your passport, please?", "هل أستطيع رؤية جواز سفرك من فضلك؟",
                best("Of course. Here you are.", "«Of course. Here you are.» الرد المثالي عند تسليم شيء."),
                ok("Yes, passport.", "مفهومة، لكن Here you are أكثر طبيعية."),
                bad("No, thank you.", "لا يمكنك رفض إظهار الجواز!"),
            ),
            turn(
                "Thank you. Do you have any bags to check in?", "شكراً. هل لديك حقائب لشحنها؟",
                best("Yes, just one suitcase.", "واضح ودقيق: حقيبة واحدة فقط."),
                ok("One.", "صحيح، لكن اذكر الاسم: one suitcase."),
                bad("I have a bag of rice.", "السؤال عن حقائب السفر، لا عن الطعام!"),
            ),
            turn(
                "Would you like a window or an aisle seat?", "هل تفضّل مقعداً بجانب النافذة أم بجانب الممر؟",
                best("A window seat, please.", "ممتاز — اختيار واضح مع please."),
                ok("Window.", "مفهومة، وأضف please."),
                bad("Yes, I would.", "السؤال فيه خياران، فلا تجب بنعم؛ اختر أحدهما."),
            ),
            turn(
                "Here's your boarding pass. Boarding is at gate 12 at 10:30.", "هذه بطاقة الصعود. الصعود من البوابة 12 الساعة 10:30.",
                best("Gate 12 at 10:30. Thank you!", "تكرار رقم البوابة والوقت يمنع الأخطاء — ذكي جداً."),
                ok("Thank you.", "جيد، والتكرار يساعدك على التذكر."),
                bad("Gate 10 at 12:30?", "انتبه للأرقام! البوابة 12 والوقت 10:30."),
            ),
        ),
        "Have a pleasant flight!", "رحلة ممتعة!",
    ),
    Scenario(
        "conv-b1-booking", CefrLevel.B1, "Booking a table by phone", "حجز طاولة بالهاتف",
        "تتصل بمطعم لحجز طاولة لعيد ميلاد صديقك يوم الجمعة.", "Luca", "موظف المطعم",
        listOf(
            turn(
                "Good evening, Bella Roma. How can I help?", "مساء الخير، مطعم بيلا روما. كيف أساعدك؟",
                best("Hi, I'd like to book a table for Friday evening, please.", "ممتاز! «I'd like to book a table…» عبارة الحجز المهذبة القياسية."),
                ok("I want a table on Friday.", "مفهومة، لكن I'd like to… أكثر تهذيباً على الهاتف."),
                bad("I booked a table last Friday.", "هذا ماضٍ؛ أنت تريد الحجز الآن للجمعة القادمة."),
            ),
            turn(
                "Certainly. For how many people?", "بالتأكيد. لكم شخصاً؟",
                best("For six people, please.", "دقيق ومهذب: For + العدد + people."),
                ok("Six.", "صحيح، لكن الجملة الكاملة أوضح على الهاتف."),
                bad("Six o'clock.", "سُئلت عن عدد الأشخاص لا عن الوقت."),
            ),
            turn(
                "And what time would you like?", "وفي أي ساعة تريد الحجز؟",
                best("Around eight o'clock, if possible.", "«if possible» تجعل طلبك لطيفاً ومرناً."),
                ok("Eight.", "مفهومة، وأضف o'clock أو if possible."),
                bad("I'd like Friday.", "سبق أن قلت الجمعة؛ السؤال الآن عن الساعة."),
            ),
            turn(
                "Eight is fine. Can I have your name and number?", "الثامنة مناسبة. هل أستطيع أخذ اسمك ورقمك؟",
                best("Sure, it's Omar Saleh, and my number is 0794 512 300.", "ممتاز — الاسم ثم الرقم بوضوح."),
                ok("Omar.", "الاسم وحده لا يكفي؛ أعطه الرقم أيضاً."),
                bad("My number is six.", "الرقم المطلوب هو رقم الهاتف!"),
            ),
            turn(
                "Lovely. Is it a special occasion?", "رائع. هل هي مناسبة خاصة؟",
                best("Yes, it's my friend's birthday. Could you prepare a small cake?", "رائع — أجبت ثم طلبت طلباً إضافياً بأدب بـ Could you…?"),
                ok("Yes, a birthday.", "جيد، ويمكنك أن تطلب شيئاً إضافياً بـ Could you…?"),
                bad("No, it's a restaurant.", "السؤال عن مناسبتك لا عن المكان."),
            ),
        ),
        "Perfect, we'll see you on Friday at eight!", "ممتاز، نراكم الجمعة الساعة الثامنة!",
    ),
    Scenario(
        "conv-b2-advisor", CefrLevel.B2, "Meeting a university advisor", "لقاء المرشد الأكاديمي",
        "تقابل مرشدتك في الجامعة لأنك تجد صعوبة في التوفيق بين الدراسة والعمل.", "Dr Evans", "المرشدة الأكاديمية",
        listOf(
            turn(
                "Come in, have a seat. What would you like to talk about?", "تفضّل واجلس. عمّ تريد أن نتحدث؟",
                best("Thanks. I've been struggling to balance my part-time job with my coursework.", "ممتاز! المضارع التام المستمر يصف مشكلة مستمرة بدقة."),
                ok("I have a problem with my job.", "مفهومة، لكنها عامة؛ وضّح المشكلة أكثر."),
                bad("I'm fine, thank you.", "جئت لطلب المساعدة؛ اشرح مشكلتك."),
            ),
            turn(
                "I see. How many hours a week are you working?", "فهمت. كم ساعة تعمل أسبوعياً؟",
                best("About twenty-five, mostly evenings and weekends.", "إجابة دقيقة مع تفاصيل مفيدة."),
                ok("Many hours.", "غير دقيقة؛ المرشدة تحتاج رقماً."),
                bad("I work in a café.", "السؤال عن عدد الساعات لا عن مكان العمل."),
            ),
            turn(
                "That's quite a lot. Have you considered reducing your hours?", "هذا كثير. هل فكرت في تقليل ساعاتك؟",
                best("I have, but I can't afford to lose the income. Are there any other options?", "رائع — شرحت السبب ثم سألت عن بدائل بأسلوب لبق."),
                ok("No, I need money.", "صادقة، لكنها مقتضبة؛ اشرح واسأل عن بدائل."),
                bad("Yes, I will reduce my studies.", "هذا عكس المطلوب؛ الحديث عن تقليل العمل."),
            ),
            turn(
                "Well, you might be eligible for a hardship grant. Would you like me to check?", "قد تكون مؤهلاً لمنحة دعم مالي. هل تريدني أن أتحقق؟",
                best("That would be really helpful, thank you.", "«That would be really helpful» رد مهذب وطبيعي لقبول عرض."),
                ok("Yes, OK.", "مقبولة، لكن That would be helpful أكثر لباقة."),
                bad("No, I don't need help.", "جئت طالباً المساعدة؛ رفض العرض غير منطقي."),
            ),
            turn(
                "In the meantime, let's plan your deadlines for this term.", "في الأثناء، لنخطّط لمواعيد تسليم واجباتك هذا الفصل.",
                best("Good idea. Could we start with the essay that's due next week?", "ممتاز — وافقت ثم اقترحت أولوية واضحة."),
                ok("OK.", "مقبولة، لكن المبادرة باقتراح تُظهر جدّيتك."),
                bad("I don't have any deadlines.", "تناقض ما قلته عن صعوبة الواجبات."),
            ),
        ),
        "Great. Let's see you again in two weeks.", "رائع. نلتقي مجدداً بعد أسبوعين.",
    ),
)
