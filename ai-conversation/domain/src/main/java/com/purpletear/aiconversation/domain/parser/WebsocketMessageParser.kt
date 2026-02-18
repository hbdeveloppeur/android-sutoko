package com.purpletear.aiconversation.domain.parser

import com.purpletear.aiconversation.domain.enums.CharacterStatus
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup

interface WebsocketMessageParser {
    /**
     * @throws WebsocketMessageParserException
     */
    fun parseMessage(rawMessage: String): Message
    fun parseMessagesAck(rawMessage: String): List<String>
    fun parseError(rawMessage: String): Exception
    fun parseCharacters(rawMessage: String): List<AiCharacter>
    fun parseStoryChoice(rawMessage: String): MessageStoryChoiceGroup
    fun parseImageMessage(rawMessage: String): Message
    fun parseConversationMode(rawMessage: String): ConversationMode
    fun parseCharacterStatus(rawMessage: String): CharacterStatus
    fun parseBackgroundImageUpdateUrl(rawMessage: String): String
}