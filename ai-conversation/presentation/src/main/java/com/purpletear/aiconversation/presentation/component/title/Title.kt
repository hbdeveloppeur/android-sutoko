package com.purpletear.aiconversation.presentation.component.title

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.buildColoredAnnotatedString

@Composable
internal fun Title(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    style: TextStyle = MaterialTheme.typography.labelMedium
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            buildColoredAnnotatedString(title), style = style, color = Color.White,
            fontFamily = FontFamily(Font(R.font.montserrat_medium))
        )
        subtitle?.let {
            Text(
                subtitle, style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily(Font(R.font.montserrat_medium)),
                color = Color.White,
            )
        }
    }
}