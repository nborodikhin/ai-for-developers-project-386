package com.calendar.services

import com.calendar.db.Bookings
import com.calendar.db.EventTypes
import com.calendar.generated.model.CreateBookingRequest
import com.calendar.generated.model.CreateEventTypeRequest
import com.calendar.generated.model.UpdateBookingRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest
@AutoConfigureMockMvc
class BookingServiceTest {

    @Autowired lateinit var bookingService: BookingService
    @Autowired lateinit var eventTypeService: EventTypeService

    private val testDate = OffsetDateTime.of(2026, 5, 10, 9, 0, 0, 0, ZoneOffset.UTC)

    @BeforeEach
    fun cleanup() {
        transaction {
            Bookings.deleteAll()
            EventTypes.deleteAll()
        }
    }

    private fun createEventType(duration: Int = 30) =
        eventTypeService.create(CreateEventTypeRequest("Test", "Desc", duration))

    private fun bookingRequest(
        startTime: OffsetDateTime,
        name: String = "Alice",
        email: String = "alice@test.com"
    ) = CreateBookingRequest(guestName = name, guestEmail = email, comment = null, startTime = startTime)

    @Test
    fun `create booking returns booking with correct fields`() {
        val et = createEventType(30)
        val req = bookingRequest(testDate)
        val booking = bookingService.create(et.id, req)

        assertNotNull(booking)
        assertEquals(et.id, booking!!.eventTypeId)
        assertEquals("Alice", booking.guestName)
        assertEquals("alice@test.com", booking.guestEmail)
        assertEquals(testDate, booking.startTime)
        assertEquals(testDate.plusMinutes(30), booking.endTime)
    }

    @Test
    fun `create booking returns null for non-existent event type`() {
        val result = bookingService.create(99999L, bookingRequest(testDate))
        assertNull(result)
    }

    @Test
    fun `create booking throws ConflictException on exact time overlap`() {
        val et = createEventType(30)
        bookingService.create(et.id, bookingRequest(testDate))

        assertThrows(ConflictException::class.java) {
            bookingService.create(et.id, bookingRequest(testDate, name = "Bob", email = "bob@test.com"))
        }
    }

    @Test
    fun `create booking throws ConflictException when new start is inside existing booking`() {
        val et = createEventType(30) // 09:00–09:30
        bookingService.create(et.id, bookingRequest(testDate))

        // New booking starts at 09:15 (inside existing 09:00–09:30)
        val overlapping = testDate.plusMinutes(15)
        assertThrows(ConflictException::class.java) {
            bookingService.create(et.id, bookingRequest(overlapping, name = "Bob", email = "bob@test.com"))
        }
    }

    @Test
    fun `create booking throws ConflictException when new booking wraps existing`() {
        // Existing: 09:15–10:15 (60 min)
        val et = createEventType(60)
        bookingService.create(et.id, bookingRequest(testDate.plusMinutes(15)))

        // New booking 09:00–10:00 overlaps 09:15–10:15
        assertThrows(ConflictException::class.java) {
            bookingService.create(et.id, bookingRequest(testDate, name = "Bob", email = "bob@test.com"))
        }
    }

    @Test
    fun `adjacent slots do not conflict`() {
        val et = createEventType(30) // first: 09:00–09:30
        bookingService.create(et.id, bookingRequest(testDate))

        // Second booking starts exactly when first ends: 09:30–10:00
        val adjacentStart = testDate.plusMinutes(30)
        val booking = bookingService.create(et.id, bookingRequest(adjacentStart, name = "Bob", email = "bob@test.com"))
        assertNotNull(booking)
    }

    @Test
    fun `different event types at same time do not conflict`() {
        val et1 = createEventType(30)
        val et2 = createEventType(30)
        bookingService.create(et1.id, bookingRequest(testDate))

        // Same slot but different event type — no conflict
        val booking = bookingService.create(et2.id, bookingRequest(testDate, name = "Bob", email = "bob@test.com"))
        assertNotNull(booking)
    }

    @Test
    fun `list returns all bookings`() {
        val et = createEventType(30)
        bookingService.create(et.id, bookingRequest(testDate, name = "Alice", email = "alice@test.com"))
        bookingService.create(et.id, bookingRequest(testDate.plusMinutes(30), name = "Bob", email = "bob@test.com"))

        val list = bookingService.list(null)
        assertEquals(2, list.size)
    }

    @Test
    fun `list filters by email`() {
        val et = createEventType(30)
        bookingService.create(et.id, bookingRequest(testDate, name = "Alice", email = "alice@test.com"))
        bookingService.create(et.id, bookingRequest(testDate.plusMinutes(30), name = "Bob", email = "bob@test.com"))

        val aliceOnly = bookingService.list("alice@test.com")
        assertEquals(1, aliceOnly.size)
        assertEquals("alice@test.com", aliceOnly[0].guestEmail)
    }

    @Test
    fun `update changes guestName`() {
        val et = createEventType(30)
        val booking = bookingService.create(et.id, bookingRequest(testDate))!!
        val updated = bookingService.update(booking.id, UpdateBookingRequest(guestName = "Updated"))
        assertNotNull(updated)
        assertEquals("Updated", updated!!.guestName)
    }

    @Test
    fun `update does not change startTime or endTime`() {
        val et = createEventType(30)
        val booking = bookingService.create(et.id, bookingRequest(testDate))!!
        val updated = bookingService.update(booking.id, UpdateBookingRequest(guestName = "Updated"))!!
        assertEquals(booking.startTime, updated.startTime)
        assertEquals(booking.endTime, updated.endTime)
    }

    @Test
    fun `update returns null for non-existent id`() {
        assertNull(bookingService.update(99999L, UpdateBookingRequest(guestName = "X")))
    }

    @Test
    fun `delete removes booking`() {
        val et = createEventType(30)
        val booking = bookingService.create(et.id, bookingRequest(testDate))!!
        assertTrue(bookingService.delete(booking.id))
        assertTrue(bookingService.list(null).isEmpty())
    }

    @Test
    fun `delete returns false for non-existent id`() {
        assertFalse(bookingService.delete(99999L))
    }
}
