package com.purpletear.aiconversation.data.parser

import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject

object ApiErrorParser {
    /**
     * Parse error response body to get error code
     * @param response error response body
     * @throws JSONException
     * @return error code
     */
    fun parseError(response: ResponseBody): String {
        val errorJsonString = response.string()
        return try {
            val jsonObject = JSONObject(errorJsonString)
            jsonObject.getString("code")
        } catch (e: JSONException) {
            throw e
        }
    }
}