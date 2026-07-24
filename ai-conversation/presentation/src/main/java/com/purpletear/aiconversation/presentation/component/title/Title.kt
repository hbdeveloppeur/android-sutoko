package com.purpletear.aiconversation.presentation.component.title

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                buildColoredAnnotatedString(title), style = style, color = Color.White,
                fontFamily = FontFamily(Font(R.font.ai_conversation_presentation_montserrat_medium))
            )
            Box(
                Modifier
                    .background(Color.White.copy(alpha = 0.14f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text("Version 2.1", color = Color.White, fontSize = 11.sp)
            }
        }
        subtitle?.let {
            Text(
                subtitle, style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily(Font(R.font.ai_conversation_presentation_montserrat_medium)),
                color = Color.White,
            )
        }
    }
}