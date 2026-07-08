package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.core.domain.helper.AppVersionProvider

class FakeAppVersionProvider(private val versionCode: Int = 100) : AppVersionProvider {
    override fun getVersionCode(): Int = versionCode
}
