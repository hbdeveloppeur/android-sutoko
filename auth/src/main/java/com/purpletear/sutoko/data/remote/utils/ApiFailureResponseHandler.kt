package com.purpletear.sutoko.data.remote.utils

import okhttp3.ResponseBody
import org.json.JSONObject

object ApiFailureResponseHandler {

    fun handler(errorBody: ResponseBody?): Exception {
        return errorBody?.let { body ->
            val code = try {
                JSONObject(body.string()).getString("code")
            } catch (e: Exception) {
                null
            }
            Exception(code ?: "Unknown error")
        } ?: Exception("Response body is null")
    }
}
