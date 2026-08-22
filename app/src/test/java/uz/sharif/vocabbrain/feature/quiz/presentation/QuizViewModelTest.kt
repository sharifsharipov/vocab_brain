package uz.sharif.vocabbrain.feature.quiz.presentation

import app.cash.turbine.test
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import uz.sharif.vocabbrain.feature.quiz.domain.repository.QuizRepository
import uz.sharif.vocabbrain.feature.quiz.domain.usecase.StartQuizUseCase
import uz.sharif.vocabbrain.feature.review.domain.model.QuizResult
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewOutcome
import uz.sharif.vocabbrain.feature.review.domain.repository.ReviewRepository
import uz.sharif.vocabbrain.feature.review.domain.usecase.SubmitQuizResultUseCase
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.Effect
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.Intent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeQuizRepository()
    private val reviewRepository = FakeReviewRepository()

    private fun createViewModel() = QuizViewModel(
        startQuiz = StartQuizUseCase(repository),
        submitQuizResult = SubmitQuizResultUseCase(reviewRepository),
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `state - the quiz starts on the first question with a full clock`() = runTest(dispatcher) {
        val state = createViewModel().state.value

        assertThat(state.questionNumber).isEqualTo(1)
        assertThat(state.secondsLeft).isEqualTo(30)
        assertThat(state.isCurrentAnswered).isFalse()
    }

    @Test
    fun `state - a correct multiple choice answer scores and reveals`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(Intent.OptionSelected("tejamkor"))

        assertThat(viewModel.state.value.isCurrentAnswered).isTrue()
        assertThat(viewModel.state.value.score).isEqualTo(1)
    }

    @Test
    fun `state - the first answer is final`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(Intent.OptionSelected("eskirgan"))
        viewModel.onIntent(Intent.OptionSelected("tejamkor"))

        assertThat(viewModel.state.value.answerForCurrent).isEqualTo("eskirgan")
        assertThat(viewModel.state.value.score).isEqualTo(0)
    }

    @Test
    fun `state - a writing answer ignores case and spaces`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onIntent(Intent.OptionSelected("tejamkor"))
        viewModel.onIntent(Intent.NextClicked)

        viewModel.onIntent(Intent.WritingChanged("  Wary "))
        viewModel.onIntent(Intent.WritingSubmitted)

        assertThat(viewModel.state.value.score).isEqualTo(2)
    }

    @Test
    fun `state - running out of time answers the question as missed`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        advanceTimeBy(31_000)

        val state = viewModel.state.value
        assertThat(state.secondsLeft).isEqualTo(0)
        assertThat(state.isCurrentTimedOut).isTrue()
        assertThat(state.score).isEqualTo(0)
    }

    @Test
    fun `state - answering stops the clock`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(Intent.OptionSelected("tejamkor"))
        val remaining = viewModel.state.value.secondsLeft
        advanceTimeBy(10_000)

        assertThat(viewModel.state.value.secondsLeft).isEqualTo(remaining)
    }

    @Test
    fun `effect - the last question submits every answer and opens the result`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            viewModel.onIntent(Intent.OptionSelected("eskirgan"))
            viewModel.onIntent(Intent.NextClicked)
            viewModel.onIntent(Intent.WritingChanged("wary"))
            viewModel.onIntent(Intent.WritingSubmitted)

            viewModel.effect.test {
                viewModel.onIntent(Intent.NextClicked)

                assertThat(awaitItem()).isEqualTo(Effect.NavigateToResult)
            }

            assertThat(reviewRepository.submitted).containsExactly(
                ReviewOutcome(term = "frugal", wasCorrect = false),
                ReviewOutcome(term = "wary", wasCorrect = true),
            ).inOrder()
        }

    @Test
    fun `state - a cached quiz is played once, then a fresh one is generated`() =
        runTest(dispatcher) {
            repository.cacheQuiz(aQuiz())
            createViewModel()

            assertThat(repository.generatedConfigs).isEmpty()

            createViewModel()
            assertThat(repository.generatedConfigs).hasSize(1)
        }

    @Test
    fun `effect - closing navigates back`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(Intent.CloseClicked)

            assertThat(awaitItem()).isEqualTo(Effect.NavigateBack)
        }
    }
}

private class FakeReviewRepository : ReviewRepository {

    val submitted = mutableListOf<ReviewOutcome>()
    private var last: QuizResult? = null

    override suspend fun submitOutcomes(outcomes: List<ReviewOutcome>): QuizResult {
        submitted += outcomes
        return QuizResult(
            total = outcomes.size,
            correct = outcomes.count { it.wasCorrect },
            reviewedWords = emptyList(),
        ).also { last = it }
    }

    override fun lastResult(): QuizResult? = last

    override fun observeDueWords(): Flow<List<Word>> = flowOf(emptyList())
}

private class FakeQuizRepository(private val quiz: Quiz = aQuiz()) : QuizRepository {

    val generatedConfigs = mutableListOf<QuizConfig>()
    private var cached: Quiz? = null

    override suspend fun generateQuiz(config: QuizConfig): Quiz {
        generatedConfigs += config
        return quiz
    }

    override fun cacheQuiz(quiz: Quiz) {
        cached = quiz
    }

    override fun takeCachedQuiz(): Quiz? = cached.also { cached = null }
}
