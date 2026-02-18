package com.purpletear.aiconversation.presentation.common.utils

import com.purpletear.core.remote.Server

fun getRemoteAssetsUrl(url: String): String {
    if (url.contains("http") || url.contains(".com")) return url
    return "${Server.urlPrefix()}/$url".replace("//", "/")
}