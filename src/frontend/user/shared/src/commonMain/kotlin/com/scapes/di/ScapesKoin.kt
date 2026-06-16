package com.scapes.di

import org.koin.core.context.startKoin

/** Starts the shared dependency graph from application shells. */
fun initializeScapesKoin() {
    startKoin { modules(scapesModules()) }
}
