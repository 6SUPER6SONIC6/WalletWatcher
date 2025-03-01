package com.supersonic.walletwatcher.utils

import com.supersonic.walletwatcher.data.remote.models.TokenInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

object TokenInfoSerializer: KSerializer<TokenInfo?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TokenInfo")

    override fun deserialize(decoder: Decoder): TokenInfo? {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()

        return if (element is JsonObject){
            jsonDecoder.json.decodeFromJsonElement(TokenInfo.serializer(), element)
        } else null
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: TokenInfo?) {
        if (value != null){
            encoder.encodeSerializableValue(TokenInfo.serializer(), value)
        } else encoder.encodeNull()
    }
}