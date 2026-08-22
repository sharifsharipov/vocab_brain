package uz.sharif.vocabbrain.feature.quiz.presentation

import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.UiEffect
import uz.sharif.vocabbrain.core.mvi.UiIntent
import uz.sharif.vocabbrain.core.mvi.UiState
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizQuestion
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

object QuizContract {

    /** Recorded when the timer runs out: an answer that exists but matches nothing. */
    const val TIMED_OUT = ""

    data class State(
        val quiz: AsyncData<Quiz> = AsyncData.Uninitialized,
        val currentIndex: Int = 0,
        /** Question id to the answer given. An answered question cannot be changed. */
        val answers: PersistentMap<Int, String> = persistentMapOf(),
        val writingDraft: String = "",
        val secondsLeft: Int = 0,
        val isSubmitting: Boolean = false,
    ) : UiState {

        val questions: List<QuizQuestion> get() = quiz.dataOrNull()?.questions.orEmpty()

        val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)

        val answerForCurrent: String? get() = currentQuestion?.let { answers[it.id] }

        val isCurrentAnswered: Boolean get() = answerForCurrent != null

        val isCurrentTimedOut: Boolean get() = answerForCurrent == TIMED_OUT

        val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex

        val questionNumber: Int get() = currentIndex + 1

        val timePerQuestion: Int get() = quiz.dataOrNull()?.settings?.timePerQuestionSeconds ?: 0

        val score: Int
            get() = questions.count { question ->
                answers[question.id]?.let(question::isCorrect) == true
            }
    }

    sealed interface Intent : UiIntent {
        data object Load : Intent
        data class OptionSelected(val option: String) : Intent
        data class WritingChanged(val draft: String) : Intent
        data object WritingSubmitted : Intent
        data object NextClicked : Intent
        data object CloseClicked : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToResult : Effect
        data object NavigateBack : Effect
        data class ShowMessage(val text: String) : Effect
    }
}
