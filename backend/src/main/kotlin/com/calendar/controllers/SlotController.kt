package com.calendar.controllers

import com.calendar.generated.api.AvailableSlotsApi
import com.calendar.generated.model.AvailableSlot
import com.calendar.services.SlotService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class SlotController(
    private val service: SlotService
) : AvailableSlotsApi {

    override fun availableSlotsList(eventTypeId: Long, date: LocalDate): ResponseEntity<List<AvailableSlot>> =
        service.getSlots(eventTypeId, date)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}
