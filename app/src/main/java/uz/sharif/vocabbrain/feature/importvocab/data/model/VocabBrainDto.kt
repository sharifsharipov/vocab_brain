package uz.sharif.vocabbrain.feature.importvocab.data.model

import uz.sharif.vocabbrain.feature.quiz.data.model.QuizQuestionDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSettingsDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full analysis response: the vocabulary found in the text plus the quiz built from it.
 * Field names follow the JSON contract the language model is asked to return.
 */
@Serializable
data class VocabBrainDto(
    @SerialName("extracted_vocabulary") val extractedVocabulary: List<ExtractedWordDto>,
    @SerialName("quiz_settings") val quizSettings: QuizSettingsDto,
    val questions: List<QuizQuestionDto>,
)
