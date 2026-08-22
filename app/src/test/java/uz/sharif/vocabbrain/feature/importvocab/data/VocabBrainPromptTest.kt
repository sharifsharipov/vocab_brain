package uz.sharif.vocabbrain.feature.importvocab.data

import uz.sharif.vocabbrain.feature.importvocab.data.remote.VOCAB_BRAIN_SYSTEM_PROMPT
import uz.sharif.vocabbrain.feature.importvocab.data.remote.buildVocabBrainPrompt
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VocabBrainPromptTest {

    @Test
    fun `defaults render both question types and the standard timing`() {
        val prompt = buildVocabBrainPrompt(rawText = "  A frugal shopper.  ")

        assertThat(prompt).contains("QUESTION_COUNT: 10")
        assertThat(prompt).contains("QUESTION_TYPES: [\"MULTIPLE_CHOICE\", \"WRITING\"]")
        assertThat(prompt).contains("TIME_PER_QUESTION: 30")
        assertThat(prompt).endsWith("A frugal shopper.")
    }

    @Test
    fun `a config renders its own parameters`() {
        val config = QuizConfig(
            questionCount = 5,
            questionTypes = setOf(QuestionType.WRITING),
            timePerQuestionSeconds = 60,
        )

        val prompt = buildVocabBrainPrompt(rawText = "Be wary of cheap offers.", config = config)

        assertThat(prompt).contains("QUESTION_COUNT: 5")
        assertThat(prompt).contains("QUESTION_TYPES: [\"WRITING\"]")
        assertThat(prompt).contains("TIME_PER_QUESTION: 60")
    }

    @Test
    fun `an empty text is refused before a request is spent on it`() {
        val error = runCatching { buildVocabBrainPrompt(rawText = "   ") }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `the schema stays in the system turn, not in the per-request turn`() {
        val prompt = buildVocabBrainPrompt(rawText = "Any text")

        assertThat(VOCAB_BRAIN_SYSTEM_PROMPT).contains("\"sentence_translation_uz\"")
        assertThat(VOCAB_BRAIN_SYSTEM_PROMPT).contains("Bosh harfi: R (8 ta harf)")
        assertThat(prompt).doesNotContain("extracted_vocabulary")
    }
}
