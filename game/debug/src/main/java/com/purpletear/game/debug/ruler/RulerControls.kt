package com.purpletear.game.debug.ruler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Control panel for ruler operations.
 *
 * Provides buttons to:
 * - Add horizontal ruler at center
 * - Add vertical ruler at center
 * - Clear all rulers
 *
 * @param state The ruler state to control
 * @param modifier Additional modifier
 */
@Composable
fun RulerControls(
    state: RulerState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray.copy(alpha = 0.9f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { state.add(RulerOrientation.HORIZONTAL, 0.5f) },
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add horizontal ruler",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { state.add(RulerOrientation.VERTICAL, 0.5f) },
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add vertical ruler",
                    tint = Color.Cyan
                )
            }
        }

        IconButton(
            onClick = { state.clear() },
            enabled = !state.isEmpty,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (state.isEmpty) Color.Gray.copy(alpha = 0.5f) else Color.Red.copy(
                        alpha = 0.7f
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear all rulers",
                tint = if (state.isEmpty) Color.Gray else Color.White
            )
        }
    }
}
