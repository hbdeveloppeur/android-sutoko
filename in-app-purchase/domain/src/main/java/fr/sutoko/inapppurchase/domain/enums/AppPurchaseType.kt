package fr.sutoko.inapppurchase.domain.enums

enum class AppPurchaseType(val prefix: String) {
    MESSAGE_COINS("ai_message_pack"),
    STORY_COINS("coins_pack_mega"),
    STORY("story"),
    UNKNOWN("unknown");
}