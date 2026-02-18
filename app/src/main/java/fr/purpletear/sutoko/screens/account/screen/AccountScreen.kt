package fr.purpletear.sutoko.screens.account.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.purpletear.sutoko.screens.account.screen.components.Connection
import fr.purpletear.sutoko.screens.account.screen.components.GamesGrid

@Composable
fun AccountScreen(viewModel: AccountViewModel) {
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
                    painterResource(fr.purpletear.sutoko.shop.presentation.R.drawable.sutoko_shop_card_item_premium_pass),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .matchParentSize()
                )
                Connection(
                    isConnected = viewModel.isUserConnected.value,
                    coins = viewModel.coinsBalance.value.data?.coins ?: -1,
                    diamonds = viewModel.coinsBalance.value.data?.diamonds ?: -1,
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
                list = viewModel.allGames.value,
                onTap = { viewModel.onEvent(AccountEvents.OnGamePressed(it)) }
            )
        }

    }
}
