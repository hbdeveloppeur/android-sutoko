package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sutokosharedelements.theme.SutokoTypography


@Composable
fun SectionTitle(modifier: Modifier = Modifier, title: String, subtitle: String? = null) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 6.dp)
            .then(modifier)
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            style = SutokoTypography.h3.copy(
                letterSpacing = 0.5.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ), color = Color(0xFFFAFAFA)
            )
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                style = SutokoTypography.body1.copy(
                    letterSpacing = 0.5.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ), color = Color(0xFFD3DAD2)
                )
            )
        }
    }
}