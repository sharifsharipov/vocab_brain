package uz.sharif.vocabbrain.feature.quiz.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizQuestion
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizSettings
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.State
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

class QuizStateProvider : PreviewParameterProvider<State> {
    override val values: Sequence<State>
        get() = sequenceOf(
            aQuizState(),
            aQuizState(answers = persistentMapOf(1 to "tejamkor")),
            aQuizState(answers = persistentMapOf(1 to "eskirgan")),
            aQuizState(answers = persistentMapOf(1 to QuizContract.TIMED_OUT)),
            aQuizState(currentIndex = 1, writingDraft = "war"),
            aQuizState(quiz = AsyncData.Loading()),
            aQuizState(quiz = AsyncData.Failure(IllegalArgumentException("Kamida 4 ta so'z kerak, hozir 2 ta bor"))),
        )
}

fun aQuizState(
    quiz: AsyncData<Quiz> = AsyncData.Success(aQuiz()),
    currentIndex: Int = 0,
    answers: PersistentMap<Int, String> = persistentMapOf(),
    writingDraft: String = "",
    secondsLeft: Int = 24,
) = State(
    quiz = quiz,
    currentIndex = currentIndex,
    answers = answers,
    writingDraft = writingDraft,
    secondsLeft = secondsLeft,
)

fun aQuiz() = Quiz(
    settings = QuizSettings(totalQuestions = 2, timePerQuestionSeconds = 30),
    questions = listOf(
        QuizQuestion(
            id = 1,
            type = QuestionType.MULTIPLE_CHOICE,
            targetWord = "frugal",
            prompt = "\"frugal\" so'zining o'zbekcha ma'nosi qaysi?",
            options = listOf("eskirgan", "tejamkor", "ehtiyotkor", "so'zga o'ch"),
            correctAnswer = "tejamkor",
            hint = null,
            explanation = "\"frugal\" — tejamkor. Masalan: A frugal shopper compares prices.",
        ),
        QuizQuestion(
            id = 2,
            type = QuestionType.WRITING,
            targetWord = "wary",
            prompt = "\"ehtiyotkor\" ma'nosini bildiruvchi inglizcha so'zni yozing.",
            options = emptyList(),
            correctAnswer = "wary",
            hint = "Bosh harfi: W (4 ta harf)",
            explanation = "To'g'ri javob: wary — ehtiyotkor. Masalan: Be wary of cheap offers.",
        ),
    ),
)
