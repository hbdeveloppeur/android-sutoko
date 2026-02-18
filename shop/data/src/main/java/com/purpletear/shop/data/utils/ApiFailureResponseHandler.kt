package com.purpletear.shop.data.utils

import com.purpletear.shop.data.exception.ShopApiException
import com.purpletear.shop.data.mapper.ShopApiExceptionMapper
import com.purpletear.sutoko.ai_conversation_data.remote.utils.ApiErrorParser
import okhttp3.ResponseBody

internal object ApiFailureResponseHandler {

    /**
     * Handles the error response from the server
     * @param errorBody the error response body
     * @return Result<ApiException>
     */
    fun handler(errorBody: ResponseBody?): ShopApiException {
        return errorBody?.let { errorString ->
            val code = try {
                ApiErrorParser.parseError(errorString)
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown error : " + e.message
            }
            ShopApiExceptionMapper.mapToException(code)
        } ?: ShopApiException("Response body is null")
    }
}