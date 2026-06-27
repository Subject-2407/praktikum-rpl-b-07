package com.scapes.di

import org.koin.core.module.Module

/** Platform-provided dependencies for shared data and presentation layers. */
expect fun platformModule(): Module
