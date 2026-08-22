package uz.sharif.vocabbrain.feature.word.domain.usecase

import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow

class ObserveWordsUseCase(private val repository: WordRepository) {
    operator fun invoke(): Flow<List<Word>> = repository.observeWords()
}
