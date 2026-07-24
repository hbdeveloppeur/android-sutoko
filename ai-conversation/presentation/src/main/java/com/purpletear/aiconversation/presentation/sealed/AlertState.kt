package com.purpletear.aiconversation.presentation.sealed

import com.purpletear.aiconversation.presentation.R
import com.purpletear.core.UiText
import androidx.annotation.Keep

sealed class AlertState(
    open val message: UiText,
    open val icon: Int? = null,
    open val button: UiText? = null
) {
    @Keep
    data class CharacterBlockedUser(val characterName: String) : AlertState(
        message = UiText.StringResource(
            R.string.ai_conversation_character_blocked_user,
            characterName,
        ),
        icon = R.drawable.ai_conversation_presentation_ic_alert,
        button = UiText.StringResource(R.string.ai_conversation_restart)
    )

    @Keep
    data object ConnectionError : AlertState(
        message = UiText.StringResource(R.string.ai_conversation_connection_error),
        icon = R.drawable.ai_conversation_presentation_ic_alert,
        button = UiText.StringResource(R.string.ai_conversation_retry)
    )
}