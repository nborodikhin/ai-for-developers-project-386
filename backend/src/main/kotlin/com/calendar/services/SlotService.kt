package com.calendar.services

import com.calendar.db.Bookings
import com.calendar.db.EventTypes
import com.calendar.generated.model.AvailableSlot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class SlotService(private val settingsService: SettingsService) {

    companion object {
        private const val WORK_START_HOUR = 9
        private const val WORK_END_HOUR   = 18
    }

    private val fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun getSlots(eventTypeId: Long, date: LocalDate): List<AvailableSlot>? {
        val ownerTz = ZoneId.of(settingsService.getOwnerTimezone())
        return transaction {
            val eventTypeRow = EventTypes
                .selectAll()
                .where { (EventTypes.id eq eventTypeId) and (EventTypes.deleted eq false) }
                .singleOrNull() ?: return@transaction null

            val durationMinutes = eventTypeRow[EventTypes.durationMinutes].toLong()

            val dayStart = ZonedDateTime.of(date, LocalTime.of(WORK_START_HOUR, 0), ownerTz)
                .withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
            val dayEnd = ZonedDateTime.of(date, LocalTime.of(WORK_END_HOUR, 0), ownerTz)
                .withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()

            val dayStartStr = dayStart.format(fmt)
            val dayEndStr   = dayEnd.format(fmt)

            val existingBookings = Bookings
                .selectAll()
                .where {
                    (Bookings.eventTypeId eq eventTypeId) and
                    (Bookings.startTime less dayEndStr) and
                    (Bookings.endTime greater dayStartStr)
                }
                .map { it[Bookings.startTime] to it[Bookings.endTime] }

            val slots = mutableListOf<AvailableSlot>()
            var cursor = dayStart
            while (cursor.plusMinutes(durationMinutes) <= dayEnd) {
                val slotEnd      = cursor.plusMinutes(durationMinutes)
                val slotStartStr = cursor.format(fmt)
                val slotEndStr   = slotEnd.format(fmt)

                val available = existingBookings.none { (bStart, bEnd) ->
                    bStart < slotEndStr && bEnd > slotStartStr
                }

                slots.add(AvailableSlot(startTime = cursor, endTime = slotEnd, available = available))
                cursor = slotEnd
            }
            slots
        }
    }
}
