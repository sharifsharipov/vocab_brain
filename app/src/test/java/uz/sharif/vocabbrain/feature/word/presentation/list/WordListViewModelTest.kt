package uz.sharif.vocabbrain.feature.word.presentation.list

import app.cash.turbine.test
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.feature.word.data.FakeWordRepository
import uz.sharif.vocabbrain.feature.word.domain.model.aWord
import uz.sharif.vocabbrain.feature.word.domain.usecase.ObserveWordsUseCase
import uz.sharif.vocabbrain.feature.word.domain.usecase.ToggleWordLearnedUseCase
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.Effect
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.Intent
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
class WordListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(repository: FakeWordRepository = FakeWordRepository()) =
        WordListViewModel(
            observeWords = ObserveWordsUseCase(repository),
            toggleWordLearned = ToggleWordLearnedUseCase(repository),
        )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `state - load emits the stored words`() = runTest(dispatcher) {
        createViewModel().state.test {
            val state = awaitItem()
            assertThat(state.words).isInstanceOf(AsyncData.Success::class.java)
            assertThat(state.visibleWords.map { it.term })
                .containsExactly("abundant", "candid", "diligent", "frugal")
                .inOrder()
        }
    }

    @Test
    fun `state - a failing source becomes a Failure keeping the screen renderable`() =
        runTest(dispatcher) {
            val repository = FakeWordRepository(failure = IllegalStateException("boom"))

            createViewModel(repository).state.test {
                val state = awaitItem()
                assertThat(state.words.errorOrNull()).hasMessageThat().isEqualTo("boom")
                assertThat(state.visibleWords).isEmpty()
            }
        }

    @Test
    fun `state - query filters visible words without dropping loaded data`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()

            viewModel.onIntent(Intent.QueryChanged("can"))

            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.visibleWords.map { it.term }).containsExactly("candid")
                assertThat(state.words.dataOrNull()).hasSize(4)
            }
        }

    @Test
    fun `effect - toggling learned writes through and reports it`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(Intent.LearnedToggled(aWord()))

            assertThat(awaitItem()).isEqualTo(Effect.ShowMessage("abundant marked as learned"))
            assertThat(viewModel.state.value.visibleWords.first().isLearned).isTrue()
        }
    }

    @Test
    fun `effect - clicking a word navigates and leaves state untouched`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val before = viewModel.state.value

        viewModel.effect.test {
            viewModel.onIntent(Intent.WordClicked("w-candid"))

            assertThat(awaitItem()).isEqualTo(Effect.NavigateToDetail("w-candid"))
            assertThat(viewModel.state.value).isEqualTo(before)
        }
    }
}
