package uz.sharif.vocabbrain.feature.importvocab.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.feature.importvocab.domain.model.Analysis
import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.State
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import uz.sharif.vocabbrain.feature.quiz.presentation.aQuiz
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet

class ImportStateProvider : PreviewParameterProvider<State> {
    override val values: Sequence<State>
        get() = sequenceOf(
            State(),
            anImportState(),
            anImportState(selectedTerms = persistentSetOf("frugal")),
            anImportState(analysis = AsyncData.Loading()),
            anImportState(analysis = AsyncData.Failure(Exception("Tahlil xizmati ishlamayapti"))),
            anImportState(importing = AsyncData.Loading()),
        )
}

fun anImportState(
    text: String = "A frugal shopper stays wary of obsolete deals.",
    config: QuizConfig = QuizConfig(),
    analysis: AsyncData<Analysis> = AsyncData.Success(anAnalysis()),
    selectedTerms: PersistentSet<String> = anAnalysis().words.mapTo(mutableSetOf()) { it.term }.toPersistentSet(),
    importing: AsyncData<Int> = AsyncData.Uninitialized,
) = State(
    text = text,
    config = config,
    analysis = analysis,
    selectedTerms = selectedTerms,
    importing = importing,
)

fun anAnalysis() = Analysis(words = anExtractedWordList(), quiz = aQuiz())

fun anExtractedWordList() = listOf(
    ExtractedWord(
        term = "frugal",
        phonetic = "/ˈfruː.ɡəl/",
        partOfSpeech = "adjective",
        translationUz = "tejamkor",
        exampleSentence = "A frugal shopper compares prices.",
        exampleTranslationUz = "Tejamkor xaridor narxlarni solishtiradi.",
    ),
    ExtractedWord(
        term = "wary",
        phonetic = "/ˈweə.ri/",
        partOfSpeech = "adjective",
        translationUz = "ehtiyotkor",
        exampleSentence = "Be wary of cheap offers.",
        exampleTranslationUz = "Arzon takliflarga nisbatan ehtiyotkor bo'ling.",
    ),
    ExtractedWord(
        term = "obsolete",
        phonetic = "/ˈɒb.sə.liːt/",
        partOfSpeech = "adjective",
        translationUz = "eskirgan",
        exampleSentence = "That format is now obsolete.",
        exampleTranslationUz = "Bu format endi eskirgan.",
    ),
)
