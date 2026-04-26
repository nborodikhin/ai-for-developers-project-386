package com.calendar.services

import com.calendar.db.Bookings
import com.calendar.db.EventTypes
import com.calendar.db.Settings
import com.calendar.generated.model.CreateBookingRequest
import com.calendar.generated.model.CreateEventTypeRequest
import com.calendar.generated.model.UpdateTimezoneRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest
@AutoConfigureMockMvc
class SlotServiceTest {

    @Autowired lateinit var slotService: SlotService
    @Autowired lateinit var bookingService: BookingService
    @Autowired lateinit var eventTypeService: EventTypeService
    @Autowired lateinit var settingsService: SettingsService

    private val testDate = LocalDate.of(2026, 5, 15)
    private val dayStart = OffsetDateTime.of(2026, 5, 15, 9, 0, 0, 0, ZoneOffset.UTC)

    @BeforeEach
    fun cleanup() {
        transaction {
            Bookings.deleteAll()
            EventTypes.deleteAll()
            Settings.update { it[timezone] = "UTC" }
        }
    }

    @Test
    fun `returns null for unknown event type`() {
        assertNull(slotService.getSlots(99999L, testDate))
    }

    @Test
    fun `returns 18 slots for 30-minute event type`() {
        // 9 hours * 60 minutes / 30 = 18 slots
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        val slots = slotService.getSlots(et.id, testDate)
        assertNotNull(slots)
        assertEquals(18, slots!!.size)
    }

    @Test
    fun `returns 36 slots for 15-minute event type`() {
        // 9 hours * 60 minutes / 15 = 36 slots
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 15))
        val slots = slotService.getSlots(et.id, testDate)
        assertNotNull(slots)
        assertEquals(36, slots!!.size)
    }

    @Test
    fun `first slot starts at 09h00 UTC when owner timezone is UTC`() {
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        val slots = slotService.getSlots(et.id, testDate)!!
        assertEquals(dayStart, slots.first().startTime)
    }

    @Test
    fun `last slot ends at 18h00 UTC`() {
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        val slots = slotService.getSlots(et.id, testDate)!!
        val expectedEnd = OffsetDateTime.of(2026, 5, 15, 18, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(expectedEnd, slots.last().endTime)
    }

    @Test
    fun `all slots available when no bookings`() {
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        val slots = slotService.getSlots(et.id, testDate)!!
        assertTrue(slots.all { it.available })
    }

    @Test
    fun `booked slot is marked unavailable`() {
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        bookingService.create(
            et.id,
            CreateBookingRequest(
                guestName = "Alice",
                guestEmail = "alice@test.com",
                comment = null,
                startTime = dayStart  // 09:00–09:30
            )
        )

        val slots = slotService.getSlots(et.id, testDate)!!
        val slot0900 = slots.find { it.startTime == dayStart }
        assertNotNull(slot0900)
        assertFalse(slot0900!!.available)
    }

    @Test
    fun `adjacent slots remain available after one slot is booked`() {
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        bookingService.create(
            et.id,
            CreateBookingRequest(
                guestName = "Alice",
                guestEmail = "alice@test.com",
                comment = null,
                startTime = dayStart  // books 09:00–09:30
            )
        )

        val slots = slotService.getSlots(et.id, testDate)!!
        val slot0930 = slots.find { it.startTime == dayStart.plusMinutes(30) }
        assertNotNull(slot0930)
        assertTrue(slot0930!!.available)
    }

    @Test
    fun `slot count is zero for event longer than work day`() {
        // 10-hour event type won't fit in a 9-hour window → 0 slots
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 10 * 60))
        val slots = slotService.getSlots(et.id, testDate)!!
        assertEquals(0, slots.size)
    }

    @Test
    fun `owner timezone UTC+3 shifts work window to UTC+0 equivalent`() {
        // Moscow is UTC+3, so 9:00 Moscow = 06:00 UTC, 18:00 Moscow = 15:00 UTC
        settingsService.updateTimezone(UpdateTimezoneRequest(timezone = "Europe/Moscow"))
        val et = eventTypeService.create(CreateEventTypeRequest("T", "", 30))
        val slots = slotService.getSlots(et.id, testDate)!!
        assertNotNull(slots)
        assertEquals(18, slots.size)
        val expectedStart = OffsetDateTime.of(2026, 5, 15, 6, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(expectedStart, slots.first().startTime)
    }
}
