package com.scapes.platform

import com.scapes.data.local.db.ScapesDatabase

/** Platform SQLDelight database factory. */
expect class ScapesDatabaseFactory() {
    /** Creates the application database. */
    fun createDatabase(): ScapesDatabase
}
