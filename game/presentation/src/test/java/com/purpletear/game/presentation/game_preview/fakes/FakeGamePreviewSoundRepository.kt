package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.repository.GamePreviewSoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGamePreviewSoundRepository : GamePreviewSoundRepository {
    private val muted = MutableStateFlow(false)

    override fun observeMuted(): Flow<Boolean> = muted

    override suspend fun setMuted(muted: Boolean) {
        this.muted.value = muted
    }
}
