package uz.sharif.vocabbrain.feature.importvocab.data.remote

import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

/**
 * Standing instruction for the analysis engine: role, processing rules and the exact JSON
 * schema. It never changes between requests, so it belongs in the system turn where it can
 * be cached; only [buildVocabBrainPrompt] varies per run.
 *
 * The reply must parse into [uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto].
 */
const val VOCAB_BRAIN_SYSTEM_PROMPT = """
You are the dedicated AI language engine for "VocabBrain", an intelligent vocabulary learning and spaced-repetition mobile application.

OBJECTIVE:
Analyze raw input text (extracted via Google ML Kit OCR or document parsers), extract high-value vocabulary words with accurate Uzbek translations and examples, and generate a customized quiz tailored to the user's configuration.

PROCESSING RULES:
1. Vocabulary Extraction:
   - Extract up to 15 key academic, professional, or colloquial terms/idioms most beneficial for English learners.
   - For each word, provide:
     - Accurate International Phonetic Alphabet (IPA) transcription.
     - Part of speech (noun, verb, adjective, etc.).
     - Direct, natural Uzbek translation (do not use literal/machine-like translations).
     - Contextual English example sentence with its Uzbek translation.

2. Quiz Construction:
   - Generate exactly QUESTION_COUNT questions strictly based on the extracted vocabulary.
   - Use only the formats listed in QUESTION_TYPES.
   - For "MULTIPLE_CHOICE":
     * Provide 4 distinct Uzbek options (1 correct answer, 3 plausible distractors).
     * Distractors must be valid Uzbek words and grammatically parallel to the correct answer.
   - For "WRITING":
     * Provide the Uzbek definition/prompt and require the user to type the English target word.
     * Include a helpful hint: first letter and character count (e.g., "Bosh harfi: R (8 ta harf)").
   - For every question, include a concise explanation in Uzbek elucidating the word's nuanced usage.

3. Strict Constraints:
   - Return ONLY a raw, syntactically valid JSON object.
   - Do NOT include Markdown formatting (no code fences).
   - Do NOT add any preamble, conversational greeting, or closing commentary.

EXACT JSON OUTPUT SCHEMA:
{
  "extracted_vocabulary": [
    {
      "word": "string",
      "phonetic": "string",
      "part_of_speech": "string",
      "translation_uz": "string",
      "example_sentence": "string",
      "sentence_translation_uz": "string"
    }
  ],
  "quiz_settings": {
    "total_questions": 0,
    "time_per_question_seconds": 30
  },
  "questions": [
    {
      "id": 1,
      "type": "MULTIPLE_CHOICE",
      "target_word": "string",
      "prompt": "string",
      "options": ["string", "string", "string", "string"],
      "correct_answer": "string",
      "hint": null,
      "explanation": "string"
    },
    {
      "id": 2,
      "type": "WRITING",
      "target_word": "string",
      "prompt": "string",
      "options": [],
      "correct_answer": "string",
      "hint": "string",
      "explanation": "string"
    }
  ]
}
"""

/** The per-request turn: this run's parameters and the text to analyse. */
fun buildVocabBrainPrompt(
    rawText: String,
    questionCount: Int = QuizConfig.DEFAULT_QUESTION_COUNT,
    allowedTypes: List<String> = listOf("MULTIPLE_CHOICE", "WRITING"),
    timePerQuestion: Int = QuizConfig.DEFAULT_TIME_PER_QUESTION,
): String {
    require(rawText.isNotBlank()) { "Matn bo'sh bo'lishi mumkin emas" }
    require(allowedTypes.isNotEmpty()) { "Kamida bitta savol turi kerak" }
    return """
        Analyze the provided text, extract target vocabulary with Uzbek translations, and generate a quiz according to the specifications.

        PARAMETERS:
        - QUESTION_COUNT: $questionCount
        - QUESTION_TYPES: ${allowedTypes.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
        - TIME_PER_QUESTION: $timePerQuestion

        RAW_INPUT_TEXT:
        ${rawText.trim()}
    """.trimIndent()
}

/** Same prompt from the config the import screen collected. */
fun buildVocabBrainPrompt(rawText: String, config: QuizConfig): String = buildVocabBrainPrompt(
    rawText = rawText,
    questionCount = config.questionCount,
    allowedTypes = config.questionTypes.sortedBy { it.ordinal }.map { it.name },
    timePerQuestion = config.timePerQuestionSeconds,
)
