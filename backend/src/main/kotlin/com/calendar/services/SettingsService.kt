package com.calendar.services

import com.calendar.db.Settings
import com.calendar.generated.model.OwnerSettings
import com.calendar.generated.model.UpdateTimezoneRequest
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Service

@Service
class SettingsService {

    fun getSettings(): OwnerSettings = transaction {
        val row = Settings.selectAll().single()
        OwnerSettings(ownerTimezone = row[Settings.timezone])
    }

    fun updateTimezone(request: UpdateTimezoneRequest): OwnerSettings = transaction {
        Settings.update { it[timezone] = request.timezone }
        OwnerSettings(ownerTimezone = request.timezone)
    }

    fun getOwnerTimezone(): String = transaction {
        Settings.selectAll().single()[Settings.timezone]
    }
}
