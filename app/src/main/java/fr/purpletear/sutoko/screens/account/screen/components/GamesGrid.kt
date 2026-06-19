package fr.purpletear.sutoko.screens.account.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.presentation.model.GameItem
import fr.purpletear.sutoko.R
import com.purpletear.sutoko.shop.R as ShopR


const val GamesGridTag = "GamesGridTag"

@Composable
fun GamesGrid(list: List<GameItem>, onTap: (GameItem) -> Unit) {
    Surface(modifier = Modifier.background(Color(0xFF1A1A1A))) {
        Column(
            modifier = Modifier
                .testTag(GamesGridTag)
                .background(Color(0xFF1A1A1A))
                .padding(12.dp)
                .fillMaxHeight()
                .widthIn(min = Dp.Infinity, max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp),
                text = stringResource(R.string.sutoko_my_downloaded_games),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.White,
                fontFamily = FontFamily(
                    Font(
                        R.font.font_poppins_semibold,
                        FontWeight.Bold,
                    )
                ),
                style = TextStyle(
                    letterSpacing = 0.5.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 92.dp)) {
                items(
                    count = list.size
                ) { index ->
                    CardView(
                        modifier = Modifier
                            .padding(8.dp)
                            .alpha(if (list[index].isPurchased) 1f else 0.4f),
                        card = list[index],
                        onTap = onTap,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardView(
    modifier: Modifier = Modifier,
    card: GameItem,
    onTap: (GameItem) -> Unit,
    icon: Int? = null,
) {
    Box(
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onTap(card)
        }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(alignment = Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(0.3f))
            ) {
                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(card.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                icon?.let {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(0.3f))
                    )
                    Image(
                        modifier = Modifier
                            .size(42.dp)
                            .align(Alignment.Center),
                        painter = painterResource(id = it),
                        contentDescription = null
                    )
                }
            }

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = card.title,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
                fontFamily = FontFamily(Font(R.font.font_poppins_semibold, FontWeight.SemiBold))
            )
            Row {
                if (!card.isFree) {
                    Text(
                        text = "960",
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        color = Color.White,
                        fontFamily = FontFamily(
                            Font(
                                R.font.font_poppins_regular,
                                FontWeight.Normal
                            )
                        )
                    )
                    Image(
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 4.dp),
                        painter = painterResource(id = ShopR.drawable.sutoko_shop_item_coins),
                        contentDescription = null
                    )
                }
            }
        }
    }
}