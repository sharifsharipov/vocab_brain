package uz.sharif.vocabbrain.feature.importvocab.domain.usecase

import android.net.Uri
import uz.sharif.vocabbrain.feature.importvocab.data.ocr.OcrManager

/** Picture in, raw text out — the input the analysis prompt expects. */
class RecognizeTextUseCase(private val ocrManager: OcrManager) {
    suspend operator fun invoke(uri: Uri): String = ocrManager.recognizeText(uri)
}
