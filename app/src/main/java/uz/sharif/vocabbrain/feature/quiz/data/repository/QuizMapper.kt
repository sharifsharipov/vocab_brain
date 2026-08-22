package uz.sharif.vocabbrain.feature.quiz.data.repository

import uz.sharif.vocabbrain.feature.quiz.data.model.QuizDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizQuestionDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSettingsDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSourceWordDto
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizQuestion
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizSettings
import uz.sharif.vocabbrain.feature.word.domain.model.Word

class UnknownQuestionTypeException(type: String) :
    IllegalArgumentException("Unsupported question type: $type")

fun QuizDto.toDomain() = Quiz(
    settings = quizSettings.toDomain(),
    questions = questions.map { it.toDomain() },
)

fun QuizSettingsDto.toDomain() = QuizSettings(
    totalQuestions = totalQuestions,
    timePerQuestionSeconds = timePerQuestionSeconds,
)

fun QuizQuestionDto.toDomain() = QuizQuestion(
    id = id,
    type = QuestionType.entries.firstOrNull { it.name == type } ?: throw UnknownQuestionTypeException(type),
    targetWord = targetWord,
    prompt = prompt,
    options = options,
    correctAnswer = correctAnswer,
    hint = hint,
    explanation = explanation,
)

fun Word.toQuizSource() = QuizSourceWordDto(
    id = id,
    term = term,
    translationUz = translationUz,
    exampleSentence = exampleSentence,
)
