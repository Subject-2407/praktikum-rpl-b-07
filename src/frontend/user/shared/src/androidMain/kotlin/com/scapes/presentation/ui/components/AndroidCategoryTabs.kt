package com.scapes.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.ui.theme.ScapesThemeColors


@Composable
fun AndroidCategoryTabs(
    categories: List<WallpaperCategory>,
    activeCategorySlug: String?,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    enabledSources: Set<WallpaperSource>,
    selectedSource: SourceOption,
    onFeedSelected: () -> Unit,
    onCategorySelected: (WallpaperCategory) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.base.copy(alpha = 0.94f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryTab(
                label = "Feed",
                active = activeCategorySlug == null,
                colors = colors,
                onClick = onFeedSelected,
            )
            categories.forEach { category ->
                CategoryTab(
                    label = category.name,
                    active = activeCategorySlug == category.slug,
                    colors = colors,
                    onClick = { onCategorySelected(category) },
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp).height(24.dp).background(colors.support.copy(alpha = 0.3f)))

        SourceDropdown(
            selectedSource = selectedSource,
            colors = colors,
            isDarkMode = isDarkMode,
            enabledSources = enabledSources,
            onSourceSelected = onSourceSelected,
            modifier = Modifier.width(110.dp),
        )
    }
}

@Composable
private fun CategoryTab(
    label: String,
    active: Boolean,
    colors: ScapesThemeColors,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier =
            Modifier
                .hoverable(interactionSource)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color =
                when {
                    active -> colors.text
                    isHovered -> colors.text.copy(alpha = 0.9f)
                    else -> colors.secondaryText
                },
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
        Box(
            modifier =
                Modifier.padding(top = 4.dp)
                    .height(2.dp)
                    .width(if (active) 28.dp else 0.dp)
                    .background(
                        if (active) colors.accent else Color.Transparent
                    )
        )
    }
}
