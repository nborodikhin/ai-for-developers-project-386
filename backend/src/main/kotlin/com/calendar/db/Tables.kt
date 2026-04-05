package com.calendar.db

import org.jetbrains.exposed.sql.Table

object EventTypes : Table("event_types") {
    val id              = long("id").autoIncrement()
    val name            = varchar("name", 255)
    val description     = text("description")
    val durationMinutes = integer("duration_minutes")
    val deleted         = bool("deleted").default(false)

    override val primaryKey = PrimaryKey(id)
}

object Bookings : Table("bookings") {
    val id            = long("id").autoIncrement()
    val eventTypeId   = long("event_type_id").references(EventTypes.id)
    val eventTypeName = varchar("event_type_name", 255)
    val guestName     = varchar("guest_name", 255)
    val guestEmail    = varchar("guest_email", 255)
    val comment       = text("comment").nullable()
    val startTime     = varchar("start_time", 50)   // ISO-8601 UTC string
    val endTime       = varchar("end_time", 50)

    override val primaryKey = PrimaryKey(id)
}
