package com.example.sharedelements

import android.content.res.AssetManager
import android.graphics.Point
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.IOException

class Data {
    companion object {
        var mainActivityPremiumSimpleStorySize: Point? = null

        // DEFAULT FALSE
        const val NOTIFICATION_CHANNEL = "hello-sutoko"

        const val PRIVACY_POLICY_URL = "https://purpletear.net/sutoko/confidentialite"
        const val INSTAGRAM_PROFIL_NAME: String = "https://www.instagram.com/hocinehope/"
        const val FIREBASE_STORAGE_URL_PREFIX: String =
            "https://firebasestorage.googleapis.com/v0/b/sutori-cbac9.appspot.com/o/"

        const val FIREBASE_COLLECTION_PRODUCT: String = "store"
        const val FIREBASE_COLLECTION_PRODUCTS: String = "items"
        const val FIREBASE_COLLECTION_SEARCH_ENGINE: String = "search-engine"
        const val FIREBASE_COLLECTION_USTORIES: String = "ustories"
        const val FIREBASE_COLLECTION_APP_PARAMS: String = "params"
        const val FIREBASE_COLLECTION_STORIES: String = "stories"

        const val FIREBASE_DOCUMENT_USTORIES_JSON: String = "json"

        const val FIREBASE_COLLECTION_NEWS_V2: String = "news_v2"
        const val FIREBASE_COLLECTION_ITEMS: String = "items"
        const val FIREBASE_COLLECTION_TROPHIES: String = "trophies"
        const val FIREBASE_COLLECTION_TROPHY: String = "trophy"
        const val FIREBASE_COLLECTION_TICKETS: String = "tickets"

        const val FIREBASE_STORY_KEY_NICE: String = "nice"
        const val FIREBASE_STORY_KEY_CATCHING_PHRASE: String = "cp"
        const val FIREBASE_STORY_KEY_DESCRIPTION: String = "desc"
        const val FIREBASE_STORY_KEY_ID: String = "id"
        const val FIREBASE_STORY_KEY_RANK_GOOD_NUMBER: String = "rgn"
        const val FIREBASE_STORY_KEY_RANK_WRONG_NUMBER: String = "rwn"
        const val FIREBASE_STORY_KEY_MIN_APP_VERSION: String = "mav"
        const val FIREBASE_STORY_KEY_MEDIA1URL: String = "media1"
        const val FIREBASE_STORY_KEY_MEDIA2URL: String = "media2"
        const val FIREBASE_STORY_KEY_MEDIA3URL: String = "media3"
        const val FIREBASE_STORY_KEY_TITLE: String = "title"
        const val FIREBASE_STORY_KEY_SUBTITLE: String = "sbt"
        const val FIREBASE_STORY_KEY_VERSION: String = "version"
        const val FIREBASE_STORY_KEY_IS_ONLINE: String = "isOnline"
        const val FIREBASE_STORY_KEY_IS_VISIBLE: String = "isVis"
        const val FIREBASE_STORY_KEY_IS_DISPLAYED_ON_HOME_SCREEN: String = "isDisplayedOnHomeScreen"
        const val FIREBASE_STORY_KEY_NB_CHAPTERS: String = "nbchapters"
        const val FIREBASE_STORY_KEY_FREE_UNTIL_MIDNIGHT: String = "fum"
        const val FIREBASE_STORY_KEY_DOWNLOADS: String = "dwn"
        const val FIREBASE_STORY_KEY_ADULT: String = "a"
        const val FIREBASE_STORY_KEY_SKU: String = "sku"
        const val FIREBASE_STORY_KEY_MENU_SOUND: String = "ms"
        const val FIREBASE_STORY_KEY_RELEASE_DATE: String = "releaseDate"
        const val FIREBASE_STORY_KEY_CATEGORY: String = "category"
        const val FIREBASE_STORY_KEY_KEYWORDS: String = "kw"
        const val FIREBASE_STORY_KEY_SCREENSHOT: String = "previews"
        const val FIREBASE_STORY_KEY_PRICE_COINS: String = "coins"
        const val FIREBASE_STORY_KEY_TAG: String = "tag"
        const val FIREBASE_STORY_KEY_VIDEO_URL: String = "videoUrl"
        const val FIREBASE_STORY_KEY_IS_PREMIUM: String = "isPre"
        const val FIREBASE_STORY_KEY_HAS_HIDDEN_COINS: String = "hasCoins"
        const val FIREBASE_STORY_KEY_MIN_APP_CODE: String = "mac"
        const val FIREBASE_STORY_KEY_MARK: String = "m"
        const val FIREBASE_STORY_KEY_SHOULD_REQUEST_NAME: String = "srn"
        const val FIREBASE_STORY_KEY_PREMIUM_VIDEO: String = "pv"
        const val FIREBASE_STORY_KEY_CHARACTER_DEFAULT_NAME: String = "cdn"

        const val FIREBASE_NEWS_KEY_ID: String = "id"
        const val FIREBASE_NEWS_KEY_DESCRIPTION: String = "description"
        const val FIREBASE_NEWS_KEY_TAG: String = "tag"
        const val FIREBASE_NEWS_KEY_URL: String = "url"
        const val FIREBASE_NEWS_KEY_BACKGROUND_URL: String = "backgroundUrl"
        const val FIREBASE_NEWS_KEY_TITLE: String = "title"
        const val FIREBASE_NEWS_KEY_BACKGROUND_TYPE: String = "backgroundType"
        const val FIREBASE_NEWS_KEY_IMAGE_PLACEHOLDER: String = "imagePlaceholder"
        const val FIREBASE_NEWS_KEY_OS: String = "os"
        const val FIREBASE_NEWS_KEY_IS_ONLINE: String = "isOnline"
        const val FIREBASE_NEWS_KEY_NICE: String = "nice"

        const val FIREBASE_TROPHY_KEY_ID: String = "id"
        const val FIREBASE_TROPHY_KEY_TITLE: String = "title"
        const val FIREBASE_TROPHY_KEY_SHORT_DESCRIPTION: String = "sd"
        const val FIREBASE_TROPHY_KEY_DESCRIPTION: String = "d"
        const val FIREBASE_TROPHY_KEY_STORY_ID: String = "story"

        const val FIREBASE_PRODUCT_MIN_APP_CODE: String = "mac"
        const val FIREBASE_PRODUCT_KEY_NICE: String = "nice"


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
            LINKS("l"),
            PHRASES("p"),
            CHARACTERS("c"),
            MESSAGES_SIDE("s"),
        }

