package com.purpletear.smartads.adConsent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.purpletear.smartads.SmartAdsInterface
import com.purpletear.smartads.ads.AdsActivity

class AdmobConsent : AppCompatActivity() {
    private lateinit var adActivityResultLauncher: ActivityResultLauncher<Intent>
    private var adStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            this.adStarted = savedInstanceState.getBoolean("adStarted")
        }

        this.adActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_CANCELED) {
                    setResult(Activity.RESULT_CANCELED)
                } else {
                    if (result.data != null
                        && result.data!!.hasExtra("adErrorCode")
                        && result.data!!.hasExtra("adErrorMessage")
                    ) {

                        val intent = Intent()
                        intent.putExtra("adErrorCode", result.data!!.getStringExtra("adErrorCode"))
                        intent.putExtra(
                            "adErrorMessage",
                            result.data!!.getStringExtra("adErrorMessage")
                        )
                        intent.putExtra("adUnit", result.data!!.getStringExtra("adUnit"))
                        setResult(RESULT_OK, intent)
                    } else {
                        setResult(RESULT_OK)
                    }
                }
                finish()
            }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("adStarted", adStarted)
    }

    override fun onStart() {
        super.onStart()

        if (this.adStarted) {
            return
        }

        this.adStarted = true
        this.adActivityResultLauncher.launch(
            Intent(
                this@AdmobConsent,
                AdsActivity::class.java
            )
        )
    }


    companion object {
        private var lastSeenAdTime: Long? = null

        fun updateLastSeenAdTime() {
            lastSeenAdTime = System.currentTimeMillis()
        }

        fun canShowAd(): Boolean {
            if (lastSeenAdTime == null) {
                return true
            }
            val currentTime = System.currentTimeMillis()
            val diff = currentTime - lastSeenAdTime!!
            return diff > (1000 * 60 * 5)
        }

        fun registerActivityResultLauncher(
            activity: ComponentActivity,
            callback: SmartAdsInterface
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // Si non vu, result
                if (result.resultCode == RESULT_CANCELED) {
                    activity.setResult(Activity.RESULT_CANCELED)
                    callback.onAdAborted()
                } else {
                    if (result.data != null
                        && result.data!!.hasExtra("adErrorCode")
                        && result.data!!.hasExtra("adErrorMessage")
                    ) {

                        val adErrorCode = result.data!!.getStringExtra("adErrorCode")
                        val adErrorMessage = result.data!!.getStringExtra("adErrorMessage")
                        val adUnit = result.data!!.getStringExtra("adUnit")
                        callback.onErrorFound(adErrorCode, adErrorMessage, adUnit)
                    }
                    callback.onAdSuccessfullyWatched()
                    activity.setResult(RESULT_OK)
                }
            }
        }

        fun start(
            activity: Activity,
            activityResultLauncher: ActivityResultLauncher<Intent>
        ) {
            activityResultLauncher.launch(Intent(activity, AdmobConsent::class.java))
        }
    }
}