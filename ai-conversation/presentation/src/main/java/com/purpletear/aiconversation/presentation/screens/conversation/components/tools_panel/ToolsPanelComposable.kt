package com.purpletear.aiconversation.presentation.screens.conversation.components.tools_panel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.navigation.AiConversationRouteDestination
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "ToolsPanelComposable", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(14.dp)
    val rulesEnabled = false
    val navController = rememberNavController()
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_tool_button),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    ToolsPanelComposable(
                        modifier = Modifier,
                        viewModel = hiltViewModel(),
                        navController = navController
                    )
                }
            }
            if (rulesEnabled) {
                verticalRules.forEach { startPadding ->
                    Box(
                        Modifier
                            .padding(start = startPadding)
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ToolsPanelComposable(
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel,
    navController: NavHostController,
    isOpened: Boolean = false
) {
    Box(
        modifier
            .height(162.dp)
            .fillMaxWidth()
            .background(Color(0xFF0E1218))
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (isOpened) {
            Row(
                Modifier
                    .padding(start = 12.dp)

            ) {
                ToolButton(
                    url = "https://data.sutoko.app/resources/sutoko-ai/image/tools-btn-invite-character.jpg",
                    text = stringResource(R.string.ai_conversation_title_invite_character),
                    icon = R.drawable.ic_add,
                    onClick = viewModel::onClickAddCharacter
                )
                ToolButton(
                    url = "https://data.sutoko.app/resources/sutoko-ai/image/tools-btn-generate-image.jpg",
                    text = stringResource(R.string.ai_conversation_image_generator_title),
                    icon = R.drawable.ic_stars,
                    onClick = {
                        navController.navigate(AiConversationRouteDestination.GenerateImage.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    modifier: Modifier = Modifier,
    url: String,
    text: String,
    icon: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(max = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val shape = RoundedCornerShape(18.dp)

        Box(
            Modifier
                .size(58.dp)
                .border(2.dp, Color(0xFFF0E8F8).copy(0.4f), shape)
                .clip(shape),


            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(58.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = onClick
                    ),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0B0E1E).copy(0.83f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp),
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = "icon send a message",
                    tint = Color.White
                )
            }
        }

        Text(
            modifier = Modifier
                .widthIn(max = 80.dp)
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 11.sp
        )
    }
}