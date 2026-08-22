package uz.sharif.vocabbrain.feature.review.domain.model

/** What a quiz says about one word. */
data class ReviewOutcome(
    val term: String,
    val wasCorrect: Boolean,
)

/** Where a word landed after the quiz updated its schedule. */
data class ReviewedWord(
    val term: String,
    val wasCorrect: Boolean,
    val intervalDays: Int,
    val dueAtMillis: Long,
)

data class QuizResult(
    val total: Int,
    val correct: Int,
    val reviewedWords: List<ReviewedWord>,
) {
    val wrong: Int get() = total - correct
    val accuracyPercent: Int get() = if (total == 0) 0 else correct * 100 / total
}

/** Spaced-repetition state of one word. */
data class ReviewState(
    val repetitions: Int,
    val easeFactor: Double,
    val intervalDays: Int,
    val dueAtMillis: Long,
)
