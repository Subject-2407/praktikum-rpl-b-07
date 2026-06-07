package com.scapes.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Data-layer dependency bindings.
 */
fun dataModule(): Module =
    module {
        // Repository implementations are added as feature data sources are built.
    }
