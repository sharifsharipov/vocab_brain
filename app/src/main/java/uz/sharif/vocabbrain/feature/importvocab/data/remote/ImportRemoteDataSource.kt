package uz.sharif.vocabbrain.feature.importvocab.data.remote

import uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

/**
 * Boundary to the analysis engine: raw text in, vocabulary plus a ready quiz out.
 * A real client sends [VOCAB_BRAIN_SYSTEM_PROMPT] plus [buildVocabBrainPrompt] and parses
 * the reply into [VocabBrainDto].
 */
interface ImportRemoteDataSource {
    suspend fun analyze(rawText: String, config: QuizConfig): VocabBrainDto
}
