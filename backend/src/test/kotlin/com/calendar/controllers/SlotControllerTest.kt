package com.calendar.controllers

import com.calendar.db.Bookings
import com.calendar.db.EventTypes
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class SlotControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper

    @BeforeEach
    fun cleanup() {
        transaction {
            Bookings.deleteAll()
            EventTypes.deleteAll()
        }
    }

    private fun createEventType(duration: Int = 30): Long {
        val body = mapper.writeValueAsString(
            mapOf("name" to "Test", "description" to "D", "durationMinutes" to duration)
        )
        val response = mockMvc.perform(
            post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(body)
        ).andReturn().response.contentAsString
        return mapper.readTree(response)["id"].asLong()
    }

    @Test
    fun `GET available-slots returns 200 with slot array`() {
        val eventTypeId = createEventType(30)

        mockMvc.perform(get("/api/event-types/$eventTypeId/available-slots?date=2026-05-15"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(18))  // 9h * 60 / 30 = 18 slots
    }

    @Test
    fun `GET available-slots returns 404 for unknown event type`() {
        mockMvc.perform(get("/api/event-types/99999/available-slots?date=2026-05-15"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `booked slot appears as available false`() {
        val eventTypeId = createEventType(30)
        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "guestName" to "Alice",
                            "guestEmail" to "alice@test.com",
                            "startTime" to "2026-05-15T09:00:00Z"
                        )
                    )
                )
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/api/event-types/$eventTypeId/available-slots?date=2026-05-15"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].available").value(false))   // first slot (09:00) is booked
            .andExpect(jsonPath("$[1].available").value(true))    // second slot (09:30) is free
    }

    @Test
    fun `all slots available when no bookings`() {
        val eventTypeId = createEventType(30)

        mockMvc.perform(get("/api/event-types/$eventTypeId/available-slots?date=2026-05-15"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].available").value(true))
            .andExpect(jsonPath("$[17].available").value(true))
    }
}
