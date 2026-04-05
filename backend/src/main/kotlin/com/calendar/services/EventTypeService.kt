package com.calendar.services

import com.calendar.db.EventTypes
import com.calendar.generated.model.CreateEventTypeRequest
import com.calendar.generated.model.EventType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class EventTypeService {

    fun create(req: CreateEventTypeRequest): EventType = transaction {
        val id = EventTypes.insert {
            it[name]            = req.name
            it[description]     = req.description
            it[durationMinutes] = req.durationMinutes
            it[deleted]         = false
        } get EventTypes.id

        EventType(
            id              = id,
            name            = req.name,
            description     = req.description,
            durationMinutes = req.durationMinutes
        )
    }

    fun list(): List<EventType> = transaction {
        EventTypes
            .selectAll()
            .where { EventTypes.deleted eq false }
            .map { it.toEventType() }
    }

    fun findById(id: Long): EventType? = transaction {
        EventTypes
            .selectAll()
            .where { (EventTypes.id eq id) and (EventTypes.deleted eq false) }
            .singleOrNull()
            ?.toEventType()
    }

    fun update(id: Long, req: CreateEventTypeRequest): EventType? = transaction {
        val count = EventTypes.update(
            where = { (EventTypes.id eq id) and (EventTypes.deleted eq false) }
        ) {
            it[name]            = req.name
            it[description]     = req.description
            it[durationMinutes] = req.durationMinutes
        }
        if (count == 0) return@transaction null

        EventTypes.selectAll().where { EventTypes.id eq id }.single().toEventType()
    }

    fun softDelete(id: Long): Boolean = transaction {
        val count = EventTypes.update(
            where = { (EventTypes.id eq id) and (EventTypes.deleted eq false) }
        ) {
            it[deleted] = true
        }
        count > 0
    }

    private fun ResultRow.toEventType() = EventType(
        id              = this[EventTypes.id],
        name            = this[EventTypes.name],
        description     = this[EventTypes.description],
        durationMinutes = this[EventTypes.durationMinutes]
    )
}
