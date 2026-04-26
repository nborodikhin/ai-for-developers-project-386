package com.calendar.services

import com.calendar.db.Bookings
import com.calendar.db.EventTypes
import com.calendar.generated.model.CreateEventTypeRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
class EventTypeServiceTest {

    @Autowired
    lateinit var service: EventTypeService

    @BeforeEach
    fun cleanup() {
        transaction {
            Bookings.deleteAll()
            EventTypes.deleteAll()
        }
    }

    @Test
    fun `create returns event type with correct fields`() {
        val result = service.create(CreateEventTypeRequest("Meeting", "Quick sync", 15))
        assertEquals("Meeting", result.name)
        assertEquals("Quick sync", result.description)
        assertEquals(15, result.durationMinutes)
        assertTrue(result.id > 0)
    }

    @Test
    fun `list returns only non-deleted types`() {
        val et1 = service.create(CreateEventTypeRequest("Active", "", 15))
        val et2 = service.create(CreateEventTypeRequest("Deleted", "", 30))
        service.softDelete(et2.id)

        val list = service.list()
        assertEquals(1, list.size)
        assertEquals(et1.id, list[0].id)
    }

    @Test
    fun `list returns empty when all deleted`() {
        val et = service.create(CreateEventTypeRequest("T", "", 15))
        service.softDelete(et.id)
        assertTrue(service.list().isEmpty())
    }

    @Test
    fun `findById returns correct event type`() {
        val created = service.create(CreateEventTypeRequest("Test", "Desc", 30))
        val found = service.findById(created.id)
        assertNotNull(found)
        assertEquals(created.id, found!!.id)
        assertEquals("Test", found.name)
    }

    @Test
    fun `findById returns null for non-existent id`() {
        assertNull(service.findById(99999L))
    }

    @Test
    fun `findById returns null for soft-deleted type`() {
        val et = service.create(CreateEventTypeRequest("T", "", 15))
        service.softDelete(et.id)
        assertNull(service.findById(et.id))
    }

    @Test
    fun `update changes all fields`() {
        val et = service.create(CreateEventTypeRequest("Old", "Old desc", 15))
        val updated = service.update(et.id, CreateEventTypeRequest("New", "New desc", 30))
        assertNotNull(updated)
        assertEquals("New", updated!!.name)
        assertEquals("New desc", updated.description)
        assertEquals(30, updated.durationMinutes)
    }

    @Test
    fun `update returns null for non-existent id`() {
        assertNull(service.update(99999L, CreateEventTypeRequest("T", "", 15)))
    }

    @Test
    fun `update returns null for soft-deleted type`() {
        val et = service.create(CreateEventTypeRequest("T", "", 15))
        service.softDelete(et.id)
        assertNull(service.update(et.id, CreateEventTypeRequest("New", "", 15)))
    }

    @Test
    fun `softDelete returns true and hides type from list`() {
        val et = service.create(CreateEventTypeRequest("T", "", 15))
        assertTrue(service.softDelete(et.id))
        assertFalse(service.list().any { it.id == et.id })
    }

    @Test
    fun `softDelete returns false for non-existent id`() {
        assertFalse(service.softDelete(99999L))
    }

    @Test
    fun `softDelete is idempotent - returns false on second call`() {
        val et = service.create(CreateEventTypeRequest("T", "", 15))
        assertTrue(service.softDelete(et.id))
        assertFalse(service.softDelete(et.id))
    }
}
