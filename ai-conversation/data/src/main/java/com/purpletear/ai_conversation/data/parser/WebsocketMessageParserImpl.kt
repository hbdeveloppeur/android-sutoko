package com.purpletear.ai_conversation.data.parser

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.purpletear.ai_conversation.data.remote.deserializer.CharacterStatusDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.ConversationModeDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.ErrorCodeDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.MessageStoryChoiceGroupDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.MessagesAckDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.WebsocketCharactersDeserializer
import com.purpletear.ai_conversation.data.remote.deserializer.message.MessageDeserializer
import com.purpletear.ai_conversation.data.remote.dto.websocket.AiConversationChatBackgroundImageUpdateDto
import com.purpletear.ai_conversation.data.remote.dto.websocket.AiConversationChatImageMessageDto
import com.purpletear.ai_conversation.data.remote.dto.websocket.toDomain
import com.purpletear.ai_conversation.domain.enums.CharacterStatus
import com.purpletear.ai_conversation.domain.enums.ConversationMode
import com.purpletear.ai_conversation.domain.exception.WebsocketMessageParserException
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.ai_conversation.domain.parser.WebsocketMessageParser

class WebsocketMessageParserImpl(private val gson: Gson) : WebsocketMessageParser {
    /**
     * @throws WebsocketMessageParserException
     */
    override fun parseMessage(rawMessage: String): Message {
        try {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(Message::class.java, MessageDeserializer())
            val customGson = gsonBuilder.create()
            return customGson
                .fromJson(rawMessage, Message::class.java)

        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseError(rawMessage: String): Exception {
        try {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(Exception::class.java, ErrorCodeDeserializer())
            val customGson = gsonBuilder.create()
            return customGson
                .fromJson(rawMessage, Exception::class.java)
        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseMessagesAck(rawMessage: String): List<String> {
        try {
            val gsonBuilder = GsonBuilder()
            val serialIdsType = object : TypeToken<List<String>>() {}.type
            gsonBuilder.registerTypeAdapter(serialIdsType, MessagesAckDeserializer())
            val customGson = gsonBuilder.create()
            return customGson.fromJson(rawMessage, serialIdsType)
        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseCharacters(rawMessage: String): List<AiCharacter> {
        try {
            val gsonBuilder = GsonBuilder()
            val characterListType = object : TypeToken<List<AiCharacter>>() {}.type
            gsonBuilder.registerTypeAdapter(characterListType, WebsocketCharactersDeserializer())
            val customGson = gsonBuilder.create()
            return customGson
                .fromJson(rawMessage, characterListType)

        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseStoryChoice(rawMessage: String): MessageStoryChoiceGroup {
        try {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(
                MessageStoryChoiceGroup::class.java,
                MessageStoryChoiceGroupDeserializer()
            )
            val customGson = gsonBuilder.create()
            return customGson
                .fromJson(rawMessage, MessageStoryChoiceGroup::class.java)

        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseCharacterStatus(rawMessage: String): CharacterStatus {
        try {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(
                CharacterStatus::class.java,
                CharacterStatusDeserializer()
            )
            val customGson = gsonBuilder.create()
            return customGson
                .fromJson(rawMessage, CharacterStatus::class.java)

        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseConversationMode(rawMessage: String): ConversationMode {
        try {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(
                ConversationMode::class.java,
                ConversationModeDeserializer()
            )
            val customGson = gsonBuilder.create()
            return customGson
                .fromJson(rawMessage, ConversationMode::class.java)

        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    /**
     * @throws WebsocketMessageParserException
     */
    override fun parseImageMessage(rawMessage: String): Message {
        return try {
            val dto = gson.fromJson(rawMessage, AiConversationChatImageMessageDto::class.java)
            dto.toDomain()
        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }

    override fun parseBackgroundImageUpdateUrl(rawMessage: String): String {
        return try {
            val dto =
                gson.fromJson(rawMessage, AiConversationChatBackgroundImageUpdateDto::class.java)
            dto.url
        } catch (e: JsonSyntaxException) {
            throw WebsocketMessageParserException(e.message)
        }
    }
}