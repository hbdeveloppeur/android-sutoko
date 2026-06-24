package fr.purpletear.sutoko.screens.account.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.purpletear.sutoko.screens.account.screen.components.Connection
import fr.purpletear.sutoko.screens.account.screen.components.GamesGrid
import com.purpletear.sutoko.shop.R as ShopR

@Composable
fun AccountScreen(viewModel: AccountViewModel) {
    val isConnected by viewModel.isUserConnected.collectAsState()
    val coinsBalance by viewModel.balance.collectAsState()
    val allGames by viewModel.allGames.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {

        Column {
            Box(
                modifier = Modifier
                    .heightIn(max = 400.dp)
            ) {
                Image(
                    painterResource(ShopR.drawable.sutoko_shop_card_item_premium_pass),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .matchParentSize()
                )
                Connection(
                    isConnected = isConnected,
                    coins = coinsBalance.data?.coins ?: -1,
                    diamonds = coinsBalance.data?.diamonds ?: -1,
                    onClickCoins = {
                        viewModel.onEvent(AccountEvents.OnClickCoins)
                    },
                    onClickDiamonds = {
                        viewModel.onEvent(AccountEvents.OnClickDiamonds)
                    },
                    onTapSignIn = { viewModel.onEvent(AccountEvents.OnAccountButtonPressed) }
                )
            }
            GamesGrid(
                list = allGames,
                onTap = { viewModel.onEvent(AccountEvents.OnGamePressed(it)) }
            )
        }

    }
}
