package uz.sharif.vocabbrain.feature.word.domain.model

/**
 * Shared sample data for previews and tests. Named `aFoo()` so call sites read as prose
 * and every field has a default — a test overrides only what it asserts on.
 */
fun aWord(
    id: String = "w-abundant",
    term: String = "abundant",
    phonetic: String = "/əˈbʌn.dənt/",
    partOfSpeech: String = "adjective",
    translationUz: String = "mo'l-ko'l",
    exampleSentence: String = "The region has abundant water supplies.",
    exampleTranslationUz: String = "Bu hududda suv zaxiralari mo'l-ko'l.",
    isLearned: Boolean = false,
) = Word(
    id = id,
    term = term,
    phonetic = phonetic,
    partOfSpeech = partOfSpeech,
    translationUz = translationUz,
    exampleSentence = exampleSentence,
    exampleTranslationUz = exampleTranslationUz,
    isLearned = isLearned,
)

fun aWordList() = listOf(
    aWord(),
    aWord(
        id = "w-candid",
        term = "candid",
        phonetic = "/ˈkæn.dɪd/",
        partOfSpeech = "adjective",
        translationUz = "samimiy",
        exampleSentence = "She gave a candid answer.",
        exampleTranslationUz = "U samimiy javob berdi.",
    ),
    aWord(
        id = "w-diligent",
        term = "diligent",
        phonetic = "/ˈdɪl.ɪ.dʒənt/",
        partOfSpeech = "adjective",
        translationUz = "tirishqoq",
        exampleSentence = "He is a diligent student.",
        exampleTranslationUz = "U tirishqoq talaba.",
        isLearned = true,
    ),
    aWord(
        id = "w-frugal",
        term = "frugal",
        phonetic = "/ˈfruː.ɡəl/",
        partOfSpeech = "adjective",
        translationUz = "tejamkor",
        exampleSentence = "A frugal shopper compares prices.",
        exampleTranslationUz = "Tejamkor xaridor narxlarni solishtiradi.",
    ),
)
