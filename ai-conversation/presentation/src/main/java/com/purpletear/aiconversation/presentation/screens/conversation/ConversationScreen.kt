package com.purpletear.aiconversation.presentation.screens.conversation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.purpletear.aiconversation.presentation.common.utils.SharedElementTransitionState
import com.purpletear.aiconversation.presentation.component.loading_box.LoadingBox
import com.purpletear.aiconversation.presentation.component.shared_element_transition.SharedElementTransitionBox
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_background.ConversationBackgroundComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items.ConversationList
import com.purpletear.aiconversation.presentation.screens.conversation.components.footer.ConversationFooter
import com.purpletear.aiconversation.presentation.screens.conversation.components.header.ConversationHeader
import com.purpletear.aiconversation.presentation.screens.conversation.components.page_invite_characters.InviteCharacterComposable
import com.purpletear.aiconversation.presentation.screens.conversation.components.tools_panel.ToolsPanelComposable
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.aiconversation.presentation.screens.conversation.viewmodels.VoiceRecordViewModel
import com.purpletear.aiconversation.presentation.screens.settings.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    voiceRecordViewModel: VoiceRecordViewModel,
    navController: NavHostController
) {
    val panelHeight = if (viewModel.toolsViewIsOpened.value) 148.dp else 0.dp
    val animatedHeight by animateDpAsState(
        targetValue = panelHeight,
        animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing),
        label = "Animated height",
    )
    val coroutineScope = rememberCoroutineScope()
    val transitionState = remember { mutableStateOf(SharedElementTransitionState()) }
    val focusManager = LocalFocusManager.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                viewModel.onResume()
            }

            else -> {}
        }
    }

    BackHandler(enabled = viewModel.inviteCharacterPageIsOpened.value) {
        if (viewModel.inviteCharacterPageIsOpened.value) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    viewModel.closeInviteCharacterPage()
                }
            }
        }
    }

    BackHandler(enabled = viewModel.settingsViewIsOpened.value) {
        if (viewModel.settingsViewIsOpened.value) {
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    viewModel.closeSettingsPage()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.closeToolsView()
        }
    }

    val lazyState = rememberLazyListState()
    LaunchedEffect(Unit) {
        snapshotFlow {
            Pair(viewModel.messages.size, viewModel.alert.value)
        }.collect { (messagesSize, alert) ->
            if (messagesSize > 0 || alert != null) {
                delay(120)
                lazyState.animateScrollToItem(0)
            }
        }
    }


    ConversationBackgroundComposable(viewModel = viewModel)

    ConversationScreenColumn(focusManager, viewModel::closeToolsView) {

        // Header
        ConversationHeader(Modifier.padding(top = 16.dp), focusManager, transitionState, viewModel)

        // Conversation
        ConversationList(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = lazyState,
            viewModel = viewModel,
            navController = navController
        )

        // Footer
        Column(Modifier.padding(bottom = 12.dp)) {
            Hr()
            ConversationFooter(
                Modifier.padding(vertical = 16.dp),
                viewModel = viewModel,
                voiceRecordViewModel = voiceRecordViewModel,
            )
        }

        // Tools Panel
        ToolsPanelComposable(
            modifier = Modifier.height(animatedHeight),
            viewModel = viewModel,
            navController = navController,
            isOpened = viewModel.toolsViewIsOpened.value
        )
    }

    SharedElementTransitionBox(transitionState.value, onStopExpend = {
        transitionState.value = transitionState.value.copy(expanded = false)
    })

    BackHandler(enabled = transitionState.value.expanded) {
        transitionState.value = transitionState.value.copy(expanded = false)
    }



    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = viewModel.isLoading.value,
        enter = fadeIn(),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing
            )
        ),
    ) {
        LoadingBox()
    }


    AnimatedVisibility(
        visible = viewModel.inviteCharacterPageIsOpened.value,
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
        InviteCharacterComposable(
            viewModel = hiltViewModel(),
            conversationViewModel = viewModel,
            navController = navController
        )
    }

    AnimatedVisibility(
        visible = viewModel.settingsViewIsOpened.value,
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
        SettingsScreen(onBackButtonPressed = {
            viewModel.closeSettingsPage()
        })
    }
}

@Composable
private fun Hr() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(.5.dp)
            .background(Color.Gray.copy(0.12f))
    )
}


@Composable
private fun ConversationScreenColumn(
    focusManager: FocusManager,
    onTouch: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Box(modifier = Modifier
        .imePadding()
        .fillMaxSize(), content = {
        Column(modifier = Modifier.pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus()
                onTouch()
            }
        }, content = content)
    })
}