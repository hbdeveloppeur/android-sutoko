package fr.purpletear.sutoko.screens.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.sutoko.auth.presentation.AccountConnectionActivity
import com.purpletear.sutoko.auth.presentation.AccountConnectionActivityModel
import com.purpletear.sutoko.shop.presentation.ShopActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.screens.account.screen.AccountEvents
import fr.purpletear.sutoko.screens.account.screen.AccountScreen
import fr.purpletear.sutoko.screens.account.screen.AccountViewModel

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
                    viewModel.onEvent(AccountEvents.OnAccountStateChanged(true))
                } else {
                    viewModel.onEvent(AccountEvents.OnAccountStateChanged(false))
                }
            }
    }

    private fun registerGameLauncher() {
        this.gameLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

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

        this.viewModel.openGameCatalogEntityScreen.observe(this) {
            // TODO : Connect to StoryPreview
        }
    }


    private fun registerLaunchForResultShopActivity(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
