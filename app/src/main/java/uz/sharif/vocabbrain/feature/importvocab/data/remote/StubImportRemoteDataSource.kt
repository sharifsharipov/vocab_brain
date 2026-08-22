package uz.sharif.vocabbrain.feature.importvocab.data.remote

import uz.sharif.vocabbrain.feature.importvocab.data.model.ExtractedWordDto
import uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto
import uz.sharif.vocabbrain.feature.quiz.data.model.QuizSourceWordDto
import uz.sharif.vocabbrain.feature.quiz.data.remote.QuizRemoteDataSource
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

/**
 * Offline stand-in for the analysis engine. It looks the text up in a small built-in
 * glossary instead of calling a model, then hands the hits to [quizRemoteDataSource]
 * so the response has the same shape the real contract promises.
 *
 * Only glossary words are found — an unknown text yields an empty vocabulary, which is
 * the honest offline answer rather than invented translations.
 */
class StubImportRemoteDataSource(
    private val quizRemoteDataSource: QuizRemoteDataSource,
) : ImportRemoteDataSource {

    override suspend fun analyze(rawText: String, config: QuizConfig): VocabBrainDto {
        require(rawText.isNotBlank()) { "Matn bo'sh bo'lishi mumkin emas" }

        val terms = TERM_REGEX.findAll(rawText)
            .map { it.value.lowercase() }
            .distinct()
            .toList()
        val found = GLOSSARY.filter { it.key in terms }.values.take(MAX_WORDS)

        val quiz = if (found.size >= MIN_WORDS_FOR_QUIZ) {
            quizRemoteDataSource.generateQuiz(
                words = found.map {
                    QuizSourceWordDto(
                        id = "w-${it.word}",
                        term = it.word,
                        translationUz = it.translationUz,
                        exampleSentence = it.exampleSentence,
                    )
                },
                config = config,
            )
        } else {
            null
        }

        return VocabBrainDto(
            extractedVocabulary = found,
            quizSettings = quiz?.quizSettings
                ?: uz.sharif.vocabbrain.feature.quiz.data.model.QuizSettingsDto(
                    totalQuestions = 0,
                    timePerQuestionSeconds = config.timePerQuestionSeconds,
                ),
            questions = quiz?.questions.orEmpty(),
        )
    }

    private companion object {
        val TERM_REGEX = Regex("[A-Za-z][A-Za-z'-]+")
        const val MAX_WORDS = 15
        const val MIN_WORDS_FOR_QUIZ = 4

        fun entry(
            word: String,
            phonetic: String,
            partOfSpeech: String,
            translationUz: String,
            exampleSentence: String,
            sentenceTranslationUz: String,
        ) = word to ExtractedWordDto(
            word = word,
            phonetic = phonetic,
            partOfSpeech = partOfSpeech,
            translationUz = translationUz,
            exampleSentence = exampleSentence,
            sentenceTranslationUz = sentenceTranslationUz,
        )

        val GLOSSARY = mapOf(
            entry("abundant", "/əˈbʌn.dənt/", "adjective", "mo'l-ko'l",
                "The region has abundant water supplies.", "Bu hududda suv zaxiralari mo'l-ko'l."),
            entry("candid", "/ˈkæn.dɪd/", "adjective", "samimiy",
                "She gave a candid answer.", "U samimiy javob berdi."),
            entry("diligent", "/ˈdɪl.ɪ.dʒənt/", "adjective", "tirishqoq",
                "He is a diligent student.", "U tirishqoq talaba."),
            entry("eloquent", "/ˈel.ə.kwənt/", "adjective", "so'zamol",
                "The lawyer gave an eloquent speech.", "Advokat so'zamol nutq so'zladi."),
            entry("frugal", "/ˈfruː.ɡəl/", "adjective", "tejamkor",
                "A frugal shopper compares prices.", "Tejamkor xaridor narxlarni solishtiradi."),
            entry("gregarious", "/ɡrɪˈɡeə.ri.əs/", "adjective", "davrani yaxshi ko'radigan",
                "Gregarious people enjoy big teams.", "Davrani yaxshi ko'radigan odamlar katta jamoani yoqtiradi."),
            entry("meticulous", "/məˈtɪk.jə.ləs/", "adjective", "puxta",
                "She keeps meticulous records.", "U hisobotlarni puxta yuritadi."),
            entry("obsolete", "/ˈɒb.sə.liːt/", "adjective", "eskirgan",
                "That format is now obsolete.", "Bu format endi eskirgan."),
            entry("pragmatic", "/præɡˈmæt.ɪk/", "adjective", "amaliy",
                "We need a pragmatic solution.", "Bizga amaliy yechim kerak."),
            entry("resilient", "/rɪˈzɪl.i.ənt/", "adjective", "chidamli",
                "The system is resilient to failure.", "Tizim nosozliklarga chidamli."),
            entry("scarce", "/skeəs/", "adjective", "tanqis",
                "Clean water is scarce there.", "U yerda toza suv tanqis."),
            entry("tenacious", "/təˈneɪ.ʃəs/", "adjective", "qat'iyatli",
                "A tenacious runner never stops.", "Qat'iyatli yuguruvchi hech qachon to'xtamaydi."),
            entry("verbose", "/vɜːˈbəʊs/", "adjective", "so'zga o'ch",
                "The report was too verbose.", "Hisobot haddan tashqari so'zga o'ch edi."),
            entry("wary", "/ˈweə.ri/", "adjective", "ehtiyotkor",
                "Be wary of cheap offers.", "Arzon takliflarga nisbatan ehtiyotkor bo'ling."),
        )
    }
}
