package uz.sharif.vocabbrain.feature.importvocab.data.parser

import android.net.Uri

/** Turns a picked document into the raw text the analysis prompt expects. */
interface DocumentParser {

    /** Supported: PDF, DOCX and plain text. Throws on anything else, or on unreadable files. */
    suspend fun extractText(uri: Uri): String
}
