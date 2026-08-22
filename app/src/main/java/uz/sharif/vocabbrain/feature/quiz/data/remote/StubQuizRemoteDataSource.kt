package uz.sharif.vocabbrain.feature.quiz.data.remote

import uz.sharif.vocabbrain.feature.quiz.data.model.QuizDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizQuestionDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSettingsDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSourceWordDto
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

/**
 * Offline stand-in for the question generator, producing the same JSON shape the model
 * is asked for. Deterministic on purpose: the same vocabulary always yields the same
 * quiz, so previews and tests stay stable.
 *
 * Two known gaps against the real generator, both harmless while offline: distractors are
 * other words' translations rather than model-picked near-misses, and fewer questions than
 * requested are returned when the vocabulary is small (the returned settings say how many).
 */
class StubQuizRemoteDataSource : QuizRemoteDataSource {

    override suspend fun generateQuiz(
        words: List<QuizSourceWordDto>,
        config: QuizConfig,
    ): QuizDto {
        require(words.size >= MIN_WORDS) {
            "Kamida $MIN_WORDS ta so'z kerak, hozir ${words.size} ta bor"
        }
        val types = config.questionTypes.sortedBy { it.ordinal }
        val questions = words.take(config.questionCount).mapIndexed { index, word ->
            when (types[index % types.size]) {
                QuestionType.MULTIPLE_CHOICE -> multipleChoice(index, word, words)
                QuestionType.WRITING -> writing(index, word)
            }
        }
        return QuizDto(
            quizSettings = QuizSettingsDto(
                totalQuestions = questions.size,
                timePerQuestionSeconds = config.timePerQuestionSeconds,
            ),
            questions = questions,
        )
    }

    private fun multipleChoice(
        index: Int,
        word: QuizSourceWordDto,
        allWords: List<QuizSourceWordDto>,
    ): QuizQuestionDto {
        val distractors = allWords.asSequence()
            .filter { it.id != word.id }
            .map { it.translationUz }
            .distinct()
            .rotatedBy(index)
            .take(OPTION_COUNT - 1)
            .toList()
        return QuizQuestionDto(
            id = index + 1,
            type = QuestionType.MULTIPLE_CHOICE.name,
            targetWord = word.term,
            prompt = "\"${word.term}\" so'zining o'zbekcha ma'nosi qaysi?",
            // The correct answer lands on a different position per question, without randomness.
            options = (distractors + word.translationUz).rotatedBy(index),
            correctAnswer = word.translationUz,
            hint = null,
            explanation = "\"${word.term}\" — ${word.translationUz}. Masalan: ${word.exampleSentence}",
        )
    }

    private fun writing(index: Int, word: QuizSourceWordDto) = QuizQuestionDto(
        id = index + 1,
        type = QuestionType.WRITING.name,
        targetWord = word.term,
        prompt = "\"${word.translationUz}\" ma'nosini bildiruvchi inglizcha so'zni yozing.",
        options = emptyList(),
        correctAnswer = word.term,
        hint = "Bosh harfi: ${word.term.first().uppercase()} (${word.term.length} ta harf)",
        explanation = "To'g'ri javob: ${word.term} — ${word.translationUz}. Masalan: ${word.exampleSentence}",
    )

    private fun <T> Sequence<T>.rotatedBy(steps: Int): Sequence<T> = toList().rotatedBy(steps).asSequence()

    private fun <T> List<T>.rotatedBy(steps: Int): List<T> =
        if (isEmpty()) this else drop(steps % size) + take(steps % size)

    private companion object {
        const val MIN_WORDS = 4
        const val OPTION_COUNT = 4
    }
}
