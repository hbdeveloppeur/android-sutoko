package fr.purpletear.sutoko.screens.params

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.helpers.GdprConsentHelper

@Composable
fun SutokoParamsScreen(
    viewModel: SutokoParamsViewModel,
    onOpenPrivacyPolicy: (String) -> Unit,
    onOpenInstagram: () -> Unit,
    onShareApp: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findHostActivity()
    val consentInformation = remember(context) { UserMessagingPlatform.getConsentInformation(context) }
    var showPrivacyOptions by remember {
        mutableStateOf(consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED)
    }

    LaunchedEffect(uiState.effect) {
        when (val effect = uiState.effect) {
            is SutokoParamsEffect.OpenPrivacyPolicy -> onOpenPrivacyPolicy(effect.url)
            is SutokoParamsEffect.OpenInstagram -> onOpenInstagram()
            is SutokoParamsEffect.ShareApp -> onShareApp()
            is SutokoParamsEffect.NavigateBack -> onNavigateBack()
            null -> Unit
        }
        viewModel.onEvent(SutokoParamsEvent.OnEffectConsumed)
    }

    SutokoParamsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        showPrivacyOptions = showPrivacyOptions && activity != null,
        onPrivacyOptions = {
            activity?.let { host ->
                UserMessagingPlatform.showPrivacyOptionsForm(host) { error ->
                    GdprConsentHelper.notifyConsentUpdated()
                    if (error != null) {
                        android.widget.Toast.makeText(context, R.string.sutoko_ad_privacy_error, android.widget.Toast.LENGTH_SHORT).show()
                    }
                    showPrivacyOptions = consentInformation.privacyOptionsRequirementStatus ==
                        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                }
            }
        },
    )
}

@Composable
private fun SutokoParamsContent(
    uiState: SutokoParamsUiState,
    onEvent: (SutokoParamsEvent) -> Unit,
    showPrivacyOptions: Boolean = false,
    onPrivacyOptions: () -> Unit = {},
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

            if (showPrivacyOptions) {
                ParamsRow(
                    label = stringResource(R.string.sutoko_ad_privacy_options),
                    onClick = onPrivacyOptions,
                )
            }

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

            ParamsRow(
                label = stringResource(R.string.sutoko_params_activity_delete_downloaded_stories),
                isLoading = uiState.isDeleteDownloadedStoriesLoading,
                onClick = { onEvent(SutokoParamsEvent.OnDeleteDownloadedStoriesPressed) }
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

            BuiltByRow(
                onClick = { onEvent(SutokoParamsEvent.OnBuiltByPressed) }
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
private fun BuiltByRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.built_by_hocinehope),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(R.string.sutoko_built_by_hocinehope),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_instagram),
            contentDescription = stringResource(R.string.sutoko_built_by_instagram_description),
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp),
        )
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
            fontSize = 13.sp,
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

private fun Context.findHostActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
