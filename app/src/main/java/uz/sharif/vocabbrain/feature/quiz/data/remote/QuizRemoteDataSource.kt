package uz.sharif.vocabbrain.feature.quiz.data.remote

import uz.sharif.vocabbrain.feature.quiz.data.model.QuizDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSourceWordDto
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

/** Boundary to whatever turns vocabulary into questions. */
interface QuizRemoteDataSource {
    suspend fun generateQuiz(words: List<QuizSourceWordDto>, config: QuizConfig): QuizDto
}
