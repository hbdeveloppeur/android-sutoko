package fr.purpletear.sutoko.screens.accountConnection

import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sharedelements.Data
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.google.firebase.auth.FirebaseAuth
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.helpers.UserHelper
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import purpletear.fr.purpleteartools.DelayHandler
import purpletear.fr.purpleteartools.FingerV2

interface AccountConnectionCallBack {
    fun onLoginPressed()
    fun onRegisterPressed()
    fun onSendPasswordResetPressed()
    fun onMailValidatedStartUsingAccountPressed()
    fun onRequestToChangePagePressed(page: AccountConnectionActivityModel.Page)
}

class AccountConnectionActivityModel(activity: AccountConnectionActivity) {
    enum class Page {
        SIGNIN,
        REGISTER,
        FORGOT_PASSWORD,
        FORGOT_PASSWORD_MAIL_SENT,
        VALIDATE_MAIL_WAITING,
        VALIDATE_MAIL_SUCCESS,
    }

    var currentPage =
        activity.intent?.getSerializableExtra(Data.Companion.Extra.ACCOUNT_PAGE.id) as Page?
            ?: Page.SIGNIN
    private val initialPage =
        activity.intent?.getSerializableExtra(Data.Companion.Extra.ACCOUNT_PAGE.id) as Page?
            ?: Page.SIGNIN
    var schemas: MutableList<Page> = mutableListOf(currentPage)
    private var delayHandler: DelayHandler = DelayHandler()
    private var requestCounter = 0
    private var requestCounterLimit = 50
    private var request: CancellableRequest? = null
    var connectionSuccess: Boolean = false
    var requestManager: RequestManager = Glide.with(activity)
    var customer: Customer =
        Customer(callbacks = null)

    private var formData = mapOf<AccountFormHelper.Input, Any>(
    )
        get() {
            if (BuildConfig.DEBUG) {
                return mapOf<AccountFormHelper.Input, Any>(
                    AccountFormHelper.Input.MAIL to "hbdeveloppeur@gmail.com",
                    AccountFormHelper.Input.PASSWORD to "1234AaBb",
                    AccountFormHelper.Input.NICKNAME to "",
                    AccountFormHelper.Input.PASSWORD_CONFIRMATION to "",
                    AccountFormHelper.Input.CGU to false,
                )
            }
            return mapOf<AccountFormHelper.Input, Any>(
                AccountFormHelper.Input.NICKNAME to "",
                AccountFormHelper.Input.MAIL to "",
                AccountFormHelper.Input.PASSWORD to "",
                AccountFormHelper.Input.PASSWORD_CONFIRMATION to "",
                AccountFormHelper.Input.CGU to false,
            )
        }

    /**
     * Returns true if it consumes onBackPressed
     */
    fun onBackPressed(): Boolean {
        if (this.schemas.size >= 1 && this.schemas[this.schemas.lastIndex] == initialPage) {
            return false
        }
        this.schemas.removeAt(this.schemas.size - 1)
        if (this.schemas.size >= 1) {
            return true
        }
        return false
    }

    fun getPreviousPage(): Page {
        assert(this.schemas.size >= 1)
        return this.schemas[this.schemas.lastIndex]
    }

    fun addPageToSchemas(page: Page) {
        val disallowedPages = arrayOf(
            Page.FORGOT_PASSWORD_MAIL_SENT,
            Page.VALIDATE_MAIL_WAITING,
            Page.VALIDATE_MAIL_SUCCESS
        )
        if (this.schemas.size > 0 && this.schemas[this.schemas.lastIndex] != page && !disallowedPages.contains(
                page
            )
        ) {
            this.schemas.add(page)
        }
    }

    fun stopControlMailValidation() {
        this.delayHandler.stop("startControlMailValidation")
    }

    fun startControlMailValidation(
        activity: AccountConnectionActivity,
        isFirstCall: Boolean = true,
        onResult: (isValidated: Boolean) -> Unit
    ) {
        this.delayHandler.stop("startControlMailValidation")
        this.delayHandler.operation("startControlMailValidation", if (isFirstCall) 0 else 5000) {
            if (requestCounterLimit == requestCounter) {
                requestCounter = 0
                return@operation
            }
            requestCounter++
            request?.cancel()
            request = UserHelper.isMailValidated(
                activity,
                AccountFormHelper.getMail(activity)
            ) { isValidated ->
                Handler(Looper.getMainLooper()).post {
                    if (isValidated == true) {
                        onResult(true)
                    } else {
                        startControlMailValidation(activity, false, onResult)
                    }
                }

            }
        }
    }

