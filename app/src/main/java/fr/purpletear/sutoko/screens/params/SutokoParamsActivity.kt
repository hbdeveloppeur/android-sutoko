package fr.purpletear.sutoko.screens.params

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sharedelements.Data
import com.example.sharedelements.SutokoAppParams
import com.purpletear.sutoko.user.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.themes_flavors_settings.ThemesFlavorActivity
import fr.purpletear.sutoko.screens.web.WebActivity
import purpletear.fr.purpleteartools.FingerV2
import purpletear.fr.purpleteartools.Std
import javax.inject.Inject


@AndroidEntryPoint
class SutokoParamsActivity : AppCompatActivity() {

    // Handles vars
    private lateinit var model: SutokoParamsActivityModel
    private var isReloadingAccountData: Boolean = false


    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sutoko_params)
        SutokoParamsActivityGraphics.setVersionText(this)
        model = SutokoParamsActivityModel(this)
        setListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {

            SutokoParamsActivityGraphics.setOptionSentence(
                this, R.id.sutoko_params_privacy_hitbox, getString(
                    R.string.sutoko_privacy_policy
                )
            )
            SutokoParamsActivityGraphics.setOptionSentence(
                this, R.id.sutoko_params_social_share, getString(
                    R.string.sutoko_share_app
                )
            )
            SutokoParamsActivityGraphics.setOptionSentence(
                this, R.id.sutoko_params_account_disconnect, getString(
                    R.string.sutoko_disconnect
                )
            )

            if (model.isUserConnected(this)) {
                setAccountOptionsVisibility(true)
                SutokoParamsActivityGraphics.setOptionSentence(
                    this, R.id.sutoko_params_user_reload, getString(
                        R.string.sutoko_params_activity_reload_my_account_data
                    )
                )
                SutokoParamsActivityGraphics.setOptionSentence(
                    this, R.id.sutoko_params_user_delete, getString(
                        R.string.sutoko_params_activity_delete_my_account_data
                    )
                )
            } else {
                setAccountOptionsVisibility(false)
            }
        }
    }

    private fun setAccountOptionsVisibility(isVisible: Boolean) {

        SutokoParamsActivityGraphics.setRowVisibility(
            this,
            R.id.sutoko_params_user_reload,
            isVisible
        )
        SutokoParamsActivityGraphics.setRowVisibility(
            this,
            R.id.sutoko_params_user_delete,
            isVisible
        )
        SutokoParamsActivityGraphics.setRowVisibility(
            this,
            R.id.sutoko_params_account_disconnect,
            isVisible
        )
    }

    private fun onFlavorSettingsButtonPressed() {
        startActivity(
            Intent(this, ThemesFlavorActivity::class.java)
        )
    }

    private fun onBugSignalButtonPressed() {

    }

    private fun onCreditsButtonPressed() {
        val params =
            intent.getParcelableExtra(Data.Companion.Extra.APP_PARAMS.id) as SutokoAppParams?
        params?.let {
            val i = WebActivity.require(
                this,
                "https://www.sutoko.app/credits",
                null,
                it
            )
            startActivity(i)
        }
    }

    private fun onReloadAccountPressed() {
        if (isReloadingAccountData) {
            return
        }

        Std.confirm(
            this,
            R.string.sutoko_params_activity_reload_my_account_data_confirm,
            R.string.sutoko_ok,
            R.string.sutoko_abort, {
                reloadAccountData()
            }, {}
        )
    }

    private fun onDeleteAccountPressed() {
        Std.confirm(
            this,
            R.string.sutoko_params_activity_delete_my_account_data_confirm,
            R.string.sutoko_ok,
            R.string.sutoko_abort, {
                SutokoParamsActivityGraphics.setProgressBarVisibility(
                    this,
                    SutokoParamsActivityGraphics.SutokoParamsProgressBar.USER_DELETE,
                    true
                )
                userRepository.disconnect()
                model.deleteAccount(this) {
                    SutokoParamsActivityGraphics.setProgressBarVisibility(
                        this,
                        SutokoParamsActivityGraphics.SutokoParamsProgressBar.USER_DELETE,
                        false
                    )
                    setAccountOptionsVisibility(false)
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.account_deleted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }, {}
        )
    }

    private fun onDisconnectButtonPressed() {
        Std.confirm(
            this,
            R.string.sutoko_disconnect,
            R.string.sutoko_ok,
            R.string.sutoko_abort, {
                userRepository.disconnect()
                model.disconnectUser(this)
                setAccountOptionsVisibility(false)
                Toast.makeText(
                    applicationContext,
                    getString(R.string.sutoko_params_activity_disconnect_success),
                    Toast.LENGTH_LONG
                ).show()
            }, {}
        )
    }

    private fun reloadAccountData() {
        isReloadingAccountData = true

        SutokoParamsActivityGraphics.setProgressBarVisibility(
            this,
            SutokoParamsActivityGraphics.SutokoParamsProgressBar.USER_RELOAD,
            true
        )
    }

    private fun onShareButtonPressed() {
        this.model.shareApp(this)
    }

    private fun setListeners() {
        FingerV2.register(this, R.id.sutoko_params_privacy_hitbox) {
            val appParams: SutokoAppParams =
                intent.getParcelableExtra(Data.Companion.Extra.APP_PARAMS.id)!!
            SutokoParamsActivityModel.onPrivacyButtonPressed(this, appParams)
        }
//        FingerV2.register(
//            this,
//            R.id.sutoko_params_settings_flavors_gender
//        ) { onFlavorSettingsButtonPressed() }
//        FingerV2.register(this, R.id.sutoko_params_signal_bug_hitbox) { onBugSignalButtonPressed() }
        FingerV2.register(this, R.id.sutoko_params_user_reload) { onReloadAccountPressed() }
        FingerV2.register(this, R.id.sutoko_params_user_delete, ::onDeleteAccountPressed)
        FingerV2.register(this, R.id.sutoko_params_social_share, ::onShareButtonPressed)
        FingerV2.register(this, R.id.sutoko_params_account_disconnect, ::onDisconnectButtonPressed)
        findViewById<Toolbar>(R.id.sutoko_params_toolbars).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun require(activity: Activity, appParams: SutokoAppParams): Intent {
            val i = Intent(activity, SutokoParamsActivity::class.java)
            i.putExtra(Data.Companion.Extra.APP_PARAMS.id, appParams as Parcelable)
            return i
        }
    }
}
