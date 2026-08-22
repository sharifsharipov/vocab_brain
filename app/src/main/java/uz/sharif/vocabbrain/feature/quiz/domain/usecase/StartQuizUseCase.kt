package uz.sharif.vocabbrain.feature.quiz.domain.usecase

import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import uz.sharif.vocabbrain.feature.quiz.domain.repository.QuizRepository

/**
 * Plays the quiz that came with the last import if there is one; otherwise builds a fresh
 * quiz from the stored vocabulary, so the quiz screen also works when opened on its own.
 */
class StartQuizUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(config: QuizConfig = QuizConfig()): Quiz =
        repository.takeCachedQuiz() ?: repository.generateQuiz(config)
}
