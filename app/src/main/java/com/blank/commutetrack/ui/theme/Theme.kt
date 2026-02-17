package com.blank.commutetrack.ui.theme

import androidx.compose.runtime.Composable
import com.blank.commutetrack.core.ui.theme.CommuteTrackTheme as CoreTheme

@Composable
fun CommuteTrackTheme(
    content: @Composable () -> Unit
) {
    CoreTheme(content = content)
}
