package uz.sharif.vocabbrain.feature.importvocab.domain.usecase

import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.importvocab.domain.repository.VocabImportRepository
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz

class ImportWordsUseCase(private val repository: VocabImportRepository) {
    suspend operator fun invoke(words: List<ExtractedWord>, quiz: Quiz): Int =
        repository.import(words, quiz)
}
