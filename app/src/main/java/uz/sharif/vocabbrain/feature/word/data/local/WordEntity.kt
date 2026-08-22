package uz.sharif.vocabbrain.feature.word.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Row of the vocabulary table. Besides the word itself it carries the spaced-repetition
 * state, so scheduling never needs a second table joined to this one.
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: String,
    val term: String,
    val phonetic: String,
    val partOfSpeech: String,
    val translationUz: String,
    val exampleSentence: String,
    val exampleTranslationUz: String,
    val isLearned: Boolean,
    /** How many times in a row the word was answered correctly. */
    val repetitions: Int = 0,
    /** SM-2 ease factor; lower means the word comes back sooner. */
    val easeFactor: Double = DEFAULT_EASE_FACTOR,
    val intervalDays: Int = 0,
    /** When the word is next due, in epoch milliseconds. */
    val dueAtMillis: Long = 0L,
    /** Last local change, in epoch milliseconds. Sync keeps the newer side. */
    val updatedAtMillis: Long = 0L,
) {
    companion object {
        const val DEFAULT_EASE_FACTOR = 2.5
    }
}
