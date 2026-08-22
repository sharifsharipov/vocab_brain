package uz.sharif.vocabbrain.feature.importvocab.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Reads text out of a picture. An interface so callers can be tested off-device. */
interface OcrManager {
    suspend fun recognizeText(uri: Uri): String
}

/** On-device OCR through ML Kit; no image ever leaves the phone. */
class MlKitOcrManager(private val context: Context) : OcrManager {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result -> continuation.resume(result.text) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
        }
    }
}
