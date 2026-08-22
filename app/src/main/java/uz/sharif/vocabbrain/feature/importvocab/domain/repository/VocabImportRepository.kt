package uz.sharif.vocabbrain.feature.importvocab.domain.repository

import uz.sharif.vocabbrain.feature.importvocab.domain.model.Analysis
import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

interface VocabImportRepository {

    suspend fun analyze(text: String, config: QuizConfig): Analysis

    /** Stores [words] in the vocabulary and keeps [quiz] ready to play. Returns how many were new. */
    suspend fun import(words: List<ExtractedWord>, quiz: Quiz): Int
}
