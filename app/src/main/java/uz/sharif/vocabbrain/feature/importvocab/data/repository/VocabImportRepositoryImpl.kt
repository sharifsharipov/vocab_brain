package uz.sharif.vocabbrain.feature.importvocab.data.repository

import uz.sharif.vocabbrain.feature.importvocab.data.remote.ImportRemoteDataSource
import uz.sharif.vocabbrain.feature.importvocab.domain.model.Analysis
import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.importvocab.domain.repository.VocabImportRepository
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import uz.sharif.vocabbrain.feature.quiz.domain.repository.QuizRepository
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository

/**
 * Analysis comes from the remote source; its two products are handed to their owners —
 * words to the vocabulary store, the quiz to the quiz feature.
 */
class VocabImportRepositoryImpl(
    private val remoteDataSource: ImportRemoteDataSource,
    private val wordRepository: WordRepository,
    private val quizRepository: QuizRepository,
) : VocabImportRepository {

    override suspend fun analyze(text: String, config: QuizConfig): Analysis =
        remoteDataSource.analyze(text, config).toDomain()

    override suspend fun import(words: List<ExtractedWord>, quiz: Quiz): Int {
        val added = wordRepository.addWords(words.map { it.toWord() })
        // Only questions about imported words are worth playing.
        val importedTerms = words.mapTo(mutableSetOf()) { it.term }
        val questions = quiz.questions.filter { it.targetWord in importedTerms }
        if (questions.isNotEmpty()) {
            quizRepository.cacheQuiz(
                quiz.copy(
                    settings = quiz.settings.copy(totalQuestions = questions.size),
                    questions = questions,
                )
            )
        }
        return added
    }
}
