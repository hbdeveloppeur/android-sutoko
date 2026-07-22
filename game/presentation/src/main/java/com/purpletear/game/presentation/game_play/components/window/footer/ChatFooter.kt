package com.purpletear.game.presentation.game_play.components.window.footer

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.MontserratFontFamily
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R
import com.purpletear.game.debug.R as DebugR


private val BACKGROUND_COLOR = Color(0xFF383850).copy(0.1f)


@Preview(name = "ChatFooter")
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .padding(2.dp)
            .height(52.dp)
            .aspectRatio(982f / 164f),
        drawable = DebugR.drawable.game_debug_preview_message_box,
    ) {
        Box(Modifier.padding(start = 8.dp, end = 34.dp)) {
            ChatFooter()
        }
    }
}

@Composable
internal fun ChatFooter(
    onClickSend: () -> Unit = {},
    sendContentDescription: String = stringResource(R.string.chat_footer_send_description),
) {
    Row(
        Modifier
            .height(44.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MessageBox(
            Modifier.weight(1f)
        )
        ChatFooterButton(
            icon = R.drawable.ic_send_white,
            size = 16.dp,
            transformX = 5f,
            onClick = onClickSend,
            contentDescription = sendContentDescription
        )
    }
}

@Composable
internal fun MessageBox(
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.chat_footer_placeholder),
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .clip(shape)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.04f), shape = shape)
            .background(BACKGROUND_COLOR)
            .padding(vertical = 12.dp, horizontal = 28.dp)
            .then(modifier)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = placeholder,
            color = Color.White,
            fontFamily = MontserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
    }
}


@Composable
private fun ChatFooterButton(
    @DrawableRes icon: Int,
    size: Dp = 16.dp,
    transformX: Float = 0f,
    onClick: () -> Unit = {},
    contentDescription: String? = null,
) {
    val shape = CircleShape
    Box(
        Modifier
            .width(38.dp)
            .aspectRatio(1f)
            .clip(shape)
            .background(BACKGROUND_COLOR)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = transformX
                },
            tint = Color.White
        )
    }
}
