package com.scapes.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts the shared dependency graph from application shells. */
fun initializeScapesKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(scapesModules())
    }
}
