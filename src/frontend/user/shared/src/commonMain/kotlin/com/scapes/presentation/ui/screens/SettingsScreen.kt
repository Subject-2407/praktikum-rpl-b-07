package com.scapes.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.ApiKeyFormState
import com.scapes.presentation.model.SettingsUiState
import com.scapes.presentation.ui.components.BackGlyph
import com.scapes.presentation.ui.components.IconShell
import com.scapes.presentation.ui.theme.ScapesThemeColors

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    colors: ScapesThemeColors,
    onInputChange: (WallpaperSource, String) -> Unit,
    onSave: (WallpaperSource) -> Unit,
    onRemove: (WallpaperSource) -> Unit,
    onDownloadFolderChange: (String) -> Unit,
    onChooseDownloadFolder: () -> Unit,
    onDownloadOrganizationChange: (DownloadOrganization) -> Unit,
    onSaveDownloadSettings: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.base, colors.elevated.copy(alpha = 0.84f), colors.base)
                    )
                ),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(colors.base.copy(alpha = 0.96f))
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .height(60.dp)
                        .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconShell(onClick = onBack, colors = colors) { BackGlyph(colors.text) }
                Text("Settings", color = colors.text, style = MaterialTheme.typography.titleLarge)
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Downloads",
                    color = colors.text,
                    style = MaterialTheme.typography.headlineMedium,
                )
                DownloadSettingsCard(
                    state = state,
                    colors = colors,
                    onFolderChange = onDownloadFolderChange,
                    onChooseFolder = onChooseDownloadFolder,
                    onOrganizationChange = onDownloadOrganizationChange,
                    onSave = onSaveDownloadSettings,
                )
                Text(
                    "API Keys",
                    color = colors.text,
                    style = MaterialTheme.typography.headlineMedium,
                )
                state.message?.let { message ->
                    SearchStatusPanel(message = message, colors = colors, loading = state.isLoading)
                }
                state.forms.forEach { form ->
                    ApiKeyCard(
                        form = form,
                        colors = colors,
                        onInputChange = onInputChange,
                        onSave = onSave,
                        onRemove = onRemove,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadSettingsCard(
    state: SettingsUiState,
    colors: ScapesThemeColors,
    onFolderChange: (String) -> Unit,
    onChooseFolder: () -> Unit,
    onOrganizationChange: (DownloadOrganization) -> Unit,
    onSave: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .border(1.dp, colors.support.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Download folder",
                color = colors.text,
                style = MaterialTheme.typography.titleMedium,
            )
            SettingsTextInput(
                value = state.downloadFolderInput,
                placeholder = "Scapes",
                colors = colors,
                enabled = !state.isSavingDownloadSettings,
                onValueChange = onFolderChange,
            )
            SettingsActionButton(
                label = "Browse folder",
                colors = colors,
                enabled = !state.isSavingDownloadSettings,
                outlined = true,
                onClick = onChooseFolder,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DownloadOrganization.entries.forEach { organization ->
                    SettingsChoiceButton(
                        label = organization.label(),
                        selected = state.downloadOrganization == organization,
                        colors = colors,
                        onClick = { onOrganizationChange(organization) },
                    )
                }
            }
            SettingsActionButton(
                label = if (state.isSavingDownloadSettings) "Saving" else "Save downloads",
                colors = colors,
                enabled = state.downloadFolderInput.isNotBlank() && !state.isSavingDownloadSettings,
                onClick = onSave,
            )
            state.downloadSettingsMessage?.let { message ->
                Text(
                    message,
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ApiKeyCard(
    form: ApiKeyFormState,
    colors: ScapesThemeColors,
    onInputChange: (WallpaperSource, String) -> Unit,
    onSave: (WallpaperSource) -> Unit,
    onRemove: (WallpaperSource) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .border(1.dp, colors.support.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(colors.text))
                Text(
                    form.sourceOption.label,
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    form.maskedKey ?: "Not set",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            ApiKeyInput(
                value = form.input,
                onValueChange = { onInputChange(form.sourceOption.source, it) },
                colors = colors,
                enabled = !form.isSaving && !form.isRemoving,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(
                    label = if (form.isSaving) "Saving" else "Save",
                    colors = colors,
                    enabled = form.input.isNotBlank() && !form.isSaving && !form.isRemoving,
                    onClick = { onSave(form.sourceOption.source) },
                )
                SettingsActionButton(
                    label = if (form.isRemoving) "Resetting" else "Reset to Default",
                    colors = colors,
                    enabled = form.maskedKey != null && !form.isSaving && !form.isRemoving,
                    outlined = true,
                    onClick = { onRemove(form.sourceOption.source) },
                )
            }

            form.message?.let { message ->
                Text(
                    message,
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ApiKeyInput(
    value: String,
    onValueChange: (String) -> Unit,
    colors: ScapesThemeColors,
    enabled: Boolean,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.text, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.base.copy(alpha = 0.72f))
                        .border(1.dp, colors.support.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    Text("Enter API key", color = colors.secondaryText)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun SettingsTextInput(
    value: String,
    placeholder: String,
    colors: ScapesThemeColors,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.text, fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.base.copy(alpha = 0.72f))
                        .border(1.dp, colors.support.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    Text(placeholder, color = colors.secondaryText)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun SettingsChoiceButton(
    label: String,
    selected: Boolean,
    colors: ScapesThemeColors,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier =
            Modifier.height(38.dp)
                .clip(shape)
                .background(if (selected) colors.text else Color.Transparent)
                .border(1.dp, colors.text.copy(alpha = 0.7f), shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) colors.base else colors.text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

private fun DownloadOrganization.label(): String =
    when (this) {
        DownloadOrganization.BY_CATEGORY -> "Category"
        DownloadOrganization.BY_SOURCE -> "Source"
        DownloadOrganization.NONE -> "Flat"
    }

@Composable
private fun SettingsActionButton(
    label: String,
    colors: ScapesThemeColors,
    enabled: Boolean,
    outlined: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val background =
        when {
            outlined -> Color.Transparent
            enabled -> colors.text
            else -> colors.secondaryText.copy(alpha = 0.28f)
        }
    val foreground =
        when {
            outlined -> colors.text
            enabled -> colors.base
            else -> colors.secondaryText
        }
    val clickableModifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier =
            Modifier.height(40.dp)
                .clip(shape)
                .background(background)
                .border(1.dp, colors.text.copy(alpha = if (enabled) 0.75f else 0.24f), shape)
                .then(clickableModifier)
                .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = foreground, style = MaterialTheme.typography.labelLarge)
    }
}
