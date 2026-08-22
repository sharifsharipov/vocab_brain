package uz.sharif.vocabbrain.feature.importvocab.presentation

import app.cash.turbine.test
import uz.sharif.vocabbrain.feature.importvocab.data.remote.StubImportRemoteDataSource
import uz.sharif.vocabbrain.feature.importvocab.data.repository.VocabImportRepositoryImpl
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.AnalyzeTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.data.ocr.OcrManager
import uz.sharif.vocabbrain.feature.importvocab.data.parser.DocumentParser
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.ImportWordsUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.ExtractDocumentTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.RecognizeTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.Effect
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.Intent
import uz.sharif.vocabbrain.feature.quiz.data.remote.StubQuizRemoteDataSource
import uz.sharif.vocabbrain.feature.quiz.data.repository.QuizRepositoryImpl
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.word.data.FakeWordRepository
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

/**
 * Runs the real offline stack — stub analysis, real repositories, in-memory vocabulary —
 * so the test covers the mapping and the wiring, not just the ViewModel branches.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val wordRepository = FakeWordRepository(initialWords = emptyList())
    private val quizRepository = QuizRepositoryImpl(StubQuizRemoteDataSource(), wordRepository)
    private val importRepository = VocabImportRepositoryImpl(
        remoteDataSource = StubImportRemoteDataSource(StubQuizRemoteDataSource()),
        wordRepository = wordRepository,
        quizRepository = quizRepository,
    )

    private val text =
        "A frugal shopper stays wary of obsolete deals and keeps meticulous notes."

    private fun createViewModel() = ImportViewModel(
        analyzeText = AnalyzeTextUseCase(importRepository),
        importWords = ImportWordsUseCase(importRepository),
        recognizeText = RecognizeTextUseCase(NoOcr),
        extractDocumentText = ExtractDocumentTextUseCase(NoDocuments),
    )

    private object NoOcr : OcrManager {
        override suspend fun recognizeText(uri: android.net.Uri) =
            error("This test never reads an image")
    }

    private object NoDocuments : DocumentParser {
        override suspend fun extractText(uri: android.net.Uri) =
            error("This test never opens a document")
    }

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `state - analysis fills the word list and preselects everything`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(Intent.TextChanged(text))
        viewModel.onIntent(Intent.AnalyzeClicked)

        val state = viewModel.state.value
        assertThat(state.extractedWords.map { it.term })
            .containsExactly("frugal", "meticulous", "obsolete", "wary")
        assertThat(state.selectedTerms).containsExactlyElementsIn(state.extractedWords.map { it.term })
        assertThat(state.questionCount).isEqualTo(state.config.questionCount.coerceAtMost(4))
    }

    @Test
    fun `state - extracted words carry phonetic, part of speech and Uzbek translation`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()

            viewModel.onIntent(Intent.TextChanged(text))
            viewModel.onIntent(Intent.AnalyzeClicked)

            val word = viewModel.state.value.extractedWords.first { it.term == "frugal" }
            assertThat(word.phonetic).isEqualTo("/ˈfruː.ɡəl/")
            assertThat(word.partOfSpeech).isEqualTo("adjective")
            assertThat(word.translationUz).isEqualTo("tejamkor")
            assertThat(word.exampleTranslationUz).isNotEmpty()
        }

    @Test
    fun `state - the last question type cannot be switched off`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onIntent(Intent.QuestionTypeToggled(QuestionType.WRITING))
        viewModel.onIntent(Intent.QuestionTypeToggled(QuestionType.MULTIPLE_CHOICE))

        assertThat(viewModel.state.value.config.questionTypes)
            .containsExactly(QuestionType.MULTIPLE_CHOICE)
    }

    @Test
    fun `effect - importing stores the selected words and starts the quiz`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onIntent(Intent.TextChanged(text))
        viewModel.onIntent(Intent.AnalyzeClicked)

        viewModel.effect.test {
            viewModel.onIntent(Intent.ImportClicked)

            assertThat(awaitItem()).isEqualTo(Effect.ShowMessage("4 ta so'z qo'shildi"))
            assertThat(awaitItem()).isEqualTo(Effect.NavigateToQuiz)
        }

        assertThat(viewModel.state.value.importing.dataOrNull()).isEqualTo(4)
        assertThat(quizRepository.takeCachedQuiz()?.questions).isNotEmpty()
    }

    @Test
    fun `effect - a text with no known words reports it instead of importing`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            viewModel.onIntent(Intent.TextChanged("zzz qqq"))

            viewModel.effect.test {
                viewModel.onIntent(Intent.AnalyzeClicked)

                assertThat(awaitItem()).isEqualTo(Effect.ShowMessage("Bu matndan so'z topilmadi"))
            }
            assertThat(viewModel.state.value.canImport).isFalse()
        }
}
