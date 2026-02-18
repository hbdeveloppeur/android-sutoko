package com.purpletear.game_data.remote.utils

import retrofit2.Response

/**
 * Utility class for handling API failure responses.
 */
object ApiFailureResponseHandler {
    /**
     * Handle a failed API response.
     *
     * @param response The failed API response.
     * @return A Result.failure with an appropriate exception.
     */
    fun <T> handleFailure(response: Response<T>): Result<T> {
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        val exception = Exception("API call failed with code ${response.code()}: $errorBody")
        return Result.failure(exception)
    }
}