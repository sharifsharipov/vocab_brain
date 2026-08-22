package uz.sharif.vocabbrain.feature.importvocab.data.parser

import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/**
 * Reads the text of a .docx without Apache POI: the format is a zip whose `word/document.xml`
 * holds the body, and a streaming SAX pass over that entry is enough.
 *
 * POI would also work, but poi-ooxml drags xmlbeans and a large part of the Java XML stack
 * into an APK that has no other use for them. This class has no Android dependency at all,
 * which also makes it testable on the JVM.
 */
object DocxTextExtractor {

    private const val BODY_ENTRY = "word/document.xml"

    fun extract(input: InputStream): String {
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == BODY_ENTRY) return parseBody(zip)
                zip.closeEntry()
            }
        }
        error("DOCX ichidan $BODY_ENTRY topilmadi")
    }

    private fun parseBody(stream: InputStream): String {
        val handler = DocxHandler()
        SAXParserFactory.newInstance().newSAXParser().parse(InputSource(stream), handler)
        return handler.text()
    }

    /**
     * `w:t` holds the runs of text, `w:p` ends a paragraph, and tabs and breaks are kept so
     * table rows and line breaks do not run words together.
     */
    private class DocxHandler : DefaultHandler() {

        private val builder = StringBuilder()
        private var insideText = false

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            when (tagOf(localName, qName)) {
                "t" -> insideText = true
                "tab" -> builder.append('\t')
                "br", "cr" -> builder.append('\n')
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if (insideText) builder.appendRange(ch, start, start + length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            when (tagOf(localName, qName)) {
                "t" -> insideText = false
                "p" -> builder.append('\n')
                "tc" -> builder.append(' ')
            }
        }

        fun text(): String = builder.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")

        private fun tagOf(localName: String?, qName: String?): String =
            localName?.takeIf { it.isNotEmpty() }?.substringAfter(':')
                ?: qName.orEmpty().substringAfter(':')
    }
}
