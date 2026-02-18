package fr.purpletear.sutoko.shop.shop

import android.app.Activity
import android.widget.Toast
import com.android.billingclient.api.Purchase
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SutokoAppParams
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import fr.purpletear.sutoko.shop.coinsLogic.CoinsBuyerListener
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import fr.purpletear.sutoko.shop.coinsLogic.CustomerCallbacks
import fr.purpletear.sutoko.shop.premium.Premium
import fr.purpletear.sutoko.shop.premium.PremiumSubscriptorListener
import fr.purpletear.sutoko.shop.presentation.R
import fr.purpletear.sutoko.shop.presentation.databinding.ActivityShopFixedBinding
import purpletear.fr.purpleteartools.FingerV2


class ShopManager(
    val activity: Activity,
    val binding: ActivityShopFixedBinding,
    val appParams: SutokoAppParams,
    val requestManager: RequestManager,
    private var firebaseAnalytics: FirebaseAnalytics? = FirebaseAnalytics.getInstance(activity),
    private val onProductBoughtCallback: ((purchase: Purchase, pack: ShopValues.SutokoShopPack) -> Unit)? = null
) : CustomerCallbacks, CoinsBuyerListener,
    PremiumSubscriptorListener {
    var customer: Customer
        private set
    lateinit var model: ShopActivityModel

    // Not safe to use.
    private var cachedPremiumValue: Boolean = Premium.userIsPremium(activity)


    fun pause() {
        this.model.coinsBuyer.isPaused = true
    }

    fun resume() {
        this.model.coinsBuyer.isPaused = false
    }

    init {

        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        }
        model = ShopActivityModel(activity, this, this)
        customer = Customer(callbacks = this)
        customer.read(activity)
        ShopActivityGraphics.setDiamondsAndCoins(
            binding,
            customer.getDiamonds(),
            customer.getCoins()
        )
        this.model.coinsBuyer.connect(onConnected = {
            setProductsInfo(activity, binding)
            this.model.coinsBuyer.getUnacknowledgedPurchases { purchases ->
                customer.processPurchasesIfRequired(
                    activity,
                    this.model.shopValues,
                    this.model.coinsBuyer,
                    purchases
                ) { isSuccessful, exception ->
                }
            }
        }, {
            Toast.makeText(
                activity.applicationContext,
                R.string.sutoko_check_google_play_connexion,
                Toast.LENGTH_LONG
            ).show()
        })

        ShopActivityGraphics.setStatusBar(activity)
        ShopActivityGraphics.setFakeStatusBarSize(activity, binding)
        ShopActivityGraphics.setMaxContentHeight(activity, binding)
        ShopActivityGraphics.setImages(binding, requestManager)
        ShopActivityGraphics.setUI(binding, this.model.shopValues)


        ShopActivityGraphics.setBackground(activity, binding, requestManager)

        this.model.headerShouldDisappear(activity, binding) { shouldDisappear ->
            if (shouldDisappear) {
                ShopActivityGraphics.setHeaderFading(binding)
            }
        }
        this.model.pointsInfoConstraintShouldStickToScrollView(activity, binding) { shouldStick ->
            if (shouldStick) {
                ShopActivityGraphics.setUserPointsStickScrollViewsTop(binding)
            }
        }

        model.premium.connect({

        }, {
        })
        setListeners()
    }


    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            this.refreshIsPremiumProfile()
        }
    }

    private fun refreshIsPremiumProfile() {
        val v = this.model.premium.hasProduct
        if (cachedPremiumValue == v) {
            return
        }
        cachedPremiumValue = v
        this.firebaseAnalytics?.setUserProperty("is_premium", if (v) "yes" else "no")
    }

    override fun onBillingServicesNotAvailable() {

    }

    override fun onConnectionFailed() {

    }

    override fun onUnhandledError() {

    }


    override fun onSubscriptionGrant() {
        val instance = FirebaseFirestore.getInstance()
        val sfDocRef = instance.collection("params").document("fr_purpletear_sutoko")

        instance.runTransaction { transaction ->
            val snapshot = transaction.get(sfDocRef)
            var newPopulation = snapshot.getDouble("leavingKeys")!! - 1
            this.appParams.leavingKeys = newPopulation.toInt()
            if (newPopulation.toInt() == -1) {
                newPopulation = 0.toDouble()
            }
            transaction.update(sfDocRef, "leavingKeys", newPopulation)

            null
        }

    }

    private fun setListeners() {
        FingerV2.register(this.binding.sutokoShopButtonBack, null) {
            activity.onBackPressed()
        }

        FingerV2.register(this.binding.sutokoShopCard1.root, null) {
            this.onCoinsPackPressed(this.model.shopValues.lowPack ?: return@register)
        }
        FingerV2.register(this.binding.sutokoShopCard2.root, null) {
            this.onCoinsPackPressed(this.model.shopValues.mediumPack ?: return@register)
        }
        FingerV2.register(this.binding.sutokoShopCard3.root, null) {
            this.onCoinsPackPressed(this.model.shopValues.highPack ?: return@register)
        }
        FingerV2.register(this.binding.buyValidation.buttonContinue, null) {
            this.onUnlockItemContinuePressed()
        }
    }

    private fun setProductsInfo(activity: Activity, binding: ActivityShopFixedBinding) {
        val skus = this.model.shopValues.getSkus()
        this.model.coinsBuyer.getProductsInfo(skus, onFound = { details ->
            when (details.productId) {
                this.model.shopValues.lowPack?.sku -> {
                    binding.sutokoShopCard1.sutokoCoinsCardButtonBuy.text = details.price
                }

                this.model.shopValues.mediumPack?.sku -> {
                    binding.sutokoShopCard2.sutokoCoinsCardButtonBuy.text = details.price
                }

                this.model.shopValues.highPack?.sku -> {
                    binding.sutokoShopCard3.sutokoCoinsCardLargeButtonBuy.text = details.price
                }

                else -> {
                    return@getProductsInfo
                }
            }
        })
        this.model.coinsBuyer.getPremiumKeyInfos(
            this.model.shopValues.premium?.sku ?: return,
            onFound = { details ->
                if (!Premium.userIsPremium(activity)) {

                }
            })
    }

    private fun onUnlockItemContinuePressed() {
        ShopActivityGraphics.setUnlockItemPageVisibility(activity, binding, false)
    }

    override fun onAlreadyBought() {
        this.model.coinsBuyer.getUnacknowledgedPurchases { purchases ->
            this.customer.processPurchasesIfRequired(
                activity,
                this.model.shopValues,
                this.model.coinsBuyer,
                purchases
            ) { _, _ ->
            }
        }
    }

    override fun onProductBought(purchase: Purchase) {
        if (this.model.coinsBuyer.isPaused) {
            return
        }

        this.model.playCoinsSound(activity)

        val pack = try {
            this.customer.getPackFromProduct(this.model.shopValues, purchase)
        } catch (e: IllegalStateException) {
            return
        }

        onProductBoughtCallback?.invoke(purchase, pack)
    }

    override fun onError(responseCode: Int) {

    }

    override fun onAcknowledgeFailure() {

    }


    private fun onCoinsPackPressed(pack: ShopValues.SutokoShopPack) {
        this.model.coinsBuyer.buy(activity, pack.sku ?: return) {

        }
    }

    override fun onCoinsOrDiamondsUpdated(coins: Int, diamonds: Int) {
        if (!customer.isUserConnected()) {
            ShopActivityGraphics.setDiamondsAndCoins(
                binding,
                coins,
                diamonds,
            )
        }
    }
}
