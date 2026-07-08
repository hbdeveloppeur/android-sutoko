package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.core.presentation.services.ToastService

class FakeToastService : ToastService {
    val shownMessages = mutableListOf<Int>()

    override fun invoke(message: Int, vararg formatArgs: Any?) {
        shownMessages.add(message)
    }
}
