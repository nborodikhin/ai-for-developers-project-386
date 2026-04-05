package com.calendar.services

import com.calendar.db.Bookings
import com.calendar.db.EventTypes
import com.calendar.generated.model.Booking
import com.calendar.generated.model.CreateBookingRequest
import com.calendar.generated.model.UpdateBookingRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ConflictException(message: String) : RuntimeException(message)

@Service
class BookingService {

    private val fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun create(eventTypeId: Long, req: CreateBookingRequest): Booking? = transaction {
        val eventTypeRow = EventTypes
            .selectAll()
            .where { (EventTypes.id eq eventTypeId) and (EventTypes.deleted eq false) }
            .singleOrNull() ?: return@transaction null

        val durationMinutes = eventTypeRow[EventTypes.durationMinutes]
        val eventTypeName   = eventTypeRow[EventTypes.name]

        val startTime = req.startTime
        val endTime   = startTime.plusMinutes(durationMinutes.toLong())

        val newStart = startTime.format(fmt)
        val newEnd   = endTime.format(fmt)

        val conflict = Bookings
            .selectAll()
            .where {
                (Bookings.eventTypeId eq eventTypeId) and
                (Bookings.startTime less newEnd) and
                (Bookings.endTime greater newStart)
            }
            .count() > 0

        if (conflict) throw ConflictException("Time slot is already booked")

        val id = Bookings.insert {
            it[Bookings.eventTypeId]   = eventTypeId
            it[Bookings.eventTypeName] = eventTypeName
            it[Bookings.guestName]     = req.guestName
            it[Bookings.guestEmail]    = req.guestEmail
            it[Bookings.comment]       = req.comment
            it[Bookings.startTime]     = newStart
            it[Bookings.endTime]       = newEnd
        } get Bookings.id

        Booking(
            id            = id,
            eventTypeId   = eventTypeId,
            eventTypeName = eventTypeName,
            guestName     = req.guestName,
            guestEmail    = req.guestEmail,
            comment       = req.comment,
            startTime     = startTime,
            endTime       = endTime
        )
    }

    fun list(email: String?): List<Booking> = transaction {
        val query = if (email != null)
            Bookings.selectAll().where { Bookings.guestEmail eq email }
        else
            Bookings.selectAll()
        query.map { it.toBooking() }
    }

    fun update(id: Long, req: UpdateBookingRequest): Booking? = transaction {
        val exists = Bookings.selectAll().where { Bookings.id eq id }.singleOrNull()
            ?: return@transaction null

        Bookings.update(where = { Bookings.id eq id }) {
            if (req.guestName != null) it[guestName] = req.guestName
            if (req.comment   != null) it[comment]   = req.comment
        }

        Bookings.selectAll().where { Bookings.id eq id }.single().toBooking()
    }

    fun delete(id: Long): Boolean = transaction {
        Bookings.deleteWhere { Bookings.id eq id } > 0
    }

    private fun ResultRow.toBooking() = Booking(
        id            = this[Bookings.id],
        eventTypeId   = this[Bookings.eventTypeId],
        eventTypeName = this[Bookings.eventTypeName],
        guestName     = this[Bookings.guestName],
        guestEmail    = this[Bookings.guestEmail],
        comment       = this[Bookings.comment],
        startTime     = OffsetDateTime.parse(this[Bookings.startTime], fmt),
        endTime       = OffsetDateTime.parse(this[Bookings.endTime], fmt)
    )
}
