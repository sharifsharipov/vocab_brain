package uz.sharif.vocabbrain.feature.word.presentation.detail

import app.cash.turbine.test
import uz.sharif.vocabbrain.feature.word.data.FakeWordRepository
import uz.sharif.vocabbrain.feature.word.domain.usecase.ObserveWordUseCase
import uz.sharif.vocabbrain.feature.word.domain.usecase.ToggleWordLearnedUseCase
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.Effect
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.Intent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WordDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(
        wordId: String = "w-abundant",
        repository: FakeWordRepository = FakeWordRepository(),
    ) = WordDetailViewModel(
        wordId = wordId,
        observeWord = ObserveWordUseCase(repository),
        toggleWordLearned = ToggleWordLearnedUseCase(repository),
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `state - load emits the requested word`() = runTest(dispatcher) {
        createViewModel().state.test {
            assertThat(awaitItem().word.dataOrNull()?.term).isEqualTo("abundant")
        }
    }

    @Test
    fun `state - unknown id fails with WordNotFoundException`() = runTest(dispatcher) {
        createViewModel(wordId = "w-unknown").state.test {
            assertThat(awaitItem().word.errorOrNull()).isInstanceOf(WordNotFoundException::class.java)
        }
    }

    @Test
    fun `state - toggling learned updates the observed word`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(Intent.LearnedToggled)

        assertThat(viewModel.state.value.word.dataOrNull()?.isLearned).isTrue()
    }

    @Test
    fun `effect - back click navigates back`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(Intent.BackClicked)

            assertThat(awaitItem()).isEqualTo(Effect.NavigateBack)
        }
    }
}
