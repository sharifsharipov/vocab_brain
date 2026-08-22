package uz.sharif.vocabbrain.feature.word.presentation.detail

import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.UiEffect
import uz.sharif.vocabbrain.core.mvi.UiIntent
import uz.sharif.vocabbrain.core.mvi.UiState
import uz.sharif.vocabbrain.feature.word.domain.model.Word

object WordDetailContract {

    data class State(
        val word: AsyncData<Word> = AsyncData.Uninitialized,
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Load : Intent
        data object LearnedToggled : Intent
        data object BackClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
    }
}

class WordNotFoundException(wordId: String) : NoSuchElementException("No word with id $wordId")
