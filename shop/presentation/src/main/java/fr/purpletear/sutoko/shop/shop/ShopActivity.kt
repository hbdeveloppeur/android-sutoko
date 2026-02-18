package fr.purpletear.sutoko.shop.shop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.Purchase
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.SutokoAppParams
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.purpletear.shop.domain.model.Balance
import com.purpletear.shop.domain.usecase.GetShopBalanceUseCase
import com.purpletear.shop.domain.usecase.ObserveShopBalanceUseCase
import com.purpletear.shop.domain.usecase.RegisterOrderUseCaseIfNecessary
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.shop.presentation.R
import fr.purpletear.sutoko.shop.presentation.databinding.ActivityShopFixedBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import purpletear.fr.purpleteartools.Language
import javax.inject.Inject

@AndroidEntryPoint
class ShopActivity : AppCompatActivity() {
    lateinit var shopManager: ShopManager
    lateinit var binding: ActivityShopFixedBinding
    private lateinit var requestManager: RequestManager

    private var balance: Balance = Balance(coins = -1, diamonds = -1)

    @Inject
    lateinit var getShopBalanceUseCase: GetShopBalanceUseCase

    @Inject
    lateinit var observeShopBalanceUseCase: ObserveShopBalanceUseCase

    @Inject
    lateinit var registerOrderUseCaseIfNecessary: RegisterOrderUseCaseIfNecessary

    private fun onProductBought(purchase: Purchase, pack: ShopValues.SutokoShopPack) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val purchaseToken = purchase.purchaseToken
                val skuIdentifier = pack.sku ?: return@launch
                val userId = shopManager.customer.getUserId()
                val userToken = shopManager.customer.getUserToken()

                registerOrderUseCaseIfNecessary(
                    purchaseToken = purchaseToken,
                    skuIdentifier = skuIdentifier,
                    userId = userId,
                    userToken = userToken
                ).collect { result ->
                    if (result.isSuccess) {
                        CoroutineScope(Dispatchers.Main).launch {
                            startBuyAnimation(purchase, pack)
                        }
                    } else {
                        Log.e("ShopActivity", "Failed to register order", result.exceptionOrNull())
                    }
                }
            } catch (e: Exception) {
                Log.e("ShopActivity", "Error delivering remotely", e)
            }
        }

        // Process the purchase
        shopManager.customer.buyPack(
            this,
            purchase,
            shopManager.model.shopValues,
            shopManager.model.coinsBuyer,
            {

            }) { _, _ -> }

    }

    private fun startBuyAnimation(purchase: Purchase, pack: ShopValues.SutokoShopPack) {
        ShopActivityGraphics.setUnlockItemDesign(
            this,
            binding.buyValidation,
            requestManager,
            shopManager.customer.isUserConnected(),
            purchase.purchaseState == Purchase.PurchaseState.PENDING,
            shopManager.model.shopValues,
            pack
        ) {
            ShopActivityGraphics.animateUnlockItem(
                this,
                binding.buyValidation,
                balance.coins,
                balance.diamonds,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestManager = Glide.with(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_fixed)!!
        shopManager = ShopManager(
            activity = this,
            binding = this.binding,
            appParams = SutokoAppParams(),
            requestManager = requestManager,
            onProductBoughtCallback = this::onProductBought
        )
        ShopActivityGraphics.setStatusBar(this)
        setFirebaseCrashlyticsCustomKeys()

        lifecycleScope.launch {
            observeShopBalanceUseCase.invoke().collect {
                it?.let { balance ->
                    this@ShopActivity.balance = balance
                    ShopActivityGraphics.setDiamondsAndCoins(
                        binding,
                        balance.diamonds,
                        balance.coins
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent()
                intent.putExtra("coins", balance.coins)
                intent.putExtra("diamonds", balance.diamonds)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
    }


    private fun setFirebaseCrashlyticsCustomKeys() {
        val instance = FirebaseCrashlytics.getInstance()
        instance.setCustomKey("langCode", Language.determineLangDirectory())
        instance.setCustomKey("customer_is_connected", shopManager.customer.isUserConnected())
        this.shopManager.customer.user.uid?.let {
            instance.setCustomKey("user_id", it)
        }
    }
}
