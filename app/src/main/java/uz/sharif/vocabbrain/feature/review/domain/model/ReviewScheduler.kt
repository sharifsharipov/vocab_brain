package uz.sharif.vocabbrain.feature.review.domain.model

import kotlin.math.roundToInt

/**
 * SM-2 without the six-point grade: a quiz answer is only right or wrong.
 *
 * Right: the streak grows and the gap widens (1 day, 3 days, then interval × ease).
 * Wrong: the streak resets, the word comes back tomorrow, and its ease drops so it keeps
 * showing up more often than words the learner knows.
 */
object ReviewScheduler {

    const val MIN_EASE_FACTOR = 1.3
    const val MAX_EASE_FACTOR = 2.8
    private const val EASE_BONUS = 0.1
    private const val EASE_PENALTY = 0.2
    private const val FIRST_INTERVAL_DAYS = 1
    private const val SECOND_INTERVAL_DAYS = 3
    const val DAY_MILLIS = 24L * 60 * 60 * 1000

    fun next(current: ReviewState, wasCorrect: Boolean, nowMillis: Long): ReviewState {
        if (!wasCorrect) {
            return ReviewState(
                repetitions = 0,
                easeFactor = (current.easeFactor - EASE_PENALTY).coerceAtLeast(MIN_EASE_FACTOR),
                intervalDays = FIRST_INTERVAL_DAYS,
                dueAtMillis = nowMillis + FIRST_INTERVAL_DAYS * DAY_MILLIS,
            )
        }
        val repetitions = current.repetitions + 1
        val intervalDays = when (repetitions) {
            1 -> FIRST_INTERVAL_DAYS
            2 -> SECOND_INTERVAL_DAYS
            else -> (current.intervalDays * current.easeFactor).roundToInt()
                .coerceAtLeast(SECOND_INTERVAL_DAYS)
        }
        return ReviewState(
            repetitions = repetitions,
            easeFactor = (current.easeFactor + EASE_BONUS).coerceAtMost(MAX_EASE_FACTOR),
            intervalDays = intervalDays,
            dueAtMillis = nowMillis + intervalDays * DAY_MILLIS,
        )
    }
}
