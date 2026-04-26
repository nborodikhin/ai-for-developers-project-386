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
class EventTypeControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var mapper: ObjectMapper

    @BeforeEach
    fun cleanup() {
        transaction {
            Bookings.deleteAll()
            EventTypes.deleteAll()
        }
    }

    private fun createBody(name: String = "Meeting", desc: String = "Quick sync", duration: Int = 30) =
        mapper.writeValueAsString(mapOf("name" to name, "description" to desc, "durationMinutes" to duration))

    @Test
    fun `POST event-types returns 201 with body`() {
        mockMvc.perform(
            post("/api/event-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody("Meeting", "Sync", 30))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Meeting"))
            .andExpect(jsonPath("$.durationMinutes").value(30))
            .andExpect(jsonPath("$.id").isNumber)
    }

    @Test
    fun `GET event-types returns 200 with list`() {
        mockMvc.perform(post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(createBody()))
        mockMvc.perform(post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(createBody("Other")))

        mockMvc.perform(get("/api/event-types"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `GET event-types by id returns 200`() {
        val created = mockMvc.perform(
            post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(createBody())
        ).andReturn().response.contentAsString
        val id = mapper.readTree(created)["id"].asLong()

        mockMvc.perform(get("/api/event-types/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
    }

    @Test
    fun `GET event-types by id returns 404 for missing`() {
        mockMvc.perform(get("/api/event-types/99999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PUT event-types updates and returns 200`() {
        val created = mockMvc.perform(
            post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(createBody())
        ).andReturn().response.contentAsString
        val id = mapper.readTree(created)["id"].asLong()

        mockMvc.perform(
            put("/api/event-types/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody("Updated", "New desc", 15))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
            .andExpect(jsonPath("$.durationMinutes").value(15))
    }

    @Test
    fun `PUT event-types returns 404 for missing`() {
        mockMvc.perform(
            put("/api/event-types/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody())
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `DELETE event-types returns 204`() {
        val created = mockMvc.perform(
            post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(createBody())
        ).andReturn().response.contentAsString
        val id = mapper.readTree(created)["id"].asLong()

        mockMvc.perform(delete("/api/event-types/$id"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE event-types removes it from list`() {
        val created = mockMvc.perform(
            post("/api/event-types").contentType(MediaType.APPLICATION_JSON).content(createBody())
        ).andReturn().response.contentAsString
        val id = mapper.readTree(created)["id"].asLong()

        mockMvc.perform(delete("/api/event-types/$id"))
        mockMvc.perform(get("/api/event-types"))
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `DELETE event-types returns 404 for missing`() {
        mockMvc.perform(delete("/api/event-types/99999"))
            .andExpect(status().isNotFound)
    }
}
