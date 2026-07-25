package com.purpletear.game.presentation.game_preview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.PlusJakartaSansFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.Chapter

@Composable
internal fun GamePreviewUnavailable(modifier: Modifier = Modifier, chapter: Chapter) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.game_presentation_calendar),
            contentDescription = stringResource(R.string.game_presentation_game_preview_calendar_icon),
            modifier = Modifier.size(14.dp),
            tint = Color.Gray
        )
        Text(
            text = stringResource(
                R.string.game_presentation_game_preview_next_chapter,
                chapter.formatReleaseDate()
            ),
            fontFamily = PlusJakartaSansFontFamily,
            color = Color(0xFF90EE90),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
