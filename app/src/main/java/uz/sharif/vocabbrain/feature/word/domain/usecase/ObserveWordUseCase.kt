package uz.sharif.vocabbrain.feature.word.domain.usecase

import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow

class ObserveWordUseCase(private val repository: WordRepository) {
    operator fun invoke(id: String): Flow<Word?> = repository.observeWord(id)
}
