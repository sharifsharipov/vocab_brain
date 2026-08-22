package uz.sharif.vocabbrain.feature.review.domain.repository

import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewOutcome
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {

    /** Applies a finished quiz to the review schedule and keeps its result for the result screen. */
    suspend fun submitOutcomes(outcomes: List<ReviewOutcome>): QuizResult

    /** The most recent result, or null if no quiz has been finished this session. */
    fun lastResult(): QuizResult?

    /** Words whose review is due now. */
    fun observeDueWords(): Flow<List<Word>>
}
