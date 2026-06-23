package com.scapes.platform

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.scapes.data.local.db.ScapesDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val DatabaseName = "scapes.db"

/** Android SQLDelight database factory. */
actual class ScapesDatabaseFactory : KoinComponent {
    private val context: Context by inject()

    /** Creates the application database. */
    actual fun createDatabase(): ScapesDatabase {
        val driver = AndroidSqliteDriver(ScapesDatabase.Schema, context, DatabaseName)
        return ScapesDatabase(driver)
    }
}
