package fr.purpletear.sutoko.helpers

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.sutokosharedelements.R
import com.example.sutokosharedelements.User
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import fr.purpletear.sutoko.screens.accountConnection.AccountFormHelper
import fr.purpletear.sutoko.screens.accountConnection.objects.AccountConnectionRequestResponse
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import purpletear.fr.purpleteartools.Language
import purpletear.fr.purpleteartools.Std
import java.io.IOException

object UserHelper {

    fun login(
        activity: Activity,
        mail: String,
        password: String,
        onComplete: (isSuccessful: Boolean, resultCode: AccountFormHelper.FormValidationCode) -> Unit
    ) {
        val parameters = listOf(
            "email" to mail,
            "password" to password
        )

        request(activity, "https://create.sutoko.app/api/login", parameters) { response ->
            if (response.token != null) {
                val user = User()
                user.saveToken(activity, mail, response.token.toString(), response.uid.toString())
            }
            val isSuccess = response.result
            val resultCode = response.error
            if (isSuccess == true) {
                onComplete(true, AccountFormHelper.FormValidationCode.SUCCESS)
            } else {
                val formValidationCode = when (resultCode) {
                    "WRONG_CREDENTIALS" -> AccountFormHelper.FormValidationCode.INVALID_CREDENTIALS
                    "USER_NOT_VALIDATED" -> AccountFormHelper.FormValidationCode.MAIL_NOT_VALIDATED
                    "REQUIRE_AUTH_CHECK" -> AccountFormHelper.FormValidationCode.REQUIRE_FIREBASE_AUTH_CHECK
                    else -> AccountFormHelper.FormValidationCode.UNKNOWN_ERROR
                }
                onComplete(false, formValidationCode)
            }
        }
    }

    fun register(
        activity: Activity,
        username: String,
        mail: String,
        password: String,
        onComplete: (isSuccessful: Boolean, resultCode: AccountFormHelper.FormValidationCode) -> Unit
    ) {
        val parameters = listOf(
            "email" to mail,
            "username" to username,
            "password" to password,
            "language" to Language.determineLangDirectory()
        )
        request(activity, "https://create.sutoko.app/api/register", parameters) { response ->
            val isSuccess = response.result
            val resultCode = response.reason
            //isSuccess, resultCode, _ ->
            if (isSuccess == true) {
                onComplete(true, AccountFormHelper.FormValidationCode.SUCCESS)
            } else {
                val formValidationCode = when (resultCode) {
                    "USERNAME_TOO_SHORT" -> AccountFormHelper.FormValidationCode.NICKNAME_LENGTH_TOO_SHORT
                    "USERNAME_TOO_LONG" -> AccountFormHelper.FormValidationCode.NICKNAME_LENGTH_TOO_LONG
                    "USER_REGEX_ERROR" -> AccountFormHelper.FormValidationCode.NICKNAME_REGEX_ERROR
                    "MAIL_FORMAT_ERROR" -> AccountFormHelper.FormValidationCode.UNKNOWN_ERROR
                    "PASSWORD_TOO_SHORT" -> AccountFormHelper.FormValidationCode.PASSWORD_WEAK
                    "EMAIL_INVALID_DOMAIN" -> AccountFormHelper.FormValidationCode.MAIL_ADDRESS_DOMAIN_FORBIDDEN
                    "MAIL_EXISTS" -> AccountFormHelper.FormValidationCode.MAIL_ADDRESS_NOT_AVAILABLE
                    "USERNAME_EXISTS" -> AccountFormHelper.FormValidationCode.NICKNAME_NOT_AVAILABLE
                    else -> AccountFormHelper.FormValidationCode.UNKNOWN_ERROR
                }
                onComplete(false, formValidationCode)
            }
        }
    }

    fun sendForgotPasswordMail(
        activity: Activity,
        mail: String,
        onComplete: (isSuccessful: Boolean, error: String?) -> Unit
    ): CancellableRequest? {
        val parameters = listOf(
            "email" to mail,
            "language" to Language.determineLangDirectory()
        )
        return request(activity, "https://create.sutoko.app/api/forgot", parameters) { response ->
            val isSuccessful = response.res == true
            val error = response.error ?: ""
            onComplete(isSuccessful, error.toString())
        }
    }

