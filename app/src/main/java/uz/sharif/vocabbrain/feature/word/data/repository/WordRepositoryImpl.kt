package uz.sharif.vocabbrain.feature.word.data.repository

import uz.sharif.vocabbrain.core.time.TimeProvider
import uz.sharif.vocabbrain.feature.word.data.local.WordDao
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WordRepositoryImpl(
    private val wordDao: WordDao,
    private val timeProvider: TimeProvider = TimeProvider.System,
) : WordRepository {

    override fun observeWords(): Flow<List<Word>> =
        wordDao.observeWords().map { entities -> entities.map { it.toDomain() } }

    override fun observeWord(id: String): Flow<Word?> =
        wordDao.observeWord(id).map { it?.toDomain() }

    override suspend fun setLearned(id: String, isLearned: Boolean) =
        wordDao.setLearned(id, isLearned, timeProvider.nowMillis())

    /** Rows that already exist keep their review progress, so re-importing is harmless. */
    override suspend fun addWords(words: List<Word>): Int {
        val now = timeProvider.nowMillis()
        val rows = words.map { it.toEntity().copy(updatedAtMillis = now) }
        return wordDao.insertIgnoring(rows).count { it != -1L }
    }
}
