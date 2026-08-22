package uz.sharif.vocabbrain.feature.word.presentation.detail

import androidx.lifecycle.viewModelScope
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.MviViewModel
import uz.sharif.vocabbrain.feature.word.domain.usecase.ObserveWordUseCase
import uz.sharif.vocabbrain.feature.word.domain.usecase.ToggleWordLearnedUseCase
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.Effect
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.Intent
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WordDetailViewModel(
    private val wordId: String,
    private val observeWord: ObserveWordUseCase,
    private val toggleWordLearned: ToggleWordLearnedUseCase,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var observeJob: Job? = null

    init {
        onIntent(Intent.Load)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> startObservingWord()
            Intent.LearnedToggled -> currentState.word.dataOrNull()?.let { toggleWordLearned(it) }
            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
        }
    }

    private fun startObservingWord() {
        observeJob?.cancel()
        setState { copy(word = AsyncData.Loading(word.dataOrNull())) }
        observeJob = viewModelScope.launch {
            observeWord(wordId)
                .catch { error ->
                    setState { copy(word = AsyncData.Failure(error, word.dataOrNull())) }
                }
                .collect { word ->
                    setState {
                        copy(
                            word = if (word == null) AsyncData.Failure(WordNotFoundException(wordId))
                            else AsyncData.Success(word)
                        )
                    }
                }
        }
    }
}
