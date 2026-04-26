package com.calendar.controllers

import com.calendar.generated.api.SettingsApi
import com.calendar.generated.model.OwnerSettings
import com.calendar.generated.model.UpdateTimezoneRequest
import com.calendar.services.SettingsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingsController(
    private val service: SettingsService
) : SettingsApi {

    override fun settingsGet(): ResponseEntity<OwnerSettings> =
        ResponseEntity.ok(service.getSettings())

    override fun settingsUpdateTimezone(updateTimezoneRequest: UpdateTimezoneRequest): ResponseEntity<OwnerSettings> =
        ResponseEntity.ok(service.updateTimezone(updateTimezoneRequest))
}
