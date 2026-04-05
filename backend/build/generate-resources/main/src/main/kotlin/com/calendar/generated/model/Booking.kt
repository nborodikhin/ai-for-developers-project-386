package com.calendar.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 
 * @param id 
 * @param eventTypeId 
 * @param eventTypeName 
 * @param guestName 
 * @param guestEmail 
 * @param startTime 
 * @param endTime 
 * @param comment 
 */
data class Booking(

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("id", required = true) val id: kotlin.Long,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("eventTypeId", required = true) val eventTypeId: kotlin.Long,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("eventTypeName", required = true) val eventTypeName: kotlin.String,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("guestName", required = true) val guestName: kotlin.String,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("guestEmail", required = true) val guestEmail: kotlin.String,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("startTime", required = true) val startTime: java.time.OffsetDateTime,

    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("endTime", required = true) val endTime: java.time.OffsetDateTime,

    @Schema(example = "null", description = "")
    @get:JsonProperty("comment") val comment: kotlin.String? = null
) {

}

