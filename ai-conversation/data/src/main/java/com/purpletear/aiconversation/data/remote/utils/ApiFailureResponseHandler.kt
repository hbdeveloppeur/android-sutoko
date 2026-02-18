package com.purpletear.aiconversation.data.remote.utils

import com.purpletear.aiconversation.data.exception.ApiException
import com.purpletear.aiconversation.data.mapper.ApiExceptionMapper
import com.purpletear.aiconversation.data.parser.ApiErrorParser
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