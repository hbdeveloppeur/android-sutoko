package fr.purpletear.sutoko.screens.main.presentation.screens.community.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.purpletear.sutoko.R
import com.example.sutokosharedelements.theme.SutokoTypography


@Composable
fun CommunityCtaRow(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color(0xFFD9D9D9).copy(0.04f))
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sutoko_community_cta_row_stories),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                style = SutokoTypography.h2.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Spacer(Modifier.weight(1f))
            Button()
        }
    }
}

@Composable
private fun Button() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(0.15f))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(
            text = stringResource(R.string.sutoko_create),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            style = SutokoTypography.h2.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )

        Image(
            modifier = Modifier.size(18.dp),
            painter = painterResource(id = R.drawable.sutoko_ic_games),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}