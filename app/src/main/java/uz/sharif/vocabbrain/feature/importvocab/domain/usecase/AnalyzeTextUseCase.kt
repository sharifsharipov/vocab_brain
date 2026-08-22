package uz.sharif.vocabbrain.feature.importvocab.domain.usecase

import uz.sharif.vocabbrain.feature.importvocab.domain.model.Analysis
import uz.sharif.vocabbrain.feature.importvocab.domain.repository.VocabImportRepository
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

class AnalyzeTextUseCase(private val repository: VocabImportRepository) {
    suspend operator fun invoke(text: String, config: QuizConfig): Analysis =
        repository.analyze(text, config)
}
