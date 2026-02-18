package fr.purpletear.sutoko.screens.main.presentation.screens.calendar.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.objects.CalendarEvent
import fr.purpletear.sutoko.objects.nbPlayersToString
import com.example.sutokosharedelements.theme.SutokoTypography

@Composable
fun CalendarEvent(event: CalendarEvent, onClick: (CalendarEvent) -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clickable(onClick = { onClick(event) })
    ) {

        val players = remember(event) {
            event.nbPlayersToString()
        }

        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .aspectRatio(ratio = 828f / 340f),
            model = ImageRequest.Builder(LocalContext.current)
                .data(event.backgroundImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 8.dp)
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.sutoko_ic_games),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Text(
                text = stringResource(id = R.string.sutoko_event_waited_by, players ?: -1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 11.sp,
                style = SutokoTypography.body1.copy(
                    letterSpacing = 0.5.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)

                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                event.title,
                fontSize = 18.sp,
                style = SutokoTypography.h1.copy(letterSpacing = 0.5.sp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = event.subtitle,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    style = SutokoTypography.body1.copy(
                        letterSpacing = 0.5.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )

                event.subSubtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 11.sp,
                        style = SutokoTypography.h2.copy(
                            letterSpacing = 0.5.sp,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }

            }
        }
    }
}