    fun convertAccount(
        activity: Activity,
        mail: String,
        password: String,
        isValidatedUser: Boolean,
        onComplete: (isSuccessful: Boolean, error: String?, token: String?, uid: String?) -> Unit
    ): CancellableRequest? {
        val parameters = listOf(
            "email" to mail,
            "password" to password,
            "isMobile" to true,
            "language" to Language.determineLangDirectory(),
            "isValidatedUser" to isValidatedUser,
        )
        return request(
            activity,
            "https://create.sutoko.app/api/firebase-account/convert",
            parameters
        ) { response ->
            val isSuccessful = response.result == true
            val error = response.error ?: ""
            onComplete(
                isSuccessful,
                error.toString(),
                response.token?.toString(),
                response.uid?.toString()
            )
        }
    }

    fun isMailValidated(
        activity: Activity,
        mail: String,
        onComplete: (isValidated: Boolean?) -> Unit
    ): CancellableRequest? {
        val parameters = listOf(
            "email" to mail
        )
        return request(
            activity,
            "https://create.sutoko.app/api/is-validated-account",
            parameters
        ) { response ->
            onComplete(response.result == true)
        }
    }

    private fun request(
        activity: Activity,
        url: String,
        parameters: List<Pair<String, Comparable<*>?>>,
        onComplete: (response: AccountConnectionRequestResponse) -> Unit
    ): CancellableRequest? {
        val body = getFormattedBodyRequest(parameters)
        assert(Looper.getMainLooper().thread == Thread.currentThread())
        try {
            url.httpPost(parameters).useHttpCache(false)
                .body(body)
                .header(
                    mapOf(
                        "content-type" to "application/json"
                    )
                )

                .responseString { request, response, result ->
                    Handler(Looper.getMainLooper()).post {

                        if (activity.isFinishing) {
                            return@post
                        }
                        val json: String
                        try {
                            json = result.get()
                        } catch (e: Exception) {
                            AppTesterHelper.sendReport(
                                activity, e.toString(), url
                            )
                            val accountConnectionRequestResponse =
                                AccountConnectionRequestResponse()
                            accountConnectionRequestResponse.result = "DEV_ERROR"
                            onComplete(accountConnectionRequestResponse)
                            return@post
                        } catch (e: IOException) {
                            val accountConnectionRequestResponse =
                                AccountConnectionRequestResponse()
                            accountConnectionRequestResponse.result = "DEV_ERROR"
                            onComplete(accountConnectionRequestResponse)
                            return@post
                        } catch (e: FuelError) {
                            val accountConnectionRequestResponse =
                                AccountConnectionRequestResponse()
                            accountConnectionRequestResponse.result = "DEV_ERROR"
                            onComplete(accountConnectionRequestResponse)
                            return@post
                        }
                        if (!isJSONValid(json)) {
                            val accountConnectionRequestResponse =
                                AccountConnectionRequestResponse()
                            accountConnectionRequestResponse.result = "MALFORMED_STRING"
                            onComplete(accountConnectionRequestResponse)
                            return@post
                        }
                        when (result) {
                            is Result.Success -> {
                                val o = Gson().fromJson(
                                    json,
                                    AccountConnectionRequestResponse::class.java
                                ) as AccountConnectionRequestResponse
                                onComplete(o)
                            }

                            is Result.Failure -> {
                                val accountConnectionRequestResponse =
                                    AccountConnectionRequestResponse()
                                accountConnectionRequestResponse.result = "REQUEST_ERROR"
                                onComplete(accountConnectionRequestResponse)
                            }
                        }
                    }
                }
        } catch (e: Throwable) {
            val accountConnectionRequestResponse = AccountConnectionRequestResponse()
            accountConnectionRequestResponse.result = "DEV_ERROR"
            onComplete(accountConnectionRequestResponse)
        } catch (e: IOException) {
            val accountConnectionRequestResponse = AccountConnectionRequestResponse()
            accountConnectionRequestResponse.result = "DEV_ERROR"
            onComplete(accountConnectionRequestResponse)
        } catch (e: Exception) {
            val accountConnectionRequestResponse = AccountConnectionRequestResponse()
            accountConnectionRequestResponse.result = "DEV_ERROR"
            onComplete(accountConnectionRequestResponse)
        } catch (e: FuelError) {
            val accountConnectionRequestResponse = AccountConnectionRequestResponse()
            accountConnectionRequestResponse.result = "DEV_ERROR"
            onComplete(accountConnectionRequestResponse)
        }
        return null
    }

