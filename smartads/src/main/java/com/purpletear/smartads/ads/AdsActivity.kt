package com.purpletear.smartads.ads

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.purpletear.smartads.BuildConfig
import com.purpletear.smartads.R
import com.purpletear.smartads.adConsent.AdmobConsent
import purpletear.fr.purpleteartools.MemoryHandler
import purpletear.fr.purpleteartools.Runnable2

class AdsActivity : AppCompatActivity() {

    private var countDownText: TextView? = null
    private var memoryHandler: MemoryHandler = MemoryHandler()
    private var timeOutSignal: Boolean = false
    private var loadingSuccess: Boolean = false
    var mRewardedAd: RewardedAd? = null
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ads)
        timeOut()
        isLoading = true
        MobileAds.initialize(this) {
            loadRewardedAd()
        }

    }

    override fun onDestroy() {
        this.memoryHandler.kill()
        super.onDestroy()
    }

    private fun timeOut() {
        val r = object : Runnable2("timeOut", 10000) {
            override fun run() {
                if (this@AdsActivity.isFinishing || loadingSuccess) {
                    return
                }
                if (isLoading) {
                    isLoading = false
                    timeOutSignal = true
                    countDown()
                }
            }
        }
        memoryHandler.push(r)
        memoryHandler.run(r)
    }

    private fun loadRewardedAd() {
        mRewardedAd = null
        val adRequest = AdRequest.Builder().build()
        adRequest.isTestDevice(this)

        RewardedAd.load(
            this,
            if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else "ca-app-pub-8906119025049365/3624834213",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardedAd = null
                    if (this@AdsActivity.isFinishing) {
                        return
                    }

                    if (timeOutSignal) {
                        return
                    }
                    isLoading = false
                    // Count down
                    countDown()
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    if (this@AdsActivity.isFinishing) {
                        mRewardedAd = null
                        return
                    }
                    loadingSuccess = true
                    mRewardedAd = rewardedAd
                    isLoading = false
                    showRewardedVideo()
                }
            }
        )
    }

    private fun countDown(text: Int = 30) {
        if (text == 0) {
            AdmobConsent.updateLastSeenAdTime()
            setResult(RESULT_OK)
            finish()
            return
        }
        if (countDownText == null) {
            countDownText = findViewById(R.id.ads_progress_text)
        }
        countDownText!!.text = text.toString()
        val r = object : Runnable2("countDown", 1000) {
            override fun run() {
                if (this@AdsActivity.isFinishing) {
                    return
                }
                countDown(text - 1)
            }
        }
        memoryHandler.push(r)
        memoryHandler.run(r)
    }

    private fun showRewardedVideo() {
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("SmartAds", "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mRewardedAd = null
                    if (this@AdsActivity.isFinishing) {
                        return
                    }
                    finish()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("SmartAds", "Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mRewardedAd = null
                    if (this@AdsActivity.isFinishing) {
                        return
                    }
                    val intent = Intent()
                    intent.putExtra("adErrorCode", adError.code.toString())
                    intent.putExtra("adErrorMessage", adError.message)
                    intent.putExtra("adUnit", mRewardedAd?.adUnitId)
                    setResult(RESULT_OK, intent)
                    finish()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("SmartAds", "Ad showed fullscreen content.")
                }
            }

            mRewardedAd?.show(
                this
            ) {
                AdmobConsent.updateLastSeenAdTime()
                setResult(RESULT_OK)
            }
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

}