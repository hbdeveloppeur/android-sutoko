package fr.purpletear.sutoko.screens.params

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.sharedelements.Data
import com.example.sharedelements.SutokoAppParams
import com.example.sharedelements.theme.SutokoTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.web.WebActivity

@AndroidEntryPoint
class SutokoParamsActivity : ComponentActivity() {

    private val viewModel: SutokoParamsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appParams = extractAppParams()
        viewModel.setPrivacyPolicyUrl(appParams.privacyPolicyUrl)

        setContent {
            SutokoTheme {
                SutokoParamsScreen(
                    viewModel = viewModel,
                    onOpenPrivacyPolicy = ::openPrivacyPolicy,
                    onShareApp = ::shareApp,
                    onNavigateBack = ::finish,
                )
            }
        }
    }

    private fun extractAppParams(): SutokoAppParams {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Data.Companion.Extra.APP_PARAMS.id,
                SutokoAppParams::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Data.Companion.Extra.APP_PARAMS.id)
        } ?: SutokoAppParams()
    }

    private fun openPrivacyPolicy(url: String) {
        if (url.isBlank()) return
        val intent = WebActivity.require(this, url, null, extractAppParams())
        startActivity(intent)
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(
                    R.string.sutoko_share_app_message,
                    "https://play.google.com/store/apps/details?id=fr.purpletear.sutoko"
                )
            )
        }
        startActivity(Intent.createChooser(intent, null))
    }

    companion object {
        fun require(activity: Activity, appParams: SutokoAppParams): Intent {
            return Intent(activity, SutokoParamsActivity::class.java).apply {
                putExtra(Data.Companion.Extra.APP_PARAMS.id, appParams as Parcelable)
            }
        }
    }
}
