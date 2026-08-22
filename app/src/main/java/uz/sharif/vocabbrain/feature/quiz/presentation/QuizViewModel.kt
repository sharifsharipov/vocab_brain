package uz.sharif.vocabbrain.feature.quiz.presentation

import androidx.lifecycle.viewModelScope
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.MviViewModel
import uz.sharif.vocabbrain.feature.quiz.domain.usecase.StartQuizUseCase
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewOutcome
import uz.sharif.vocabbrain.feature.review.domain.usecase.SubmitQuizResultUseCase
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.Effect
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.Intent
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.State
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.TIMED_OUT
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizViewModel(
    private val startQuiz: StartQuizUseCase,
    private val submitQuizResult: SubmitQuizResultUseCase,
) : MviViewModel<State, Intent, Effect>(State()) {

    private var timerJob: Job? = null

    init {
        onIntent(Intent.Load)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> load()
            is Intent.OptionSelected -> answer(intent.option)
            is Intent.WritingChanged -> setState { copy(writingDraft = intent.draft) }
            Intent.WritingSubmitted -> answer(currentState.writingDraft)
            Intent.NextClicked -> advance()

            Intent.CloseClicked -> {
                timerJob?.cancel()
                sendEffect(Effect.NavigateBack)
            }
        }
    }

    private suspend fun load() {
        setState { copy(quiz = AsyncData.Loading(quiz.dataOrNull())) }
        runCatching { startQuiz() }.fold(
            onSuccess = { quiz ->
                setState { copy(quiz = AsyncData.Success(quiz)) }
                if (quiz.questions.isEmpty()) {
                    sendEffect(Effect.ShowMessage("Hozircha savol yo'q"))
                } else {
                    startTimer()
                }
            },
            onFailure = { error ->
                setState { copy(quiz = AsyncData.Failure(error, quiz.dataOrNull())) }
            },
        )
    }

    /** First answer per question counts; re-tapping an option does not change the score. */
    private fun answer(answer: String) {
        val question = currentState.currentQuestion ?: return
        if (currentState.isCurrentAnswered) return
        timerJob?.cancel()
        setState { copy(answers = answers.put(question.id, answer)) }
    }

    private suspend fun advance() {
        if (!currentState.isCurrentAnswered) return
        timerJob?.cancel()
        if (currentState.isLastQuestion) {
            finish()
        } else {
            setState { copy(currentIndex = currentIndex + 1, writingDraft = "") }
            startTimer()
        }
    }

    /** Every answer becomes a review outcome, so the schedule reflects the whole quiz. */
    private suspend fun finish() {
        setState { copy(isSubmitting = true, secondsLeft = 0) }
        val outcomes = currentState.questions.map { question ->
            ReviewOutcome(
                term = question.targetWord,
                wasCorrect = currentState.answers[question.id]?.let(question::isCorrect) == true,
            )
        }
        runCatching { submitQuizResult(outcomes) }.fold(
            onSuccess = {
                setState { copy(isSubmitting = false) }
                sendEffect(Effect.NavigateToResult)
            },
            onFailure = { error ->
                setState { copy(isSubmitting = false) }
                sendEffect(Effect.ShowMessage(error.message ?: "Natijani saqlab bo'lmadi"))
            },
        )
    }

    /**
     * Counts the current question down. Running out records [TIMED_OUT], which reveals the
     * answer and scores as wrong — the learner still sees the explanation before moving on.
     */
    private fun startTimer() {
        timerJob?.cancel()
        val limit = currentState.timePerQuestion
        if (limit <= 0) return
        setState { copy(secondsLeft = limit) }
        timerJob = viewModelScope.launch {
            while (currentState.secondsLeft > 0) {
                delay(ONE_SECOND_MILLIS)
                setState { copy(secondsLeft = secondsLeft - 1) }
            }
            val question = currentState.currentQuestion ?: return@launch
            if (!currentState.isCurrentAnswered) {
                setState { copy(answers = answers.put(question.id, TIMED_OUT)) }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val ONE_SECOND_MILLIS = 1_000L
    }
}
