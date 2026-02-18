package fr.purpletear.sutoko.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.purpletear.sutoko.BuildConfig

class Std {

    companion object {


        fun View.hideKeyboard() = ViewCompat.getWindowInsetsController(this)
            ?.hide(WindowInsetsCompat.Type.ime())

        /**
         * Hides the status and/or nav bar
         * @param window Window
         */
        fun hideBars(window: Window, isStatusBarHidden: Boolean, isNavBarHidden: Boolean) {
            val options: Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                if (isNavBarHidden && isStatusBarHidden) {
                    options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                } else if (isNavBarHidden) {
                    options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                } else {
                    options = View.SYSTEM_UI_FLAG_FULLSCREEN
                }
                val decorView = window.decorView
                decorView.systemUiVisibility = options
            } else {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        }

        fun dpToPx(dip: Float, r: Resources): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.displayMetrics
            ).toInt()
        }

        fun vibrate(view: View) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        /**
         * Displays a message when the debug mode is activated
         * @param message: Any
         */
        fun debug(vararg message: Any) {
            if (!BuildConfig.DEBUG) {
                return
            }
            val tag = "purpletearDebug"
            val text = StringBuilder()
            for (m in message) {
                text.append(m.toString())
                text.append(" ")
            }

            Log.v(tag, text.toString())
        }

        /**
         * Returns the id of the raw resources given the mname of the resource and the context.
         * @param name the mname of the resource (without the extension)
         * @param context the calling activity context
         * @return id of the drawable.
         */
        fun getDrawableFrom(name: String, context: Context): Int? {
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            return if (id == 0) null else id
        }

        fun newInstagramProfileIntent(pm: PackageManager, url: String): Intent? {
            var url = url
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                if (pm.getPackageInfo("com.instagram.android", 0) != null) {
                    if (url.endsWith("/")) {
                        url = url.substring(0, url.length - 1)
                    }
                    val username = url.substring(url.lastIndexOf("/") + 1)

                    intent.data = Uri.parse("http://instagram.com/_u/$username")
                    intent.setPackage("com.instagram.android")
                    return intent
                }
            } catch (ignored: PackageManager.NameNotFoundException) {

            }
            intent.data = Uri.parse(url)
            return intent
        }
    }

}