package uz.sharif.vocabbrain.feature.word.domain.repository

import uz.sharif.vocabbrain.feature.word.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun observeWords(): Flow<List<Word>>
    fun observeWord(id: String): Flow<Word?>
    suspend fun setLearned(id: String, isLearned: Boolean)

    /** Adds words, ignoring any whose term already exists. Returns how many were stored. */
    suspend fun addWords(words: List<Word>): Int
}
