package com.purpletear.ai_conversation.ui.screens.home.components.characters_slider

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.character.avatar.character_avatar.CharacterAvatar
import com.purpletear.ai_conversation.ui.model.GridItem
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme
import com.purpletear.ai_conversation.ui.theme.PinkColor
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Preview(name = "CharacterGrid", showBackground = false, showSystemUi = false)
@Composable
private fun Preview() {
    AiConversationTheme {
        Column(Modifier.background(Color.Black)) {
            Image(
                painter = painterResource(id = R.drawable.preview_characters_grid),
                contentDescription = null,
            )
            CharacterSlider(elementSize = 60.dp, items = emptyList(), onClickNewElement = {})
            CharactersGrid(
                modifier = Modifier.padding(20.dp),
                elementSize = 60.dp,
                characters = emptyList(),
                onClickNewElement = {}
            )
        }
    }
}

@Composable
internal fun CharacterSlider(
    modifier: Modifier = Modifier,
    items: List<GridItem>,
    elementSize: Dp,
    onClickNewElement: () -> Unit,
    onClickElement: (code: String) -> Unit = {},
    isEnabled: Boolean = true
) {
    val scrollState = rememberScrollState()
    val spacedBy = 0.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(start = 20.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(spacedBy)
    ) {

        Box(
            modifier = Modifier
                .height(elementSize + 12.dp)
                .alpha(if (isEnabled) 1f else 0.3f),
            contentAlignment = Alignment.BottomStart
        ) {
            ButtonAddCharacter(
                modifier = Modifier,
                onClick = onClickNewElement,
                fillColor = Color(0xFF040617),
                strokeColor = Color(0xFF4C505F),
                tintColor = Color.White,
                size = elementSize
            )
        }

        Spacer(modifier = Modifier.width(12.dp - spacedBy * 2))
        items.forEach { item ->
            ButtonSelectCharacter(
                url = item.url,
                isSelected = item.isSelected,
                onClick = {
                    onClickElement(item.code)
                },
                size = elementSize
            )
        }
    }

    /*var shouldAutoScroll by remember { mutableStateOf(true) }

    LaunchedEffect(shouldAutoScroll) {
        if (shouldAutoScroll) {
            delay(2000)
            scrollState.animateScrollBy(
                100f,
                animationSpec = androidx.compose.animation.core.tween(280)
            )
            scrollState.animateScrollBy(
                -100f,
                animationSpec = androidx.compose.animation.core.tween(280)
            )
            shouldAutoScroll = false
        }
    }*/
}

@Composable
internal fun CharactersGrid(
    modifier: Modifier = Modifier,
    elementSize: Dp,
    characters: List<AiCharacter>,
    onClickNewElement: () -> Unit
) {


    BoxWithConstraints(modifier = modifier) {
        val screenWidth = maxWidth
        val itemSize = 60.dp
        val spacing = 12.dp
        val columns = (screenWidth / (itemSize + spacing)).toInt()

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            val totalItems = characters.size + 1
            val rows = ceil(totalItems / columns.toFloat()).toInt()

            for (rowIndex in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    for (columnIndex in 0 until columns) {
                        val itemIndex = rowIndex * columns + columnIndex
                        when {
                            itemIndex < characters.size -> {
                                val character = characters[itemIndex]
                                CharacterAvatar(
                                    bitmap = null,
                                    orUrl = character.avatarUrl
                                        ?: "https://data.sutoko.app/resources/sutoko-ai/image/DefaultAvatar.jpg",
                                    isSelected = false,
                                    size = elementSize
                                )
                            }

                            itemIndex == characters.size -> {
                                ButtonAddCharacter(
                                    onClick = {
                                        onClickNewElement()
                                    },
                                    fillColor = Color(0xFF111420),
                                    strokeColor = Color(0xFF9093FF),
                                    strokeWidth = 2.dp,
                                    tintColor = Color.White,
                                    size = elementSize,

                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonAddCharacter(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    fillColor: Color,
    strokeColor: Color,
    tintColor: Color,
    strokeWidth: Dp = 1.dp,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)

            .clip(MaterialTheme.shapes.large)
            .background(fillColor)
            .border(strokeWidth, strokeColor, MaterialTheme.shapes.large)
            .then(modifier)


            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val vector = ImageVector.vectorResource(id = R.drawable.vec_add_box)
        val painter = rememberVectorPainter(image = vector)


        Image(
            painter = painter,
            contentDescription = "Add Box",
            modifier = Modifier.size(14.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
        )
    }
}

@Composable
private fun ButtonSelectCharacter(
    modifier: Modifier = Modifier,
    url: String,
    isSelected: Boolean,
    notification: Int = 0,
    onClick: () -> Unit,
    size: Dp
) {
    Box(
        Modifier
            .size(size + 12.dp)
    ) {

        CharacterAvatar(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .clickable(onClick = onClick),
            orUrl = url,
            isSelected = isSelected,
            size = size
        )

        if (notification > 0) {
            Box(
                Modifier
                    .size(22.dp)
                    .background(PinkColor, RoundedCornerShape(8.dp, 8.dp, 8.dp, 4.dp))
                    .align(Alignment.TopEnd), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

