package com.purpletear.ai_conversation.ui.sealed

import com.purpletear.ai_conversation.presentation.R
import com.purpletear.core.UiText

sealed class AlertState(
    open val message: UiText,
    open val icon: Int? = null,
    open val button: UiText? = null
) {
    data class CharacterBlockedUser(val characterName: String) : AlertState(
        message = UiText.StringResource(
            R.string.ai_conversation_character_blocked_user,
            characterName,
        ),
        icon = R.drawable.ic_alert,
        button = UiText.StringResource(R.string.ai_conversation_restart)
    )
}