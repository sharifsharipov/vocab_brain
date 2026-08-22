package uz.sharif.vocabbrain.feature.word.domain.usecase

import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository

class ToggleWordLearnedUseCase(private val repository: WordRepository) {
    suspend operator fun invoke(word: Word) =
        repository.setLearned(word.id, !word.isLearned)
}
