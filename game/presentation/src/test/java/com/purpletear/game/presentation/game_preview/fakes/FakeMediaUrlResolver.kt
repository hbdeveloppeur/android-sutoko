package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.service.MediaUrlResolver

class FakeMediaUrlResolver(private val prefix: String = "https://example.com/") : MediaUrlResolver {
    override fun resolveBannerUrl(storagePath: String?): String? {
        return storagePath?.let { "$prefix$it" }
    }
}
