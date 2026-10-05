package com.fluently.english.data.content

import com.fluently.english.data.content.CefrLevel.A1
import com.fluently.english.data.content.CefrLevel.A2
import com.fluently.english.data.content.CefrLevel.B1
import com.fluently.english.data.content.CefrLevel.B2
import com.fluently.english.data.content.CefrLevel.C1
import com.fluently.english.data.content.CefrLevel.C2

/*
 * Original items written in the format of the large adaptive placement tests used
 * by language schools (Oxford Placement Test, Cambridge English Placement Test,
 * EF SET): Use of English, Reading and Listening sections, each item calibrated to
 * a CEFR level. Every level has two blocks of five items (3 use of English,
 * 1 reading, 1 listening) so the adaptive engine can re-test borderline results.
 */

private fun use(level: CefrLevel, q: Question.Choice) = PlacementItem(level, Skill.USE_OF_ENGLISH, q)
private fun read(level: CefrLevel, text: String, q: Question.Choice) = PlacementItem(level, Skill.READING, q, text)
private fun hear(level: CefrLevel, audio: String, prompt: String, vararg options: String) =
    PlacementItem(level, Skill.LISTENING, listen(audio, prompt, *options))

val PlacementBank: List<PlacementItem> = listOf(
    // ---------- A1 ----------
    use(A1, q("Hello, I ___ Maria.", "am", "is", "are", "be")),
    use(A1, q("They ___ from Brazil.", "are", "is", "am", "be")),
    use(A1, q("I have two ___.", "sisters", "sister", "sisteres", "a sister")),
    read(A1, "SHOP OPEN: Monday – Friday 9:00 – 17:00. CLOSED at weekends.", q("When is the shop closed?", "On Saturday and Sunday", "On Monday", "In the morning", "At 9:00")),
    hear(A1, "My phone number is oh-seven-nine, four-five-two, one-eight.", "What is the phone number?", "079 452 18", "079 542 18", "097 452 81", "079 452 80"),
    use(A1, q("She ___ in a bank.", "works", "work", "working", "is work")),
    use(A1, q("What's this? — It's ___ umbrella.", "an", "a", "the a", "—")),
    use(A1, q("I ___ like coffee.", "don't", "doesn't", "not", "am not")),
    read(A1, "Hi Sam, I'm at the park with Lucy. Come and play football with us! — Dan", q("Where is Dan?", "At the park", "At home", "At school", "At the football stadium")),
    hear(A1, "Excuse me, where is the bathroom? — It's on the left, next to the kitchen.", "Where is the bathroom?", "Next to the kitchen", "Next to the bedroom", "On the right", "Upstairs"),

    // ---------- A2 ----------
    use(A2, q("We ___ to the cinema last night.", "went", "go", "goes", "have go")),
    use(A2, q("My house is ___ than yours.", "bigger", "more big", "biggest", "big")),
    use(A2, q("Look! The children ___ in the garden.", "are playing", "play", "plays", "played")),
    read(A2, "Dear guests, the swimming pool will be closed tomorrow for cleaning. It will open again on Thursday at 8 a.m.", q("Why is the pool closed tomorrow?", "For cleaning", "Because of bad weather", "For a party", "Because it's Thursday")),
    hear(A2, "I usually take the bus to work, but today I'm walking because the weather is so nice.", "How is the speaker going to work today?", "On foot", "By bus", "By car", "By bike"),
    use(A2, q("How ___ money do you have?", "much", "many", "lot", "any")),
    use(A2, q("I'm tired. I think I ___ go to bed.", "will", "am", "going", "do")),
    use(A2, q("Did you ___ your homework?", "do", "did", "done", "doing")),
    read(A2, "Tom's party is on Saturday at 7 p.m. at his flat. Please bring something to drink. There will be pizza.", q("What should guests bring?", "Drinks", "Pizza", "Presents", "Nothing")),
    hear(A2, "The museum opens at ten, but the guided tour doesn't start until eleven thirty.", "When does the guided tour start?", "11:30", "10:00", "10:30", "11:00"),

    // ---------- B1 ----------
    use(B1, q("I ___ here since 2019.", "have lived", "live", "lived", "am living")),
    use(B1, q("If it ___ tomorrow, we'll cancel the picnic.", "rains", "will rain", "rained", "would rain")),
    use(B1, q("You ___ smoke in the hospital.", "mustn't", "don't have to", "needn't", "haven't")),
    read(B1, "Although the hotel was in a great location, the rooms were small and noisy. The staff were helpful, but I wouldn't stay there again.", q("What is the writer's overall opinion of the hotel?", "Negative", "Very positive", "Neutral", "The writer loved the rooms.")),
    hear(B1, "I was going to study engineering, but in the end I chose architecture because it's more creative.", "Why did the speaker choose architecture?", "It's more creative.", "It's easier.", "It pays more.", "His parents wanted it."),
    use(B1, q("I ___ a bath when the doorbell rang.", "was having", "had", "have had", "am having")),
    use(B1, q("I'm not used to ___ up so early.", "getting", "get", "got", "have got")),
    use(B1, q("He asked me to look ___ his cat while he was away.", "after", "for", "at", "up")),
    read(B1, "Volunteers wanted! We need people to help at the charity race on 12 May. No experience needed, but you must be over 16. Free T-shirt and lunch for all volunteers.", q("Who can volunteer?", "Anyone over 16", "Only experienced people", "Only runners", "Children under 16")),
    hear(B1, "Sorry I'm late. There was an accident on the motorway, so I had to take a different route.", "Why was the speaker late?", "An accident on the motorway", "He overslept.", "His car broke down.", "He got lost."),

    // ---------- B2 ----------
    use(B2, q("The museum ___ in 1890.", "was built", "built", "has built", "is building")),
    use(B2, q("If I had known, I ___ you.", "would have told", "would tell", "told", "will tell")),
    use(B2, q("She told me she ___ the film before.", "had seen", "has seen", "saw", "sees")),
    read(B2, "While remote working offers flexibility, many employees report feeling isolated. Companies are therefore experimenting with hybrid models that combine home and office work.", q("Why are companies trying hybrid models?", "To reduce the isolation of remote workers", "To save money on offices", "Because employees hate flexibility", "Because the law requires it")),
    hear(B2, "Had the referee not given that penalty in the final minute, we would have gone through to the next round.", "What does the speaker think?", "The penalty cost his team the match.", "His team won thanks to the penalty.", "The referee was excellent.", "The match was cancelled."),
    use(B2, q("I wish I ___ more time to travel.", "had", "have", "would have", "will have")),
    use(B2, q("The man ___ son won the award gave a speech.", "whose", "who", "which", "whom")),
    use(B2, q("She's been working ___ she graduated.", "ever since", "for", "during", "until")),
    read(B2, "The author's latest novel, despite receiving mixed reviews from critics, has topped the bestseller lists for twelve consecutive weeks.", q("What does the text say about the novel?", "It's commercially successful despite mixed reviews.", "Critics loved it.", "It sold badly.", "It was released twelve years ago.")),
    hear(B2, "We're not ruling out the possibility of a merger, but at this stage no formal discussions have taken place.", "What is the situation regarding the merger?", "It's possible, but no talks have happened yet.", "The merger has been completed.", "The merger has been cancelled.", "Talks are at an advanced stage."),

    // ---------- C1 ----------
    use(C1, q("Never ___ so much rain in one day.", "have I seen", "I have seen", "I saw", "did I saw")),
    use(C1, q("He ___ have taken the money; he was abroad at the time.", "can't", "mustn't", "shouldn't", "needn't")),
    use(C1, q("___ the report, she sent it to her manager.", "Having written", "Writing it", "Written", "Being written")),
    read(C1, "The proposal, though ostensibly aimed at reducing bureaucracy, would in practice concentrate decision-making power in the hands of a few senior officials.", q("What is the writer suggesting?", "The proposal's real effect differs from its stated aim.", "The proposal will reduce bureaucracy.", "Senior officials oppose the proposal.", "The proposal has no effect.")),
    hear(C1, "What struck me most about the exhibition wasn't the paintings themselves, but the way the curator had arranged them to tell a story.", "What impressed the speaker most?", "How the paintings were arranged", "The quality of the paintings", "The size of the exhibition", "The famous artists"),
    use(C1, q("It was not until midnight ___ the results were announced.", "that", "when", "which", "then")),
    use(C1, q("The negotiations reached a ___ when neither side would compromise.", "deadlock", "deadline", "breakthrough", "standstill point")),
    use(C1, q("She is widely ___ to be the best surgeon in the country.", "considered", "considering", "consider", "considers")),
    read(C1, "Critics have dismissed the film as derivative, yet its deliberate echoes of earlier classics are, arguably, precisely what lend it its charm.", q("What is the writer's view?", "The film's similarities to older films are a strength.", "The film is unoriginal and boring.", "Critics are right about the film.", "The film is a classic.")),
    hear(C1, "Contrary to what many people assume, the decline in bee populations is driven less by a single factor than by the interaction of pesticides, disease and habitat loss.", "What causes the decline in bees, according to the speaker?", "A combination of several factors", "Pesticides alone", "Disease alone", "Climate change only"),

    // ---------- C2 ----------
    use(C2, q("The board insisted that he ___ the position.", "resign", "resigns", "resigned", "would resign")),
    use(C2, q("So ___ was the evidence that the jury needed only an hour.", "compelling", "compelled", "compulsive", "compulsory")),
    use(C2, q("___ it not been for her intervention, the deal would have collapsed.", "Had", "Were", "If", "Should")),
    read(C2, "Her prose eschews ornamentation, yet its very austerity throws into relief the emotional turmoil simmering beneath the narrative's placid surface.", q("What does the sentence say about the author's writing?", "Its simplicity highlights hidden emotion.", "It is highly decorative.", "It lacks emotion.", "It is calm and shallow.")),
    hear(C2, "Far from vindicating the minister, the inquiry's findings, measured as they were, cast considerable doubt on the account he had given parliament.", "What did the inquiry's findings do?", "They raised serious doubts about the minister.", "They proved the minister innocent.", "They praised parliament.", "They were exaggerated."),
    use(C2, q("The politician's speech was full of ___ promises that few believed.", "hollow", "hallow", "hollowed", "shallowly")),
    use(C2, q("It's high time the issue ___ properly addressed.", "were", "is", "be", "has been")),
    use(C2, q("I'm not one to ___ words: the plan is a disaster.", "mince", "chew", "cut", "eat")),
    read(C2, "To attribute the company's collapse solely to mismanagement would be to overlook the broader macroeconomic headwinds that rendered its business model untenable.", q("What does the writer argue?", "External economic factors also caused the collapse.", "Mismanagement was the only cause.", "The business model was excellent.", "The company did not collapse.")),
    hear(C2, "The novel, for all its narrative ambition, ultimately buckles under the weight of its own erudition.", "What is the speaker's criticism of the novel?", "It is too full of learned references.", "It is not ambitious enough.", "It is too short.", "It has too much action."),
)
