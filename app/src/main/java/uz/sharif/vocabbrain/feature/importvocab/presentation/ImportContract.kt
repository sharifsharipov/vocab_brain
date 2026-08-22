package uz.sharif.vocabbrain.feature.importvocab.presentation

import android.net.Uri
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.UiEffect
import uz.sharif.vocabbrain.core.mvi.UiIntent
import uz.sharif.vocabbrain.core.mvi.UiState
import uz.sharif.vocabbrain.feature.importvocab.domain.model.Analysis
import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

object ImportContract {

    data class State(
        val text: String = "",
        val config: QuizConfig = QuizConfig(),
        val analysis: AsyncData<Analysis> = AsyncData.Uninitialized,
        val selectedTerms: PersistentSet<String> = persistentSetOf(),
        val importing: AsyncData<Int> = AsyncData.Uninitialized,
        val isReadingSource: Boolean = false,
    ) : UiState {

        val extractedWords: List<ExtractedWord> get() = analysis.dataOrNull()?.words.orEmpty()

        val selectedWords: List<ExtractedWord>
            get() = extractedWords.filter { it.term in selectedTerms }

        val questionCount: Int get() = analysis.dataOrNull()?.quiz?.questions?.size ?: 0

        val canAnalyze: Boolean get() = text.isNotBlank() && !analysis.isLoading && !isReadingSource

        val canImport: Boolean get() = selectedWords.isNotEmpty() && !importing.isLoading
    }

    sealed interface Intent : UiIntent {
        data class TextChanged(val text: String) : Intent
        data class ImageSelected(val uri: Uri) : Intent
        data class DocumentSelected(val uri: Uri) : Intent
        data class QuestionCountChanged(val count: Int) : Intent
        data class QuestionTypeToggled(val type: QuestionType) : Intent
        data class TimePerQuestionChanged(val seconds: Int) : Intent
        data object AnalyzeClicked : Intent
        data class WordToggled(val term: String) : Intent
        data object ImportClicked : Intent
        data object BackClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowMessage(val text: String) : Effect
        data object NavigateToQuiz : Effect
        data object NavigateBack : Effect
    }
}
