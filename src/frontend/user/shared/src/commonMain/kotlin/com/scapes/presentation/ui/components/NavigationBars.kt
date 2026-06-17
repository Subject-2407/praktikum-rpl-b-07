package com.scapes.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.SearchRecommendationType
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.ui.theme.ThemePreference
import com.scapes.shared.generated.resources.Res
import com.scapes.shared.generated.resources.pexels_dark
import com.scapes.shared.generated.resources.pexels_light
import com.scapes.shared.generated.resources.pixabay_dark
import com.scapes.shared.generated.resources.pixabay_light
import com.scapes.shared.generated.resources.scapes_dark
import com.scapes.shared.generated.resources.scapes_light
import com.scapes.shared.generated.resources.unsplash_dark
import com.scapes.shared.generated.resources.unsplash_light
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconShell(
    onClick: () -> Unit,
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
    hoverBackground: Color = colors.support.copy(alpha = 0.2f),
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier =
            modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !showBackground -> Color.Transparent
                        isHovered -> hoverBackground
                        else -> colors.surface.copy(alpha = 0.22f)
                    }
                )
                .pointerHoverIcon(PointerIcon.Hand)
                .hoverable(interactionSource)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun HomeAppBar(
    query: String,
    selectedSource: SourceOption,
    searchRecommendations: List<SearchRecommendation>,
    isLoadingRecommendations: Boolean,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    topBarModifier: Modifier = Modifier,
    onOpenMenu: () -> Unit,
    onQueryChange: (String) -> Unit,
    onRecommendationSelected: (SearchRecommendation) -> Unit,
    onDismissRecommendations: () -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    enabledSources: Set<WallpaperSource>,
    onToggleTheme: () -> Unit,
    onSearch: () -> Unit,
    windowControls: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(64.dp)
                .then(topBarModifier)
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter =
                painterResource(
                    if (isDarkMode) Res.drawable.scapes_dark else Res.drawable.scapes_light
                ),
            contentDescription = "Scapes",
            modifier =
                Modifier.height(36.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(onClick = onOpenMenu),
        )
        SourceDropdown(
            selectedSource = selectedSource,
            colors = colors,
            isDarkMode = isDarkMode,
            enabledSources = enabledSources,
            onSourceSelected = onSourceSelected,
            modifier = Modifier.width(118.dp),
        )
        SearchInput(
            query = query,
            onQueryChange = onQueryChange,
            searchRecommendations = searchRecommendations,
            isLoadingRecommendations = isLoadingRecommendations,
            colors = colors,
            onRecommendationSelected = onRecommendationSelected,
            onDismissRecommendations = onDismissRecommendations,
            onSearch = onSearch,
            modifier = Modifier.weight(1f),
        )
        ThemeToggleButton(
            isDarkMode = isDarkMode,
            colors = colors,
            onToggleTheme = onToggleTheme,
        )
        IconShell(
            onClick = onSearch,
            colors = colors,
            hoverBackground = colors.support.copy(alpha = if (isDarkMode) 0.34f else 0.26f),
        ) {
            SearchGlyph(colors.text)
        }
        windowControls?.invoke()
    }
}

@Composable
fun SearchResultBar(
    query: String,
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    topBarModifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    searchRecommendations: List<SearchRecommendation>,
    isLoadingRecommendations: Boolean,
    onRecommendationSelected: (SearchRecommendation) -> Unit,
    onDismissRecommendations: () -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    enabledSources: Set<WallpaperSource>,
    onToggleTheme: () -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    windowControls: @Composable (() -> Unit)? = null,
) {
    HomeAppBar(
        query = query,
        selectedSource = selectedSource,
        searchRecommendations = searchRecommendations,
        isLoadingRecommendations = isLoadingRecommendations,
        colors = colors,
        isDarkMode = isDarkMode,
        topBarModifier = topBarModifier,
        onOpenMenu = onBack,
        onQueryChange = onQueryChange,
        onRecommendationSelected = onRecommendationSelected,
        onDismissRecommendations = onDismissRecommendations,
        onSourceSelected = onSourceSelected,
        enabledSources = enabledSources,
        onToggleTheme = onToggleTheme,
        onSearch = onSearch,
        windowControls = windowControls,
    )
}

