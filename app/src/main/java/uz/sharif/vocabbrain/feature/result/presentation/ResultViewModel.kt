package uz.sharif.vocabbrain.feature.result.presentation

import uz.sharif.vocabbrain.core.mvi.MviViewModel
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.Effect
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.Intent
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.State
import uz.sharif.vocabbrain.feature.review.domain.usecase.GetLastResultUseCase

class ResultViewModel(
    private val getLastResult: GetLastResultUseCase,
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        onIntent(Intent.Load)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            // The quiz stored its result before navigating here; nothing to load remotely.
            Intent.Load -> setState { copy(result = getLastResult()) }
            Intent.PlayAgainClicked -> sendEffect(Effect.NavigateToQuiz)
            Intent.DoneClicked -> sendEffect(Effect.NavigateToWords)
        }
    }
}
