package uz.sharif.vocabbrain.feature.importvocab.domain.usecase

import android.net.Uri
import uz.sharif.vocabbrain.feature.importvocab.data.parser.DocumentParser

/** PDF, DOCX or plain text in, raw text out — the same input OCR produces. */
class ExtractDocumentTextUseCase(private val documentParser: DocumentParser) {
    suspend operator fun invoke(uri: Uri): String = documentParser.extractText(uri)
}
