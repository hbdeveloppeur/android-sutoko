package com.purpletear.aiconversation.presentation.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R

@Composable
internal fun MicrophoneIcon(size: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .size(size + 14.dp)
            .background(Color.Black.copy(0.2f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier
                .size(size),
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_microphone),
            contentDescription = "icon microphone",
            tint = Color.White
        )
    }
}