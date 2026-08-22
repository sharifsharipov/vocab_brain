package uz.sharif.vocabbrain.feature.sync.domain

import uz.sharif.vocabbrain.feature.word.data.local.WordEntity

/** What a sync run decided to write on each side. */
data class MergePlan(
    val toLocal: List<WordEntity>,
    val toRemote: List<WordEntity>,
)

/**
 * Last write wins, per word: whichever side carries the newer `updatedAtMillis` is copied
 * to the other. Words that exist on one side only are copied across unchanged.
 *
 * Deletions are not represented: nothing in the app deletes a word yet, and a missing
 * document would otherwise be indistinguishable from one that never synced.
 */
object WordMerge {

    fun plan(local: List<WordEntity>, remote: List<WordEntity>): MergePlan {
        val localById = local.associateBy { it.id }
        val remoteById = remote.associateBy { it.id }

        val toLocal = remote.filter { remoteWord ->
            val localWord = localById[remoteWord.id]
            localWord == null || remoteWord.updatedAtMillis > localWord.updatedAtMillis
        }
        val toRemote = local.filter { localWord ->
            val remoteWord = remoteById[localWord.id]
            remoteWord == null || localWord.updatedAtMillis > remoteWord.updatedAtMillis
        }
        return MergePlan(toLocal = toLocal, toRemote = toRemote)
    }
}
