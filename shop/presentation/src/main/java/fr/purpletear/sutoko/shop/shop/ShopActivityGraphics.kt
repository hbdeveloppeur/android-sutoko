package fr.purpletear.sutoko.shop.shop

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.sharedelements.SutokoSharedElementsData
import fr.purpletear.sutoko.shop.presentation.R
import fr.purpletear.sutoko.shop.presentation.databinding.ActivityShopFixedBinding
import fr.purpletear.sutoko.shop.presentation.databinding.LayoutShopBuyValidationBinding
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.Std

object ShopActivityGraphics {


    fun setBackground(
        activity: Activity,
        binding: ActivityShopFixedBinding,
        requestManager: RequestManager
    ) {
        val ratio = "600:1072"
        SutokoSharedElementsData.loadScreenSize(activity)

        binding.sutokoShopBackground.updateSize(ratio, SutokoSharedElementsData.screenSize)
        binding.sutokoShopBackground.setImage(requestManager, R.drawable.shop_background_fix)
        binding.sutokoShopBackground.setVideo(
            activity,
            "https://data.sutoko.app/resources/shop_background.mp4"
        )
        binding.sutokoShopBackground.post {
            if (binding.sutokoShopBackground.shouldResize(ratio)) {
                binding.sutokoShopBackground.playVideo()
            }
        }
    }

    fun setDiamondsAndCoins(
        binding: ActivityShopFixedBinding,
        diamonds: Int,
        coins: Int,
    ) {
        binding.sutokoCoins.text = coins.toString()
        binding.sutokoDiamonds.text = diamonds.toString()
    }


    fun unlockItemPageIsVisible(binding: ActivityShopFixedBinding): Boolean {
        return binding.buyValidation.root.visibility == View.VISIBLE
    }

    fun setUnlockItemPageVisibility(
        activity: Activity,
        binding: ActivityShopFixedBinding,
        isVisible: Boolean
    ) {
        if (!isVisible) {
            binding.buyValidation.root.visibility = View.INVISIBLE
        } else {
            Animation.setAnimation(
                binding.buyValidation.root,
                Animation.Animations.ANIMATION_FADEIN,
                activity,
                380
            )
        }
    }

    private fun setUnlockCoinsInformationVisibility(
        binding: LayoutShopBuyValidationBinding,
        isVisible: Boolean
    ) {
        binding.sutokoCoinsInformation.visibility = if (isVisible) View.VISIBLE else View.GONE
    }


