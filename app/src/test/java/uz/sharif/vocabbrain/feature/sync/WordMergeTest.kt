package uz.sharif.vocabbrain.feature.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uz.sharif.vocabbrain.feature.sync.domain.WordMerge
import uz.sharif.vocabbrain.feature.word.data.local.WordEntity

class WordMergeTest {

    private fun word(id: String, updatedAtMillis: Long, isLearned: Boolean = false) = WordEntity(
        id = id,
        term = id,
        phonetic = "/$id/",
        partOfSpeech = "adjective",
        translationUz = "tarjima",
        exampleSentence = "An example.",
        exampleTranslationUz = "Misol.",
        isLearned = isLearned,
        updatedAtMillis = updatedAtMillis,
    )

    @Test
    fun `words missing on one side are copied to it`() {
        val plan = WordMerge.plan(
            local = listOf(word("frugal", 10)),
            remote = listOf(word("wary", 20)),
        )

        assertThat(plan.toLocal.map { it.id }).containsExactly("wary")
        assertThat(plan.toRemote.map { it.id }).containsExactly("frugal")
    }

    @Test
    fun `the newer side wins for a word both sides know`() {
        val plan = WordMerge.plan(
            local = listOf(word("frugal", 30, isLearned = true)),
            remote = listOf(word("frugal", 20)),
        )

        assertThat(plan.toLocal).isEmpty()
        assertThat(plan.toRemote.single().isLearned).isTrue()
    }

    @Test
    fun `a newer remote row is pulled down`() {
        val plan = WordMerge.plan(
            local = listOf(word("frugal", 10)),
            remote = listOf(word("frugal", 40, isLearned = true)),
        )

        assertThat(plan.toRemote).isEmpty()
        assertThat(plan.toLocal.single().isLearned).isTrue()
    }

    @Test
    fun `identical timestamps move nothing`() {
        val plan = WordMerge.plan(
            local = listOf(word("frugal", 10)),
            remote = listOf(word("frugal", 10)),
        )

        assertThat(plan.toLocal).isEmpty()
        assertThat(plan.toRemote).isEmpty()
    }
}
