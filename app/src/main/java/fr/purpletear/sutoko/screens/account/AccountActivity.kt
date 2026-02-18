package fr.purpletear.sutoko.screens.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sutokosharedelements.theme.SutokoTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.screens.account.screen.AccountEvents
import fr.purpletear.sutoko.screens.account.screen.AccountScreen
import fr.purpletear.sutoko.screens.account.screen.AccountViewModel
import fr.purpletear.sutoko.screens.accountConnection.AccountConnectionActivity
import fr.purpletear.sutoko.screens.accountConnection.AccountConnectionActivityModel
import fr.purpletear.sutoko.shop.shop.ShopActivity

@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {
    val viewModel: AccountViewModel by viewModels()
    private lateinit var accountLauncher: ActivityResultLauncher<Intent>
    private lateinit var gameLauncher: ActivityResultLauncher<Intent>
    private var shopActivityLauncher: ActivityResultLauncher<Intent> =
        registerLaunchForResultShopActivity()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.registerAccountLauncher()
        this.registerGameLauncher()

        setContent {
            SutokoTheme {
                AccountScreen(
                    viewModel = viewModel
                )
            }
        }
        this.observer()
    }

    private fun registerAccountLauncher() {
        this.accountLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    viewModel.customer.read(this@AccountActivity)
                    viewModel.onEvent(AccountEvents.OnAccountStateChanged(true))
                } else {
                    viewModel.onEvent(AccountEvents.OnAccountStateChanged(false))
                }
            }
    }

    private fun registerGameLauncher() {
        this.gameLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                viewModel.reloadGames()
            }
    }

    private fun observer() {
        this.viewModel.openAccountConnectionScreen.observe(this) {
            val intent = AccountConnectionActivity.require(
                this,
                AccountConnectionActivityModel.Page.SIGNIN
            )
            this.accountLauncher.launch(intent)
        }


        this.viewModel.openShopScreen.observe(this) {
            val intent = Intent(this, ShopActivity::class.java)
            shopActivityLauncher.launch(intent)
        }

        this.viewModel.openGameScreen.observe(this) {
            // TODO : Connect to StoryPreview
        }
    }


    private fun registerLaunchForResultShopActivity(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val coins = data?.getIntExtra("coins", -1) ?: -1
                val diamonds = data?.getIntExtra("diamonds", -1) ?: -1
                if (coins != -1 && diamonds != -1) {
                    viewModel.onEvent(AccountEvents.OnShopStateChanged(coins, diamonds))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}
