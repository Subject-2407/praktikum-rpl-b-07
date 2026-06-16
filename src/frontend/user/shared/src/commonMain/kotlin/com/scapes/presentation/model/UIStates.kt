package com.scapes.presentation.model

import androidx.compose.runtime.Immutable
import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.ui.theme.ThemePreference

@Immutable
data class SourceOption(val source: WallpaperSource, val label: String) {
    companion object {
        fun scapes() = SourceOption(WallpaperSource.SCAPES_API, "Scapes")

        fun pexels() = SourceOption(WallpaperSource.PEXELS, "Pexels")

        fun unsplash() = SourceOption(WallpaperSource.UNSPLASH, "Unsplash")

        fun pixabay() = SourceOption(WallpaperSource.PIXABAY, "Pixabay")

        fun defaults(): List<SourceOption> = listOf(scapes(), pexels(), unsplash(), pixabay())

        fun externalDefaults(): List<SourceOption> = listOf(pexels(), unsplash(), pixabay())
    }
}

enum class ScapesDestination {
    HOME,
    SEARCH_RESULTS,
    SETTINGS,
}

@Immutable
data class ScapesUiState(
    val drawerOpen: Boolean = false,
    val query: String = "",
    val selectedSource: SourceOption = SourceOption.scapes(),
    val destination: ScapesDestination = ScapesDestination.HOME,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
) {
    val showResults: Boolean
        get() = destination == ScapesDestination.SEARCH_RESULTS

    val showSettings: Boolean
        get() = destination == ScapesDestination.SETTINGS
}

@Immutable
data class WallpaperFeedState(
    val query: String = "",
    val source: WallpaperSource = WallpaperSource.SCAPES_API,
    val wallpapers: List<com.scapes.presentation.ui.components.WallpaperUi> = emptyList(),
    val nextPage: Int = 0,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = true,
    val message: String? = null,
)

@Immutable
data class WallpaperActionState(
    val isSaving: Boolean = false,
    val isApplying: Boolean = false,
    val localPath: String? = null,
    val message: String? = null,
)

internal val DefaultLandingSectionTitles =
    listOf(
        "Quiet Forests",
        "Amber Evenings",
        "Urban Lights",
        "Stone and Ruins",
        "Soft Horizons",
        "Road Motion",
        "Minimal Calm",
    )

@Immutable
data class LandingSectionState(
    val title: String,
    val wallpapers: List<com.scapes.presentation.ui.components.WallpaperUi> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

@Immutable
data class LandingFeedState(val source: WallpaperSource, val sections: List<LandingSectionState>) {
    fun updateSection(title: String, sectionState: LandingSectionState): LandingFeedState =
        copy(
            sections =
                sections.map { section -> if (section.title == title) sectionState else section }
        )

    companion object {
        fun loading(source: WallpaperSource): LandingFeedState =
            LandingFeedState(
                source = source,
                sections =
                    DefaultLandingSectionTitles.map {
                        LandingSectionState(title = it, isLoading = true)
                    },
            )

        fun message(source: WallpaperSource, message: String): LandingFeedState =
            LandingFeedState(
                source = source,
                sections =
                    DefaultLandingSectionTitles.map {
                        LandingSectionState(title = it, message = message)
                    },
            )
    }
}

@Immutable
data class ApiKeyFormState(
    val sourceOption: SourceOption,
    val input: String = "",
    val maskedKey: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isRemoving: Boolean = false,
    val message: String? = null,
)

@Immutable
data class SettingsUiState(
    val forms: List<ApiKeyFormState>,
    val downloadFolderInput: String = "",
    val downloadOrganization: DownloadOrganization = DownloadOrganization.BY_CATEGORY,
    val isSavingDownloadSettings: Boolean = false,
    val downloadSettingsMessage: String? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
) {
    fun form(source: WallpaperSource): ApiKeyFormState? =
        forms.firstOrNull { it.sourceOption.source == source }

    fun updateForm(
        source: WallpaperSource,
        transform: ApiKeyFormState.() -> ApiKeyFormState,
    ): SettingsUiState =
        copy(
            forms =
                forms.map { form ->
                    if (form.sourceOption.source == source) form.transform() else form
                }
        )

    fun downloadSettings(): DownloadSettings =
        DownloadSettings(folderPath = downloadFolderInput, organization = downloadOrganization)

    companion object {
        fun fromSources(
            downloadSettings: DownloadSettings =
                DownloadSettings(
                    folderPath = "Scapes",
                    organization = DownloadOrganization.BY_CATEGORY,
                )
        ): SettingsUiState =
            SettingsUiState(
                forms = SourceOption.externalDefaults().map { ApiKeyFormState(sourceOption = it) },
                downloadFolderInput = downloadSettings.folderPath,
                downloadOrganization = downloadSettings.organization,
            )
    }
}
