package fr.purpletear.sutoko.screens.main.presentation.screens.calendar

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.WorkSansFontFamily
import com.purpletear.game.presentation.game_preview.BackgroundMedia
import fr.purpletear.sutoko.R

internal const val CalendarReleaseVideoTestTag = "calendar_release_video"

private val CalendarCardText = TextStyle(
    fontFamily = WorkSansFontFamily,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    color = Color.White,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CalendarReleaseCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    titleImageUrl: String? = null,
    secondarySubtitle: String? = null,
    playerLabel: String? = null,
    keywords: List<String> = emptyList(),
    @RawRes videoRes: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Column(Modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF141118))
                .then(clickModifier),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            if (videoRes != null) {
                var hasFrame by remember(videoRes) { mutableStateOf(false) }
                var failed by remember(videoRes) { mutableStateOf(false) }
                if (!failed) {
                    val packageName = LocalContext.current.packageName
                    BackgroundMedia(
                        videoUrl = "android.resource://$packageName/$videoRes",
                        modifier = Modifier.matchParentSize().alpha(if (hasFrame) 1f else 0f)
                            .then(if (hasFrame) Modifier.testTag(CalendarReleaseVideoTestTag) else Modifier),
                        onFirstFrame = { hasFrame = true },
                        onError = { failed = true },
                    )
                }
            }
            Box(
                modifier = Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.36f),
                        0.48f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.80f),
                    )
                )
            )
            if (titleImageUrl != null) {
                val context = LocalContext.current
                val titleImage = remember(context, titleImageUrl) {
                    ImageRequest.Builder(context)
                        .data(titleImageUrl)
                        .allowHardware(false)
                        .transformations(TrimTitleTransparency)
                        .build()
                }
                AsyncImage(
                    model = titleImage,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 20.dp, top = 14.dp)
                        .width(144.dp)
                        .height(54.dp),
                    alignment = Alignment.TopStart,
                    contentScale = ContentScale.Fit,
                )
            }
            if (playerLabel != null) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 22.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.sutoko_ic_games),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(playerLabel, style = CalendarCardText, fontSize = 12.sp)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = CalendarCardText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = subtitle,
                        modifier = Modifier.weight(1f, fill = false),
                        style = CalendarCardText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    secondarySubtitle?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = it,
                            style = CalendarCardText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        if (keywords.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                keywords.forEachIndexed { index, keyword ->
                    Box(
                        modifier = Modifier.heightIn(min = 38.dp)
                            .background(
                                color = Color(0xFF1C1C1C),
                                shape = RoundedCornerShape(
                                    topStart = if (index == 0) 8.dp else 22.dp,
                                    topEnd = 22.dp,
                                    bottomEnd = 22.dp,
                                    bottomStart = 22.dp,
                                ),
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            keyword,
                            style = CalendarCardText,
                            color = Color(0xFFA6BCC7),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
