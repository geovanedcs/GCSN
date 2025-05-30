package br.com.omnidevs.gcsn.util

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.LabelObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object LabelsSerializer : KSerializer<List<Label>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Labels")

    override fun serialize(encoder: Encoder, value: List<Label>) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("Can only serialize to JSON")

        val jsonObject = buildJsonObject {
            put("\$type", JsonPrimitive("com.atproto.label.defs#selfLabels"))
            putJsonArray("values") {
                value.forEach { label ->
                    addJsonObject {
                        put("val", JsonPrimitive(label.value))
                    }
                }
            }
        }

        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): List<Label> {
        val jsonDecoder = decoder as? JsonDecoder ?: error("Can only deserialize JSON")
        val element = jsonDecoder.decodeJsonElement()

        try {
            val labels = ArrayList<Label>()

            when (element) {
                is JsonObject -> {
                    val valuesElement = element["values"] as? JsonArray
                    valuesElement?.forEach { valueElement ->
                        when (valueElement) {
                            is JsonObject -> {
                                valueElement["val"]?.jsonPrimitive?.content?.let {
                                    labels.add(Label(value = it))
                                }
                            }

                            is JsonPrimitive -> {
                                valueElement.contentOrNull?.let {
                                    labels.add(Label(value = it))
                                }
                            }

                            else -> {} // Skip other types
                        }
                    }
                }

                is JsonArray -> {
                    element.forEach { item ->
                        when (item) {
                            is JsonObject -> {
                                item["val"]?.jsonPrimitive?.content?.let {
                                    labels.add(Label(value = it))
                                }
                            }

                            is JsonPrimitive -> {
                                item.contentOrNull?.let {
                                    labels.add(Label(value = it))
                                }
                            }

                            else -> {} // Skip other types
                        }
                    }
                }

                else -> {} // Return empty list for unknown formats
            }

            return labels
        } catch (e: Exception) {
            println("Error deserializing labels: ${e.message}")
            return ArrayList()
        }
    }
}