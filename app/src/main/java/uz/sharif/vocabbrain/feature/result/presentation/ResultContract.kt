package uz.sharif.vocabbrain.feature.result.presentation

import uz.sharif.vocabbrain.core.mvi.UiEffect
import uz.sharif.vocabbrain.core.mvi.UiIntent
import uz.sharif.vocabbrain.core.mvi.UiState
import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult

object ResultContract {

    data class State(
        val result: QuizResult? = null,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Load : Intent
        data object PlayAgainClicked : Intent
        data object DoneClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToQuiz : Effect
        data object NavigateToWords : Effect
    }
}
