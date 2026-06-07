package com.scapes.di

import org.koin.core.module.Module

/**
 * Koin module list shared by application shells.
 */
fun scapesModules(): List<Module> =
    listOf(
        dataModule(),
        domainModule(),
        presentationModule(),
    )
