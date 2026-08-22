package uz.sharif.vocabbrain.feature.importvocab.data.remote

import com.google.firebase.ai.GenerativeModel
import uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

/**
 * Runs the analysis through Firebase AI Logic. The API key stays in Firebase, so nothing
 * secret ships inside the APK; App Check is what keeps other callers out.
 */
class FirebaseVocabEngine(
    private val model: GenerativeModel,
) : ImportRemoteDataSource {

    override suspend fun analyze(rawText: String, config: QuizConfig): VocabBrainDto {
        val response = model.generateContent(buildVocabBrainPrompt(rawText, config))
        return VocabBrainResponseParser.parse(response.text)
    }
}
