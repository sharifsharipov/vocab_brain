package uz.sharif.vocabbrain.feature.importvocab.data

import uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto
import uz.sharif.vocabbrain.feature.importvocab.data.repository.toDomain
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Pins the wire contract: this is the JSON the language model is told to return, so if a
 * field name or shape drifts, this test fails before the app sees a parse error at runtime.
 */
class VocabBrainDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val response = """
        {
          "extracted_vocabulary": [
            {
              "word": "resilient",
              "phonetic": "/rɪˈzɪl.i.ənt/",
              "part_of_speech": "adjective",
              "translation_uz": "chidamli",
              "example_sentence": "The system is resilient to failure.",
              "sentence_translation_uz": "Tizim nosozliklarga chidamli."
            }
          ],
          "quiz_settings": {
            "total_questions": 2,
            "time_per_question_seconds": 30
          },
          "questions": [
            {
              "id": 1,
              "type": "MULTIPLE_CHOICE",
              "target_word": "resilient",
              "prompt": "\"resilient\" so'zining ma'nosi?",
              "options": ["chidamli", "tejamkor", "eskirgan", "samimiy"],
              "correct_answer": "chidamli",
              "hint": null,
              "explanation": "Qiyinchilikdan tez tiklanadigan ma'nosida."
            },
            {
              "id": 2,
              "type": "WRITING",
              "target_word": "resilient",
              "prompt": "\"chidamli\" so'zining inglizchasini yozing.",
              "options": [],
              "correct_answer": "resilient",
              "hint": "Bosh harfi: R (9 ta harf)",
              "explanation": "Ko'pincha tizim va odam haqida ishlatiladi."
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `snake case payload maps onto the dto`() {
        val dto = json.decodeFromString<VocabBrainDto>(response)

        val word = dto.extractedVocabulary.single()
        assertThat(word.word).isEqualTo("resilient")
        assertThat(word.partOfSpeech).isEqualTo("adjective")
        assertThat(word.translationUz).isEqualTo("chidamli")
        assertThat(word.sentenceTranslationUz).isEqualTo("Tizim nosozliklarga chidamli.")
        assertThat(dto.quizSettings.timePerQuestionSeconds).isEqualTo(30)
    }

    @Test
    fun `both question types survive the mapping to domain`() {
        val analysis = json.decodeFromString<VocabBrainDto>(response).toDomain()

        val (multipleChoice, writing) = analysis.quiz.questions
        assertThat(multipleChoice.type).isEqualTo(QuestionType.MULTIPLE_CHOICE)
        assertThat(multipleChoice.options).hasSize(4)
        assertThat(multipleChoice.hint).isNull()
        assertThat(writing.type).isEqualTo(QuestionType.WRITING)
        assertThat(writing.options).isEmpty()
        assertThat(writing.hint).isEqualTo("Bosh harfi: R (9 ta harf)")
        assertThat(analysis.words.single().exampleTranslationUz)
            .isEqualTo("Tizim nosozliklarga chidamli.")
    }

    @Test
    fun `an unknown question type is rejected with the offending value`() {
        val broken = response.replace("\"MULTIPLE_CHOICE\"", "\"MATCHING\"")

        val error = runCatching { json.decodeFromString<VocabBrainDto>(broken).toDomain() }
            .exceptionOrNull()

        assertThat(error).hasMessageThat().contains("MATCHING")
    }
}
