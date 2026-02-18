package fr.purpletear.sutoko.screens.accountConnection

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.children
import com.google.android.material.textfield.TextInputLayout
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.tools.Std.Companion.hideKeyboard

object AccountFormHelper {
    private fun String.isEmail(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this)
            .matches()
    }

    enum class Input {
        NICKNAME,
        MAIL,
        PASSWORD,
        PASSWORD_CONFIRMATION,
        CGU,
    }


    enum class FormValidationCode(val messageId: Int?, val target: Input?) {
        SUCCESS(null, null),
        UNKNOWN_ERROR(R.string.sutoko_account_error_unknown, null),

        // LOGIN
        INVALID_CREDENTIALS(R.string.sutoko_account_error_invalid_credentials, Input.MAIL),
        MAIL_NOT_VALIDATED(null, null),
        REQUIRE_FIREBASE_AUTH_CHECK(null, null),

        // REGISTER
        MAIL_ADDRESS_NOT_AVAILABLE(R.string.sutoko_account_error_address_not_available, Input.MAIL),
        NICKNAME_NOT_AVAILABLE(
            R.string.sutoko_account_error_username_not_available,
            Input.NICKNAME
        ),
        NICKNAME_REGEX_ERROR(R.string.sutoko_account_error_invalid_nickname_format, Input.NICKNAME),
        NICKNAME_LENGTH_TOO_SHORT(
            R.string.sutoko_account_error_invalid_nickname_size,
            Input.NICKNAME
        ),
        NICKNAME_LENGTH_TOO_LONG(
            R.string.sutoko_account_error_invalid_nickname_size,
            Input.NICKNAME
        ),
        PASSWORD_WEAK(R.string.sutoko_account_error_weak_password, Input.PASSWORD),
        PASSWORD_LENGTH_NOT_VALID(
            R.string.sutoko_account_error_invalid_password_size,
            Input.PASSWORD
        ),
        PASSWORD_CONFIRMATION_NOT_VALID(
            R.string.sutoko_account_error_invalid_password_confirm,
            Input.PASSWORD_CONFIRMATION
        ),
        MAIL_ADDRESS_WRONG_FORMAT(R.string.sutoko_account_error_invalid_mail_address, Input.MAIL),
        MAIL_ADDRESS_DOMAIN_FORBIDDEN(
            R.string.sutoko_account_error_mail_provider_banned,
            Input.MAIL
        ),
        RULES_NOT_CHECKED(R.string.sutoko_account_error_cgu, Input.CGU)
    }

    fun displayError(activity: AccountConnectionActivity, validationCode: FormValidationCode) {
        val text = activity.getString(validationCode.messageId ?: return)
        if (validationCode == FormValidationCode.UNKNOWN_ERROR) {
            Toast.makeText(activity.applicationContext, text, Toast.LENGTH_LONG).show()
            return
        }
        when (validationCode.target) {
            Input.NICKNAME -> {
                activity.binding.sutokoAccountConnectionFormNickname.error = text
                activity.binding.sutokoAccountConnectionFormNickname.isErrorEnabled = true
            }
            Input.MAIL -> {
                activity.binding.sutokoAccountConnectionFormEmail.error = text
                activity.binding.sutokoAccountConnectionFormEmail.isErrorEnabled = true
            }
            Input.PASSWORD -> {
                activity.binding.sutokoAccountConnectionFormPassword.error = text
                activity.binding.sutokoAccountConnectionFormPassword.isErrorEnabled = true
            }
            Input.PASSWORD_CONFIRMATION -> {
                activity.binding.sutokoAccountConnectionFormPasswordConfirm.error = text
                activity.binding.sutokoAccountConnectionFormPasswordConfirm.isErrorEnabled = true
            }
            Input.CGU -> {
                Toast.makeText(activity.applicationContext, text, Toast.LENGTH_LONG).show()
            }
            null -> {
            }
        }
    }

    fun clearErrorOnInput(activity: AccountConnectionActivity) {
        activity.binding.sutokoAccountConnectionForm.children.forEach { v ->
            if (v is TextInputLayout) {
                (v as TextInputLayout).editText?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        v.error = null
                        v.isErrorEnabled = false
                    }

                })
                if (arrayOf(
                        R.id.sutoko_account_connection_form_email,
                        R.id.sutoko_account_connection_form_nickname,
                    ).contains(v.id)
                ) {
                    val removeFilter =
                        InputFilter { s, _, _, _, _, _ -> s.toString().replace(" ", "") }
                    v.editText?.apply { filters = filters.plus(removeFilter) }
                }

                v.editText?.setOnEditorActionListener { view, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Call onDone result here
                        Handler(Looper.getMainLooper()).post {
                            v.editText?.hideKeyboard()
                            v.editText?.clearFocus()
                        }
                        true
                    }
                    false
                }
            }
        }
    }

    fun clearErrors(activity: AccountConnectionActivity) {
        val text = null
        activity.binding.sutokoAccountConnectionFormNickname.error = text
        activity.binding.sutokoAccountConnectionFormNickname.isErrorEnabled = false
        activity.binding.sutokoAccountConnectionFormEmail.error = text
        activity.binding.sutokoAccountConnectionFormEmail.isErrorEnabled = false
        activity.binding.sutokoAccountConnectionFormPassword.error = text
        activity.binding.sutokoAccountConnectionFormPassword.isErrorEnabled = false
        activity.binding.sutokoAccountConnectionFormPasswordConfirm.error = text
        activity.binding.sutokoAccountConnectionFormPasswordConfirm.isErrorEnabled = false
    }


    fun getNickName(activity: AccountConnectionActivity): String {
        return activity.binding.sutokoAccountConnectionFormNickname.editText?.text.toString() ?: ""
    }

    fun getMail(activity: AccountConnectionActivity): String {
        return activity.binding.sutokoAccountConnectionFormEmail.editText?.text.toString()
            .replace(" ", "") ?: ""
    }

    fun getPassword(activity: AccountConnectionActivity): String {
        return activity.binding.sutokoAccountConnectionFormPassword.editText?.text.toString() ?: ""
    }

    fun getPasswordConfirmation(activity: AccountConnectionActivity): String {
        return activity.binding.sutokoAccountConnectionFormPasswordConfirm.editText?.text.toString()
            ?: ""
    }

    fun getCGU(activity: AccountConnectionActivity): Boolean {
        return activity.binding.sutokoAccountConnectionFormCguCheckbox.isChecked
    }

    fun clear(activity: AccountConnectionActivity, clearEmail: Boolean = true) {
        activity.binding.sutokoAccountConnectionFormNickname.editText?.setText("")
        if (clearEmail) {
            activity.binding.sutokoAccountConnectionFormEmail.editText?.setText("")
        }
        activity.binding.sutokoAccountConnectionFormPassword.editText?.setText("")
        activity.binding.sutokoAccountConnectionFormPasswordConfirm.editText?.setText("")
        activity.binding.sutokoAccountConnectionFormCguCheckbox.isChecked = false
    }

    fun isValidInput(activity: AccountConnectionActivity, input: Input): FormValidationCode {
        return when (input) {
            Input.NICKNAME -> {
                val value = getNickName(activity)
                if (value.isBlank() || value.length < 4) {
                    return FormValidationCode.NICKNAME_LENGTH_TOO_SHORT
                }
                if (value.length > 16) {
                    return FormValidationCode.NICKNAME_LENGTH_TOO_LONG
                }
                FormValidationCode.SUCCESS
            }

            Input.MAIL -> {
                val value = getMail(activity)
                val isValidMail = value.isNotBlank() && value.length > 3 && value.isEmail()

                if (!isValidMail) {
                    return FormValidationCode.MAIL_ADDRESS_WRONG_FORMAT
                }

                if (!maiAllowed(activity)) {
                    return FormValidationCode.MAIL_ADDRESS_DOMAIN_FORBIDDEN
                }
                FormValidationCode.SUCCESS
            }

            Input.PASSWORD -> {
                val value = getPassword(activity)
                val isTooShort = value.isBlank() || value.isNotBlank() && value.length < 4
                val isValidLength = value.isNotBlank() && value.length in 3..50
                if (isTooShort) {
                    return FormValidationCode.PASSWORD_WEAK
                } else if (!isValidLength) {
                    return FormValidationCode.PASSWORD_LENGTH_NOT_VALID
                }
                FormValidationCode.SUCCESS
            }

            Input.PASSWORD_CONFIRMATION -> {
                val password = getPassword(activity)
                val value = getPasswordConfirmation(activity)
                val isValid = value.isNotBlank() && password == value
                if (!isValid) {
                    return FormValidationCode.PASSWORD_CONFIRMATION_NOT_VALID
                }
                FormValidationCode.SUCCESS
            }

            Input.CGU -> {
                if (!getCGU(activity)) {
                    return FormValidationCode.RULES_NOT_CHECKED
                }
                FormValidationCode.SUCCESS
            }
        }
    }

    private fun maiAllowed(activity: AccountConnectionActivity): Boolean {
        val value = getMail(activity)
        return value.contains("@") && !invalidDomains().contains(value.split("@")[1])
    }

    private fun invalidDomains(): Array<String> {
        return arrayOf(
            "0815.ru0clickemail.com",
            "0-mail.com",
            "0wnd.net",
            "0wnd.org",
            "10minutemail.com",
            "20minutemail.com",
            "2prong.com",
            "3d-painting.com",
            "4warding.com",
            "4warding.net",
            "4warding.org",
            "9ox.net",
            "a-bc.net",
            "ag.us.to",
            "amilegit.com",
            "anonbox.net",
            "anonymbox.com",
            "antichef.com",
            "antichef.net",
            "antispam.de",
            "baxomale.ht.cx",
            "beefmilk.com",
            "binkmail.com",
            "bio-muesli.net",
            "bobmail.info",
            "bodhi.lawlita.com",
            "bofthew.com",
            "brefmail.com",
            "bsnow.net",
            "bugmenot.com",
            "bumpymail.com",
            "casualdx.com",
            "chogmail.com",
            "cool.fr.nf",
            "correo.blogos.net",
            "cosmorph.com",
            "courriel.fr.nf",
            "courrieltemporaire.com",
            "curryworld.de",
            "cust.in",
            "dacoolest.com",
            "dandikmail.com",
            "deadaddress.com",
            "despam.it",
            "despam.it",
            "devnullmail.com",
            "dfgh.net",
            "digitalsanctuary.com",
            "discardmail.com",
            "discardmail.de",
            "disposableaddress.com",
            "disposeamail.com",
            "disposemail.com",
            "dispostable.com",
            "dm.w3internet.co.ukexample.com",
            "dodgeit.com",
            "dodgit.com",
            "dodgit.org",
            "dontreg.com",
            "dontsendmespam.de",
            "dump-email.info",
            "dumpyemail.com",
            "e4ward.com",
            "email60.com",
            "emailias.com",
            "emailias.com",
            "emailinfive.com",
            "emailmiser.com",
            "emailtemporario.com.br",
            "emailwarden.com",
            "enterto.com",
            "ephemail.net",
            "explodemail.com",
            "fakeinbox.com",
            "fakeinformation.com",
            "fansworldwide.de",
            "fastacura.com",
            "filzmail.com",
            "fixmail.tk",
            "fizmail.com",
            "frapmail.com",
            "garliclife.com",
            "gelitik.in",
            "get1mail.com",
            "getonemail.com",
            "getonemail.net",
            "girlsundertheinfluence.com",
            "gishpuppy.com",
            "goemailgo.com",
            "great-host.in",
            "greensloth.com",
            "greensloth.com",
            "gsrv.co.uk",
            "guerillamail.biz",
            "guerillamail.com",
            "guerillamail.net",
            "guerillamail.org",
            "guerrillamail.biz",
            "guerrillamail.com",
            "guerrillamail.de",
            "guerrillamail.net",
            "guerrillamail.org",
            "guerrillamailblock.com",
            "haltospam.com",
            "hidzz.com",
            "hotpop.com",
            "ieatspam.eu",
            "ieatspam.info",
            "ihateyoualot.info",
            "imails.info",
            "inboxclean.com",
            "inboxclean.org",
            "incognitomail.com",
            "incognitomail.net",
            "ipoo.org",
            "irish2me.com",
            "jetable.com",
            "jetable.fr.nf",
            "jetable.net",
            "jetable.org",
            "jnxjn.com",
            "junk1e.com",
            "kasmail.com",
            "kaspop.com",
            "klzlk.com",
            "kulturbetrieb.info",
            "kurzepost.de",
            "kurzepost.de",
            "lifebyfood.com",
            "link2mail.net",
            "litedrop.com",
            "lookugly.com",
            "lopl.co.cc",
            "lr78.com",
            "maboard.com",
            "mail.by",
            "mail.mezimages.net",
            "mail4trash.com",
            "mailbidon.com",
            "mailcatch.com",
            "maileater.com",
            "mailexpire.com",
            "mailin8r.com",
            "mailinator.com",
            "mailinator.net",
            "mailinator2.com",
            "mailincubator.com",
            "mailme.lv",
            "mailmetrash.com",
            "mailmoat.com",
            "mailnator.com",
            "mailnull.com",
            "mailzilla.org",
            "mbx.cc",
            "mega.zik.dj",
            "meltmail.com",
            "mierdamail.com",
            "mintemail.com",
            "mjukglass.nu",
            "mobi.web.id",
            "moburl.com",
            "moncourrier.fr.nf",
            "monemail.fr.nf",
            "monmail.fr.nf",
            "mt2009.com",
            "mx0.wwwnew.eu",
            "mycleaninbox.net",
            "myspamless.com",
            "mytempemail.com",
            "mytrashmail.com",
            "netmails.net",
            "neverbox.com",
            "no-spam.ws",
            "nobulk.com",
            "noclickemail.com",
            "nogmailspam.info",
            "nomail.xl.cx",
            "nomail2me.com",
            "nospam.ze.tc",
            "nospam4.us",
            "nospamfor.us",
            "nowmymail.com",
            "objectmail.com",
            "obobbo.com",
            "odaymail.com",
            "onewaymail.com",
            "ordinaryamerican.net",
            "owlpic.com",
            "pookmail.com",
            "privymail.de",
            "proxymail.eu",
            "punkass.com",
            "putthisinyourspamdatabase.com",
            "quickinbox.com",
            "rcpt.at",
            "recode.me",
            "recursor.net",
            "regbypass.comsafe-mail.net",
            "safetymail.info",
            "sandelf.de",
            "saynotospams.com",
            "selfdestructingmail.com",
            "sendspamhere.com",
            "sharklasers.com",
            "shieldedmail.com",
            "shiftmail.com",
            "skeefmail.com",
            "slopsbox.com",
            "slushmail.com",
            "smaakt.naar.gravel",
            "smellfear.com",
            "snakemail.com",
            "sneakemail.com",
            "sofort-mail.de",
            "sogetthis.com",
            "soodonims.com",
            "spam.la",
            "spamavert.com",
            "spambob.net",
            "spambob.org",
            "spambog.com",
            "spambog.de",
            "spambog.ru",
            "spambox.info",
            "spambox.us",
            "spamcannon.com",
            "spamcannon.net",
            "spamcero.com",
            "spamcorptastic.com",
            "spamcowboy.com",
            "spamcowboy.net",
            "spamcowboy.org",
            "spamday.com",
            "spamex.com",
            "spamfree.eu",
            "spamfree24.com",
            "spamfree24.de",
            "spamfree24.eu",
            "spamfree24.info",
            "spamfree24.net",
            "spamfree24.org",
            "spamgourmet.com",
            "spamgourmet.net",
            "spamgourmet.org",
            "spamherelots.com",
            "spamhereplease.com",
            "spamhole.com",
            "spamify.com",
            "spaminator.de",
            "spamkill.info",
            "spaml.com",
            "spaml.de",
            "spammotel.com",
            "spamobox.com",
            "spamspot.com",
            "spamthis.co.uk",
            "spamthisplease.com",
            "speed.1s.fr",
            "suremail.info",
            "tempalias.com",
            "tempe-mail.com",
            "tempemail.biz",
            "tempemail.com",
            "tempemail.net",
            "tempinbox.co.uk",
            "tempinbox.com",
            "tempomail.fr",
            "temporaryemail.net",
            "temporaryinbox.com",
            "tempymail.com",
            "thankyou2010.com",
            "thisisnotmyrealemail.com",
            "throwawayemailaddress.com",
            "tilien.com",
            "tmailinator.com",
            "tradermail.info",
            "trash-amil.com",
            "trash-mail.at",
            "trash-mail.com",
            "trash-mail.de",
            "trash2009.com",
            "trashmail.at",
            "trashmail.com",
            "trashmail.me",
            "trashmail.net",
            "trashmailer.com",
            "trashymail.com",
            "trashymail.net",
            "trillianpro.com",
            "tyldd.com",
            "tyldd.com",
            "uggsrock.com",
            "wegwerfmail.de",
            "wegwerfmail.net",
            "wegwerfmail.org",
            "wh4f.org",
            "whyspam.me",
            "willselfdestruct.com",
            "winemaven.info",
            "wronghead.com",
            "wuzupmail.net",
            "xoxy.net",
            "yogamaven.com",
            "yopmail.com",
            "yopmail.fr",
            "yopmail.net",
            "yuurok.com",
            "zippymail.info",
            "zoemail.com"
        )
    }
}