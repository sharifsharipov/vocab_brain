package uz.sharif.vocabbrain.feature.importvocab.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One extracted word, matching the analysis JSON contract exactly. */
@Serializable
data class ExtractedWordDto(
    val word: String,
    val phonetic: String,
    @SerialName("part_of_speech") val partOfSpeech: String,
    @SerialName("translation_uz") val translationUz: String,
    @SerialName("example_sentence") val exampleSentence: String,
    @SerialName("sentence_translation_uz") val sentenceTranslationUz: String,
)
