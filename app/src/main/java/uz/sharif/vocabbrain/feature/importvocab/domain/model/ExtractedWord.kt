package uz.sharif.vocabbrain.feature.importvocab.domain.model

import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz

data class ExtractedWord(
    val term: String,
    val phonetic: String,
    val partOfSpeech: String,
    val translationUz: String,
    val exampleSentence: String,
    val exampleTranslationUz: String,
)

/** One analysis run: what was found in the text, and the quiz built from it. */
data class Analysis(
    val words: List<ExtractedWord>,
    val quiz: Quiz,
)
