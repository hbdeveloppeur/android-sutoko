package fr.purpletear.sutoko.screens.params

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.R

object SutokoParamsActivityGraphics {

    enum class SutokoParamsProgressBar {
        USER_DELETE,
        USER_RELOAD
    }

    fun setVersionText(activity: Activity) {
        val versionText =
            activity.getString(R.string.sutoko_version_name_build, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        val versionNameTextView: TextView = activity.findViewById(R.id.sutoko_params_version)
        versionNameTextView.text = versionText
    }

    /**
     * Sets the option's sentence
     * @param activity
     * @param optionId
     * @param str
     */
    fun setOptionSentence(activity: Activity, optionId: Int, str: String) {
        activity.findViewById<TextView>(optionId).text = str
    }

    fun setRowVisibility(activity: Activity, id: Int, isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        activity.findViewById<View>(id).visibility = visibility
    }

    fun setProgressBarVisibility(activity: SutokoParamsActivity, code: SutokoParamsProgressBar, isVisible: Boolean) {
        val id = when (code) {
            SutokoParamsProgressBar.USER_DELETE -> R.id.sutoko_params_user_delete_progress
            SutokoParamsProgressBar.USER_RELOAD -> R.id.sutoko_params_user_reload_progress
        }
        activity.findViewById<ProgressBar>(id).visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}