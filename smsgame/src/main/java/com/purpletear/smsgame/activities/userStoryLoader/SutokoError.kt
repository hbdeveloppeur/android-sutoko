package com.purpletear.smsgame.activities.userStoryLoader

import com.google.firebase.firestore.FirebaseFirestoreException
import com.purpletear.smsgame.R

enum class SutokoError(val code: Int, val messageResId: Int) {
    USER_STORY_NOT_FOUND(1, R.string.sutoko_user_story_not_found),
    UNKNOWN_ERROR(2, R.string.sutoko_error_unknown),
    USER_NOT_CONNECTED(3, R.string.sutoko_error_you_must_be_connected),
    STORY_NOT_FOUND(4, R.string.sutoko_error_story_not_found),
    CHECK_CONNECTION(5, R.string.sutoko_error_check_connection),
    PERMISSION_DENIED(6, R.string.sutoko_error_permission_denied),
    NONE(-1, R.string.sutoko_error_check_connection),
}

object SutokoErrorHandler {

    /**
     *
     * @param exception FirebaseException
     * @return SutokoError
     */
    fun firebaseExceptionToSutokoError(exception: FirebaseFirestoreException): SutokoError {
        return when (exception.code) {
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                SutokoError.CHECK_CONNECTION
            }
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                SutokoError.PERMISSION_DENIED
            }
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                SutokoError.USER_NOT_CONNECTED
            }
            FirebaseFirestoreException.Code.UNKNOWN -> {
                SutokoError.UNKNOWN_ERROR
            }
            else -> {
                SutokoError.UNKNOWN_ERROR
            }
        }

    }
}