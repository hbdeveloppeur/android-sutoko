package com.purpletear.sutoko.shop.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.purpletear.sutoko.auth.presentation.AccountConnectionActivity
import com.purpletear.sutoko.auth.presentation.AccountConnectionActivityModel
import com.purpletear.sutoko.shop.R
import com.purpletear.sutoko.shop.domain.model.PackItem
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import com.purpletear.sutoko.shop.presentation.components.CoinsPackCard
import com.purpletear.sutoko.shop.presentation.components.LargeCoinsPackCard
import com.purpletear.sutoko.shop.presentation.components.PackGradient
import com.purpletear.sutoko.shop.presentation.components.PurchaseResultOverlay
import com.purpletear.sutoko.shop.presentation.components.ShopBackground
import com.purpletear.sutoko.shop.presentation.components.ShopHeader
import com.purpletear.sutoko.shop.presentation.components.ShopTopBar

/** Phase of the purchase result overlay. */
enum class PurchasePhase { CREDITING, SUCCESS, PENDING }

/** State of the purchase result overlay; null when hidden. */
data class PurchaseOverlayState(
    val pack: ShopPack,
    val phase: PurchasePhase,
)

@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    onClose: (Balance) -> Unit,
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val headerState by viewModel.headerState.collectAsStateWithLifecycle()
    val packs by viewModel.packs.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val isUserConnected by viewModel.isUserConnected.collectAsStateWithLifecycle()

    var purchaseOverlay by remember { mutableStateOf<PurchaseOverlayState?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.retryBalanceLoad()
    }

    BackHandler {
        onClose(balance)
    }

    LaunchedEffect(Unit) {
        viewModel.purchaseEvents.collect { event ->
            val pack = packs.firstOrNull { it.pack.type == event.packType }?.pack
            when (event) {
                is ShopPurchaseEvent.Started -> {
                    // The Play Billing dialog provides its own in-progress UI.
                }

                is ShopPurchaseEvent.Processing -> {
                    // Play confirmed the payment; backend verification is running.
                    pack?.let {
                        purchaseOverlay = PurchaseOverlayState(it, PurchasePhase.CREDITING)
                    }
                }

                is ShopPurchaseEvent.Success -> {
                    pack?.let { purchaseOverlay = PurchaseOverlayState(it, PurchasePhase.SUCCESS) }
                }

                is ShopPurchaseEvent.Pending -> {
                    pack?.let { purchaseOverlay = PurchaseOverlayState(it, PurchasePhase.PENDING) }
                }

                is ShopPurchaseEvent.NotConnected -> activity?.let(::openConnectionPage)

                is ShopPurchaseEvent.Failed -> {
                    purchaseOverlay = null
                    Toast.makeText(
                        context,
                        R.string.shop_sutoko_purchase_failed,
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is ShopPurchaseEvent.Cancelled,
                is ShopPurchaseEvent.AlreadyOwned -> purchaseOverlay = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ShopBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShopTopBar(onBackClick = { onClose(balance) })

            ShopHeader(
                state = headerState,
                onSignInClick = { activity?.let(::openConnectionPage) },
                onRetryClick = viewModel::retryBalanceLoad,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CoinsPackCard(
                        packItem = packs.find(CoinsPackType.Low),
                        nameRes = R.string.shop_sutoko_shop_pack_name_starter,
                        imageRes = R.drawable.shop_sutoko_shop_item_bag,
                        gradient = PackGradient.Purple,
                        onClick = { viewModel.onEvent(ShopEvent.BuyPack(CoinsPackType.Low)) },
                        modifier = Modifier.weight(1f),
                    )
                    CoinsPackCard(
                        packItem = packs.find(CoinsPackType.Medium),
                        nameRes = R.string.shop_sutoko_shop_pack_name_treasure,
                        imageRes = R.drawable.shop_sutoko_shop_item_chest,
                        gradient = PackGradient.Green,
                        showStars = true,
                        onClick = { viewModel.onEvent(ShopEvent.BuyPack(CoinsPackType.Medium)) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LargeCoinsPackCard(
                    packItem = packs.find(CoinsPackType.High),
                    onClick = { viewModel.onEvent(ShopEvent.BuyPack(CoinsPackType.High)) },
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        PurchaseResultOverlay(
            state = purchaseOverlay,
            balance = balance,
            isUserConnected = isUserConnected,
            onContinue = { purchaseOverlay = null },
        )
    }
}

private fun List<PackItem>.find(type: CoinsPackType): PackItem? =
    firstOrNull { it.pack.type == type }

private fun openConnectionPage(activity: android.app.Activity) {
    activity.startActivity(
        AccountConnectionActivity.require(
            activity,
            AccountConnectionActivityModel.Page.SIGNIN
        )
    )
}
