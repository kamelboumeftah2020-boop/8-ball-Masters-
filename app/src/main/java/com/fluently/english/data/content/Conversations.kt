package com.fluently.english.data.content

/*
 * Role-play conversations: the app plays a character (spoken with TTS) and the
 * learner chooses — or says — a reply. Every reply carries feedback, so the
 * learner discovers why one answer is natural and another is not.
 */

enum class ReplyQuality(val points: Int) { BEST(2), OK(1), WRONG(0) }

data class Reply(val en: String, val quality: ReplyQuality, val feedback: String)

data class Turn(val line: String, val lineAr: String, val replies: List<Reply>)

data class Scenario(
    val id: String,
    val level: CefrLevel,
    val title: String,
    val titleAr: String,
    val situationAr: String,
    val partner: String,
    val partnerRoleAr: String,
    val turns: List<Turn>,
    val closing: String,
    val closingAr: String,
) {
    val maxPoints: Int get() = turns.size * ReplyQuality.BEST.points
}

private fun best(en: String, feedback: String) = Reply(en, ReplyQuality.BEST, feedback)
private fun ok(en: String, feedback: String) = Reply(en, ReplyQuality.OK, feedback)
private fun bad(en: String, feedback: String) = Reply(en, ReplyQuality.WRONG, feedback)
private fun turn(line: String, lineAr: String, vararg replies: Reply) = Turn(line, lineAr, replies.toList())

/** All conversations, easiest first (more in Conversations2.kt). */
val Scenarios: List<Scenario> by lazy { (ScenariosBase + ScenariosExtra).sortedBy { it.level.ordinal } }

