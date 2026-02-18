package com.purpletear.game_presentation.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.core.presentation.components.icon.Icon
import com.purpletear.core.presentation.components.icon.IconComposable
import com.purpletear.game_presentation.sealed.Background
import com.purpletear.game_presentation.sealed.toBrush


@Composable
internal fun GamePreviewButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    title: String? = null,
    subtitle: String? = null,
    icon: Icon? = null,
    background: Background = Background.Solid(Color(0xFF333333)),
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    iconAlignment: Alignment = Alignment.CenterEnd,
) {

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(5.dp))
            .height(50.dp)
            .background(brush = background.toBrush()),
        contentAlignment = Alignment.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isLoading || !isEnabled) 0.2f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(2.dp)
            ) {
                // Title with Work Sans Semi Bold
                Text(
                    text = title ?: "",
                    fontFamily = FontFamily(
                        Font(
                            com.example.sutokosharedelements.R.font.font_worksans_semibold,
                            FontWeight.SemiBold
                        )
                    ),
                    fontSize = 12.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Subtitle with Work Sans Regular if not null
                subtitle?.let {
                    Text(
                        text = it,
                        fontFamily = FontFamily(
                            Font(
                                com.example.sutokosharedelements.R.font.font_worksans_regular,
                                FontWeight.Normal
                            )
                        ),
                        fontSize = 9.5.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        var iconModifier = when (icon) {
            is Icon.LottieAnimation -> {
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .size(40.dp)
            }

            else -> {
                Modifier
                    .padding(end = if (iconAlignment == Alignment.CenterEnd) 14.dp else 0.dp)
                    .size(15.dp)
                    .align(iconAlignment)
                    .alpha(if (isLoading) 0.2f else 1f)
            }
        }

        iconModifier = if (!isEnabled) iconModifier.then(Modifier.alpha(0.2f)) else iconModifier

        // Icon on the right if provided
        icon?.let {
            IconComposable(
                icon = it,
                modifier = iconModifier,
            )
        }


        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(16.dp)
                    .align(Alignment.Center),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        }

    }
}
