package com.purpletear.game.presentation.components.compact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins
import com.purpletear.sutoko.game.download.GameDownloadState

private const val BUTTON_WIDTH_DP = 56
private const val BUTTON_HEIGHT_DP = 32

@Composable
fun GetButton(
    modifier: Modifier = Modifier,
    state: GameDownloadState = GameDownloadState.Idle,
    isInstalled: Boolean = false,
    onGetClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onCancelClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(width = BUTTON_WIDTH_DP.dp, height = BUTTON_HEIGHT_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Downloading with progress
            state is GameDownloadState.Downloading -> {
                val progress = state.progress
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onCancelClick?.invoke() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF4DB9EC),
                        strokeWidth = 2.dp,
                        backgroundColor = Color(0xFF3A3A3A)
                    )
                }
            }

            // Extracting (indeterminate progress)
            state == GameDownloadState.Extracting -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onCancelClick?.invoke() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF4DB9EC),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Error state - show retry
            state is GameDownloadState.Error -> {
                ButtonText(
                    text = "Retry",
                    onClick = onGetClick,
                    backgroundColor = Color(0xFFE74C3C)
                )
            }

            // Installed and ready to open
            isInstalled && (state == GameDownloadState.Idle || state == GameDownloadState.Completed) -> {
                ButtonText(
                    text = "Open",
                    onClick = onOpenClick,
                    backgroundColor = Color(0xFF27AE60)
                )
            }

            // Default - Get button
            else -> {
                ButtonText(
                    text = "Get",
                    onClick = onGetClick,
                    backgroundColor = Color(0xFF2A2A2A)
                )
            }
        }
    }
}

@Composable
private fun ButtonText(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Text(
        text = text,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}
