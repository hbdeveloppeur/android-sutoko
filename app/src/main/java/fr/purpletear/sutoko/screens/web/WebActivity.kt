package fr.purpletear.sutoko.screens.web

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.sharedelements.SutokoPlayerPointsManager
import com.example.sharedelements.SutokoSharedElementsData
import com.google.firebase.FirebaseException
import com.example.sutokosharedelements.Data
import fr.purpletear.sutoko.R
import com.example.sharedelements.SutokoAppParams
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
import java.lang.IllegalStateException

class WebActivity : AppCompatActivity() {
    private lateinit var model: WebActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SutokoSharedElementsData.setStrictMode()
        setContentView(R.layout.activity_web)

        val o = TableOfSymbols(-1)
        model = WebActivityModel(this, o.firstName)
    }

    override fun onBackPressed() {
        if (model.canGoBack()) {
            model.goBack()
            return
        }

        if (model.backButtonSentenceResId == -1) {
            super.onBackPressed()
            return
        }

        Std.confirm(
            this,
            model.backButtonSentenceResId,
            R.string.sutoko_ok,
            R.string.sutoko_abort, {
                super.onBackPressed()
            }, {}
        )
    }

    private fun onPageLoaded() {
        WebActivityGraphics.fadeLoadingFilter(this, false)
    }

    private fun onError(errorCode: Int, url: String) {
        try {
            throw IllegalStateException("Error encountered (code:$errorCode; url:$url)")
        } catch (e: IllegalStateException) {
            if (errorCode == WebViewClient.ERROR_CONNECT || errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_TIMEOUT) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.sutoko_web_not_connected),
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            } else {
                //Crashlytics.logException(e)
            }
        }
    }

    override fun onDestroy() {
        this.model.destroyWebView()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            WebActivityGraphics.setImage(this, model.requestManager)
            model.loadUrl(this, ::onPageLoaded, ::onError)
        }
    }

    companion object {
        /**
         * Method to call before coming to this activity
         *
         * @param activity
         * @return
         */
        fun require(
            activity: Activity,
            url: String,
            backButtonStringId: Int?,
            sutokoAppParams: SutokoAppParams
        ): Intent {
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra(Data.Companion.Extra.WEB_URL.id, url)
            intent.putExtra(Data.Companion.Extra.WEB_BACK_BUTTON_TEXT_ID.id, backButtonStringId)
            intent.putExtra(Data.Companion.Extra.APP_PARAMS.id, sutokoAppParams as Parcelable)
            return intent
        }

        /**
         * Method to call before coming to this activity
         *
         * @param activity
         * @return
         */
        fun require(
            activity: Activity,
            url: String,
            sutokoAppParams: SutokoAppParams
        ): Intent {
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra(Data.Companion.Extra.WEB_URL.id, url)
            intent.putExtra(Data.Companion.Extra.APP_PARAMS.id, sutokoAppParams as Parcelable)
            return intent
        }
    }


    private fun onAborted(code: SutokoPlayerPointsManager.Companion.AbortedCodes) {
        when (code) {
            SutokoPlayerPointsManager.Companion.AbortedCodes.NOT_ENOUGH_POINT -> {
                WebActivityGraphics.setBuyingViewLoaderVisibility(this, false)
                WebActivityGraphics.setBuyingViewIconVisibility(this, false)
                WebActivityGraphics.setBuyingText(
                    this,
                    getString(R.string.sutoko_cannot_unlock_this_item)
                )
                Toast.makeText(
                    applicationContext, getString(
                        R.string.sutoko_store_not_enough_points
                    ), Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    private fun onFailureBuy(exception: FirebaseException?) {
        WebActivityGraphics.setBuyingViewLoaderVisibility(this, false)
        WebActivityGraphics.setBuyingViewIconVisibility(this, false)
        WebActivityGraphics.setBuyingText(this, getString(R.string.sutoko_buy_an_error_has_occured))
    }

    private fun onSuccessBuy() {
        WebActivityGraphics.setBuyingViewLoaderVisibility(this, false)
        WebActivityGraphics.setBuyingViewIconVisibility(this, true)
        WebActivityGraphics.setBuyingText(this, getString(R.string.sutoko_congrats_item_unlocked))
    }
}
