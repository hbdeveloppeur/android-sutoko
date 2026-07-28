package com.purpletear.sutoko.shop.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopActivity : ComponentActivity() {

    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SutokoTheme {
                ShopScreen(
                    viewModel = viewModel,
                    onClose = ::closeWithBalance,
                )
            }
        }
    }

    private fun closeWithBalance(balance: Balance) {
        val intent = Intent()
            .putExtra("coins", balance.coins)
            .putExtra("diamonds", balance.diamonds)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
