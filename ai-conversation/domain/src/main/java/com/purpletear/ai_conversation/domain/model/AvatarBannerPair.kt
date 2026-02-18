package com.purpletear.ai_conversation.domain.model

import androidx.annotation.Keep

@Keep
data class AvatarBannerPair(
    val avatar: Media?,
    val banner: Media?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AvatarBannerPair) return false
        return avatar?.url == other.avatar?.url && banner?.url == other.banner?.url
    }

    override fun hashCode(): Int {
        var result = avatar?.url.hashCode()
        result = 31 * result + avatar?.url.hashCode()
        result = 31 * result + banner?.url.hashCode()
        return result
    }
}