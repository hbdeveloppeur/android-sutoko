package fr.purpletear.sutoko.screens.web

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.*
import com.bumptech.glide.Glide
import com.example.sharedelements.Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.R
import purpletear.fr.purpleteartools.Language

class WebActivityModel(activity: WebActivity, userNickName: String) {

    val instance = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var user = auth.currentUser
    val requestManager = Glide.with(activity)

    private val mWebViewContainer: ViewGroup? =
        activity.findViewById(R.id.sutoko_web_webview_container)
    private var mWebView: WebView? = activity.findViewById(R.id.sutoko_web_webview)

    fun canGoBack(): Boolean {
        return mWebView?.canGoBack() ?: false
    }

    fun goBack() {
        mWebView?.goBack()
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true
    private var startingUrl = getStartingUrl(activity, userNickName)
    val backButtonSentenceResId: Int =
        activity.intent.getIntExtra(Data.Companion.Extra.WEB_BACK_BUTTON_TEXT_ID.id, -1)

    fun destroyWebView() {
        // Make sure you remove the WebView from its parent view before doing anything.
        mWebViewContainer?.removeAllViews()
        mWebView?.clearHistory()

        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        mWebView?.clearCache(true)

        // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
        mWebView?.loadUrl("about:blank")
        mWebView?.onPause()
        mWebView?.removeAllViews()

        // NOTE: This pauses JavaScript execution for ALL WebViews,
        // do not use if you have other WebViews still alive.
        // If you create another WebView after calling this,
        // make sure to call mWebView.resumeTimers().
        mWebView?.pauseTimers()

        // NOTE: This can occasionally cause a segfault below API 17 (4.2)
        mWebView?.destroy()

        // Null out the reference so that you don't end up re-using it.
        mWebView = null
    }

    private fun getStartingUrl(activity: WebActivity, userNickName: String): String {
        val url = activity.intent.getStringExtra(Data.Companion.Extra.WEB_URL.id)
            ?: "https://sutoko.app"
        return "${url}?versionCode=${BuildConfig.VERSION_CODE}&langCode=${Language.determineLangDirectory()}"
    }

    fun isUserConnected(): Boolean {
        if (auth.currentUser != null) {
            auth.currentUser!!.reload()
            user = auth.currentUser
        }

        return user != null
    }

    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }

    /**
     *
     * @param url
     * @param activity
     */
    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    fun loadUrl(activity: WebActivity, onPageLoaded: () -> Unit, onError: (Int, String) -> Unit) {
        val webView = activity.findViewById<WebView>(R.id.sutoko_web_webview)

        if (Build.VERSION.SDK_INT < 17) {
            webView.webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.loadUrl("javascript:document.getElementsByClassName('cta-button-buy')[0].style.display=\"none\";")
                }
            }
        }
        webView.settings.builtInZoomControls = false
        webView.settings.javaScriptEnabled = true
        webView.isVerticalScrollBarEnabled = true
        webView.clearCache(true)
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.loadUrl(startingUrl)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Handler(Looper.getMainLooper()).post(onPageLoaded)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (error == null) {
                    onError(-1, "{error == null;$startingUrl}")
                    return
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    onError(-1, "{cannot get error code on this api version;$startingUrl}")
                    return
                }

                if (view == null) {
                    onError(error.errorCode, "{view == null;$startingUrl}")
                    return
                }

                Handler(Looper.getMainLooper()).post {
                    onError(error.errorCode, view.url ?: "undefined url")
                }
            }
        }
    }
}