    fun setUnlockDiamondsByWatchingAds(
        activity: Activity, binding: LayoutShopBuyValidationBinding,
        requestManager: RequestManager,
        isUserConnected: Boolean,
        onComplete: () -> Unit
    ) {

        setUnlockCoinsInformationVisibility(binding, true)


        val image = R.drawable.sutoko_diamond_big

        requestManager.load(image)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Handler(Looper.getMainLooper()).post(onComplete)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Handler(Looper.getMainLooper()).post(onComplete)
                    return false
                }
            })
            .into(binding.sutokoShopBuyValidationItemImage)
        binding.title.text = activity.getText(R.string.sutoko_unlock_diamonds_done)
        binding.packName.text = ""
        binding.sutokoAuthWarning.visibility = if (isUserConnected) {
            View.GONE
        } else {
            View.VISIBLE
        }

        requestManager.load(R.drawable.sutoko_item_coin)
            .into(binding.sutokoCoinsImage)

        requestManager.load(R.drawable.sutoko_ic_diamond)
            .into(binding.sutokoDiamondsImage)

        requestManager.load(R.drawable.sutoko_account_creation_header_background)
            .into(binding.headerBackground)

    }

    fun setUnlockItemDesign(
        context: Context, binding: LayoutShopBuyValidationBinding,
        requestManager: RequestManager,
        isUserConnected: Boolean,
        isPending: Boolean,
        shopValues: ShopValues,
        shopPack: ShopValues.SutokoShopPack,
        onComplete: () -> Unit
    ) {

        setUnlockCoinsInformationVisibility(binding, !isPending)


        val image = when (shopPack) {
            shopValues.lowPack -> {
                R.drawable.sutoko_shop_item_bag
            }

            shopValues.mediumPack -> {
                R.drawable.sutoko_shop_item_chest
            }

            shopValues.highPack -> {
                R.drawable.sutoko_shop_item_chest2
            }

            else -> return
        }

        requestManager.load(image)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Handler(Looper.getMainLooper()).post(onComplete)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Handler(Looper.getMainLooper()).post(onComplete)
                    return false
                }
            })
            .into(binding.sutokoShopBuyValidationItemImage)
        if (isPending) {
            binding.title.text = context.getText(R.string.sutoko_shop_unlock_item_pending_title)
            binding.packName.text =
                context.getText(R.string.sutoko_shop_unlock_item_pending_title_subtitle)
            binding.sutokoAuthWarning.text =
                context.getText(R.string.sutoko_shop_unlock_item_pending_title_description)
            return

        } else {
            binding.sutokoAuthWarning.visibility = if (isUserConnected) View.GONE else View.VISIBLE
            binding.packName.text = context.getString(
                if (shopPack == shopValues.premium) {
                    R.string.sutoko_shop_unlock_item_title_premium
                } else {
                    R.string.sutoko_shop_unlock_item_title_pack
                }
            )

        }

        requestManager.load(R.drawable.sutoko_item_coin)
            .into(binding.sutokoCoinsImage)

        requestManager.load(R.drawable.sutoko_ic_diamond)
            .into(binding.sutokoDiamondsImage)

        requestManager.load(R.drawable.sutoko_account_creation_header_background)
            .into(binding.headerBackground)

    }

    fun animateUnlockItem(
        activity: Activity,
        binding: LayoutShopBuyValidationBinding,
        coins: Int,
        diamonds: Int
    ) {
        binding.root.visibility = View.VISIBLE
        setUnlockItemDiamondsAndCoins(binding, diamonds, coins)
        Animation.setAnimation(
            binding.sutokoShopBuyValidationItemImage,
            Animation.Animations.ANIMATION_ZOOM_IN,
            activity,
            580
        )
    }


    fun setImages(binding: ActivityShopFixedBinding, requestManager: RequestManager) {
        val map = mapOf<ImageView, Int>(
            // Coins card.
            binding.sutokoShopCard1.sutokoCoinsCardImageItem to R.drawable.sutoko_shop_item_bag,
            binding.sutokoShopCard2.sutokoCoinsCardImageItem to R.drawable.sutoko_shop_item_chest,
            binding.sutokoShopCard1.sutokoCoinsCardCoinImage to R.drawable.sutoko_item_coin,
            binding.sutokoShopCard1.sutokoCoinsCardDiamondImage to R.drawable.sutoko_ic_diamond,
            binding.sutokoShopCard2.sutokoCoinsCardCoinImage to R.drawable.sutoko_item_coin,
            binding.sutokoShopCard2.sutokoCoinsCardDiamondImage to R.drawable.sutoko_ic_diamond,
            binding.sutokoShopCard2.sutokoCoinsCardStarsImage to R.drawable.ic_stars_multi,

            // Treasure
            binding.sutokoShopCard3.sutokoCoinsCardLargeBackgroundImage to R.drawable.sutoko_shop_item_mega_background,
            binding.sutokoShopCard3.sutokoCoinsCardLargeItemImage to R.drawable.sutoko_shop_item_chest2,
            binding.sutokoShopCard3.sutokoCoinsCardLargeGainInfoCoinsImage to R.drawable.sutoko_item_coin,
            binding.sutokoShopCard3.sutokoCoinsCardLargeGainInfoDiamondImage to R.drawable.sutoko_ic_diamond,
        )
        map.forEach { (view, id) ->
            requestManager.load(id)
                .transition(withCrossFade()).into(view)
        }
    }


    fun setUI(binding: ActivityShopFixedBinding, values: ShopValues) {
        val map = hashMapOf(
            binding.sutokoShopCard1.sutokoCoinsCardCoinText to values.lowPack?.coins.toString(),
            binding.sutokoShopCard1.sutokoCoinsCardDiamondText to values.lowPack?.diamonds.toString(),
            binding.sutokoShopCard2.sutokoCoinsCardCoinText to values.mediumPack?.coins.toString(),
            binding.sutokoShopCard2.sutokoCoinsCardDiamondText to values.mediumPack?.diamonds.toString(),
            binding.sutokoShopCard3.sutokoCoinsCardLargeGainInfoCoinsText to values.highPack?.coins.toString(),
            binding.sutokoShopCard3.sutokoCoinsCardLargeGainInfoDiamondText to values.highPack?.diamonds.toString(),
        )

        map.forEach { (view, value) ->
            view.text = value
        }
    }


    fun setCoinsAndDiamondsShopInfoLoading(binding: ActivityShopFixedBinding, isLoading: Boolean) {
        binding.sutokoCoinsInformationProgressbar.visibility =
            if (isLoading) View.VISIBLE else View.INVISIBLE
        binding.sutokoCoinsInformation.alpha = if (isLoading) 0.3f else 1f
    }

    fun setStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // setFakeStatusBarSize(activity)
            activity.window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }

                statusBarColor = Color.TRANSPARENT

            }
            WindowInsetsControllerCompat(
                activity.window,
                activity.window.decorView
            ).isAppearanceLightStatusBars = false
        }
    }

    private fun setUnlockItemDiamondsAndCoins(
        binding: LayoutShopBuyValidationBinding,
        diamonds: Int,
        coins: Int
    ) {
        binding.coins.text = coins.toString()
        binding.diamonds.text = diamonds.toString()
    }


    fun setFakeStatusBarSize(activity: Activity, binding: ActivityShopFixedBinding) {
        val statusBar = SutokoSharedElementsData.getStatusBarHeight(activity)
        val extra = Std.dpToPx(26f, activity.resources)
        (binding.root as MotionLayout).getConstraintSet(R.id.start)?.let { startConstraintSet ->
            // You can set the width and height here as well
            startConstraintSet.setMargin(
                R.id.sutoko_shop_title,
                ConstraintSet.TOP,
                statusBar + extra
            )
        }
        (binding.root as MotionLayout).getConstraintSet(R.id.end)?.let { startConstraintSet ->
            startConstraintSet.setMargin(R.id.sutoko_shop_scrollview, ConstraintSet.TOP, statusBar)
            startConstraintSet.setMargin(
                R.id.sutoko_shop_title,
                ConstraintSet.TOP,
                statusBar + extra
            )
        }
    }

    fun setMaxContentHeight(activity: Activity, binding: ActivityShopFixedBinding) {
        val statusBar = SutokoSharedElementsData.getStatusBarHeight(activity)

        binding.sutokoShopScrollview.post {
            binding.sutokoShopScrollview.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintMaxHeight = binding.sutokoShopScrollview.getChildAt(0).height
            }
            (binding.root as MotionLayout).getConstraintSet(R.id.start)?.let { constraint ->
                // You can set the width and height here as well
                constraint.setMargin(R.id.sutoko_shop_scrollview, ConstraintSet.TOP, statusBar)
                constraint.constrainMaxHeight(
                    R.id.sutoko_shop_scrollview,
                    binding.sutokoShopScrollview.getChildAt(0).height
                )
            }
            (binding.root as MotionLayout).getConstraintSet(R.id.end)?.let { constraint ->
                constraint.setMargin(R.id.sutoko_shop_scrollview, ConstraintSet.TOP, statusBar)
                constraint.constrainMaxHeight(
                    R.id.sutoko_shop_scrollview,
                    binding.sutokoShopScrollview.getChildAt(0).height
                )
            }
        }
    }


    fun setHeaderFading(binding: ActivityShopFixedBinding) {
        (binding.root as MotionLayout).getConstraintSet(R.id.start)?.let { startConstraintSet ->
            // You can set the width and height here as well
            startConstraintSet.setAlpha(R.id.sutoko_shop_subtitle, 1f)
            startConstraintSet.setAlpha(R.id.sutoko_shop_title, 1f)
            startConstraintSet.setAlpha(R.id.sutoko_shop_button_back, 1f)
        }
        binding.sutokoShopScrollview.post {
            (binding.root as MotionLayout).getConstraintSet(R.id.end)?.let { constraint ->
                constraint.setAlpha(R.id.sutoko_shop_subtitle, 0f)
                constraint.setAlpha(R.id.sutoko_shop_title, 0f)
                constraint.setAlpha(R.id.sutoko_shop_button_back, 0f)
            }
        }
    }

    fun setUserPointsStickScrollViewsTop(binding: ActivityShopFixedBinding) {
        (binding.root as MotionLayout).getConstraintSet(R.id.start)?.let { constraint ->
            constraint.setAlpha(R.id.sutoko_coins_information, 1f)
        }
        binding.sutokoShopScrollview.post {
            (binding.root as MotionLayout).getConstraintSet(R.id.end)?.let { constraint ->
                constraint.setAlpha(R.id.sutoko_coins_information, 0f)
            }
        }
    }
}