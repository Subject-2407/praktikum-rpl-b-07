package com.scapes.presentation.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scapes.presentation.ui.theme.ScapesThemeColors

@Composable
fun ScapesSnackbar(
    snackbarData: SnackbarData,
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        containerColor = colors.surface,
        contentColor = colors.text,
        actionColor = colors.amber,
        actionContentColor = colors.amber,
        dismissActionContentColor = colors.secondaryText
    )
}
