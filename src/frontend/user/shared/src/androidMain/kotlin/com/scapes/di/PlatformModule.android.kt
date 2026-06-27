package com.scapes.di

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.TargetDevice
import com.scapes.platform.DirectoryPicker
import com.scapes.platform.EncryptedStorage
import com.scapes.platform.FileSystemProvider
import com.scapes.platform.PreferencesStorage
import com.scapes.platform.ScapesDatabaseFactory
import com.scapes.platform.WallpaperApplier
import com.scapes.presentation.model.ScapesAppConfig
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DirectoryPicker() }
    single { EncryptedStorage() }
    single { PreferencesStorage() }
    single { ScapesDatabaseFactory().createDatabase() }
    single { FileSystemProvider() }
    single { WallpaperApplier() }
    single {
        ScapesAppConfig(
            defaultTargetDevice = TargetDevice.MOBILE,
            defaultApplyTarget = ApplyTarget.BOTH_SCREENS,
        )
    }
}
