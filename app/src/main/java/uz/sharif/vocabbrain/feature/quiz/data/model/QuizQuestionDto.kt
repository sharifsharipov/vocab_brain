package uz.sharif.vocabbrain.feature.quiz.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape of one question, matching the analysis JSON contract exactly.
 * `type` stays a String so an unknown value fails in mapping with a clear message
 * instead of blowing up the whole parse.
 */
@Serializable
data class QuizQuestionDto(
    val id: Int,
    val type: String,
    @SerialName("target_word") val targetWord: String,
    val prompt: String,
    val options: List<String> = emptyList(),
    @SerialName("correct_answer") val correctAnswer: String,
    val hint: String? = null,
    val explanation: String,
)

@Serializable
data class QuizSettingsDto(
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("time_per_question_seconds") val timePerQuestionSeconds: Int,
)

/** Standalone quiz response, used when questions are generated from stored vocabulary. */
@Serializable
data class QuizDto(
    @SerialName("quiz_settings") val quizSettings: QuizSettingsDto,
    val questions: List<QuizQuestionDto>,
)

/** Vocabulary handed to the generator as source material. */
data class QuizSourceWordDto(
    val id: String,
    val term: String,
    val translationUz: String,
    val exampleSentence: String,
)
