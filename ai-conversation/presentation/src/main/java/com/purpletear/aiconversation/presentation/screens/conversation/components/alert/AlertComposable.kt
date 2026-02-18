package com.purpletear.aiconversation.presentation.screens.conversation.components.alert


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.sealed.AlertState


@Composable
internal fun AlertComposable(
    modifier: Modifier = Modifier,
    state: AlertState,
    onClick: (alertState: AlertState) -> Unit
) {
    Row(
        modifier
            .padding(top = 12.dp)
    ) {
        Box(
            Modifier
                .height(58.dp)
                .width(4.dp)
                .background(Color(0xFFC95757))
        )
        Row(
            modifier = Modifier
                .height(58.dp)
                .fillMaxWidth()
                .background(Color(0x1B000000))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            state.icon?.let {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_alert),
                    contentDescription = "Alert icon",
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFC95757)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = state.message.asString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.weight(1f))

            var isClicked by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isClicked) 0.93f else 1f,
                tween(
                    durationMillis = 180,
                    easing = FastOutSlowInEasing
                ),

                finishedListener = {
                    if (isClicked) {
                        isClicked = false
                    }
                }, label = "scaleButton"
            )

            state.button?.let { button ->
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .border(.5.dp, Color.White.copy(0.15f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) {
                            isClicked = true
                            onClick(state)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = button.asString(),
                        color = Color.White.copy(0.8f),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    )
                }
            }

        }
    }
}
