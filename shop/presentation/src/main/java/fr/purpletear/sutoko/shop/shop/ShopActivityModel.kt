package fr.purpletear.sutoko.shop.shop

import android.app.Activity
import android.os.Handler
import android.os.Looper
import fr.purpletear.sutoko.shop.coinsLogic.CoinsBuyerListener
import fr.purpletear.sutoko.shop.premium.Premium
import fr.purpletear.sutoko.shop.premium.PremiumSubscriptorListener
import fr.purpletear.sutoko.shop.presentation.databinding.ActivityShopFixedBinding
import java.io.File
import java.io.IOException

class ShopActivityModel(
    activity: Activity,
    listener: CoinsBuyerListener,
    premiumListener: PremiumSubscriptorListener
) {

    var shopValues: ShopValues = ShopValues()
    var coinsBuyer: fr.purpletear.sutoko.shop.coinsLogic.CoinsBuyer =
        fr.purpletear.sutoko.shop.coinsLogic.CoinsBuyer(activity, listener)
        private set

    //  var soundPlayer: TableOfSoundsPlayer = TableOfSoundsPlayer()
    var premium: Premium = Premium(activity, premiumListener)


    init {
        shopValues.get()
    }

    fun playCoinsSound(activity: Activity) {
        val file: File
        try {
            // file = File(Std.getFileFromAssets(activity, "coins_buy.mp3").path)
        } catch (e: IOException) {
            return
        }
//        val path = file.path
//        soundPlayer.addToPreloadList(path)
//        soundPlayer.preload(activity)
//        soundPlayer.play(path, onStart@{
//
//        }, onFinish@{
//            soundPlayer.remove(path)
//        })
    }

    fun headerShouldDisappear(
        activity: Activity,
        binding: ActivityShopFixedBinding,
        onCompletion: (Boolean) -> Unit
    ) {
        binding.sutokoShopSubtitle.post {
            if (activity.isFinishing) {
                return@post
            }
            val distance = binding.sutokoShopSubtitle.y + binding.sutokoShopSubtitle.height
            binding.sutokoShopScrollview.post f2@{
                if (activity.isFinishing) {
                    return@f2
                }
                val scrollViewHeight = binding.sutokoShopScrollview.getChildAt(0).height
                val screenHeight = binding.root.height
                val shouldDisappear =
                    screenHeight != null && (scrollViewHeight + distance) > screenHeight
                Handler(Looper.getMainLooper()).post f@{
                    if (activity.isFinishing) {
                        return@f
                    }
                    onCompletion(shouldDisappear)
                }
            }
        }
    }

    fun pointsInfoConstraintShouldStickToScrollView(
        activity: Activity,
        binding: ActivityShopFixedBinding,
        onCompletion: (Boolean) -> Unit
    ) {
        binding.sutokoShopSubtitle.post {
            if (activity.isFinishing) {
                return@post
            }
            val distance = binding.sutokoCoinsInformation.y + binding.sutokoCoinsInformation.height
            binding.sutokoShopScrollview.post f2@{
                if (activity.isFinishing) {
                    return@f2
                }
                val scrollViewHeight = binding.sutokoShopScrollview.getChildAt(0).height
                val screenHeight = binding.root.height
                val shouldDisappear =
                    screenHeight != null && (scrollViewHeight + distance) > screenHeight
                Handler(Looper.getMainLooper()).post f@{
                    if (activity.isFinishing) {
                        return@f
                    }
                    onCompletion(shouldDisappear)
                }
            }
        }
    }
}