package com.scapes.platform

import com.scapes.data.local.db.ScapesDatabase

/** Android SQLDelight database factory placeholder. */
actual class ScapesDatabaseFactory {
    /** Creates the application database. */
    actual fun createDatabase(): ScapesDatabase =
        throw UnsupportedOperationException("Android database requires Context wiring.")
}
