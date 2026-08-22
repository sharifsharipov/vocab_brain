package uz.sharif.vocabbrain.feature.review.data.repository

import uz.sharif.vocabbrain.core.time.TimeProvider
import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewOutcome
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewScheduler
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewState
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewedWord
import uz.sharif.vocabbrain.feature.review.domain.repository.ReviewRepository
import uz.sharif.vocabbrain.feature.word.data.local.WordDao
import uz.sharif.vocabbrain.feature.word.data.repository.toDomain
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Schedules live on the word rows themselves, so a quiz answer is one update per word.
 * An outcome for a word that is not stored is skipped rather than failing the whole quiz.
 */
class ReviewRepositoryImpl(
    private val wordDao: WordDao,
    private val timeProvider: TimeProvider = TimeProvider.System,
) : ReviewRepository {

    private val lastResult = AtomicReference<QuizResult?>(null)

    override suspend fun submitOutcomes(outcomes: List<ReviewOutcome>): QuizResult {
        val now = timeProvider.nowMillis()
        val reviewed = outcomes.mapNotNull { outcome ->
            val word = wordDao.findByTerm(outcome.term) ?: return@mapNotNull null
            val next = ReviewScheduler.next(
                current = ReviewState(
                    repetitions = word.repetitions,
                    easeFactor = word.easeFactor,
                    intervalDays = word.intervalDays,
                    dueAtMillis = word.dueAtMillis,
                ),
                wasCorrect = outcome.wasCorrect,
                nowMillis = now,
            )
            wordDao.updateSchedule(
                id = word.id,
                repetitions = next.repetitions,
                easeFactor = next.easeFactor,
                intervalDays = next.intervalDays,
                dueAtMillis = next.dueAtMillis,
                nowMillis = now,
            )
            ReviewedWord(
                term = word.term,
                wasCorrect = outcome.wasCorrect,
                intervalDays = next.intervalDays,
                dueAtMillis = next.dueAtMillis,
            )
        }

        return QuizResult(
            total = outcomes.size,
            correct = outcomes.count { it.wasCorrect },
            reviewedWords = reviewed,
        ).also(lastResult::set)
    }

    override fun lastResult(): QuizResult? = lastResult.get()

    override fun observeDueWords(): Flow<List<Word>> =
        wordDao.observeDueWords(timeProvider.nowMillis())
            .map { entities -> entities.map { it.toDomain() } }
}
