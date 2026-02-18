package com.purpletear.game.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.game.presentation.util.ImmutableList
import com.purpletear.game.presentation.util.ImmutableMap
import com.purpletear.game.presentation.viewmodels.GameSquareViewModel
import com.purpletear.sutoko.game.model.Game
import com.example.sharedelements.R as SharedR


@Composable
fun GameSquares(
    modifier: Modifier = Modifier,
    stories: ImmutableList<Game>,
    icons: ImmutableMap<Int, Int?>,
    onTap: (Game) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(16.dp)
                .padding(
                    top = 8.dp
                )
                .align(alignment = Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stories.items.forEach { card ->
                GameSquare(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(1.dp),
                    card = card,
                    onTap = onTap,
                    icon = icons.map[card.id]
                )
            }
        }
    }
}


@Composable
private fun GameSquare(
    modifier: Modifier,
    card: Game,
    onTap: (Game) -> Unit,
    viewModel: GameSquareViewModel = hiltViewModel(),
    icon: Int? = null,
) {
    // The GameSquare composable uses the ViewModel from the parent GameSquares composable
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
                    .shadow(
                        elevation = 3.dp, shape =
                            RoundedCornerShape(12.dp), clip = true
                    )
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(0.3f))
            ) {
                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            viewModel.getImageSquareLink(
                                game = card
                            )
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                icon?.let {
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
                text = card.metadata.title,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
                color = Color.White,
                fontFamily = FontFamily(
                    Font(
                        SharedR.font.font_poppins_semibold,
                        FontWeight.SemiBold
                    )
                )
            )
        }
    }
}
