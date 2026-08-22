package uz.sharif.vocabbrain.feature.importvocab.data.repository

import uz.sharif.vocabbrain.feature.importvocab.data.model.ExtractedWordDto
import uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto
import uz.sharif.vocabbrain.feature.importvocab.domain.model.Analysis
import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.quiz.data.repository.toDomain
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.word.domain.model.Word

fun VocabBrainDto.toDomain() = Analysis(
    words = extractedVocabulary.map { it.toDomain() },
    quiz = Quiz(
        settings = quizSettings.toDomain(),
        questions = questions.map { it.toDomain() },
    ),
)

fun ExtractedWordDto.toDomain() = ExtractedWord(
    term = word,
    phonetic = phonetic,
    partOfSpeech = partOfSpeech,
    translationUz = translationUz,
    exampleSentence = exampleSentence,
    exampleTranslationUz = sentenceTranslationUz,
)

/** Extracted words become vocabulary entries, unlearned and keyed by their term. */
fun ExtractedWord.toWord() = Word(
    id = "w-${term.lowercase()}",
    term = term,
    phonetic = phonetic,
    partOfSpeech = partOfSpeech,
    translationUz = translationUz,
    exampleSentence = exampleSentence,
    exampleTranslationUz = exampleTranslationUz,
    isLearned = false,
)
