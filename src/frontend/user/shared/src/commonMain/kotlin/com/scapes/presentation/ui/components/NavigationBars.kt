package com.scapes.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.shared.generated.resources.Res
import com.scapes.shared.generated.resources.scapes_dark
import com.scapes.shared.generated.resources.scapes_light
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconShell(onClick: () -> Unit, colors: ScapesThemeColors, content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.size(40.dp)
                .clip(CircleShape)
                .background(colors.surface.copy(alpha = 0.34f))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun HomeAppBar(
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(60.dp)
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconShell(onClick = onOpenMenu, colors = colors) { MenuGlyph(colors.text) }
        Spacer(Modifier.weight(1f))
        Image(
            painter =
                painterResource(
                    if (isDarkMode) Res.drawable.scapes_dark else Res.drawable.scapes_light
                ),
            contentDescription = "Scapes",
            modifier = Modifier.height(36.dp),
        )
        Spacer(Modifier.weight(1f))
        IconShell(onClick = onSearch, colors = colors) { SearchGlyph(colors.text) }
    }
}

@Composable
fun SearchResultBar(
    query: String,
    colors: ScapesThemeColors,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(60.dp)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconShell(onClick = onBack, colors = colors) { BackGlyph(colors.text) }

        SearchInput(
            query = query,
            onQueryChange = onQueryChange,
            colors = colors,
            onSearch = onSearch,
            modifier = Modifier.weight(1f),
        )

        IconShell(onClick = onSearch, colors = colors) { SearchGlyph(colors.text) }
    }
}

@Composable
fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: ScapesThemeColors,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.text, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = modifier,
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
                if (query.isBlank()) {
                    Text("Search wallpapers", color = colors.secondaryText)
                }
                innerTextField()
            }
        },
    )
}
