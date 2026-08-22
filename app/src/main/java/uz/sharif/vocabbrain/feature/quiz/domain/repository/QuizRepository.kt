package uz.sharif.vocabbrain.feature.quiz.domain.repository

import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

interface QuizRepository {

    /** Builds a quiz from the stored vocabulary. */
    suspend fun generateQuiz(config: QuizConfig): Quiz

    /** Keeps the quiz that came back with an import, so the quiz screen can play it. */
    fun cacheQuiz(quiz: Quiz)

    /** Takes the cached quiz, if any, clearing it so it is played once. */
    fun takeCachedQuiz(): Quiz?
}
