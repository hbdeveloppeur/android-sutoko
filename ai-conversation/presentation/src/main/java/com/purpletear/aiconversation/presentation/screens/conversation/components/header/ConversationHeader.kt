package com.purpletear.aiconversation.presentation.screens.conversation.components.header

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.aiconversation.domain.enums.CharacterStatus
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.SharedElementTransition
import com.purpletear.aiconversation.presentation.common.utils.SharedElementTransitionState
import com.purpletear.aiconversation.presentation.common.utils.capitalizeFirstLetter
import com.purpletear.aiconversation.presentation.common.utils.getRemoteAssetsUrl
import com.purpletear.aiconversation.presentation.component.options_button.OptionButtonComposable
import com.purpletear.core.presentation.services.performVibration
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme
import kotlinx.coroutines.delay


@Composable
@Preview(name = "ConversationHeader", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(26.dp, 66.dp, 84.dp)
    val rulesEnabled = true
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_chat_header),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    val transitionState =
                        remember { mutableStateOf(SharedElementTransitionState()) }
                    ConversationHeader(
                        modifier = Modifier
                            .fillMaxWidth(0.95f),
                        avatarImageTransitionState = transitionState,
                        focusManager = LocalFocusManager.current,
                        viewModel = hiltViewModel()
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
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}


@Composable
internal fun ConversationHeader(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    avatarImageTransitionState: MutableState<SharedElementTransitionState>,
    viewModel: ConversationViewModel
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(62.dp)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationAvatar(
            character = viewModel.conversationSettings.value?.character,
            avatarImageTransitionState = avatarImageTransitionState,
        )
        Row(
            Modifier
                .weight(1f)
                .padding(start = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(modifier = Modifier.weight(1f)) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewModel.conversationSettings.value != null,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = LinearOutSlowInEasing
                        )
                    )
                ) {
                    TextInformation(viewModel = viewModel)
                }
            }

            if (viewModel.userIsModerator()) {
                SaveForFineTuning(
                    onClick = viewModel::onClickSaveForFineTuning,
                    isLoading = viewModel.isLoadingSavingForFineTuning.value
                )
            }

            OptionButtonComposable(onClick = {
                focusManager.clearFocus()
                viewModel.openSettingsPage()
            })
        }
    }
}

@Composable
internal fun SaveForFineTuning(onClick: () -> Unit, isLoading: Boolean, size: Dp = 16.dp) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(size + 14.dp)
            .background(Color.White.copy(0.1f), CircleShape)
            .padding(end = 3.dp)
            .clickable {
                performVibration(context, 100L)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier
                .size(size)
                .offset(x = 2.dp)
                .alpha(if (isLoading) 0.5f else 1f),
            imageVector = ImageVector.vectorResource(id = R.drawable.finetune),
            contentDescription = "moderation icon",
            tint = Color.White
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = 1.dp),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun TextInformation(modifier: Modifier = Modifier, viewModel: ConversationViewModel) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            viewModel.conversationSettings.value?.character?.let { character ->
                Title(
                    character.firstName.capitalizeFirstLetter() + " " + (character.lastName
                        ?: "").capitalizeFirstLetter()
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(.5.dp)
                    .background(Color.White.copy(0.1f))
            )
        }

        Box(
            Modifier
                .height(18.dp)
                .fillMaxWidth()
        ) {
            if (viewModel.conversationSettings.value?.isBlocked == true) {
                Block()
            } else if (viewModel.isTyping.value) {
                Typing()
            }
            viewModel.conversationSettings.value?.character?.let { character ->
                viewModel.characterStatus.value?.let { status ->
                    Status(
                        modifier = Modifier
                            .alpha(if (viewModel.isTyping.value || viewModel.conversationSettings.value?.isBlocked == true) 0f else 1f),
                        status = status,
                        conversationMode = viewModel.conversationSettings.value?.mode
                            ?: ConversationMode.Sms,
                        character = character
                    )
                }
            }
        }
    }
}

@Composable
private fun Circle() {
    Box(
        Modifier
            .padding(end = 12.dp)
            .size(16.dp)
            .border(2.5.dp, Color.White.copy(0.32f), CircleShape)
            .clip(CircleShape)
    )
}

@Composable
private fun ConversationAvatar(
    modifier: Modifier = Modifier,
    character: AiCharacter?,
    avatarImageTransitionState: MutableState<SharedElementTransitionState>
) {
    Box(
        modifier
            .height(40.dp)
            .width(60.dp)
    ) {
        Box(
            Modifier
                .height(36.dp)
                .width(40.dp)
                .border(.25.dp, Color.White.copy(0.2f), RoundedCornerShape(20))
                .align(Alignment.CenterStart)
        )
        SharedElementTransition(
            modifier = Modifier
                .padding(start = 14.dp)
                .size(40.dp)
                .border(1.dp, Color.White, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF0D111B)),
            url = getRemoteAssetsUrl(character?.avatarUrl ?: ""),
            element = avatarImageTransitionState
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getRemoteAssetsUrl(character?.avatarUrl ?: ""))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxSize(),
                visible = character == null,
                enter = fadeIn(animationSpec = tween(durationMillis = 280)),
                exit = fadeOut(animationSpec = tween(durationMillis = 280))
            ) {
                Box(
                    Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.Center),
                        color = Color.LightGray,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

    }
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.1.sp,
            fontSize = 12.sp,
        ),
        color = Color.White,
    )
}


@Composable
private fun Typing() {
    Box(
        modifier = Modifier.height(18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "En train d'écrire...",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
            ),
            color = Color.LightGray,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun Status(
    modifier: Modifier = Modifier,
    status: CharacterStatus,
    conversationMode: ConversationMode,
    character: AiCharacter,
) {
    var isActive by remember { mutableStateOf(false) }
    var sstatus by remember { mutableStateOf(status) }
    LaunchedEffect(status) {
        isActive = false
        delay(280)
        sstatus = status
        isActive = true

    }
    AnimatedVisibility(
        modifier = modifier,
        visible = isActive,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 120,
                easing = FastOutSlowInEasing
            )
        ),
    ) {
        Row(
            Modifier.height(18.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {


            if (conversationMode == ConversationMode.Sms) {
                if (sstatus == CharacterStatus.Online) {
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
            }
            Text(
                text = if (conversationMode == ConversationMode.Irl) stringResource(R.string.ai_conversation_status_close_to_you) else if (sstatus == CharacterStatus.Online) stringResource(
                    R.string.ai_conversation_status_online
                ) else "Away",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                ),
                color = Color.LightGray,
            )

            Text(
                modifier = Modifier.padding(start = 2.dp),
                text = if (conversationMode == ConversationMode.Sms) character.statusDescription
                    ?: "" else "",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                ),
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun Block(modifier: Modifier = Modifier) {

    Row(
        modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_alert),
            contentDescription = "Back button icon",
            modifier = Modifier.size(12.dp),
            tint = Color(0xFFFD5D5D)
        )

        Text(
            text = "Blocked you",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
            ),
            color = Color(0xFFFD5D5D),
            // fontStyle = FontStyle.Italic
        )

    }
}
