package uz.sharif.vocabbrain.feature.sync.domain

import uz.sharif.vocabbrain.feature.sync.data.AuthManager
import uz.sharif.vocabbrain.feature.sync.data.FirestoreWordDataSource
import uz.sharif.vocabbrain.feature.word.data.local.WordDao

/**
 * Two-way sync of the vocabulary. Room stays the source the UI reads, so a failed sync
 * leaves the app fully usable offline — the next run picks the changes up again.
 */
class SyncWordsUseCase(
    private val authManager: AuthManager,
    private val remoteDataSource: FirestoreWordDataSource,
    private val wordDao: WordDao,
) {
    suspend operator fun invoke(): Result<MergePlan> = runCatching {
        val uid = authManager.requireUid()
        val plan = WordMerge.plan(local = wordDao.allWords(), remote = remoteDataSource.fetchAll(uid))
        if (plan.toLocal.isNotEmpty()) wordDao.upsert(plan.toLocal)
        if (plan.toRemote.isNotEmpty()) remoteDataSource.push(uid, plan.toRemote)
        plan
    }
}
