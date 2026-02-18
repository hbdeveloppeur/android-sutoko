package com.purpletear.aiconversation.presentation.screens.conversation.components.footer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
internal fun ChatBottomOptionsButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    icon: Int,
    onClick: (() -> Unit)? = null
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.05f))
            .alpha(if (isEnabled) 1f else 0.5f)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = {
                        if (isEnabled) {
                            onClick()
                        }
                    })
                } else {
                    Modifier
                }
            )
            .then(modifier)
    ) {

        Icon(
            modifier = Modifier
                .size(16.dp),
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Preview
@Composable
private fun ChatBottomOptionsButtonPreview() {
    // ChatBottomOptionsButton(isEnabled = true, icon = Icons.Default.Send)
}