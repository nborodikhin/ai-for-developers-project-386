package com.calendar.controllers

import com.calendar.generated.api.BookingsApi
import com.calendar.generated.api.EventTypeBookingsApi
import com.calendar.generated.model.Booking
import com.calendar.generated.model.CreateBookingRequest
import com.calendar.generated.model.UpdateBookingRequest
import com.calendar.services.BookingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BookingController(
    private val service: BookingService
) : BookingsApi, EventTypeBookingsApi {

    override fun eventTypeBookingsCreate(
        eventTypeId: Long,
        createBookingRequest: CreateBookingRequest
    ): ResponseEntity<Booking> =
        service.create(eventTypeId, createBookingRequest)
            ?.let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
            ?: ResponseEntity.notFound().build()

    override fun bookingsList(email: String?): ResponseEntity<List<Booking>> =
        ResponseEntity.ok(service.list(email))

    override fun bookingsUpdate(id: Long, updateBookingRequest: UpdateBookingRequest): ResponseEntity<Booking> =
        service.update(id, updateBookingRequest)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    override fun bookingsDelete(id: Long): ResponseEntity<Unit> =
        if (service.delete(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
}
