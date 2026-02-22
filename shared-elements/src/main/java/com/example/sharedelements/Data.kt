package com.example.sharedelements

import android.content.res.AssetManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.IOException

class Data {
    companion object {

        const val PRIVACY_POLICY_URL = "https://purpletear.net/sutoko/confidentialite"
        const val INSTAGRAM_PROFIL_NAME: String = "https://www.instagram.com/hocinehope/"
        const val FIREBASE_COLLECTION_USTORIES: String = "ustories"
        const val FIREBASE_COLLECTION_STORIES: String = "stories"


        const val FIREBASE_APP_PARAMS_KEY_SHOULD_START_MAIN_HEADER_ANIMATION: String =
            "shouldStartMainHeaderAnimation"
        const val FIREBASE_APP_PARAMS_KEY_SHOULD_START_MAIN_POINTS_ANIMATION: String =
            "shouldStartMainPointsAnimation"
        const val FIREBASE_APP_PARAMS_KEY_HOME_VIDEOS_ENABLED: String =
            "homeVideosEnabled"
        const val FIREBASE_APP_PARAMS_KEY_SHOULD_SLIDE_MAIN_HEADER: String = "shouldSlideMainHeader"
        const val FIREBASE_APP_PARAMS_KEY_INSTAGRAM_URL: String = "instagramUrl"
        const val FIREBASE_APP_PARAMS_KEY_PRIVACY_POLICY_URL: String = "privacyPolicyUrl"
        const val FIREBASE_APP_PARAMS_KEY_REPORT_A_BUG_URL: String = "reportABugUrl"
        const val FIREBASE_APP_PARAMS_KEY_USELESS_SENTENCE_URL: String = "useLessSentencesUrl"
        const val FIREBASE_APP_PARAMS_KEY_TERMS_OF_USE_URL: String = "termOfUseUrl"
        const val FIREBASE_APP_PARAMS_KEY_MY_ORDERS_HEADER_BACKGROUND_URLL: String =
            "myOrdersHeaderbackgroundUrl"
        const val FIREBASE_APP_PARAMS_KEY_SHOP_HEADER_BACKGROUND_URL: String =
            "shopHeaderBackgroundUrl"
        const val FIREBASE_APP_PARAMS_KEY_AI_CONVERSATION_AVAILABILITY: String =
            "aiConversationAvailabilityV3"


        /**
         * Constructs a base URL from the specified suffix by replacing multiple slashes with a single one.
         * @param suffix the suffix to append to the base URL
         * @return the constructed base URL
         */
        fun getBaseUrlFromSuffix(suffix: String): String {
            if (suffix.startsWith("http")) {
                return suffix
            }
            return "https://create.sutoko.app/$suffix".replace(Regex("/{2,}"), "/")
        }


        /**
         * Returns the content of an asset file given its path
         *
         * @param am   AssetManager
         * @param path String
         * @return String content
         * @throws IOException -
         */
        @Throws(IOException::class)
        fun getAssetContent(am: AssetManager, path: String): String {
            val `is` = am.open(path)
            val size = `is`.available()
            val buffer = ByteArray(size)
            val read = `is`.read(buffer)
            if (0 == read) {
                throw IllegalStateException("Error: $path seems empty or null")
            }
            `is`.close()
            return String(buffer)
        }

        enum class FirebaseStoryData(val fieldName: String) {
            CHARACTERS("c"),
        }

        enum class Extra(val id: String) {
            PLAYER_RANK_LIST("PLAYER_RANK_LIST"),
            ITEM("item"),
            WEB_URL("web_url"),
            WEB_BACK_BUTTON_TEXT_ID("WEB_BACK_BUTTON_TEXT_ID"),
            ACCOUNT_PAGE("ACCOUNT_PAGE"),
            APP_PARAMS("APP_PARAMS"),
            CURRENT_POSITION("CURRENT_POSITION"),
            STORY("STORY"),
            USERS_STORIES("USERS_STORIES"),
            CALENDAR_EVENTS("CALENDAR_EVENTS"),
            ARRAY_DRAWABLE_IDS("ARRAY_DRAWABLE_IDS"),
            PHRASES("PHRASES"),
            LINKS("LINKS"),
            CHARACTERS("CHARACTERS"),
            MESSAGES_SIDE("MESSAGES_SIDE"),
            STORY_TYPE("STORY_TYPE"),
            MANGA_FILE_NAME("MANGA_FILE_NAME"),
            STORY_ID("STORY_ID"),
            MANGA_MESSAGES("MANGA_MESSAGES"),
            TABLE_OF_SYMBOLS("TABLE_OF_SYMBOLS"),
            USER_HISTORY_HAS_AT_LEAST_ONE_ORDER("USER_HISTORY_HAS_AT_LEAST_ONE_ORDER"),
            CHAPTERS_ARRAY("CHAPTERS_ARRAY"),
        }

        enum class Activity(name: String) {
            MAIN("main_activity"),
            ACCOUNT("account_activity")
        }

        /**
         * Returns the card video resource's mname
         * @param cardId : Int
         * @return String
         */
        fun getCardVideoResourceName(cardId: Int, isFullVideo: Boolean = false): String {
            return if (isFullVideo) {
                "preview_full_card_$cardId"
            } else {
                "preview_card_$cardId"
            }
        }

        /**
         * Returns the card logo resource's mname
         * @param cardId : Int
         * @return String
         */
        fun getCardLogoResourceName(cardId: Int): String {
            return "logo_card_$cardId"
        }

    }
}


object GraphicsPreference {

    enum class Level {
        DONT_CACHE,
        CACHE
    }

    /**
     * Returns the requested RequestOptions
     *
     * @param level
     * @return
     */
    fun getRequestOptions(level: Level): RequestOptions {
        return when (level) {
            Level.DONT_CACHE -> {
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
            }

            Level.CACHE -> {
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .skipMemoryCache(true)
            }
        }
    }
}