@Composable
fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    searchRecommendations: List<SearchRecommendation>,
    isLoadingRecommendations: Boolean,
    colors: ScapesThemeColors,
    onRecommendationSelected: (SearchRecommendation) -> Unit,
    onDismissRecommendations: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fieldWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(color = colors.text, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { fieldWidthPx = it.size.width },
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (colors.text == Color.Black) {
                                    colors.elevated.copy(alpha = 0.82f)
                                } else {
                                    colors.surface.copy(alpha = 0.72f)
                                }
                            )
                            .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isBlank()) {
                        Text(
                            text = "Search wallpapers",
                            color = colors.secondaryText.copy(alpha = 0.56f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (searchRecommendations.isNotEmpty() && fieldWidthPx > 0) {
            Popup(
                alignment = Alignment.TopStart,
                offset = with(density) { IntOffset(0, 52.dp.roundToPx()) },
                onDismissRequest = onDismissRecommendations,
                properties = PopupProperties(focusable = false),
            ) {
                Column(
                    modifier =
                        Modifier.width(with(density) { fieldWidthPx.toDp() })
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(
                                1.dp,
                                colors.support.copy(alpha = 0.24f),
                                RoundedCornerShape(10.dp),
                            )
                ) {
                    searchRecommendations.forEachIndexed { index, recommendation ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable { onRecommendationSelected(recommendation) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = recommendation.label,
                                color = colors.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            recommendationTypeLabel(recommendation.type)
                                .takeIf { it.isNotBlank() }
                                ?.let { label ->
                                    Text(
                                        text = label,
                                        color = colors.secondaryText,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                    )
                                }
                        }
                        if (index < searchRecommendations.lastIndex) {
                            Spacer(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(1.dp)
                                        .background(colors.support.copy(alpha = 0.16f))
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun recommendationTypeLabel(type: SearchRecommendationType): String =
    when (type) {
        SearchRecommendationType.TAG -> "Tag"
        SearchRecommendationType.SYSTEM_CATEGORY -> "Category"
        SearchRecommendationType.USER_KEYWORD_CATEGORY -> ""
    }

@Composable
fun SourceDropdown(
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    enabledSources: Set<WallpaperSource>,
    onSourceSelected: (SourceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { expanded = true }
                    .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (selectedSource.source == WallpaperSource.SCAPES_API) {
                    Text(
                        text = "Default",
                        color = colors.text,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                } else {
                    SourceBadge(source = selectedSource.source, isDarkMode = isDarkMode)
                }
            }
            ChevronDownGlyph(colors.secondaryText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface).widthIn(min = 118.dp),
        ) {
            SourceOption.defaults().forEach { source ->
                val enabled =
                    source.source == WallpaperSource.SCAPES_API || source.source in enabledSources
                DropdownMenuItem(
                    enabled = enabled,
                    text = { SourceDropdownItemContent(source, enabled, colors, isDarkMode) },
                    onClick = {
                        expanded = false
                        if (enabled) {
                            onSourceSelected(source)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SourceDropdownItemContent(
    source: SourceOption,
    enabled: Boolean,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier =
            Modifier.width(108.dp)
                .hoverable(interactionSource)
                .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (source.source == WallpaperSource.SCAPES_API) {
            Text("Default", color = colors.text)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SourceBadge(source = source.source, isDarkMode = isDarkMode)
                if (!enabled && isHovered) {
                    Text(
                        text = "Fill API key first",
                        color = colors.secondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeToggleButton(
    isDarkMode: Boolean,
    colors: ScapesThemeColors,
    onToggleTheme: () -> Unit,
) {
    IconShell(
        onClick = onToggleTheme,
        colors = colors,
        hoverBackground = colors.support.copy(alpha = if (isDarkMode) 0.26f else 0.18f),
    ) {
        if (isDarkMode) {
            SunGlyph(colors.text, modifier = Modifier.size(18.dp))
        } else {
            MoonGlyph(
                color = colors.text,
                cutoutColor = colors.base,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SourceBadge(source: WallpaperSource, isDarkMode: Boolean) {
    val badgeModifier = Modifier.width(76.dp).height(20.dp)

    when (source) {
        WallpaperSource.SCAPES_API ->
            Box(
                modifier =
                    Modifier.size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF9C52E))
            )

        WallpaperSource.PEXELS ->
            Image(
                painter =
                    painterResource(
                        if (isDarkMode) Res.drawable.pexels_dark else Res.drawable.pexels_light
                    ),
                contentDescription = "Pexels",
                modifier = badgeModifier,
                contentScale = ContentScale.Fit,
            )

        WallpaperSource.UNSPLASH ->
            Image(
                painter =
                    painterResource(
                        if (isDarkMode) Res.drawable.unsplash_dark else Res.drawable.unsplash_light
                    ),
                contentDescription = "Unsplash",
                modifier = badgeModifier,
                contentScale = ContentScale.Fit,
            )

        WallpaperSource.PIXABAY ->
            Image(
                painter =
                    painterResource(
                        if (isDarkMode) Res.drawable.pixabay_dark else Res.drawable.pixabay_light
                    ),
                contentDescription = "Pixabay",
                modifier = badgeModifier,
                contentScale = ContentScale.Fit,
            )
    }
}

@Composable
fun CategoryTabs(
    categories: List<WallpaperCategory>,
    activeCategorySlug: String?,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onFeedSelected: () -> Unit,
    onCategorySelected: (WallpaperCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.base.copy(alpha = 0.94f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryTab(
            label = "Feed",
            active = activeCategorySlug == null,
            colors = colors,
            isDarkMode = isDarkMode,
            onClick = onFeedSelected,
        )
        categories.forEach { category ->
            CategoryTab(
                label = category.name,
                active = activeCategorySlug == category.slug,
                colors = colors,
                isDarkMode = isDarkMode,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

@Composable
private fun CategoryTab(
    label: String,
    active: Boolean,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
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
                        if (active) {
                            if (isDarkMode) colors.amber else colors.support
                        } else {
                            Color.Transparent
                        }
                    )
        )
    }
}
