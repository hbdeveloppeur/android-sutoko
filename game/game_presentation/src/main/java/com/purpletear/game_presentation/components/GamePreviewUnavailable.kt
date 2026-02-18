package com.purpletear.game_presentation.components

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
import com.purpletear.game_presentation.R
import com.purpletear.sutoko.game.model.Chapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun GamePreviewUnavailable(modifier: Modifier = Modifier, chapter: Chapter) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.calendar),
            contentDescription = stringResource(R.string.game_preview_calendar_icon),
            modifier = Modifier.size(14.dp),
            tint = Color.Gray
        )
        val formattedDate = SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
            .format(Date(chapter.releaseDate * 1000))

        Text(
            text = stringResource(R.string.game_preview_next_chapter, formattedDate),
            fontFamily = PlusJakartaSansFontFamily,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