    fun fillForm(activity: AccountConnectionActivity) {
        this.formData.forEach { (input, any) ->
            when (input) {
                AccountFormHelper.Input.NICKNAME ->
                    activity.binding.sutokoAccountConnectionFormNickname.editText?.setText(any.toString())

                AccountFormHelper.Input.MAIL ->
                    activity.binding.sutokoAccountConnectionFormEmail.editText?.setText(any.toString())

                AccountFormHelper.Input.PASSWORD ->
                    activity.binding.sutokoAccountConnectionFormPassword.editText?.setText(any.toString())

                AccountFormHelper.Input.PASSWORD_CONFIRMATION ->
                    activity.binding.sutokoAccountConnectionFormPasswordConfirm.editText?.setText(
                        any.toString()
                    )

                AccountFormHelper.Input.CGU ->
                    activity.binding.sutokoAccountConnectionFormCguCheckbox.isChecked = any == true
            }
        }
    }

    fun firebaseConnect(
        activity: AccountConnectionActivity,
        onCompletion: (isSuccessFul: Boolean, isValidatedUser: Boolean) -> Unit
    ) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(
            AccountFormHelper.getMail(activity),
            AccountFormHelper.getPassword(activity)
        )
            .addOnCompleteListener(activity) { task ->
                Handler(Looper.getMainLooper()).post {
                    if (task.isSuccessful) {
                        onCompletion(auth.currentUser != null, true)
                    } else {
                        onCompletion(false, false)
                    }
                }
            }
    }

    fun getFormResultCode(activity: AccountConnectionActivity): AccountFormHelper.FormValidationCode {
        var a = arrayOf<AccountFormHelper.FormValidationCode>()
        when (currentPage) {
            Page.SIGNIN -> {
                a = arrayOf(
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.MAIL),
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.PASSWORD)
                )
            }

            Page.REGISTER -> {
                a = arrayOf(
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.NICKNAME),
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.MAIL),
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.PASSWORD),
                    AccountFormHelper.isValidInput(
                        activity,
                        AccountFormHelper.Input.PASSWORD_CONFIRMATION
                    ),
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.CGU)
                )
            }

            Page.FORGOT_PASSWORD -> {
                a = arrayOf(
                    AccountFormHelper.isValidInput(activity, AccountFormHelper.Input.MAIL)
                )
            }

            else -> {

            }
        }
        a.forEach { value ->
            if (value != AccountFormHelper.FormValidationCode.SUCCESS) {
                return@getFormResultCode value
            }
        }
        return AccountFormHelper.FormValidationCode.SUCCESS
    }

    fun setListeners(activity: AccountConnectionActivity) {
        FingerV2.register(activity.binding.sutokoAccountConnectionButtonMain) {
            when (currentPage) {
                Page.SIGNIN -> (activity as AccountConnectionCallBack).onLoginPressed()
                Page.REGISTER -> (activity as AccountConnectionCallBack).onRegisterPressed()
                Page.FORGOT_PASSWORD -> (activity as AccountConnectionCallBack).onSendPasswordResetPressed()
                Page.VALIDATE_MAIL_SUCCESS -> (activity as AccountConnectionCallBack).onMailValidatedStartUsingAccountPressed()
                Page.FORGOT_PASSWORD_MAIL_SENT -> (activity as AccountConnectionCallBack).onRequestToChangePagePressed(
                    Page.SIGNIN
                )

                else -> {}
            }
        }

        FingerV2.register(activity.binding.sutokoAccountConnectionButtonBottomCtaText0) {
            when (currentPage) {
                Page.SIGNIN -> (activity as AccountConnectionCallBack).onRequestToChangePagePressed(
                    Page.FORGOT_PASSWORD
                )

                else -> {

                }
            }
        }
        FingerV2.register(activity.binding.sutokoAccountConnectionButtonBottomCtaText2) {
            when (currentPage) {
                Page.SIGNIN -> (activity as AccountConnectionCallBack).onRequestToChangePagePressed(
                    Page.REGISTER
                )

                Page.REGISTER -> (activity as AccountConnectionCallBack).onRequestToChangePagePressed(
                    Page.SIGNIN
                )

                Page.FORGOT_PASSWORD -> (activity as AccountConnectionCallBack).onRequestToChangePagePressed(
                    Page.SIGNIN
                )

                else -> {

                }
            }
        }
    }
}