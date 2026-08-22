package uz.sharif.vocabbrain.feature.word.data.repository

import uz.sharif.vocabbrain.feature.word.data.local.WordEntity
import uz.sharif.vocabbrain.feature.word.domain.model.Word

fun WordEntity.toDomain() = Word(
    id = id,
    term = term,
    phonetic = phonetic,
    partOfSpeech = partOfSpeech,
    translationUz = translationUz,
    exampleSentence = exampleSentence,
    exampleTranslationUz = exampleTranslationUz,
    isLearned = isLearned,
)

fun Word.toEntity() = WordEntity(
    id = id,
    term = term,
    phonetic = phonetic,
    partOfSpeech = partOfSpeech,
    translationUz = translationUz,
    exampleSentence = exampleSentence,
    exampleTranslationUz = exampleTranslationUz,
    isLearned = isLearned,
)
