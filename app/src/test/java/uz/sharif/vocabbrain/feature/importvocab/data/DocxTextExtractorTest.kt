package uz.sharif.vocabbrain.feature.importvocab.data

import uz.sharif.vocabbrain.feature.importvocab.data.parser.DocxTextExtractor
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Test

class DocxTextExtractorTest {

    private fun docx(bodyXml: String, entryName: String = "word/document.xml"): ByteArrayInputStream {
        val bytes = ByteArrayOutputStream().also { out ->
            ZipOutputStream(out).use { zip ->
                zip.putNextEntry(ZipEntry("[Content_Types].xml"))
                zip.write("<Types/>".toByteArray())
                zip.closeEntry()
                zip.putNextEntry(ZipEntry(entryName))
                // Built without trimIndent: interpolated body lines would change the common
                // indent and leave whitespace in front of the XML declaration.
                zip.write(
                    ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
                        "<w:body>$bodyXml</w:body></w:document>").toByteArray()
                )
                zip.closeEntry()
            }
        }.toByteArray()
        return ByteArrayInputStream(bytes)
    }

    @Test
    fun `paragraphs become lines`() {
        val text = DocxTextExtractor.extract(
            docx(
                """
                <w:p><w:r><w:t>A frugal shopper</w:t></w:r></w:p>
                <w:p><w:r><w:t>stays wary of deals.</w:t></w:r></w:p>
                """
            )
        )

        assertThat(text).isEqualTo("A frugal shopper\nstays wary of deals.")
    }

    @Test
    fun `runs inside one paragraph stay on one line`() {
        val text = DocxTextExtractor.extract(
            docx("<w:p><w:r><w:t>abundant</w:t></w:r><w:r><w:t> water</w:t></w:r></w:p>")
        )

        assertThat(text).isEqualTo("abundant water")
    }

    @Test
    fun `breaks and tabs keep words apart`() {
        val text = DocxTextExtractor.extract(
            docx("<w:p><w:r><w:t>first</w:t><w:br/><w:t>second</w:t><w:tab/><w:t>third</w:t></w:r></w:p>")
        )

        assertThat(text).isEqualTo("first\nsecond\tthird")
    }

    @Test
    fun `table cells are read too`() {
        val text = DocxTextExtractor.extract(
            docx(
                """
                <w:tbl><w:tr>
                  <w:tc><w:p><w:r><w:t>term</w:t></w:r></w:p></w:tc>
                  <w:tc><w:p><w:r><w:t>tarjima</w:t></w:r></w:p></w:tc>
                </w:tr></w:tbl>
                """
            )
        )

        assertThat(text).contains("term")
        assertThat(text).contains("tarjima")
    }

    @Test
    fun `a zip without a document body is rejected`() {
        val error = runCatching {
            DocxTextExtractor.extract(docx("<w:p/>", entryName = "word/other.xml"))
        }.exceptionOrNull()

        assertThat(error).hasMessageThat().contains("word/document.xml")
    }
}
