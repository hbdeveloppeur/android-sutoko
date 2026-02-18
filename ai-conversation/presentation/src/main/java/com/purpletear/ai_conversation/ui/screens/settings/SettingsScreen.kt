package com.purpletear.ai_conversation.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sutokosharedelements.utils.UiText
import com.purpletear.ai_conversation.domain.model.Version
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.divider.DividerComposable
import com.purpletear.ai_conversation.ui.component.loading_box.LoadingBox
import com.purpletear.ai_conversation.ui.component.title_navigation.TitleNavigation
import com.purpletear.ai_conversation.ui.screens.settings.viewModels.SettingsScreenViewModel
import com.purpletear.core.date.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    onBackButtonPressed: () -> Unit = {},
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val brush = Brush.verticalGradient(colors = listOf(Color(0xFF05070C), Color(0xFF0E1116)))

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                viewModel.onResume()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing.value,
        onRefresh = viewModel::refreshVersion,
    ) {

        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(brush)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            item {
                TitleNavigation(
                    text = UiText.StringResource(R.string.ai_conversation_settings_screen_title),
                    onClickClosed = onBackButtonPressed
                )
            }
            item {
                VersionHeaderComposable(
                    currentVersion = viewModel.currentVersion.value,
                    nextVersion = viewModel.nextVersion.value
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .height(62.dp)
                        .fillMaxWidth()
                ) {
                    AnimatedVisibility(
                        visible = !viewModel.isDeleted.value,
                        enter = fadeIn(),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = FastOutSlowInEasing
                            )
                        ),
                    ) {
                        Button(
                            icon = R.drawable.ic_restart,
                            text = stringResource(R.string.ai_conversation_restart_conversation),
                            onClick = viewModel::onRestartConversationPressed
                        )
                    }
                    AnimatedVisibility(
                        visible = viewModel.isDeleted.value,
                        enter = fadeIn(),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = FastOutSlowInEasing
                            )
                        ),
                    ) {
                        Button(
                            icon = R.drawable.icons8_check_384,
                            color = Color(0xFF9BDAB9),
                            text = "Conversation deleted",
                            onClick = null
                        )
                    }
                }
            }
            item {
                DividerComposable(
                    Modifier
                        .padding(horizontal = 12.dp)
                        .alpha(0.5f)
                )
            }
        }
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
}

@Composable
private fun VersionHeaderComposable(currentVersion: Version?, nextVersion: Version?) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .padding(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black)
    ) {

        CircularProgressIndicator(
            modifier = Modifier
                .padding(15.dp)
                .size(14.dp)
                .align(Alignment.TopEnd),
            color = Color.LightGray,
            strokeWidth = 2.dp
        )

        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentVersion?.backgroundUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Column(
            Modifier
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Version ${currentVersion?.code}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily(Font(R.font.montserrat_medium))
            )
            Text(
                text = "Status : online",
                color = Color(0xFFCFFFE6),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily(Font(R.font.montserrat_medium))
            )

            Spacer(Modifier.weight(1f))
            nextVersion?.releaseDate?.let { timeStamp ->
                val days = DateUtils.daysBetween(
                    timestamp1 = System.currentTimeMillis(),
                    timestamp2 = timeStamp
                )
                if (days > 0) {
                    Text(
                        text = "Next Update in $days days",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily(Font(R.font.montserrat_medium))
                    )
                } else {
                    Text(
                        text = "Next Update is available\nDownload in Google Play",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily(Font(R.font.montserrat_medium))
                    )
                }
            }
        }
    }
}


@Composable
private fun Button(
    icon: Int,
    color: Color = Color(0xFF85929A),
    text: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier
            .height(62.dp)
            .fillMaxWidth()
            .offset(y = (-6).dp)
            .padding(start = 8.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(62.dp),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = ImageVector.vectorResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = color
            )
        }
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily(Font(R.font.montserrat_regular))
        )
    }
}

