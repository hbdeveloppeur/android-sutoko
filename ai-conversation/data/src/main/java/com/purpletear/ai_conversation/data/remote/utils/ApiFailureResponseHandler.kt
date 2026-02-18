package com.purpletear.ai_conversation.data.remote.utils

import com.purpletear.ai_conversation.data.exception.ApiException
import com.purpletear.ai_conversation.data.mapper.ApiExceptionMapper
import com.purpletear.ai_conversation.data.parser.ApiErrorParser
import okhttp3.ResponseBody

object ApiFailureResponseHandler {

    /**
     * Handles the error response from the server
     * @param errorBody the error response body
     * @return Result<ApiException>
     */
    fun handler(errorBody: ResponseBody?): ApiException {
        return errorBody?.let { errorString ->
            val code = try {
                ApiErrorParser.parseError(errorString)
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown error"
            }
            ApiExceptionMapper.mapToException(code)
        } ?: ApiException("Response body is null")
    }
}