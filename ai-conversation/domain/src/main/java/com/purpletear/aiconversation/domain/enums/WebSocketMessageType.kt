package com.purpletear.aiconversation.domain.enums

enum class WebSocketMessageType(val code: String) {
    AUTHENTICATION_SUCCESS("connected"),
    AUTHENTICATION_FAILURE("authenticate_failure"),
    PING("ping"),
    SEEN("seen"),
    TYPING("typing"),
    CONVERSATION_MODE_UPDATE("conversation_mode_update"),
    STOP_TYPING("stop_typing"),
    BLOCK("block_conversation"),
    CHAT_MESSAGE_IMAGE("new_message_image"),
    CHAT_MESSAGE("new_message"),
    ERROR_CODE("error_code"),
    MESSAGE_ACK("message_ack"),
    INVITE_CHARACTERS("invite_characters"),
    NEW_CHARACTER_STATUS("new_character_status"),
    NEW_BACKGROUND_IMAGE("new_background_url"),
    STORY_CHOICE("new_story_choice"),
    CHAT_NARRATION("new_narration"),
    ERROR("error")
}