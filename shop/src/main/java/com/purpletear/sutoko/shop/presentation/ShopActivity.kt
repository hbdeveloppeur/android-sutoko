package com.purpletear.sutoko.shop.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.purpletear.sutoko.auth.presentation.AccountConnectionActivity
import com.purpletear.sutoko.auth.presentation.AccountConnectionActivityModel
import com.purpletear.sutoko.shop.R
import com.purpletear.sutoko.shop.databinding.ActivityShopFixedBinding
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import purpletear.fr.purpleteartools.FingerV2

@AndroidEntryPoint
class ShopActivity : AppCompatActivity() {
    lateinit var binding: ActivityShopFixedBinding
    private lateinit var requestManager: RequestManager

    private val viewModel: ShopViewModel by viewModels()


    private fun startBuyAnimation(isPending: Boolean, pack: ShopPack) {
        ShopActivityGraphics.setUnlockItemDesign(
            this,
            binding.buyValidation,
            requestManager,
            viewModel.isUserConnected.value,
            isPending,
            pack,
        ) {
            ShopActivityGraphics.animateUnlockItem(
                this,
                binding.buyValidation,
                viewModel.balance.value.coins,
                viewModel.balance.value.diamonds,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestManager = Glide.with(this)
        binding = DataBindingUtil.setContentView(this, R.layout.shop_activity_shop_fixed)!!
        ShopActivityGraphics.setStatusBar(this)

        ShopActivityGraphics.setFakeStatusBarSize(this, binding)
        ShopActivityGraphics.setMaxContentHeight(this, binding)
        ShopActivityGraphics.setImages(binding, requestManager)

        ShopActivityGraphics.setBackground(this, binding, requestManager)

        ShopActivityGraphics.headerShouldDisappear(this, binding) { shouldDisappear ->
            if (shouldDisappear) {
                ShopActivityGraphics.setHeaderFading(binding)
            }
        }

        ShopActivityGraphics.pointsInfoConstraintShouldStickToScrollView(
            this,
            binding
        ) { shouldStick ->
            if (shouldStick) {
                ShopActivityGraphics.setUserPointsStickScrollViewsTop(binding)
            }
        }

        setListeners()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.headerState.collect { state ->
                    ShopActivityGraphics.setHeaderState(binding, state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.packs.collect { packs ->
                    ShopActivityGraphics.setCardsBalances(binding, packs)
                    ShopActivityGraphics.setProductsInfo(binding = binding, packsItems = packs)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseEvents.collect { event ->
                    val pack = viewModel.packs.value
                        .firstOrNull { it.pack.type == event.packType }
                        ?.pack

                    when (event) {
                        is ShopPurchaseEvent.Started -> {
                            // The Play Billing dialog provides its own in-progress UI.
                            // Do not show the unlock/success overlay before a real result.
                        }

                        is ShopPurchaseEvent.Success -> {
                            pack?.let { startBuyAnimation(isPending = false, it) }
                        }

                        is ShopPurchaseEvent.Pending -> {
                            pack?.let { startBuyAnimation(isPending = true, it) }
                        }

                        is ShopPurchaseEvent.Cancelled,
                        is ShopPurchaseEvent.AlreadyOwned,
                        is ShopPurchaseEvent.Failed -> {
                            ShopActivityGraphics.setUnlockItemPageVisibility(
                                this@ShopActivity,
                                binding,
                                false
                            )
                        }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent()
                intent.putExtra("coins", viewModel.balance.value.coins)
                intent.putExtra("diamonds", viewModel.balance.value.diamonds)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
    }


    private fun setListeners() {
        FingerV2.register(this.binding.sutokoShopButtonBack, null) {
            this.onBackPressed()
        }

        FingerV2.register(this.binding.sutokoShopSignInButton, null) {
            this.openConnectionPage()
        }

        FingerV2.register(this.binding.sutokoShopCard1.root, null) {
            this.onCoinsPackPressed(CoinsPackType.Low)
        }
        FingerV2.register(this.binding.sutokoShopCard2.root, null) {
            this.onCoinsPackPressed(CoinsPackType.Medium)
        }
        FingerV2.register(this.binding.sutokoShopCard3.root, null) {
            this.onCoinsPackPressed(CoinsPackType.High)
        }
        FingerV2.register(this.binding.buyValidation.buttonContinue, null) {
            ShopActivityGraphics.setUnlockItemPageVisibility(this, binding, false)
        }
    }

    private fun openConnectionPage() {
        startActivity(
            AccountConnectionActivity.require(
                this@ShopActivity,
                AccountConnectionActivityModel.Page.SIGNIN
            )
        )
    }

    private fun onCoinsPackPressed(packType: CoinsPackType) {
        viewModel.onEvent(ShopEvent.BuyPack(packType))
    }
}