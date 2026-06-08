package com.pani.app.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Supabase/PostgREST returns GEOMETRY(Point, 4326) columns as GeoJSON:
 *   {"type":"Point","coordinates":[longitude, latitude]}
 *
 * This serializer handles both directions so we can use GeoJsonPoint
 * transparently inside @Serializable DTOs.
 */
@Serializable(with = GeoJsonPointSerializer::class)
data class GeoJsonPoint(
    val latitude: Double,
    val longitude: Double
)

object GeoJsonPointSerializer : KSerializer<GeoJsonPoint> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GeoJsonPoint") {
        element<String>("type")
        element<List<Double>>("coordinates")
    }

    override fun deserialize(decoder: Decoder): GeoJsonPoint {
        val json = (decoder as JsonDecoder).decodeJsonElement() as JsonObject
        val coords = json["coordinates"]!!.jsonArray
        // GeoJSON coordinate order: [longitude, latitude]
        return GeoJsonPoint(
            longitude = coords[0].jsonPrimitive.double,
            latitude  = coords[1].jsonPrimitive.double
        )
    }

    override fun serialize(encoder: Encoder, value: GeoJsonPoint) {
        val json = (encoder as JsonEncoder)
        json.encodeJsonElement(
            JsonObject(mapOf(
                "type"        to JsonPrimitive("Point"),
                "coordinates" to JsonArray(listOf(
                    JsonPrimitive(value.longitude),
                    JsonPrimitive(value.latitude)
                ))
            ))
        )
    }
}
