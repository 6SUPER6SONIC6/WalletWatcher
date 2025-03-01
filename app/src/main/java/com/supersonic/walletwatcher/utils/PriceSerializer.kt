package com.supersonic.walletwatcher.utils

import com.supersonic.walletwatcher.data.remote.models.Price
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

object PriceSerializer : KSerializer<Price?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Price")

    override fun deserialize(decoder: Decoder): Price? {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()

        return if (element is JsonObject) {
            jsonDecoder.json.decodeFromJsonElement(Price.serializer(), element)
        } else null
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Price?) {
        if (value != null) {
            encoder.encodeSerializableValue(Price.serializer(), value)
        } else encoder.encodeNull()

    }
}