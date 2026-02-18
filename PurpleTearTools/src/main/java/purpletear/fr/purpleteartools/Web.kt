package purpletear.fr.purpleteartools

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object Web {

    /**
     * Opens the webBrowserWith the
     *
     * @param url
     * @param activity
     */
    fun openWebBrowserWith(url : String, activity : Activity) {
        val i = Intent(Intent.ACTION_VIEW)
        if (i.resolveActivity(activity.packageManager) == null) {
            Toast.makeText(
                activity.applicationContext,
                activity.getString(R.string.pptools_no_web_navigator_found),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        i.data = Uri.parse(url)
        activity.startActivity(i)
    }
}