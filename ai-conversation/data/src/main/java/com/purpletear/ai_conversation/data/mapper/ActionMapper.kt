package com.purpletear.ai_conversation.data.mapper

import com.purpletear.ai_conversation.data.enum.Action

object ActionMapper {

    fun map(action: String) = when (action) {
        Action.GenerationSuccess.code -> Action.GenerationSuccess
        Action.GenerationFailure.code -> Action.GenerationFailure
        else -> null
    }
}