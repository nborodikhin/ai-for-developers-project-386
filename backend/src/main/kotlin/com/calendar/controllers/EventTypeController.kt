package com.calendar.controllers

import com.calendar.generated.api.EventTypesApi
import com.calendar.generated.model.CreateEventTypeRequest
import com.calendar.generated.model.EventType
import com.calendar.services.EventTypeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class EventTypeController(
    private val service: EventTypeService
) : EventTypesApi {

    override fun eventTypesCreate(createEventTypeRequest: CreateEventTypeRequest): ResponseEntity<EventType> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(createEventTypeRequest))

    override fun eventTypesList(): ResponseEntity<List<EventType>> =
        ResponseEntity.ok(service.list())

    override fun eventTypesRead(id: Long): ResponseEntity<EventType> =
        service.findById(id)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    override fun eventTypesUpdate(id: Long, createEventTypeRequest: CreateEventTypeRequest): ResponseEntity<EventType> =
        service.update(id, createEventTypeRequest)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    override fun eventTypesDelete(id: Long): ResponseEntity<Unit> =
        if (service.softDelete(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
}
