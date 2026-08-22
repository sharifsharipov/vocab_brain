package uz.sharif.vocabbrain.feature.review.domain.usecase

import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult
import uz.sharif.vocabbrain.feature.review.domain.repository.ReviewRepository

class GetLastResultUseCase(private val repository: ReviewRepository) {
    operator fun invoke(): QuizResult? = repository.lastResult()
}
