package fr.purpletear.sutoko.screens.params

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.purpletear.sutoko.R

@Composable
fun SutokoParamsScreen(
    viewModel: SutokoParamsViewModel,
    onOpenPrivacyPolicy: (String) -> Unit,
    onShareApp: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.effect) {
        when (val effect = uiState.effect) {
            is SutokoParamsEffect.OpenPrivacyPolicy -> onOpenPrivacyPolicy(effect.url)
            is SutokoParamsEffect.ShareApp -> onShareApp()
            is SutokoParamsEffect.NavigateBack -> onNavigateBack()
            null -> Unit
        }
        viewModel.onEvent(SutokoParamsEvent.OnEffectConsumed)
    }

    SutokoParamsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun SutokoParamsContent(
    uiState: SutokoParamsUiState,
    onEvent: (SutokoParamsEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070C)),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = stringResource(R.string.sutoko_options),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SutokoParamsEvent.OnBackPressed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sutoko_cancel),
                            tint = Color.White,
                        )
                    }
                },
                backgroundColor = Color(0xFF05070C),
                elevation = 0.dp,
            )
        },
        backgroundColor = Color(0xFF05070C),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ParamsRow(
                label = stringResource(R.string.sutoko_privacy_policy),
                onClick = { onEvent(SutokoParamsEvent.OnPrivacyPressed) }
            )

            if (uiState.isUserConnected) {
                ParamsRow(
                    label = stringResource(R.string.sutoko_params_activity_reload_my_account_data),
                    isLoading = uiState.isReloadLoading,
                    onClick = { onEvent(SutokoParamsEvent.OnReloadPressed) }
                )
                ParamsRow(
                    label = stringResource(R.string.sutoko_params_activity_delete_my_account_data),
                    isLoading = uiState.isDeleteLoading,
                    onClick = { onEvent(SutokoParamsEvent.OnDeletePressed) }
                )
            }

            ParamsRow(
                label = stringResource(R.string.sutoko_share_app),
                onClick = { onEvent(SutokoParamsEvent.OnSharePressed) }
            )

            if (uiState.isUserConnected) {
                ParamsRow(
                    label = stringResource(R.string.sutoko_disconnect),
                    onClick = { onEvent(SutokoParamsEvent.OnDisconnectPressed) }
                )
            }

            Divider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                color = Color.White.copy(alpha = 0.11f),
                thickness = 1.dp,
            )

            Text(
                text = uiState.versionText,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ParamsRow(
    label: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (isLoading) {
            Spacer(modifier = Modifier.width(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        }
    }
}
