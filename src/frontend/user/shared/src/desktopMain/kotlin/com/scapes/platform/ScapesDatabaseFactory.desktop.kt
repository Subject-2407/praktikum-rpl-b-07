package com.scapes.platform

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.scapes.data.local.db.ScapesDatabase
import java.nio.file.Files

private const val DatabaseFileName = "scapes.db"

/** Desktop SQLDelight database factory. */
actual class ScapesDatabaseFactory {
    /** Creates the application database. */
    actual fun createDatabase(): ScapesDatabase {
        val databasePath = appDataDirectory("database").resolve(DatabaseFileName)
        val shouldCreateSchema = !Files.exists(databasePath)
        Files.createDirectories(databasePath.parent)

        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.toAbsolutePath()}")
        if (shouldCreateSchema) {
            ScapesDatabase.Schema.create(driver)
        }
        return ScapesDatabase(driver)
    }
}
