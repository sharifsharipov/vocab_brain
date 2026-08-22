package uz.sharif.vocabbrain.feature.review.domain.usecase

import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewOutcome
import uz.sharif.vocabbrain.feature.review.domain.repository.ReviewRepository

class SubmitQuizResultUseCase(private val repository: ReviewRepository) {
    suspend operator fun invoke(outcomes: List<ReviewOutcome>): QuizResult =
        repository.submitOutcomes(outcomes)
}
