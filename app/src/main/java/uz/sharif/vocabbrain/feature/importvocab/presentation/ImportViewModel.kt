package uz.sharif.vocabbrain.feature.importvocab.presentation

import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.mvi.MviViewModel
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.AnalyzeTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.ExtractDocumentTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.ImportWordsUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.RecognizeTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.Effect
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.Intent
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.State
import kotlinx.collections.immutable.toPersistentSet

class ImportViewModel(
    private val analyzeText: AnalyzeTextUseCase,
    private val importWords: ImportWordsUseCase,
    private val recognizeText: RecognizeTextUseCase,
    private val extractDocumentText: ExtractDocumentTextUseCase,
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.TextChanged -> setState { copy(text = intent.text) }

            is Intent.ImageSelected -> readSource(
                failureMessage = "Rasmni o'qib bo'lmadi",
                emptyMessage = "Rasmdan matn topilmadi",
            ) { recognizeText(intent.uri) }

            is Intent.DocumentSelected -> readSource(
                failureMessage = "Faylni o'qib bo'lmadi",
                emptyMessage = "Hujjat ichida matn topilmadi",
            ) { extractDocumentText(intent.uri) }

            is Intent.QuestionCountChanged -> setState {
                copy(config = config.copy(questionCount = intent.count))
            }

            is Intent.TimePerQuestionChanged -> setState {
                copy(config = config.copy(timePerQuestionSeconds = intent.seconds))
            }

            // The last remaining type cannot be switched off: a quiz needs at least one.
            is Intent.QuestionTypeToggled -> setState {
                val types = config.questionTypes
                val updated = if (intent.type in types) types - intent.type else types + intent.type
                if (updated.isEmpty()) this else copy(config = config.copy(questionTypes = updated))
            }

            is Intent.WordToggled -> setState {
                copy(
                    selectedTerms = if (intent.term in selectedTerms) selectedTerms.remove(intent.term)
                    else selectedTerms.add(intent.term)
                )
            }

            Intent.AnalyzeClicked -> analyze()
            Intent.ImportClicked -> import()
            Intent.BackClicked -> sendEffect(Effect.NavigateBack)
        }
    }

    /**
     * Text from a picture or a document is appended, so several sources can feed one import.
     */
    private suspend fun readSource(
        failureMessage: String,
        emptyMessage: String,
        read: suspend () -> String,
    ) {
        if (currentState.isReadingSource) return
        setState { copy(isReadingSource = true) }
        runCatching { read() }.fold(
            onSuccess = { extracted ->
                setState {
                    copy(
                        isReadingSource = false,
                        text = if (text.isBlank()) extracted else "$text\n$extracted",
                    )
                }
                if (extracted.isBlank()) sendEffect(Effect.ShowMessage(emptyMessage))
            },
            onFailure = { error ->
                setState { copy(isReadingSource = false) }
                sendEffect(Effect.ShowMessage(error.message ?: failureMessage))
            },
        )
    }

    private suspend fun analyze() {
        if (!currentState.canAnalyze) return
        setState { copy(analysis = AsyncData.Loading(analysis.dataOrNull())) }
        runCatching { analyzeText(currentState.text, currentState.config) }.fold(
            onSuccess = { analysis ->
                // Everything found is pre-selected: the common case is importing all of it.
                setState {
                    copy(
                        analysis = AsyncData.Success(analysis),
                        selectedTerms = analysis.words.mapTo(mutableSetOf()) { it.term }.toPersistentSet(),
                    )
                }
                if (analysis.words.isEmpty()) {
                    sendEffect(Effect.ShowMessage("Bu matndan so'z topilmadi"))
                }
            },
            onFailure = { error ->
                setState { copy(analysis = AsyncData.Failure(error, analysis.dataOrNull())) }
                sendEffect(Effect.ShowMessage(error.message ?: "Tahlil qilib bo'lmadi"))
            },
        )
    }

    private suspend fun import() {
        val words = currentState.selectedWords
        val quiz = currentState.analysis.dataOrNull()?.quiz ?: return
        if (words.isEmpty() || currentState.importing.isLoading) return

        setState { copy(importing = AsyncData.Loading(importing.dataOrNull())) }
        runCatching { importWords(words, quiz) }.fold(
            onSuccess = { added ->
                setState { copy(importing = AsyncData.Success(added)) }
                val skipped = words.size - added
                sendEffect(
                    Effect.ShowMessage(
                        if (skipped == 0) "$added ta so'z qo'shildi"
                        else "$added ta so'z qo'shildi, $skipped tasi allaqachon bor edi"
                    )
                )
                val importedTerms = words.mapTo(mutableSetOf()) { it.term }
                val hasQuestions = quiz.questions.any { it.targetWord in importedTerms }
                sendEffect(if (hasQuestions) Effect.NavigateToQuiz else Effect.NavigateBack)
            },
            onFailure = { error ->
                setState { copy(importing = AsyncData.Failure(error, importing.dataOrNull())) }
                sendEffect(Effect.ShowMessage(error.message ?: "Saqlab bo'lmadi"))
            },
        )
    }
}
