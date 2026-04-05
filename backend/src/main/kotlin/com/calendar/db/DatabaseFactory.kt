package com.calendar.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component

@Component
class DatabaseFactory {

    @PostConstruct
    fun init() {
        // Pool size 1: all transactions share the same connection → same in-memory SQLite DB
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite::memory:"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
        }
        Database.connect(HikariDataSource(config))
        transaction {
            SchemaUtils.create(EventTypes, Bookings)
        }
    }
}
