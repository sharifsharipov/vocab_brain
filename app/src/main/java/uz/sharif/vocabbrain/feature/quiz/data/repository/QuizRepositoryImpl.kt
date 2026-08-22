package uz.sharif.vocabbrain.feature.quiz.data.repository

import uz.sharif.vocabbrain.feature.quiz.data.remote.QuizRemoteDataSource
import uz.sharif.vocabbrain.feature.quiz.domain.model.Quiz
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig
import uz.sharif.vocabbrain.feature.quiz.domain.repository.QuizRepository
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.first

/**
 * Source material is the stored vocabulary; question shaping belongs to the generator.
 * A quiz that arrives with an import is held here until the quiz screen picks it up.
 */
class QuizRepositoryImpl(
    private val remoteDataSource: QuizRemoteDataSource,
    private val wordRepository: WordRepository,
) : QuizRepository {

    private val cachedQuiz = AtomicReference<Quiz?>(null)

    override suspend fun generateQuiz(config: QuizConfig): Quiz {
        val words = wordRepository.observeWords().first()
        return remoteDataSource.generateQuiz(words.map { it.toQuizSource() }, config).toDomain()
    }

    override fun cacheQuiz(quiz: Quiz) = cachedQuiz.set(quiz)

    override fun takeCachedQuiz(): Quiz? = cachedQuiz.getAndSet(null)
}
