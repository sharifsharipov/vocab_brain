package uz.sharif.vocabbrain.feature.word.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words ORDER BY term ASC")
    fun observeWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    fun observeWord(id: String): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun findById(id: String): WordEntity?

    @Query("SELECT * FROM words WHERE term = :term COLLATE NOCASE LIMIT 1")
    suspend fun findByTerm(term: String): WordEntity?

    /** Words whose review is due, soonest first. */
    @Query("SELECT * FROM words WHERE dueAtMillis <= :nowMillis ORDER BY dueAtMillis ASC")
    fun observeDueWords(nowMillis: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    suspend fun allWords(): List<WordEntity>

    /** Used by sync: a remote row replaces the local one wholesale. */
    @Upsert
    suspend fun upsert(words: List<WordEntity>)

    /** Import must not overwrite review progress, so existing rows are kept. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoring(words: List<WordEntity>): List<Long>

    @Query("UPDATE words SET isLearned = :isLearned, updatedAtMillis = :nowMillis WHERE id = :id")
    suspend fun setLearned(id: String, isLearned: Boolean, nowMillis: Long)

    @Query(
        """
        UPDATE words
        SET repetitions = :repetitions,
            easeFactor = :easeFactor,
            intervalDays = :intervalDays,
            dueAtMillis = :dueAtMillis,
            updatedAtMillis = :nowMillis
        WHERE id = :id
        """
    )
    suspend fun updateSchedule(
        id: String,
        repetitions: Int,
        easeFactor: Double,
        intervalDays: Int,
        dueAtMillis: Long,
        nowMillis: Long,
    )
}