private val ScenariosBase: List<Scenario> = listOf(
    Scenario(
        "conv-a1-cafe", CefrLevel.A1, "At the café", "في المقهى",
        "أنت في مقهى وتريد أن تطلب مشروباً وشيئاً تأكله.", "Emma", "النادلة",
        listOf(
            turn(
                "Hi! What can I get you?", "مرحباً! ماذا أحضر لك؟",
                best("Can I have a coffee, please?", "ممتاز! «Can I have…, please?» أكثر طريقة مهذبة وشائعة للطلب."),
                ok("I want coffee.", "مفهومة، لكنها مباشرة جداً. أضف please أو استخدم Can I have…"),
                bad("I am coffee.", "هذه تعني «أنا قهوة»! استخدم Can I have a coffee?"),
            ),
            turn(
                "Sure. Small or large?", "بالتأكيد. صغير أم كبير؟",
                best("Large, please.", "رائع — قصير ومهذب، هكذا يتحدث الناس فعلاً."),
                ok("Large.", "صحيح، لكن please تجعلها ألطف."),
                bad("Yes, please.", "سُئلت «صغير أم كبير؟» فالإجابة بـ yes غير مناسبة."),
            ),
            turn(
                "Anything to eat?", "هل تريد شيئاً تأكله؟",
                best("Yes, a chocolate muffin, please.", "ممتاز! طلبت بوضوح وأدب."),
                ok("Muffin.", "مفهومة، لكن الأفضل: a muffin, please."),
                bad("I eat every day.", "هذه لا تجيب عن السؤال؛ هي تعني «آكل كل يوم»."),
            ),
            turn(
                "That's six dollars, please.", "المجموع ستة دولارات من فضلك.",
                best("Here you are.", "«Here you are» هي العبارة الطبيعية عند تسليم شيء لشخص."),
                ok("OK, money.", "مفهومة، لكن الناطقين يقولون Here you are."),
                bad("You are welcome.", "تُقال رداً على thank you، وليس عند الدفع."),
            ),
            turn(
                "Thank you! Have a nice day.", "شكراً لك! يوماً سعيداً.",
                best("Thanks, you too!", "«You too» ترد بها الأمنية نفسها — طبيعية جداً."),
                ok("Thank you.", "جيدة. ويمكنك إضافة you too."),
                bad("Me too.", "me too تعني «وأنا أيضاً» وتُستخدم عندما تشارك شعوراً، والصحيح you too."),
            ),
        ),
        "Enjoy your coffee!", "استمتع بقهوتك!",
    ),
    Scenario(
        "conv-a1-meet", CefrLevel.A1, "Meeting a new friend", "التعرف على صديق جديد",
        "أنت في أول يوم في دورة لغة، وطالب بجانبك يبدأ الحديث معك.", "Leo", "زميل في الدورة",
        listOf(
            turn(
                "Hi! I'm Leo. What's your name?", "مرحباً! أنا ليو. ما اسمك؟",
                best("Hi Leo, I'm Sami. Nice to meet you.", "ممتاز! عرّفت بنفسك وأضفت Nice to meet you."),
                ok("Sami.", "صحيحة لكنها قصيرة جداً. جرّب: I'm Sami."),
                bad("My name are Sami.", "الصحيح: My name is Sami — name مفرد."),
            ),
            turn(
                "Nice to meet you too. Where are you from?", "تشرفت بك أيضاً. من أين أنت؟",
                best("I'm from Jordan. And you?", "رائع! أجبت ثم أعدت السؤال بـ And you? — هكذا تستمر المحادثة."),
                ok("Jordan.", "صحيحة. الجملة الكاملة أفضل: I'm from Jordan."),
                bad("I from Jordan.", "نسيت am: I am from Jordan."),
            ),
            turn(
                "I'm from Brazil. What do you do?", "أنا من البرازيل. ماذا تعمل؟",
                best("I'm an engineer.", "ممتاز! وتذكر an قبل engineer لأنها تبدأ بصوت متحرك."),
                ok("I'm engineer.", "قريبة! ينقصها an: I'm an engineer."),
                bad("I do fine, thanks.", "«What do you do?» تسأل عن عملك، لا عن حالك."),
            ),
            turn(
                "Cool! Do you like English?", "رائع! هل تحب الإنجليزية؟",
                best("Yes, I do. It's really interesting.", "ممتاز! Yes, I do هي الإجابة القصيرة الصحيحة، وأضفت رأيك."),
                ok("Yes.", "صحيحة، لكن أضف تفاصيل لتستمر المحادثة."),
                bad("Yes, I like.", "like تحتاج مفعولاً: Yes, I like it. أو Yes, I do."),
            ),
        ),
        "Great! See you in class!", "رائع! أراك في الصف!",
    ),
    Scenario(
        "conv-a2-hotel", CefrLevel.A2, "Checking in at a hotel", "تسجيل الدخول في فندق",
        "وصلت إلى فندق في لندن ولديك حجز مسبق.", "Mr Brown", "موظف الاستقبال",
        listOf(
            turn(
                "Good evening. How can I help you?", "مساء الخير. كيف أساعدك؟",
                best("Good evening. I have a reservation under the name Hassan.", "ممتاز! «a reservation under the name…» عبارة الفنادق الرسمية."),
                ok("I have a room.", "مفهومة تقريباً، لكن الأدق: I have a reservation."),
                bad("I want to buy the hotel.", "😄 هذه تعني «أريد شراء الفندق»!"),
            ),
            turn(
                "Yes, Mr Hassan, three nights. Can I see your passport, please?", "نعم سيد حسن، ثلاث ليالٍ. هل يمكنني رؤية جواز سفرك؟",
                best("Of course. Here it is.", "مثالي! Here it is للشيء الواحد."),
                ok("Yes, passport.", "مفهومة، والأطبع: Here it is."),
                bad("No, I don't see.", "لقد طُلب منك الجواز، لا أن ترى شيئاً."),
            ),
            turn(
                "Thank you. Would you like breakfast included?", "شكراً. هل تريد أن يشمل الحجز الفطور؟",
                best("Yes, please. What time is breakfast?", "رائع! وافقت وسألت سؤالاً مفيداً."),
                ok("Yes.", "صحيحة، لكن Yes, please ألطف."),
                bad("I would like breakfast included yesterday.", "yesterday لا معنى لها هنا."),
            ),
            turn(
                "From 7 to 10 a.m. Here's your key. Room 305 is on the third floor.", "من السابعة حتى العاشرة. هذا مفتاحك. الغرفة 305 في الطابق الثالث.",
                best("Thanks. Is there a lift?", "ممتاز! lift (البريطانية) = elevator (الأمريكية)."),
                ok("OK.", "مقبولة. جرب أن تشكر وتسأل عن شيء تحتاجه."),
                bad("Where is the key?", "لقد أعطاك المفتاح للتو!"),
            ),
        ),
        "Yes, just on your left. Enjoy your stay!", "نعم، على يسارك مباشرة. إقامة سعيدة!",
    ),
    Scenario(
        "conv-a2-shop", CefrLevel.A2, "Returning a shirt", "إرجاع قميص",
        "اشتريت قميصاً أمس لكن مقاسه صغير، وتريد استبداله.", "Kate", "موظفة المتجر",
        listOf(
            turn(
                "Hello, can I help you?", "مرحباً، هل أستطيع مساعدتك؟",
                best("Yes, I bought this shirt yesterday, but it's too small.", "ممتاز! استخدمت الماضي (bought) وشرحت المشكلة بوضوح."),
                ok("This shirt is small.", "مفهومة، لكن اذكر أنك اشتريته: I bought it yesterday."),
                bad("I buy this shirt tomorrow.", "الحدث في الماضي: I bought it yesterday."),
            ),
            turn(
                "I see. Do you have the receipt?", "فهمت. هل لديك الإيصال؟",
                best("Yes, here you are.", "ممتاز."),
                ok("Yes, I have.", "تحتاج مفعولاً: Yes, I have it. أو أعطه الإيصال: Here you are."),
                bad("No, I have a shirt.", "الإجابة لا تتعلق بالسؤال."),
            ),
            turn(
                "Would you like a refund or a bigger size?", "هل تريد استرداد المال أم مقاساً أكبر؟",
                best("I'd like a bigger size, please. Do you have a large?", "ممتاز! I'd like أكثر تهذيباً من I want."),
                ok("Bigger.", "صحيحة لكنها مختصرة جداً."),
                bad("I'd like a more big size.", "الصحيح: a bigger size (صفة قصيرة + er)."),
            ),
        ),
        "Here's a large. You can try it on over there.", "هذا مقاس كبير. يمكنك تجربته هناك.",
    ),
    Scenario(
        "conv-b1-doctor", CefrLevel.B1, "At the doctor's", "عند الطبيب",
        "تشعر بالمرض منذ يومين وذهبت إلى الطبيب.", "Dr Patel", "الطبيبة",
        listOf(
            turn(
                "Good morning. What seems to be the problem?", "صباح الخير. ما المشكلة؟",
                best("I've had a sore throat and a fever since Monday.", "ممتاز! المضارع التام + since مثالي لعرَض بدأ ومازال مستمراً."),
                ok("I am sick.", "صحيحة لكنها عامة جداً. صف الأعراض."),
                bad("I have a sore throat since Monday.", "مع since لحالة مستمرة نستخدم المضارع التام: I've had."),
            ),
            turn(
                "I see. Have you taken any medicine?", "فهمت. هل تناولت أي دواء؟",
                best("Just some paracetamol, but it didn't help much.", "رائع! أجبت وأضفت معلومة مهمة للطبيب."),
                ok("Yes.", "أي دواء؟ التفاصيل مهمة عند الطبيب."),
                bad("Yes, I have took.", "التصريف الثالث لـ take هو taken: I have taken."),
            ),
            turn(
                "OK. It looks like an infection. You should rest and drink plenty of water.", "يبدو أنها عدوى. يجب أن ترتاح وتشرب الكثير من الماء.",
                best("Should I stay at home from work?", "ممتاز! سؤال منطقي باستخدام should."),
                ok("OK, thanks.", "مقبولة. ويمكنك أن تسأل عن العمل أو الدواء."),
                bad("I must not drink water?", "الطبيبة قالت العكس تماماً."),
            ),
            turn(
                "Yes, for two or three days. I'll give you a prescription too.", "نعم، يومين أو ثلاثة. وسأعطيك وصفة طبية.",
                best("Thank you, doctor. How often should I take it?", "ممتاز! How often تسأل عن عدد المرات."),
                ok("Thank you.", "جيدة، لكن اسأل عن طريقة استخدام الدواء."),
                bad("How much does the doctor cost?", "ليس الوقت المناسب، وصيغة السؤال غريبة."),
            ),
        ),
        "Three times a day after meals. Get well soon!", "ثلاث مرات يومياً بعد الأكل. شفاك الله!",
    ),
    Scenario(
        "conv-b1-plans", CefrLevel.B1, "Making weekend plans", "التخطيط لعطلة نهاية الأسبوع",
        "صديقتك تتصل بك لتقترح الخروج في عطلة نهاية الأسبوع.", "Mia", "صديقتك",
        listOf(
            turn(
                "Hey! Are you doing anything this Saturday?", "مرحباً! هل لديك أي خطط يوم السبت؟",
                best("Not really. Why, have you got something in mind?", "ممتاز! تعبير طبيعي جداً للسؤال عن فكرتها."),
                ok("No.", "صحيحة، لكنها قد تبدو باردة. أظهر اهتمامك."),
                bad("I did nothing on Saturday.", "السؤال عن السبت القادم، لا الماضي."),
            ),
            turn(
                "How about going hiking in the mountains?", "ما رأيك في المشي في الجبال؟",
                best("That sounds great! What time shall we meet?", "رائع! That sounds great + سؤال عملي."),
                ok("OK, good idea.", "جيدة، ويمكنك أن تكون أكثر حماساً."),
                bad("How about I am going?", "تركيب غير صحيح."),
            ),
            turn(
                "Let's meet at 8. If it rains, we'll go to the cinema instead.", "لنلتقِ في الثامنة. وإذا أمطرت سنذهب إلى السينما بدلاً من ذلك.",
                best("Perfect. I'll bring some sandwiches.", "ممتاز! will لعرض تقرره الآن."),
                ok("OK.", "مقبولة."),
                bad("If it will rain, I don't come.", "لا will بعد if، والجملة سلبية بلا سبب."),
            ),
        ),
        "Great, see you on Saturday!", "رائع، أراك يوم السبت!",
    ),
    Scenario(
        "conv-b2-interview", CefrLevel.B2, "Job interview", "مقابلة عمل",
        "أنت في مقابلة لوظيفة مسوّق في شركة دولية.", "Ms Clarke", "مديرة الموارد البشرية",
        listOf(
            turn(
                "Thanks for coming in. Could you tell me a bit about yourself?", "شكراً لحضورك. هل يمكنك أن تحدثني قليلاً عن نفسك؟",
                best("Certainly. I've been working in digital marketing for five years, mainly in e-commerce.", "ممتاز! المضارع التام المستمر يبرز خبرتك المستمرة."),
                ok("I am 28 and I like football.", "معلومات شخصية لا تهم المقابلة. ركز على خبرتك."),
                bad("I work in marketing since five years.", "الصحيح: I've been working… for five years."),
            ),
            turn(
                "Why are you interested in this position?", "لماذا تهتم بهذه الوظيفة؟",
                best("I'm looking for a role where I can lead international campaigns, and your company is known for that.", "رائع! ربطت طموحك بالشركة."),
                ok("Because the salary is good.", "صادقة، لكنها ليست إجابة مقابلات جيدة."),
                bad("I'm interesting in this position.", "interested (مهتم) وليس interesting (مثير للاهتمام)."),
            ),
            turn(
                "What would you say is your greatest weakness?", "ما أكبر نقاط ضعفك برأيك؟",
                best("I sometimes take on too much work, but I've learned to prioritise and delegate.", "ممتاز! ضعف حقيقي مع ما فعلته لتحسينه."),
                ok("I don't have any weaknesses.", "إجابة غير مقنعة؛ الجميع لديه نقاط ضعف."),
                bad("My weakness is I am very weak.", "تكرار غريب لا يعطي معلومة."),
            ),
            turn(
                "Do you have any questions for us?", "هل لديك أي أسئلة لنا؟",
                best("Yes — what would a typical day look like in this role?", "ممتاز! سؤال يُظهر اهتماماً حقيقياً."),
                ok("No, thank you.", "مقبولة، لكن السؤال يُظهر اهتمامك."),
                bad("When will I get the job?", "مبكر جداً ويبدو متعجرفاً."),
            ),
        ),
        "Great question. We'll be in touch by Friday.", "سؤال رائع. سنتواصل معك قبل يوم الجمعة.",
    ),
    Scenario(
        "conv-b2-complaint", CefrLevel.B2, "Making a complaint", "تقديم شكوى",
        "تأخر طلبك من متجر إلكتروني أسبوعين، فاتصلت بخدمة العملاء.", "Dan", "خدمة العملاء",
        listOf(
            turn(
                "Customer service, Dan speaking. How may I help?", "خدمة العملاء، معك دان. كيف أساعدك؟",
                best("Hi Dan. I ordered a laptop two weeks ago and it still hasn't arrived.", "ممتاز! وصف واضح باستخدام still hasn't."),
                ok("My laptop is not here!", "مفهومة، لكن أضف التفاصيل بهدوء."),
                bad("I have ordered a laptop two weeks ago.", "مع ago نستخدم الماضي البسيط: I ordered."),
            ),
            turn(
                "I'm sorry to hear that. Could I have your order number?", "آسف لسماع ذلك. هل يمكنني الحصول على رقم الطلب؟",
                best("Sure, it's 4 5 7 2 9.", "ممتاز."),
                ok("Yes, I could.", "حرفية جداً؛ أعطه الرقم مباشرة."),
                bad("No, you couldn't.", "غير مهذبة وغير مفيدة."),
            ),
            turn(
                "It seems it was sent to the wrong address. I do apologise.", "يبدو أنه أُرسل إلى عنوان خاطئ. أعتذر بشدة.",
                best("I see. Could you send a replacement as soon as possible, or should I ask for a refund?", "ممتاز! طلب حل واضح بأسلوب مهذب."),
                ok("This is terrible service.", "مشاعرك مفهومة، لكن اطلب حلاً."),
                bad("It's OK, no problem at all.", "لكنك لم تحصل على طلبك! اطلب حلاً."),
            ),
        ),
        "Of course. We'll send a new one by express delivery today, free of charge.", "بالطبع. سنرسل جهازاً جديداً اليوم بالشحن السريع مجاناً.",
    ),
    Scenario(
        "conv-c1-meeting", CefrLevel.C1, "Disagreeing in a meeting", "الاختلاف في الرأي خلال اجتماع",
        "في اجتماع عمل، زميلك يقترح خفض ميزانية التسويق إلى النصف، وأنت لا توافق.", "Tom", "زميل",
        listOf(
            turn(
                "I think we should cut the marketing budget by half. It's not delivering results.", "أعتقد أنه يجب خفض ميزانية التسويق للنصف. إنها لا تحقق نتائج.",
                best("I see where you're coming from, but I'm not sure halving it is the answer.", "ممتاز! اعتراف بوجهة نظره قبل الاختلاف — دبلوماسية عالية."),
                ok("I disagree.", "واضحة لكنها حادة في سياق العمل."),
                bad("You are wrong, it's a stupid idea.", "هجومية جداً وغير مهنية."),
            ),
            turn(
                "Why not? The numbers speak for themselves.", "لماذا لا؟ الأرقام تتحدث عن نفسها.",
                best("Arguably, the numbers reflect a seasonal dip rather than the campaign itself.", "رائع! Arguably + rather than = حجة متوازنة."),
                ok("The numbers are not true.", "ادعاء قوي بلا دليل."),
                bad("Numbers don't speak.", "تفهم التعبير حرفياً؛ «speak for themselves» تعني «واضحة»."),
            ),
            turn(
                "So what would you suggest?", "إذن ماذا تقترح؟",
                best("What I'd propose is that we reallocate funds to the channels that are actually performing.", "ممتاز! جملة مشطورة (What I'd propose is…) تبرز اقتراحك."),
                ok("Keep the budget.", "مختصرة؛ قدّم بديلاً مدروساً."),
                bad("I suggest you to reallocate.", "suggest لا تأخذ to: I suggest that we reallocate."),
            ),
        ),
        "That's a fair point. Let's look at the data together next week.", "نقطة منطقية. لننظر في البيانات معاً الأسبوع القادم.",
    ),
    Scenario(
        "conv-c1-landlord", CefrLevel.C1, "Negotiating rent", "التفاوض على الإيجار",
        "عقد شقتك ينتهي، والمالك يريد رفع الإيجار 15%.", "Mrs Lee", "مالكة الشقة",
        listOf(
            turn(
                "I'm afraid I'll have to raise the rent by 15% next year.", "أخشى أنني سأرفع الإيجار 15% العام القادم.",
                best("I appreciate the costs have gone up, but 15% is quite a jump. Is there any flexibility?", "ممتاز! تفهّم + اعتراض مهذب + سؤال مفتوح."),
                ok("That's too much.", "صريحة لكنها تُغلق النقاش."),
                bad("I won't pay, never.", "لا مجال للتفاوض هنا."),
            ),
            turn(
                "Well, prices have risen everywhere.", "حسناً، الأسعار ارتفعت في كل مكان.",
                best("True, but I've always paid on time and looked after the flat. Would you consider 5% if I signed for two years?", "رائع! ورقة تفاوض قوية: ميزة لك مقابل تنازل."),
                ok("Can you make it 5%?", "طلب مباشر بلا مبرر."),
                bad("Prices haven't risen.", "إنكار للواقع يضعف موقفك."),
            ),
        ),
        "Hmm, a two-year contract is appealing. Let's say 7% and we have a deal.", "عقد لسنتين مغرٍ. لنقل 7% واتفقنا.",
    ),
    Scenario(
        "conv-c2-debate", CefrLevel.C2, "A panel debate", "نقاش في ندوة",
        "أنت متحدث في ندوة عن تأثير الذكاء الاصطناعي على التعليم.", "Moderator", "مدير الندوة",
        listOf(
            turn(
                "Some argue AI will make teachers obsolete. Where do you stand?", "يرى البعض أن الذكاء الاصطناعي سيجعل المعلمين بلا فائدة. ما موقفك؟",
                best("Far from making teachers obsolete, AI is likely to redefine their role — from transmitting facts to mentoring.", "ممتاز! Far from… + تحوّط (is likely to) + تضاد بلاغي."),
                ok("I don't think so.", "رأي بلا حجة؛ في مستوى C2 يُنتظر تفصيل."),
                bad("AI will make teachers obsolete never.", "ترتيب الكلمات خاطئ."),
            ),
            turn(
                "But surely there's a risk students will simply let machines think for them?", "لكن ألا يوجد خطر أن يدع الطلاب الآلات تفكر عنهم؟",
                best("Admittedly, that risk is real — which is precisely why critical thinking must be at the heart of the curriculum.", "رائع! تنازل (Admittedly) ثم قلب الحجة لصالحك."),
                ok("Yes, maybe.", "تتنازل دون رد."),
                bad("Students are lazy, so yes.", "تعميم لا يليق بنقاش رسمي."),
            ),
        ),
        "A compelling point to end on. Thank you.", "نقطة مقنعة نختم بها. شكراً لك.",
    ),
    Scenario(
        "conv-c2-diplomacy", CefrLevel.C2, "Delivering bad news tactfully", "إيصال خبر سيئ بلباقة",
        "عليك أن تخبر عميلاً مهماً أن المشروع سيتأخر شهراً.", "Mr Grant", "عميل مهم",
        listOf(
            turn(
                "So, are we still on track for the June launch?", "إذن، هل ما زلنا على المسار الصحيح لإطلاق يونيو؟",
                best("I'll be candid with you: we've run into some unforeseen technical issues, and a short delay looks unavoidable.", "ممتاز! صراحة مع تلطيف (unforeseen, short) دون مراوغة."),
                ok("No, we are late.", "صريحة جداً وقاسية على عميل مهم."),
                bad("Everything is perfect.", "إخفاء الحقيقة سيضر بالثقة لاحقاً."),
            ),
            turn(
                "That's very disappointing. How long are we talking?", "هذا مخيب جداً. كم من الوقت نتحدث؟",
                best("Around four weeks. Rest assured, we've put additional resources in place to ensure it doesn't slip further.", "رائع! رقم واضح مع طمأنة (Rest assured)."),
                ok("Maybe one month, I'm not sure.", "عدم اليقين يقلق العميل."),
                bad("It's not our fault.", "لوم الآخرين غير مهني."),
            ),
        ),
        "I appreciate your honesty. Keep me posted weekly.", "أقدّر صراحتك. أبقني على اطلاع أسبوعياً.",
    ),
)
