package com.purpletear.game.presentation.game_preview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.R
import com.purpletear.game.presentation.R as GameR

private val DescriptionFont = FontFamily(Font(R.font.shared_elements_font_worksans_regular))

@Composable
internal fun GamePreviewDescription(
    modifier: Modifier = Modifier,
    avatarUrl: String,
    description: String,
) {
    if (description.isBlank()) return
    val text = remember(description) { buildColoredAnnotatedString(description) }
    val context = LocalContext.current
    val avatar = remember(context, avatarUrl) {
        ImageRequest.Builder(context).data(avatarUrl).crossfade(true).build()
    }

    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min).heightIn(min = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(22.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = avatar,
                contentDescription = stringResource(GameR.string.game_presentation_game_preview_avatar_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(0.75.dp, Color.White, RoundedCornerShape(4.dp))
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
                    .width(0.5.dp)
                    .background(Color.White.copy(alpha = 0.3f)),
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Color.White,
            textAlign = TextAlign.Justify,
            fontFamily = DescriptionFont,
        )
    }
}

private fun buildColoredAnnotatedString(
    text: String,
    color: Color = Color(0xFFFAD7FF)
): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("\\*".toRegex())
        if (parts.size % 2 != 0) {
            for (i in parts.indices) {
                if (i % 2 == 0) {
                    append(parts[i])
                } else {
                    withStyle(
                        style = SpanStyle(
                            color = color,
                            fontFamily = FontFamily(
                                Font(
                                    R.font.shared_elements_font_worksans_semibold,
                                    FontWeight.SemiBold
                                )
                            )
                        )
                    ) {
                        append(parts[i])
                    }
                }
            }
        } else {
            append(text)
        }
    }
}
