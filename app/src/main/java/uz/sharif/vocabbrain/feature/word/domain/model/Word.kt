package uz.sharif.vocabbrain.feature.word.domain.model

/**
 * One vocabulary entry as the learner sees it: the English term plus everything the
 * analysis returns about it — pronunciation, part of speech, Uzbek meaning and an example.
 */
data class Word(
    val id: String,
    val term: String,
    val phonetic: String,
    val partOfSpeech: String,
    val translationUz: String,
    val exampleSentence: String,
    val exampleTranslationUz: String,
    val isLearned: Boolean,
)