    /**
     * Determines if the String is a valid json
     *
     * @param test
     * @return
     */
    private fun isJSONValid(test: String?): Boolean {
        if (null == test) {
            return false
        }

        try {
            JSONObject(test)
        } catch (ex: JSONException) {
            try {
                JSONArray(test)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

    private fun getFormattedBodyRequest(parameters: List<Pair<String, Comparable<*>?>>): String {
        var body = "{"
        parameters.forEachIndexed { index, it ->
            if (it.second == null) {
                body += "\"${it.first}\":null"
            } else if (it.second is Int) {
                body += "\"${it.first}\":${it.second}"
            } else {
                body += "\"${it.first}\":\"${it.second}\""
            }

            if (index + 1 != parameters.size) {
                body += ","
            }
        }
        body += "}"
        return body
    }

    fun deleteCurrentUser(
        activity: Activity,
        user: FirebaseUser?,
        onComplete: (result: Boolean, error: String?) -> Unit
    ) {
        try {
            if (user != null) {
                firebaseAuthDelete(activity, user) {
                    onComplete(true, null)
                }
            } else {
                val customer = Customer(callbacks = null)
                customer.user.readLocalData(activity)
                if (customer.isUserConnected()) {
                    val parameters = listOf(
                        "email" to customer.user.email,
                        "token" to customer.user.token
                    )
                    request(
                        activity,
                        "https://create.sutoko.app/api/delete/user",
                        parameters
                    ) { response ->
                        val isSuccessful = response.result == true
                        val error = response.error

                        if (isSuccessful) {
                            customer.user.disconnect(activity)
                        }

                        onComplete(isSuccessful, error as String?)
                    }
                }
            }
        } catch (e: Exception) {
            onComplete(false, null)
            Log.d("Sutoko", e.message?.toString() ?: "Error found on deleting user")
        }
    }

    // TODO : VALIDATE WITH NEW INTEGRATION
    private fun firebaseAuthDelete(
        activity: Activity,
        user: FirebaseUser?,
        onComplete: () -> Unit
    ) {
        if (user == null) {
            onComplete()
            return
        }
        user.delete().addOnCompleteListener {
            if (activity.isFinishing) {
                return@addOnCompleteListener
            }

            if (!it.isSuccessful) {
                if (it.exception != null && (it.exception!! as FirebaseAuthRecentLoginRequiredException).errorCode == "ERROR_REQUIRES_RECENT_LOGIN") {
                    Std.confirm(
                        activity,
                        R.string.security_check_delete_account_title,
                        R.string.security_check_delete_account_message,
                        R.string.ok,
                        R.string.abort, {
                            Handler(Looper.getMainLooper()).post(onComplete)
                        }, {
                            Handler(Looper.getMainLooper()).post(onComplete)
                        }
                    )
                    return@addOnCompleteListener
                }
            }
            Handler(Looper.getMainLooper()).post(onComplete)
        }

    }

    // TODO : VALIDATE WITH NEW INTEGRATION
    private fun firebaseFirestoreDelete(
        activity: Activity,
        instance: FirebaseFirestore,
        user: FirebaseUser?,
        onComplete: () -> Unit
    ) {
        if (user == null) {
            onComplete()
            return
        }
        instance.collection("users").document(user.uid)
            .delete().addOnCompleteListener {
                if (activity.isFinishing) {
                    return@addOnCompleteListener
                }
                Handler(Looper.getMainLooper()).post(onComplete)
            }
    }
}