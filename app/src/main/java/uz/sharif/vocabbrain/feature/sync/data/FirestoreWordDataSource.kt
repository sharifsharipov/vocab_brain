package uz.sharif.vocabbrain.feature.sync.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import uz.sharif.vocabbrain.feature.word.data.local.WordEntity

/** One document per word under `users/{uid}/words`, mirroring the local row. */
class FirestoreWordDataSource(private val firestore: FirebaseFirestore) {

    suspend fun fetchAll(uid: String): List<WordEntity> =
        wordsOf(uid).get().await().documents.mapNotNull { it.toWordEntity() }

    /** Firestore batches take at most 500 writes, so large vocabularies go up in chunks. */
    suspend fun push(uid: String, words: List<WordEntity>) {
        words.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { word -> batch.set(wordsOf(uid).document(word.id), word.toMap()) }
            batch.commit().await()
        }
    }

    private fun wordsOf(uid: String) =
        firestore.collection("users").document(uid).collection("words")

    private fun WordEntity.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "term" to term,
        "phonetic" to phonetic,
        "partOfSpeech" to partOfSpeech,
        "translationUz" to translationUz,
        "exampleSentence" to exampleSentence,
        "exampleTranslationUz" to exampleTranslationUz,
        "isLearned" to isLearned,
        "repetitions" to repetitions,
        "easeFactor" to easeFactor,
        "intervalDays" to intervalDays,
        "dueAtMillis" to dueAtMillis,
        "updatedAtMillis" to updatedAtMillis,
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toWordEntity(): WordEntity? {
        val term = getString("term") ?: return null
        return WordEntity(
            id = getString("id") ?: id,
            term = term,
            phonetic = getString("phonetic").orEmpty(),
            partOfSpeech = getString("partOfSpeech").orEmpty(),
            translationUz = getString("translationUz").orEmpty(),
            exampleSentence = getString("exampleSentence").orEmpty(),
            exampleTranslationUz = getString("exampleTranslationUz").orEmpty(),
            isLearned = getBoolean("isLearned") ?: false,
            repetitions = getLong("repetitions")?.toInt() ?: 0,
            easeFactor = getDouble("easeFactor") ?: WordEntity.DEFAULT_EASE_FACTOR,
            intervalDays = getLong("intervalDays")?.toInt() ?: 0,
            dueAtMillis = getLong("dueAtMillis") ?: 0L,
            updatedAtMillis = getLong("updatedAtMillis") ?: 0L,
        )
    }

    private companion object {
        const val BATCH_LIMIT = 500
    }
}
