package fr.purpletear.sutoko.tools

import android.app.Activity
import com.bumptech.glide.RequestManager
import com.example.sharedelements.GraphicsPreference
import fr.purpletear.sutoko.R
import purpletear.fr.purpleteartools.FingerV2

object StdGraphics {

    fun seetBackButtonListener(activity: Activity, onPresssed: () -> Unit) {
        FingerV2.register(activity, R.id.sutoko_back_button, onPresssed)
    }

    fun setBackButtonIcon(activity: Activity, requestManager: RequestManager) {
        requestManager.load(R.drawable.ic_arrow_left_header_smsgame)
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
            .into(activity.findViewById(R.id.sutoko_back_button_image))
    }
}