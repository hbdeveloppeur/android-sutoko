package fr.sutoko.in_app_purchase_domain.enums

enum class AppPurchaseType(val prefix: String) {
    MESSAGE_COINS("ai_message_pack"),
    STORY_COINS("coins_pack_mega"),
    STORY("story"),
    UNKNOWN("unknown");
}