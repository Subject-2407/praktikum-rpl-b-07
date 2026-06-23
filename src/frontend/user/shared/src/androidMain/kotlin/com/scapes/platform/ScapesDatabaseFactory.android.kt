package com.scapes.platform

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.scapes.data.local.db.ScapesDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Android SQLDelight database factory **/
actual class ScapesDatabaseFactory : KoinComponent {
    private val context: Context by inject()

    actual fun createDatabase(): ScapesDatabase {
        val driver: SqlDriver = AndroidSqliteDriver(
            schema = ScapesDatabase.Schema,
            context = context,
            name = "scapes.db"
        )
        return ScapesDatabase(driver)
    }
}