        enum class Extra(val id: String) {
            HAS_PREMIUM("HAS_PREMIUM"),
            SHOP_VALUES("SHOP_VALUES"),
            PLAYER_RANK_LIST("PLAYER_RANK_LIST"),
            COLLECTED_TROPHIES_LIST("collected_trophies_list"),
            MAIN_ACTIVITY_ARRAY("main_activity_array"),
            MAIN_ACTIVITY_CURRENT_PAGE("MAIN_ACTIVITY_CURRENT_PAGE"),
            MAIN_ACTIVITY_CATEGORY_STATE("MAIN_ACTIVITY_CATEGORY_STATE"),
            IS_FROM_ACTIVITY("is_from_activity"),
            TROPHIES_LIST("trophies_list"),
            CODE("code"),
            ITEM("item"),
            ITEM_TYPE("item_type"),
            ITEM_LOADER_DESTINATION("item_loader_destination"),
            WEB_URL("web_url"),
            WEB_BACK_BUTTON_TEXT_ID("WEB_BACK_BUTTON_TEXT_ID"),
            CARD_ORDER_LIST("CARD_ORDER_LIST"),
            ACCOUNT_PAGE("ACCOUNT_PAGE"),
            APP_PARAMS("APP_PARAMS"),
            SUTOKO_PARAMS("SUTOKO_PARAMS"),
            CURRENT_POSITION("CURRENT_POSITION"),
            CREATE_STORY_TITLE("CREATE_STORY_TITLE"),
            CREATE_STORY("CREATE_STORY"),
            TABLE_OF_CREATOR_RESOURCES("TABLE_OF_CREATOR_RESOURCES"),
            STORY("STORY"),
            CREATE_STORY_ID("CREATE_STORY_ID"),
            USER_STORIES("USER_STORIES"),
            USERS_STORIES("USERS_STORIES"),
            BOOKS("BOOKS"),
            LOCAL_DATA_USER("LOCALDATAUSER"),
            GAMES_CATEGORIES("GAMES_CATEGORIES"),
            COMMUNITY_CATEGORIES("COMMUNITY_CATEGORIES"),
            CALENDAR_EVENTS("CALENDAR_EVENTS"),
            LAST_DOCUMENT_USER_STORIES("LAST_DOCUMENT_USER_STORIES"),
            ARRAY_DRAWABLE_IDS("ARRAY_DRAWABLE_IDS"),
            FIREBASE_STORY_DOCUMENT_NAME("FIREBASE_STORY_DOCUMENT_NAME"),
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
            SHOW_CARDS_VIDEOS("SHOW_CARDS_VIDEOS"),
            ESCAPE_GAME_ID("ESCAPE_GAME_ID"),
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