package com.purpletear.ai_conversation.ui.screens.conversation.components.page_invite_characters

import android.content.Context
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.domain.enums.CharacterStatus
import com.purpletear.ai_conversation.domain.model.AiCharacterWithStatus
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.ai_conversation.ui.component.divider.DividerComposable
import com.purpletear.ai_conversation.ui.navigation.AiConversationRouteDestination
import com.purpletear.core.presentation.services.performVibration
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.InviteCharacterViewModel


@Composable
internal fun InviteCharacterComposable(
    conversationViewModel: ConversationViewModel,
    viewModel: InviteCharacterViewModel,
    navController: NavController
) {
    val shouldTriggerAction by viewModel.closeComposable.collectAsState()

    if (shouldTriggerAction) {
        conversationViewModel.closeInviteCharacterPage()
        viewModel.resetCloseComposable()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                viewModel.loadCharacters()
            }
        }
    }

    PageColumn {
        Header(viewModel = viewModel)
        DividerComposable(
            Modifier
                .alpha(0.2f)
        )
        CharactersList(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            viewModel = viewModel
        )

        Buttons(
            parentViewModel = conversationViewModel,
            viewModel = viewModel,
            navController = navController
        )
    }

    val alphaAnimation by animateFloatAsState(
        targetValue = if (viewModel.isLoading.value) 0.3f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = LinearOutSlowInEasing
        ), label = "Animation of the loading screen"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alphaAnimation))
            .alpha(alphaAnimation),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center),
            color = Color.LightGray,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun CharactersList(
    modifier: Modifier = Modifier,
    viewModel: InviteCharacterViewModel
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        itemsIndexed(
            viewModel.characters.value,
            key = { _, item -> item.character.id }) { index, item ->
            CharacterRow(
                enable = !viewModel.characterAlreadyInConversation(item.character),
                character = viewModel.characters.value[index],
                isSelected = viewModel.selectedIds.value.contains(
                    item.character.id
                ),
                onClick = {
                    viewModel.onCharacterSelected(item.character.id)
                },
                context = context,
            )
        }
    }
}

@Composable
private fun Buttons(
    parentViewModel: ConversationViewModel,
    viewModel: InviteCharacterViewModel,
    navController: NavController
) {

    val alphaAnimation by animateFloatAsState(
        targetValue = if (viewModel.selectedIds.value.isEmpty()) 0.2f else 1f,
        animationSpec = tween(
            durationMillis = 280,
            easing = LinearOutSlowInEasing
        ), label = "Animation of the buttons"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        val c = viewModel.names()
        Button(
            text = stringResource(R.string.ai_conversation_invite_characters_names, c),
            onClick = {
                viewModel.inviteSelectedCharacters()
            },
            isLoading = viewModel.isInviteCharacterLoading.value,
            backgroundColor = Color(0xFF3D1C9E).copy(alphaAnimation)
        )
        Button(text = "Créer un personnage", onClick = {
            navController.navigate(AiConversationRouteDestination.AddCharacter.route)
        }, backgroundColor = Color(0x00101113))
    }
}

@Composable
private fun Button(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    backgroundColor: Color = Color(0xFF562AD8)
) {
    Box(
        Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = !isLoading,
                onClick = onClick
            )
            .background(backgroundColor)
            .border(.5.dp, Color.White.copy(0.1f), RoundedCornerShape(5.dp))
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center)
                    .alpha(1f),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}


@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F15))
            .navigationBarsPadding()
            .systemBarsPadding()
    ) {
        content()
    }
}

@Composable
private fun Header(viewModel: InviteCharacterViewModel) {
    Row(
        modifier = Modifier
            .height(62.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) {
                    viewModel.closeComposable()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.arrow_back),
                contentDescription = "Back button icon",
                modifier = Modifier.size(10.dp),
                tint = Color.White
            )
        }

        Text(
            text = stringResource(R.string.ai_conversation_title_invite_character),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun CharacterRow(
    enable: Boolean,
    character: AiCharacterWithStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    context: Context
) {

    Box(
        Modifier
            .fillMaxWidth()
            .height(68.dp)
            .alpha(if (enable) 1f else 0.4f)
            .background(
                Color(0xFF414141).copy(
                    if (isSelected) 0.1f else 0.05f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = enable,
                onClick = {
                    performVibration(context, 100L)
                    onClick()
                }
            )

    ) {
        Row(
            Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                url = getRemoteAssetsUrl(character.character.avatarUrl ?: ""),
                isSelected = isSelected
            )
            Column(
                Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = character.character.firstName + " " + (character.character.lastName
                        ?: ""),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (character.status.state == CharacterStatus.Online) {
                        Box(
                            modifier = Modifier
                                .height(18.dp)
                                .width(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color.Green, CircleShape)
                            )
                        }
                    } else {
                        Image(
                            modifier = Modifier
                                .size(18.dp)
                                .alpha(0.5f),
                            painter = painterResource(id = R.drawable.ic_moon),
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = if (character.status.state == CharacterStatus.Online) "Online" else "Offline",
                        color = Color.LightGray.copy(0.6f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
private fun Avatar(url: String, isSelected: Boolean) {

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(
            durationMillis = 280,
            easing = LinearOutSlowInEasing
        ), label = "Animation of the avatar"
    )
    val borderSize = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) Color(0xFFA081FE) else Color.White
    Box(
        Modifier
            .size(42.dp)
            .border(borderSize, borderColor, CircleShape)
            .clip(CircleShape)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnimation)
                .background(Color.Black.copy(0.7f))
                .graphicsLayer {
                    alpha = alphaAnimation
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(18.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.icons8_check_384),
                contentDescription = "icon checked",
                tint = Color.White
            )
        }
    }
}
