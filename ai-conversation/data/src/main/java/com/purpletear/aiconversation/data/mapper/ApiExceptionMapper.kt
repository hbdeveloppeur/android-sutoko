package com.purpletear.aiconversation.data.mapper

import com.purpletear.aiconversation.data.exception.AccessDeniedException
import com.purpletear.aiconversation.data.exception.ApiException
import com.purpletear.aiconversation.data.exception.BadRequestException
import com.purpletear.aiconversation.data.exception.CharacterNameTooShortException
import com.purpletear.aiconversation.data.exception.ConversationNotStarted
import com.purpletear.aiconversation.data.exception.EntityNotFoundException
import com.purpletear.aiconversation.data.exception.InsufficientFundsException
import com.purpletear.aiconversation.data.exception.InvalidTokenException
import com.purpletear.aiconversation.data.exception.MissingParametersException
import com.purpletear.aiconversation.data.exception.ModelNotFoundException
import com.purpletear.aiconversation.data.exception.ServerErrorException
import com.purpletear.aiconversation.data.exception.UnableToReachServiceException
import com.purpletear.aiconversation.data.exception.UserNameNotFoundException


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