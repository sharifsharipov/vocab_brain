package uz.sharif.vocabbrain.feature.importvocab.data.remote

import kotlinx.serialization.json.Json
import uz.sharif.vocabbrain.feature.importvocab.data.model.VocabBrainDto

/**
 * Turns the model's reply into [VocabBrainDto]. The model is asked for `application/json`,
 * but replies are unwrapped defensively: a stray Markdown fence is the most common way a
 * JSON-only instruction is broken. Pure Kotlin, so it is tested without a device.
 */
object VocabBrainResponseParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(reply: String?): VocabBrainDto {
        val text = reply?.trim().orEmpty()
        require(text.isNotEmpty()) { "Model javobi bo'sh keldi" }
        return json.decodeFromString(text.unwrapJson())
    }

    private fun String.unwrapJson(): String = trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}
