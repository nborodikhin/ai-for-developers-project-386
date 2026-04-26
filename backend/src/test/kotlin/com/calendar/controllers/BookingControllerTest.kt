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
class BookingControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper

    @BeforeEach
    fun cleanup() {
        transaction {
            Bookings.deleteAll()
            EventTypes.deleteAll()
        }
    }

    private fun createEventType(): Long {
        val body = mapper.writeValueAsString(
            mapOf("name" to "Test", "description" to "D", "durationMinutes" to 30)
        )
        val response = mockMvc.perform(
            post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(body)
        ).andReturn().response.contentAsString
        return mapper.readTree(response)["id"].asLong()
    }

    private fun bookingBody(
        start: String = "2026-05-10T09:00:00Z",
        name: String = "Alice",
        email: String = "alice@test.com"
    ) = mapper.writeValueAsString(
        mapOf("guestName" to name, "guestEmail" to email, "startTime" to start)
    )

    @Test
    fun `POST bookings returns 201 with body`() {
        val eventTypeId = createEventType()

        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.guestName").value("Alice"))
            .andExpect(jsonPath("$.guestEmail").value("alice@test.com"))
            .andExpect(jsonPath("$.eventTypeId").value(eventTypeId))
            .andExpect(jsonPath("$.id").isNumber)
    }

    @Test
    fun `POST bookings returns 409 on time conflict`() {
        val eventTypeId = createEventType()
        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody())
        )

        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody(name = "Bob", email = "bob@test.com"))
        ).andExpect(status().isConflict)
    }

    @Test
    fun `POST bookings returns 404 for unknown event type`() {
        mockMvc.perform(
            post("/api/event-types/99999/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody())
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `GET bookings returns 200 with all bookings`() {
        val eventTypeId = createEventType()
        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody(start = "2026-05-10T09:00:00Z"))
        )
        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody(start = "2026-05-10T09:30:00Z", name = "Bob", email = "bob@test.com"))
        )

        mockMvc.perform(get("/api/bookings"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `GET bookings filters by email`() {
        val eventTypeId = createEventType()
        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody(start = "2026-05-10T09:00:00Z"))
        )
        mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody(start = "2026-05-10T09:30:00Z", name = "Bob", email = "bob@test.com"))
        )

        mockMvc.perform(get("/api/bookings?email=alice@test.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].guestEmail").value("alice@test.com"))
    }

    @Test
    fun `PUT bookings updates guestName and returns 200`() {
        val eventTypeId = createEventType()
        val created = mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody())
        ).andReturn().response.contentAsString
        val id = mapper.readTree(created)["id"].asLong()

        mockMvc.perform(
            put("/api/bookings/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("guestName" to "Updated")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.guestName").value("Updated"))
    }

    @Test
    fun `PUT bookings returns 404 for missing`() {
        mockMvc.perform(
            put("/api/bookings/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("guestName" to "X")))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `DELETE bookings returns 204`() {
        val eventTypeId = createEventType()
        val created = mockMvc.perform(
            post("/api/event-types/$eventTypeId/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingBody())
        ).andReturn().response.contentAsString
        val id = mapper.readTree(created)["id"].asLong()

        mockMvc.perform(delete("/api/bookings/$id"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE bookings returns 404 for missing`() {
        mockMvc.perform(delete("/api/bookings/99999"))
            .andExpect(status().isNotFound)
    }
}
