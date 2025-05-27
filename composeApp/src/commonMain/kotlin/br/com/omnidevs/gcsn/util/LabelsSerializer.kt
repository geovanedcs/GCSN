package br.com.omnidevs.gcsn.util

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.LabelObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

object LabelsSerializer : KSerializer<List<Label>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("LabelsOrLabelObject")

    override fun deserialize(decoder: Decoder): List<Label> {
        val jsonDecoder = decoder as? JsonDecoder ?: error("Can only deserialize JSON")
        val element = jsonDecoder.decodeJsonElement()

        return when {
            element is JsonArray ->
                element.map { jsonDecoder.json.decodeFromJsonElement(Label.serializer(), it) }

            element is JsonObject && element["values"] != null -> {
                val labelObj = jsonDecoder.json.decodeFromJsonElement(LabelObject.serializer(), element)
                labelObj.values
            }
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<Label>) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("Can only serialize to JSON")
        val labelElements = value.map { jsonEncoder.json.encodeToJsonElement(Label.serializer(), it) }
        jsonEncoder.encodeJsonElement(JsonArray(labelElements))
    }
}