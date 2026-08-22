package uz.sharif.vocabbrain.feature.importvocab.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uz.sharif.vocabbrain.feature.importvocab.data.remote.VocabBrainResponseParser

class VocabBrainResponseParserTest {

    private val reply = """
        {
          "extracted_vocabulary": [
            {
              "word": "resilient",
              "phonetic": "/rɪˈzɪl.i.ənt/",
              "part_of_speech": "adjective",
              "translation_uz": "chidamli",
              "example_sentence": "The system is resilient.",
              "sentence_translation_uz": "Tizim chidamli."
            }
          ],
          "quiz_settings": { "total_questions": 1, "time_per_question_seconds": 30 },
          "questions": [
            {
              "id": 1,
              "type": "MULTIPLE_CHOICE",
              "target_word": "resilient",
              "prompt": "Ma'nosi?",
              "options": ["chidamli", "tejamkor", "eskirgan", "samimiy"],
              "correct_answer": "chidamli",
              "hint": null,
              "explanation": "Tez tiklanadigan."
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `a plain json reply is parsed`() {
        val dto = VocabBrainResponseParser.parse(reply)

        assertThat(dto.extractedVocabulary.single().translationUz).isEqualTo("chidamli")
        assertThat(dto.questions.single().correctAnswer).isEqualTo("chidamli")
    }

    @Test
    fun `a reply wrapped in a markdown fence is still parsed`() {
        val dto = VocabBrainResponseParser.parse("```json\n$reply\n```")

        assertThat(dto.quizSettings.timePerQuestionSeconds).isEqualTo(30)
    }

    @Test
    fun `an empty reply is reported as such`() {
        val error = runCatching { VocabBrainResponseParser.parse("   ") }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    }
}
