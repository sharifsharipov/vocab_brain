package uz.sharif.vocabbrain.feature.word.data

import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.model.aWordList
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory double for [WordRepository]; [failure] makes every read flow throw. */
class FakeWordRepository(
    initialWords: List<Word> = aWordList(),
    private val failure: Throwable? = null,
) : WordRepository {

    private val words = MutableStateFlow(initialWords)

    override fun observeWords(): Flow<List<Word>> =
        failure?.let { error -> flow { throw error } } ?: words

    override fun observeWord(id: String): Flow<Word?> =
        failure?.let { error -> flow { throw error } }
            ?: words.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun setLearned(id: String, isLearned: Boolean) = words.update { list ->
        list.map { if (it.id == id) it.copy(isLearned = isLearned) else it }
    }

    override suspend fun addWords(newWords: List<Word>): Int {
        var added = 0
        words.update { current ->
            val knownTerms = current.mapTo(mutableSetOf()) { it.term.lowercase() }
            val accepted = newWords.filter { knownTerms.add(it.term.lowercase()) }
            added = accepted.size
            current + accepted
        }
        return added
    }
}
