package fr.purpletear.sutoko.screens.params

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SutokoAppParams
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.helpers.UserHelper
import fr.purpletear.sutoko.screens.web.WebActivity
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import purpletear.fr.purpleteartools.DelayHandler


class SutokoParamsActivityModel(activity: Activity) {
    val requestManager: RequestManager = Glide.with(activity)
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var instance: FirebaseFirestore = FirebaseFirestore.getInstance()
    var user: FirebaseUser?
    var delayHandler: DelayHandler = DelayHandler()
    private var preventTest: Boolean = false
    val customer = Customer(callbacks = null)

    init {
        user = auth.currentUser
        customer.user.readLocalData(activity)
    }

    fun shareApp(activity: Activity) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            activity.getString(
                R.string.sutoko_share_app_message,
                "https://play.google.com/store/apps/details?id=fr.purpletear.sutoko"
            )
        )
        activity.startActivity(Intent(intent))
    }

    fun isUserConnected(activity: Activity): Boolean {
        if (customer.isUserConnected()) {
            return true
        }
        if (auth.currentUser != null) {
            auth.currentUser!!.reload()
            user = auth.currentUser
        }

        return user != null && user!!.isEmailVerified
    }

    fun disconnectUser(activity: Activity) {
        if (customer.isUserConnected()) {
            customer.user.disconnect(activity)
        }
    }

    fun deleteAccount(activity: Activity, onComplete: () -> Unit) {
        // TODO: delete account
    }

    private fun disconnectUser() {
        FirebaseAuth.getInstance().signOut()
        user = FirebaseAuth.getInstance().currentUser
    }


    companion object {

        fun onPrivacyButtonPressed(activity: Activity, appParams: SutokoAppParams) {
            val i = WebActivity.require(activity, appParams.privacyPolicyUrl, null, appParams)
            activity.startActivity(i)
        }
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true

    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }
}