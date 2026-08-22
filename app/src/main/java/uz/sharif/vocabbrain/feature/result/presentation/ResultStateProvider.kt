package uz.sharif.vocabbrain.feature.result.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.State
import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewScheduler
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewedWord

class ResultStateProvider : PreviewParameterProvider<State> {
    override val values: Sequence<State>
        get() = sequenceOf(
            State(result = aQuizResult()),
            State(result = aQuizResult(correct = 0)),
            State(result = null),
        )
}

fun aQuizResult(
    total: Int = 2,
    correct: Int = 1,
    nowMillis: Long = 0L,
) = QuizResult(
    total = total,
    correct = correct,
    reviewedWords = listOf(
        ReviewedWord(
            term = "frugal",
            wasCorrect = correct > 0,
            intervalDays = 3,
            dueAtMillis = nowMillis + 3 * ReviewScheduler.DAY_MILLIS,
        ),
        ReviewedWord(
            term = "wary",
            wasCorrect = false,
            intervalDays = 1,
            dueAtMillis = nowMillis + ReviewScheduler.DAY_MILLIS,
        ),
    ),
)
