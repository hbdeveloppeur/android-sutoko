package com.purpletear.game.presentation.game_play.components.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.CrimsonTextFontFamily
import com.purpletear.game.debug.PreviewOverlayWrapper
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.SimpleButton
import com.purpletear.game.presentation.game_play.components.Avatar

@Preview
@Composable
private fun Preview() {
    PreviewOverlayWrapper(
        imageModifier = Modifier
            .padding(2.dp)
            .height(200.dp)
            .aspectRatio(589f / 241f),
        drawable = R.drawable.preview_manga_page,
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            MessageManga()
        }
    }
}

@Composable
internal fun MessageManga(
    prompt: String = stringResource(R.string.message_manga_prompt),
    openButtonText: String = stringResource(R.string.message_manga_open),
    onClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            size = 44.dp,
            imageModel = R.drawable.page_manga_preview,
        )
        Text(
            text = prompt,
            color = Color.White,
            fontFamily = CrimsonTextFontFamily
        )
        SimpleButton(
            text = openButtonText,
            fontSize = 11.sp,
            onClick = onClick,
            imageVector = null,
            horizontalPadding = 14.dp,
            verticalPadding = 6.dp,
        )
    }
}
