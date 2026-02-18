package fr.purpletear.sutoko.screens.accountConnection

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.databinding.ActivityAccountConnectionBinding

object AccountConnectionActivityGraphics {

    fun setImages(activity: AccountConnectionActivity, requestManager: RequestManager) {
        requestManager.load(R.drawable.ic_logo_white).transition(withCrossFade())
            .into(activity.binding.sutokoAccountConnectionImageLogo)
        requestManager.load(fr.purpletear.sutoko.shop.presentation.R.drawable.sutoko_account_creation_header_background)
            .transition(withCrossFade())
            .into(activity.binding.sutokoAccountConnectionHeaderBackground)
    }


    fun setCguLinkClickable(activity: AccountConnectionActivity): Unit {
        val t2 = activity.findViewById(R.id.sutoko_account_connection_form_cgu_link) as TextView
        t2.movementMethod = LinkMovementMethod.getInstance()
    }

    fun updateUI(activity: AccountConnectionActivity, page: AccountConnectionActivityModel.Page) {
        val map = mapOf<View, Array<AccountConnectionActivityModel.Page>>(
            activity.binding.sutokoAccountConnectionFormNickname to arrayOf(
                AccountConnectionActivityModel.Page.REGISTER
            ),
            activity.binding.sutokoAccountConnectionFormEmail to arrayOf(
                AccountConnectionActivityModel.Page.REGISTER,
                AccountConnectionActivityModel.Page.SIGNIN,
                AccountConnectionActivityModel.Page.FORGOT_PASSWORD
            ),
            activity.binding.sutokoAccountConnectionFormPassword to arrayOf(
                AccountConnectionActivityModel.Page.REGISTER,
                AccountConnectionActivityModel.Page.SIGNIN
            ),
            activity.binding.sutokoAccountConnectionFormPasswordConfirm to arrayOf(
                AccountConnectionActivityModel.Page.REGISTER
            ),
            activity.binding.sutokoAccountConnectionFormCguContainer to arrayOf(
                AccountConnectionActivityModel.Page.REGISTER
            ),
            activity.binding.sutokoAccountConnectionMailConfirmation to arrayOf(
                AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING,
                AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS,
                AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT
            ),
        )

        setAnimation(activity, page)
        updateConstraints(activity, page)

        map.forEach { (view, arrayOfPages) ->
            view.visibility = if (arrayOfPages.contains(page)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    fun updateHeaderText(
        activity: AccountConnectionActivity,
        page: AccountConnectionActivityModel.Page
    ) {
        var title: Int? = null
        var subtitle: Int? = null
        when (page) {
            AccountConnectionActivityModel.Page.SIGNIN -> {
                title = R.string.sutoko_connection_page_signin_title
                subtitle = R.string.sutoko_connection_page_signin_subtitle
            }

            AccountConnectionActivityModel.Page.REGISTER -> {
                title = R.string.sutoko_connection_page_register_title
                subtitle = R.string.sutoko_connection_page_signin_subtitle
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING -> {
                title = R.string.sutoko_connection_page_sent_mail_title
                subtitle = R.string.sutoko_connection_page_sent_mail_subtitle
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS -> {
                title = R.string.sutoko_connection_page_register_title
                subtitle = R.string.sutoko_connection_page_sent_mail_success_subtitle
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT -> {
                title = R.string.sutoko_connection_page_forgot_pwd_title
                subtitle = R.string.sutoko_connection_page_sent_mail_subtitle
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD -> {
                title = R.string.sutoko_connection_page_forgot_pwd_title
                subtitle = R.string.sutoko_connection_page_forgot_pwd_subtitle
            }
        }
        activity.binding.sutokoAccountConnectionTitle.text =
            activity.getString(title)
        (activity.binding.sutokoAccountConnectionSubtitle as AppCompatTextView).text =
            activity.getString(subtitle)
    }

    private fun updateConstraints(
        activity: AccountConnectionActivity,
        page: AccountConnectionActivityModel.Page
    ) {
        when (page) {
            AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING,
            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS,
            AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT -> {
                activity.binding.sutokoAccountConnectionHeader.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToTop = activity.binding.sutokoAccountConnectionMailConfirmation.id
                }
            }

            else -> {
                activity.binding.sutokoAccountConnectionHeader.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToTop = activity.binding.sutokoAccountConnectionForm.id
                }
            }
        }
    }

    fun updateButton(
        activity: AccountConnectionActivity,
        page: AccountConnectionActivityModel.Page
    ) {
        var text: Int? = null
        var backgroundAlpha = 1f
        when (page) {
            AccountConnectionActivityModel.Page.SIGNIN -> {
                text = R.string.sutoko_connection_page_signin_button_text
                backgroundAlpha = 1f
            }

            AccountConnectionActivityModel.Page.REGISTER -> {
                text = R.string.sutoko_connection_page_register_button_text
                backgroundAlpha = 1f
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING -> {
                text = R.string.sutoko_connection_page_sent_button_text
                backgroundAlpha = 0.3f
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS -> {
                text = R.string.sutoko_connection_page_sent_mail_success_button_text
                backgroundAlpha = 1f
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD -> {
                text = R.string.sutoko_connection_page_forgot_pwd_button_text
                backgroundAlpha = 1f
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT -> {
                text = R.string.sutoko_connection_page_signin_button_text
                backgroundAlpha = 1f
            }
        }
        (activity.binding.sutokoAccountConnectionButtonMainText as AppCompatTextView).text =
            activity.getString(text)
        activity.binding.sutokoAccountConnectionButtonMainBackground.alpha = backgroundAlpha
    }

    fun setButtonLoading(activity: AccountConnectionActivity, isLoading: Boolean) {
        setFilterVisibility(activity.binding, isLoading)
        activity.binding.sutokoAccountConnectionButtonMain.isClickable = !isLoading
        activity.binding.sutokoAccountConnectionButtonMain.isFocusableInTouchMode = !isLoading
        activity.binding.sutokoAccountConnectionButtonMainBackground.alpha =
            if (isLoading) 0.3f else 1f
        activity.binding.sutokoAccountConnectionButtonMainProgressBar.visibility =
            if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    private fun setAnimation(
        activity: AccountConnectionActivity,
        page: AccountConnectionActivityModel.Page
    ) {
        when (page) {
            AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING -> {
                startMailAnimation(activity)
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS -> {
                startMailValidatedAnimation(activity)
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT -> {
                startMailAnimation(activity)
            }

            else -> {
                stopMailAnimation(activity)
            }
        }
    }

    private fun startMailAnimation(activity: AccountConnectionActivity) {
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.setMinFrame(0)
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.setMaxFrame(118)
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.playAnimation()
    }

    private fun stopMailAnimation(activity: AccountConnectionActivity) {
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.cancelAnimation()
    }

    private fun startMailValidatedAnimation(activity: AccountConnectionActivity) {
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.setAnimation(R.raw.email_confirmation_sent)
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.setMinFrame(123)
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.setMaxFrame(266)
        activity.binding.sutokoAccountConnectionMailConfirmationAnimation.playAnimation()
    }

    fun setFilterVisibility(binding: ActivityAccountConnectionBinding, isVisible: Boolean) {
        binding.sutokoAccountConnectionFilter.visibility =
            if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    fun updateMailValidateText(
        activity: AccountConnectionActivity,
        page: AccountConnectionActivityModel.Page,
        mail: String
    ) {
        var title: Int? = null
        var subtitle: Int? = null
        when (page) {
            AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING -> {
                title = R.string.sutoko_connection_page_mail_validated_waiting_title
                subtitle = R.string.sutoko_connection_page_mail_validated_waiting_subtitle
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS -> {
                title = R.string.sutoko_connection_page_mail_validated_success_title
                subtitle = R.string.sutoko_connection_page_mail_validated_success_subtitle
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD_MAIL_SENT -> {
                title = R.string.sutoko_connection_page_forgot_mail_sent_title
                subtitle = R.string.sutoko_connection_page_forgot_mail_sent_subtitle
            }

            else -> {

            }
        }
        (activity.binding.sutokoAccountConnectionMailConfirmationTitle as AppCompatTextView).text =
            activity.getString(title ?: return)
        (activity.binding.sutokoAccountConnectionMailConfirmationMail as AppCompatTextView).text =
            mail
        (activity.binding.sutokoAccountConnectionMailConfirmationSubtitle as AppCompatTextView).text =
            activity.getString(subtitle ?: return)

    }

    fun updateBottomCta(
        activity: AccountConnectionActivity,
        page: AccountConnectionActivityModel.Page
    ) {
        var text0: Int? = null
        var text2: Int? = null
        when (page) {
            AccountConnectionActivityModel.Page.SIGNIN -> {
                text0 = R.string.sutoko_connection_page_button_cta_forgot
                text2 = R.string.sutoko_connection_page_button_cta_register
            }

            AccountConnectionActivityModel.Page.REGISTER -> {
                text0 = null
                text2 = R.string.sutoko_connection_page_button_cta_signin
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_WAITING -> {
                text0 = null
                text2 = null
            }

            AccountConnectionActivityModel.Page.VALIDATE_MAIL_SUCCESS -> {
                text0 = null
                text2 = null
            }

            AccountConnectionActivityModel.Page.FORGOT_PASSWORD -> {
                text0 = null
                text2 = R.string.sutoko_connection_page_button_cta_signin
            }

            else -> {}
        }
        if (text0 != null) {
            (activity.binding.sutokoAccountConnectionButtonBottomCtaText0 as AppCompatTextView).text =
                activity.getString(text0)
        }
        if (text2 != null) {
            (activity.binding.sutokoAccountConnectionButtonBottomCtaText2 as AppCompatTextView).text =
                activity.getString(text2)
        }
        activity.binding.sutokoAccountConnectionButtonBottomCtaText0.visibility =
            if (text0 == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
        activity.binding.sutokoAccountConnectionButtonBottomCta.visibility =
            if (text0 == null && text2 == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }
}