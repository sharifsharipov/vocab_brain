package uz.sharif.vocabbrain.feature.word.presentation.list

import androidx.lifecycle.viewModelScope
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.MviViewModel
import uz.sharif.vocabbrain.feature.word.domain.usecase.ObserveWordsUseCase
import uz.sharif.vocabbrain.feature.word.domain.usecase.ToggleWordLearnedUseCase
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.Effect
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.Intent
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.State
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WordListViewModel(
    private val observeWords: ObserveWordsUseCase,
    private val toggleWordLearned: ToggleWordLearnedUseCase,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var observeJob: Job? = null

    init {
        onIntent(Intent.Load)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> startObservingWords()
            is Intent.QueryChanged -> setState { copy(query = intent.query) }
            is Intent.WordClicked -> sendEffect(Effect.NavigateToDetail(intent.wordId))
            Intent.ImportClicked -> sendEffect(Effect.NavigateToImport)
            Intent.QuizClicked -> sendEffect(Effect.NavigateToQuiz)
            is Intent.LearnedToggled -> {
                toggleWordLearned(intent.word)
                val message =
                    if (intent.word.isLearned) "${intent.word.term} moved back to learning"
                    else "${intent.word.term} marked as learned"
                sendEffect(Effect.ShowMessage(message))
            }
        }
    }

    private fun startObservingWords() {
        observeJob?.cancel()
        setState { copy(words = AsyncData.Loading(words.dataOrNull())) }
        observeJob = viewModelScope.launch {
            observeWords()
                .catch { error ->
                    setState { copy(words = AsyncData.Failure(error, words.dataOrNull())) }
                }
                .collect { words ->
                    setState { copy(words = AsyncData.Success(words.toImmutableList())) }
                }
        }
    }
}
