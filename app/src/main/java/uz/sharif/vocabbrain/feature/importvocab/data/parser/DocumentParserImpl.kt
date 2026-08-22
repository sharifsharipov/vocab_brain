package uz.sharif.vocabbrain.feature.importvocab.data.parser

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads PDF, DOCX and plain text files. Parsing a large document is slow and allocates,
 * so all of it runs off the main thread.
 */
class DocumentParserImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DocumentParser {

    init {
        PDFBoxResourceLoader.init(context)
    }

    override suspend fun extractText(uri: Uri): String = withContext(ioDispatcher) {
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val path = uri.path?.lowercase().orEmpty()

        when {
            mimeType == PDF_MIME || path.endsWith(".pdf") -> readPdf(uri)
            mimeType == DOCX_MIME || path.endsWith(".docx") -> readDocx(uri)
            mimeType.startsWith("text/") || path.endsWith(".txt") -> readText(uri)
            else -> error("Qo'llab-quvvatlanmaydigan fayl turi: ${mimeType.ifEmpty { path }}")
        }
    }

    private fun readPdf(uri: Uri): String = open(uri).use { stream ->
        PDDocument.load(stream).use { document ->
            check(!document.isEncrypted) { "PDF parol bilan himoyalangan" }
            PDFTextStripper().getText(document).trim()
        }
    }

    private fun readDocx(uri: Uri): String = open(uri).use { DocxTextExtractor.extract(it).trim() }

    private fun readText(uri: Uri): String = open(uri).bufferedReader().use { it.readText().trim() }

    private fun open(uri: Uri) = context.contentResolver.openInputStream(uri)
        ?: error("Faylni ochib bo'lmadi")

    companion object {
        const val PDF_MIME = "application/pdf"
        const val DOCX_MIME =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val TEXT_MIME = "text/plain"

        /** What the document picker offers. */
        val SUPPORTED_MIME_TYPES = arrayOf(PDF_MIME, DOCX_MIME, TEXT_MIME)
    }
}
