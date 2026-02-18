package com.purpletear.ai_conversation.data.mapper

import com.purpletear.ai_conversation.data.exception.AccessDeniedException
import com.purpletear.ai_conversation.data.exception.ApiException
import com.purpletear.ai_conversation.data.exception.BadRequestException
import com.purpletear.ai_conversation.data.exception.CharacterNameTooShortException
import com.purpletear.ai_conversation.data.exception.ConversationNotStarted
import com.purpletear.ai_conversation.data.exception.EntityNotFoundException
import com.purpletear.ai_conversation.data.exception.InsufficientFundsException
import com.purpletear.ai_conversation.data.exception.InvalidTokenException
import com.purpletear.ai_conversation.data.exception.MissingParametersException
import com.purpletear.ai_conversation.data.exception.ModelNotFoundException
import com.purpletear.ai_conversation.data.exception.ServerErrorException
import com.purpletear.ai_conversation.data.exception.UnableToReachServiceException
import com.purpletear.ai_conversation.data.exception.UserNameNotFoundException


object ApiExceptionMapper {

    /**
     * Maps API error code to exception
     * @param errorCode API error code
     * @param errorMessage API error message
     * @return ApiException
     */
    fun mapToException(errorCode: String, errorMessage: String? = null): ApiException {
        return when (errorCode) {
            "character.name_too_short" -> CharacterNameTooShortException(
                errorMessage ?: "Missing character name too short"
            )

            "conversation_not_started" -> ConversationNotStarted(
                errorMessage ?: "Conversation is empty"
            )

            "UserNameNotFoundException", "username_required" -> UserNameNotFoundException(
                errorMessage ?: "User name not found"
            )
 
            "missing_parameters" -> MissingParametersException(errorMessage ?: "Missing parameters")
            "unable_to_reach_service" -> UnableToReachServiceException(
                errorMessage ?: "Unable to reach service"
            )

            "access_denied" -> AccessDeniedException(errorMessage ?: "Access denied")

            "entity_not_found" -> EntityNotFoundException(errorMessage ?: "Entity not found")

            "bad_request" -> BadRequestException(errorMessage ?: "Bad request")
            "invalid_token" -> InvalidTokenException(errorMessage ?: "Invalid token")
            "generation_model_not_found" -> ModelNotFoundException(
                errorMessage ?: "Generation model not found"
            )

            "server_error" -> ServerErrorException(errorMessage ?: "Server error")
            "insufficient_funds" -> InsufficientFundsException(errorMessage ?: "Insufficient funds")

            else -> ApiException(errorMessage ?: "Unknown error $errorCode")
        }
    }
}