package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Pink
import com.example.sharedelements.theme.SutokoTypography

@Composable
fun TitleSwitch(
    isOn: Boolean,
    text: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color = Color(0xFFD9D9D9).copy(0.04f))
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                textAlign = TextAlign.Start,
                fontSize = 12.sp,
                style = SutokoTypography.body1.copy(
                    letterSpacing = 0.5.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Switch(
                checked = isOn,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Pink,
                    checkedTrackColor = Pink,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.White,
                ),
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
