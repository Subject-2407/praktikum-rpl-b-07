package com.scapes.domain.model

/** Wallpaper destination supported by Scapes. */
enum class ApplyTarget {
    /** Windows desktop wallpaper. */
    DESKTOP,

    /** Android home screen wallpaper. */
    HOME_SCREEN,

    /** Android lock screen wallpaper. */
    LOCK_SCREEN,

    /** Android home and lock screen wallpaper. */
    BOTH_SCREENS,
}
