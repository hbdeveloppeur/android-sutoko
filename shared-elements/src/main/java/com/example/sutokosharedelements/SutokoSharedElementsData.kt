@file:Suppress("SpellCheckingInspection")

package com.example.sharedelements

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.StrictMode
import android.util.Size
import android.view.Display
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.util.*


object SutokoSharedElementsData {
    var lastSeenAdsTime: Long? = null
    var lastSeenDiamondAdsTime: Long? = null
    val isDebugMode: Boolean = false
        get() {
            return field && BuildConfig.DEBUG
        }


    val STRICT_MODE = false
        get() {
            return field && BuildConfig.DEBUG
        }

    // -1 = disabled
    // 0 = no
    // 1 = true
    val debugHasPremiumCode: Int = -1
        get() {
            if (!BuildConfig.DEBUG) {
                return -1
            }
            return field
        }
    val debugHasPaidStoriesCode: Int = -1
        get() {
            if (!BuildConfig.DEBUG) {
                return -1
            }
            return field
        }

    // GAMES : FRIENDZONED 1 / 2 / 3 / 4
    // (DEFAULT : 1a)
    val FORCE_CHAPTER_CODE: String = "1a"
        get() {
            if (!BuildConfig.DEBUG) {
                return "1a"
            }
            return field
        }

    // (DEFAULT : FALSE)
    const val IS_FAST_CHAPTER: Boolean = false

    // (DEFAULT : TRUE)
    const val IS_POETRY_ENABLED: Boolean = true

    // (DEFAULT : TRUE)
    const val IS_TEXTCINEMATIC_ENABLED: Boolean = true

    // (DEFAULT : 0)
    val STARTING_PHRASE_ID: Int = 0
        get() {
            if (!BuildConfig.DEBUG) {
                return 0
            }
            return field
        }

    val SHOULD_FORCE_CHAPTER: Boolean = FORCE_CHAPTER_CODE != "1a" && BuildConfig.DEBUG

    // (DEFAULT : fr_purpletear_sutoko_iap_removeads)
    const val REMOVE_ADS_SKU: String = "fr_purpletear_sutoko_iap_removeads"
    // END GAMES

    private const val SUTOKO_DATA_URL: String = "https://www.data.sutoko.app/"

    const val FIREBASE_TROPHY_KEY_ID: String = "id"
    const val FIREBASE_TROPHY_KEY_TITLE: String = "title"
    const val FIREBASE_TROPHY_KEY_SHORT_DESCRIPTION: String = "sd"
    const val FIREBASE_TROPHY_KEY_DESCRIPTION: String = "d"
    const val FIREBASE_TROPHY_KEY_STORY_ID: String = "story"


    const val FILE_NAME_TROPHIES = "trophies.json"
    private const val DIR_NAME_TROPHIES = ""


    fun getCollectedTrophiesDirPath(): String {
        return DIR_NAME_TROPHIES + File.separator
    }

    var screenSize: Point? = null


    fun loadScreenSize(activity: Activity) {
        val display: Display = getDefaultDisplay(activity)
        var size = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val s = saveSize(activity)
            size = Point(s.width, s.height)
        } else {
            display.getSize(size)
        }
        screenSize = size
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveSize(activity: Activity): Size {
        val metrics: WindowMetrics = activity.windowManager.currentWindowMetrics
        // Gets all excluding insets
        // Gets all excluding insets
        val windowInsets: WindowInsets = metrics.windowInsets
        val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout()
        )

        val insetsWidth: Int = insets.right + insets.left
        val insetsHeight: Int = insets.top + insets.bottom

        // Legacy size that Display#getSize reports

        // Legacy size that Display#getSize reports
        val bounds: Rect = metrics.getBounds()
        return Size(
            bounds.width() - insetsWidth,
            bounds.height()
        )
    }


    @SuppressLint("InternalInsetResource")
    fun getBottomNavigationHeight(activity: Activity): Int {
        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return when {
            Build.VERSION.SDK_INT >= 30 -> {
                windowManager.currentWindowMetrics
                    .windowInsets
                    .getInsets(WindowInsets.Type.navigationBars())
                    .bottom
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.display
                } else {
                    windowManager.defaultDisplay
                }
                val appUsableSize = Point()
                val realScreenSize = Point()
                currentDisplay?.apply {
                    getSize(appUsableSize)
                    getRealSize(realScreenSize)
                }
                // navigation bar on the side
                if (appUsableSize.x < realScreenSize.x) {
                    realScreenSize.x - appUsableSize.x
                }
                // navigation bar at the bottom
                else if (appUsableSize.y < realScreenSize.y) {
                    realScreenSize.y - appUsableSize.y
                } else {
                    0
                }
            }

            else -> {
                activity.resources.getDimensionPixelSize(
                    activity.resources.getIdentifier(
                        "navigation_bar_height",
                        "dimen",
                        "android"
                    )
                )
            }
        }
    }

    enum class Type(val str: String) {
        TROPHIES("news"),
        NEWS("trophies"),
        CALENDAR("calendar"),
        CREATOR_RESOURCES("CREATOR_RESOURCES"),
        PREMIUM_CARDS("PREMIUM_CARDS"),
        CONDITIONS_PUBLISH("CONDITIONS_PUBLISH"),
        BADWORDS_LIST("BADWORDS_URL")
    }


    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else Math.ceil((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 24 else 25) * resources.displayMetrics.density.toDouble())
            .toInt()
    }

    fun setStrictMode() {
        if (BuildConfig.DEBUG && SutokoSharedElementsData.STRICT_MODE) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog()

                    .penaltyDropBox()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )

        }
    }

    fun getDefaultDisplay(activity: Activity): Display {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return activity.display!!
        } else {
            @Suppress("DEPRECATION")
            return activity.windowManager.defaultDisplay
        }
    }

    /**
     * Returns the formatted data url
     *
     * @param type
     * @param langCode
     * @return
     */
    fun getSutokoDataUrl(type: Type, langCode: String): String {
        if (type == Type.CONDITIONS_PUBLISH) {
            return "https://sutoko.app/terms-of-use"
        }
        return SUTOKO_DATA_URL + when (type) {
            Type.NEWS -> "news/${langCode}"
            Type.CALENDAR -> "calendar/${langCode}"
            Type.TROPHIES -> "trophies/${langCode}"
            Type.BADWORDS_LIST -> "banned-words/${langCode}"
            Type.CREATOR_RESOURCES -> "creator-resources/${langCode}"
            Type.PREMIUM_CARDS -> "premium-presentation/${langCode}"
            else -> {
                ""
            }
        }
    }

    fun getSutokoErrorReportUrl(): String {
        return "https://create.sutoko.app/api/sutoko-mobile-app/errors/insert"
    }

    fun getSutokoNotifyUrl(): String {
        return "https://create.sutoko.app/api/notify"
    }

    fun getFlagStoryUrl(firebaseId: String): String {
        return "https://create.sutoko.app/api/report-story/$firebaseId"
    }

}

internal object PrivateFirebaseTreeStructure {
    private const val FIREBASE_COLLECTION_USERS = "users"
    private const val FIREBASE_COLLECTION_POINTS = "points"

    internal fun getPathToPoints(firebaseUser: FirebaseUser): String {
        return "${this.FIREBASE_COLLECTION_USERS}/${firebaseUser.uid}/${FIREBASE_COLLECTION_POINTS}"
    }
}