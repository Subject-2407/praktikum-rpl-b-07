package com.scapes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.usecase.GetApiKeyUseCase
import com.scapes.domain.usecase.GetDownloadSettingsUseCase
import com.scapes.domain.usecase.RemoveApiKeyUseCase
import com.scapes.domain.usecase.SaveApiKeyUseCase
import com.scapes.domain.usecase.UpdateDownloadSettingsUseCase
import com.scapes.presentation.model.ApiKeyFormState
import com.scapes.presentation.model.SettingsUiState
import com.scapes.presentation.model.SourceOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MaxApiKeyLength = 256

/** Owns settings screen state: download preferences and personal API keys. */
class SettingsViewModel(
    private val getDownloadSettingsUseCase: GetDownloadSettingsUseCase,
    private val updateDownloadSettingsUseCase: UpdateDownloadSettingsUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val removeApiKeyUseCase: RemoveApiKeyUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingsUiState.fromSources())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    private val mutableApiKeyChanges = MutableSharedFlow<WallpaperSource>(extraBufferCapacity = 1)
    val apiKeyChanges: SharedFlow<WallpaperSource> = mutableApiKeyChanges.asSharedFlow()

    private var loadGeneration = 0

    fun load() {
        val generation = ++loadGeneration
        mutableUiState.update { state -> state.copy(isLoading = true, message = null) }

        viewModelScope.launch {
            var message: String? = null
            val downloadSettings =
                when (val result = getDownloadSettingsUseCase()) {
                    is ScapesResult.Error -> {
                        message = result.message
                        uiState.value.downloadSettings()
                    }

                    ScapesResult.Loading -> uiState.value.downloadSettings()
                    is ScapesResult.Success -> result.data
                }
            val loadedForms =
                SourceOption.externalDefaults().map { sourceOption ->
                    when (val result = getApiKeyUseCase(sourceOption.source)) {
                        is ScapesResult.Error -> {
                            message = result.message
                            ApiKeyFormState(sourceOption = sourceOption)
                        }

                        ScapesResult.Loading ->
                            ApiKeyFormState(sourceOption = sourceOption, isLoading = true)

                        is ScapesResult.Success ->
                            ApiKeyFormState(
                                sourceOption = sourceOption,
                                maskedKey = result.data?.value?.let(::maskedApiKey),
                            )
                    }
                }

            if (generation != loadGeneration) {
                return@launch
            }

            mutableUiState.update {
                SettingsUiState(
                    forms = loadedForms,
                    downloadFolderInput = downloadSettings.folderPath,
                    downloadOrganization = downloadSettings.organization,
                    isLoading = false,
                    message = message,
                )
            }
        }
    }

    fun updateApiKeyInput(source: WallpaperSource, input: String) {
        mutableUiState.updateForm(source) {
            copy(input = input.trim().take(MaxApiKeyLength), message = null)
        }
    }

    fun saveApiKey(source: WallpaperSource) {
        val rawKey = uiState.value.form(source)?.input.orEmpty().trim()
        mutableUiState.updateForm(source) { copy(isSaving = true, message = null) }

        viewModelScope.launch {
            when (val result = saveApiKeyUseCase(source, rawKey)) {
                is ScapesResult.Error ->
                    mutableUiState.updateForm(source) {
                        copy(isSaving = false, message = result.message)
                    }

                ScapesResult.Loading -> mutableUiState.updateForm(source) { copy(isSaving = true) }

                is ScapesResult.Success -> {
                    mutableUiState.updateForm(source) {
                        copy(
                            input = "",
                            maskedKey = maskedApiKey(rawKey),
                            isSaving = false,
                            message = "Saved",
                        )
                    }
                    mutableApiKeyChanges.tryEmit(source)
                }
            }
        }
    }

    fun removeApiKey(source: WallpaperSource) {
        mutableUiState.updateForm(source) { copy(isRemoving = true, message = null) }

        viewModelScope.launch {
            when (val result = removeApiKeyUseCase(source)) {
                is ScapesResult.Error ->
                    mutableUiState.updateForm(source) {
                        copy(isRemoving = false, message = result.message)
                    }

                ScapesResult.Loading ->
                    mutableUiState.updateForm(source) { copy(isRemoving = true) }

                is ScapesResult.Success -> {
                    mutableUiState.updateForm(source) {
                        copy(input = "", maskedKey = null, isRemoving = false, message = "Reset to default")
                    }
                    mutableApiKeyChanges.tryEmit(source)
                }
            }
        }
    }

    fun updateDownloadFolderInput(input: String) {
        mutableUiState.update { state ->
            state.copy(downloadFolderInput = input.take(260), downloadSettingsMessage = null)
        }
    }

    fun updateDownloadOrganization(organization: DownloadOrganization) {
        mutableUiState.update { state ->
            state.copy(downloadOrganization = organization, downloadSettingsMessage = null)
        }
    }

    fun saveDownloadSettings() {
        val nextSettings = uiState.value.downloadSettings()
        mutableUiState.update { state ->
            state.copy(isSavingDownloadSettings = true, downloadSettingsMessage = null)
        }

        viewModelScope.launch {
            when (val result = updateDownloadSettingsUseCase(nextSettings)) {
                is ScapesResult.Error ->
                    mutableUiState.update { state ->
                        state.copy(
                            isSavingDownloadSettings = false,
                            downloadSettingsMessage = result.message,
                        )
                    }

                ScapesResult.Loading ->
                    mutableUiState.update { state -> state.copy(isSavingDownloadSettings = true) }

                is ScapesResult.Success ->
                    mutableUiState.update { state ->
                        state.copy(
                            isSavingDownloadSettings = false,
                            downloadFolderInput = nextSettings.folderPath.trim(),
                            downloadSettingsMessage = "Download settings saved.",
                        )
                    }
            }
        }
    }

    private fun MutableStateFlow<SettingsUiState>.updateForm(
        source: WallpaperSource,
        transform: ApiKeyFormState.() -> ApiKeyFormState,
    ) {
        update { state -> state.updateForm(source, transform) }
    }
}

private fun maskedApiKey(rawKey: String): String {
    val key = rawKey.trim()
    return if (key.length > 4) {
        "***${key.takeLast(4)}"
    } else {
        "****"
    }
}
