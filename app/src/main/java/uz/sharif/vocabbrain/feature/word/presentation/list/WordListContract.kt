package uz.sharif.vocabbrain.feature.word.presentation.list

import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.UiEffect
import uz.sharif.vocabbrain.core.mvi.UiIntent
import uz.sharif.vocabbrain.core.mvi.UiState
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** The whole contract of one screen: what it shows, what it accepts, what it emits. */
object WordListContract {

    data class State(
        val words: AsyncData<ImmutableList<Word>> = AsyncData.Uninitialized,
        val query: String = "",
    ) : UiState {
        /** Loaded words matching [query]; stale data stays visible while reloading. */
        val visibleWords: ImmutableList<Word>
            get() {
                val loaded = words.dataOrNull() ?: return persistentListOf()
                if (query.isBlank()) return loaded
                val needle = query.trim()
                return loaded.filter {
                    it.term.contains(needle, ignoreCase = true) ||
                        it.translationUz.contains(needle, ignoreCase = true)
                }.toImmutableList()
            }
    }

    sealed interface Intent : UiIntent {
        data object Load : Intent
        data class QueryChanged(val query: String) : Intent
        data class WordClicked(val wordId: String) : Intent
        data class LearnedToggled(val word: Word) : Intent
        data object ImportClicked : Intent
        data object QuizClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val wordId: String) : Effect
        data class ShowMessage(val text: String) : Effect
        data object NavigateToImport : Effect
        data object NavigateToQuiz : Effect
    }
}
