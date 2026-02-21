package fr.purpletear.sutoko.screens.accountConnection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sutokosharedelements.Data
import com.example.sharedelements.User
import com.purpletear.shop.domain.usecase.GetShopBalanceUseCase
import com.purpletear.sutoko.user.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.databinding.ActivityAccountConnectionBinding
import fr.purpletear.sutoko.helpers.AppTesterHelper
import fr.purpletear.sutoko.helpers.UserHelper
import fr.purpletear.sutoko.shop.shop.ShopActivityGraphics
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class AccountConnectionActivity : AppCompatActivity(), AccountConnectionCallBack {
    lateinit var binding: ActivityAccountConnectionBinding
    private lateinit var model: AccountConnectionActivityModel

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var getShopBalanceUseCase: GetShopBalanceUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShopActivityGraphics.setStatusBar(this)

        binding = ActivityAccountConnectionBinding.inflate(layoutInflater)
        model = AccountConnectionActivityModel(this)
        setContentView(binding.root)
        this.model.fillForm(this)
        AccountFormHelper.clearErrorOnInput(this)
        this.updateUI()
        this.model.setListeners(this)
        AccountConnectionActivityGraphics.setImages(this, model.requestManager)
        AccountConnectionActivityGraphics.setCguLinkClickable(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("model.currentPage", model.currentPage)
        outState.putBoolean("model.connectionSuccess", model.connectionSuccess)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        model.currentPage =
            savedInstanceState.getSerializable("model.currentPage") as AccountConnectionActivityModel.Page
        model.connectionSuccess = savedInstanceState.getBoolean("model.connectionSuccess")
    }

    override fun onBackPressed() {
        if (this.model.onBackPressed()) {
            val previousPage = this.model.getPreviousPage()
            this.displayPage(previousPage)
            return
        }
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        this.manageValidationMailControlWindowFocusChanged(hasFocus)
    }


    private fun updateUI() {
        AccountConnectionActivityGraphics.updateUI(this, model.currentPage)
        AccountConnectionActivityGraphics.updateHeaderText(this, model.currentPage)
        AccountConnectionActivityGraphics.updateButton(this, model.currentPage)
        AccountConnectionActivityGraphics.updateBottomCta(this, model.currentPage)
    }

    private fun onRegistrationSuccess() {
        AccountFormHelper.clear(this, false)
        displayPage(AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING)
        AccountConnectionActivityGraphics.updateMailValidateText(
            this,
            model.currentPage,
            AccountFormHelper.getMail(this)
        )
        manageValidationMailControlWindowFocusChanged(true)
    }

    private fun onLoginSuccess(isValidatedUser: Boolean) {


        lifecycleScope.launch {
            if (!model.customer.isUserConnected()) {
                return@launch
            }
            try {
                getShopBalanceUseCase(
                    userId = model.customer.getUserId(),
                    userToken = model.customer.getUserToken()
                ).collect {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        userRepository.onLoginSuccess()
        this.model.connectionSuccess = true
        if (!isValidatedUser) {
            displayPage(AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING)
            AccountFormHelper.clear(this, false)
            AccountConnectionActivityGraphics.updateMailValidateText(
                this,
                model.currentPage,
                AccountFormHelper.getMail(this)
            )
            manageValidationMailControlWindowFocusChanged(true)
        } else {
            // Quit + toast
            AccountConnectionActivityGraphics.setButtonLoading(this, true)
            onStartOrderHistoryValidation {
                AccountConnectionActivityGraphics.setButtonLoading(this, false)
                Toast.makeText(
                    applicationContext,
                    R.string.sutoko_you_are_connected,
                    Toast.LENGTH_LONG
                ).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun onFirebaseConvertionDone(
        isValidatedUser: Boolean,
        email: String,
        token: String?,
        uid: String?
    ) {
        if (isValidatedUser) {
            if (token != null && uid != null) {
                val user = User()
                user.saveToken(this, email, token, uid)
            }
            this.onLoginSuccess(true)
        } else {
            displayPage(AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING)
            AccountConnectionActivityGraphics.updateMailValidateText(
                this,
                model.currentPage,
                AccountFormHelper.getMail(this)
            )
            manageValidationMailControlWindowFocusChanged(true)
        }
    }

    private fun onStartOrderHistoryValidation(onComplete: () -> Unit) {
        this.model.customer.read(this)
        this.model.customer.load(this) f@{
            if (this.isFinishing) {
                return@f
            }
            onComplete()
        }
    }

    private fun onRequireFirebaseConvertion() {
        this.model.firebaseConnect(this) { res, isValidatedUser ->
            if (res) {
                UserHelper.convertAccount(
                    this,
                    AccountFormHelper.getMail(this),
                    AccountFormHelper.getPassword(this),
                    isValidatedUser
                ) { isSuccessful, error, token, uid ->
                    if (isSuccessful) {
                        onFirebaseConvertionDone(
                            isValidatedUser,
                            AccountFormHelper.getMail(this),
                            token,
                            uid
                        )
                    } else {
                        AppTesterHelper.sendReport(
                            this,
                            "onRequireFirebaseConvertion - $error",
                            AccountFormHelper.getMail(this)
                        )
                        Toast.makeText(
                            applicationContext,
                            "Error found : $error. An admin has been contacted.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    AccountConnectionActivityGraphics.setButtonLoading(this, false)
                }
            } else {
                AccountConnectionActivityGraphics.setButtonLoading(this, false)
                AccountFormHelper.displayError(
                    this,
                    AccountFormHelper.FormValidationCode.INVALID_CREDENTIALS
                )
            }
        }
    }

    override fun onLoginPressed() {
        AccountConnectionActivityGraphics.setButtonLoading(this, true)
        val r = model.getFormResultCode(this)
        if (r == AccountFormHelper.FormValidationCode.SUCCESS) {
            UserHelper.login(
                this,
                AccountFormHelper.getMail(this),
                AccountFormHelper.getPassword(this)
            ) { isSuccessful, resultCode ->
                val userNotValidated =
                    resultCode == AccountFormHelper.FormValidationCode.MAIL_NOT_VALIDATED
                if (resultCode == AccountFormHelper.FormValidationCode.REQUIRE_FIREBASE_AUTH_CHECK) {
                    onRequireFirebaseConvertion()
                } else if (isSuccessful || userNotValidated) {
                    // onRegistered
                    AccountConnectionActivityGraphics.setButtonLoading(this, false)
                    onLoginSuccess(!userNotValidated)
                } else {
                    AccountConnectionActivityGraphics.setButtonLoading(this, false)
                    AccountFormHelper.displayError(this, resultCode)
                }
            }
        } else {
            AccountFormHelper.displayError(this, r)
            AccountConnectionActivityGraphics.setButtonLoading(this, false)
        }
    }

    override fun onRegisterPressed() {
        AccountConnectionActivityGraphics.setButtonLoading(this, true)
        val resultCode = model.getFormResultCode(this)
        if (resultCode == AccountFormHelper.FormValidationCode.SUCCESS) {
            UserHelper.register(
                this,
                AccountFormHelper.getNickName(this),
                AccountFormHelper.getMail(this),
                AccountFormHelper.getPassword(this)
            ) { isSuccessful, resultCode ->
                AccountConnectionActivityGraphics.setButtonLoading(this, false)
                if (isSuccessful) {
                    // onRegistered
                    onRegistrationSuccess()
                } else {
                    AccountFormHelper.displayError(this, resultCode)
                }
            }
        } else {
            AccountFormHelper.displayError(this, resultCode)
            AccountConnectionActivityGraphics.setButtonLoading(this, false)
        }
    }

    override fun onSendPasswordResetPressed() {
        AccountConnectionActivityGraphics.setButtonLoading(this, true)
        val r = model.getFormResultCode(this)
        if (r == AccountFormHelper.FormValidationCode.SUCCESS) {
            UserHelper.sendForgotPasswordMail(
                this,
                AccountFormHelper.getMail(this)
            ) { isSuccessful, _ ->
                if (isSuccessful) {
                    displayPage(AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT)
                    AccountConnectionActivityGraphics.updateMailValidateText(
                        this,
                        model.currentPage,
                        AccountFormHelper.getMail(this)
                    )
                } else {
                    Toast.makeText(
                        applicationContext,
                        R.string.sutoko_error_cannot_reach_this_mail_address,
                        Toast.LENGTH_LONG
                    ).show()
                }
                AccountConnectionActivityGraphics.setButtonLoading(this, false)
            }
        } else {
            AccountFormHelper.displayError(this, r)
            AccountConnectionActivityGraphics.setButtonLoading(this, false)
        }
    }

    private fun onMailValidated() {
        val mail = AccountFormHelper.getMail(this)
        AccountConnectionActivityGraphics.updateMailValidateText(
            this,
            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS,
            mail
        )
        displayPage(AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS)
    }

    override fun onMailValidatedStartUsingAccountPressed() {
        if (model.connectionSuccess) {
            onLoginSuccess(true)
        } else {
            displayPage(AccountConnectionActivityModel.Page.SIGNIN)
        }
    }

    override fun onRequestToChangePagePressed(page: AccountConnectionActivityModel.Page) {
        displayPage(page)
    }

    private fun displayPage(page: AccountConnectionActivityModel.Page) {
        this.model.addPageToSchemas(page)
        AccountFormHelper.clear(this, page == AccountConnectionActivityModel.Page.REGISTER)
        AccountFormHelper.clearErrors(this)
        model.currentPage = page
        AccountConnectionActivityGraphics.updateUI(this, page)
        updateUI()
    }

    private fun manageValidationMailControlWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && this.model.currentPage == AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING) {
            this.model.startControlMailValidation(this) { isValidated ->
                if (isValidated) {
                    onMailValidated()
                }
            }
        } else {
            this.model.stopControlMailValidation()
        }
    }

    companion object {
        fun require(activity: Activity, page: AccountConnectionActivityModel.Page): Intent {
            val intent = Intent(activity, AccountConnectionActivity::class.java)
            intent.putExtra(Data.Companion.Extra.ACCOUNT_PAGE.id, page as Serializable)
            return intent
        }
    }
}