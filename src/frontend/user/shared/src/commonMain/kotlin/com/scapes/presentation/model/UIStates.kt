package com.scapes.presentation.model

import androidx.compose.runtime.Immutable
import com.scapes.domain.model.WallpaperSource

@Immutable
data class SourceOption(
    val source: WallpaperSource,
    val label: String,
) {
    companion object {
        fun scapes() = SourceOption(WallpaperSource.SCAPES_API, "Scapes")
        fun pexels() = SourceOption(WallpaperSource.PEXELS, "Pexels")
        fun unsplash() = SourceOption(WallpaperSource.UNSPLASH, "Unsplash")
        fun pixabay() = SourceOption(WallpaperSource.PIXABAY, "Pixabay")
        fun defaults(): List<SourceOption> = listOf(scapes(), pexels(), unsplash(), pixabay())
    }
}

@Immutable
data class WallpaperFeedState(
    val query: String = "",
    val source: WallpaperSource = WallpaperSource.PEXELS,
    val wallpapers: List<com.scapes.presentation.ui.components.WallpaperUi> = emptyList(),
    val nextPage: Int = 0,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = true,
    val message: String? = null,
)

@Immutable
data class LandingSectionState(
    val title: String,
    val wallpapers: List<com.scapes.presentation.ui.components.WallpaperUi> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

@Immutable
data class LandingFeedState(
    val source: WallpaperSource,
    val sections: List<LandingSectionState>,
) {
    fun updateSection(title: String, sectionState: LandingSectionState): LandingFeedState =
        copy(
            sections = sections.map { section ->
                if (section.title == title) sectionState else section
            }
        )

    companion object {
        fun loading(source: WallpaperSource): LandingFeedState =
            LandingFeedState(
                source = source,
                sections = listOf(
                    "Quiet Forests", "Amber Evenings", "Urban Lights",
                    "Stone and Ruins", "Soft Horizons", "Road Motion", "Minimal Calm"
                ).map { LandingSectionState(title = it, isLoading = true) }
            )

        fun message(source: WallpaperSource, message: String): LandingFeedState =
            LandingFeedState(
                source = source,
                sections = listOf(
                    "Quiet Forests", "Amber Evenings", "Urban Lights",
                    "Stone and Ruins", "Soft Horizons", "Road Motion", "Minimal Calm"
                ).map { LandingSectionState(title = it, message = message) }
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
            forms = forms.map { form ->
                if (form.sourceOption.source == source) form.transform() else form
            }
        )

    companion object {
        fun fromSources(): SettingsUiState =
            SettingsUiState(
                forms = listOf(SourceOption.pexels(), SourceOption.unsplash(), SourceOption.pixabay())
                    .map { ApiKeyFormState(sourceOption = it) }
            )
    }
}
