package fr.purpletear.friendzone2.activities.choice

import android.app.Activity
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.sharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.GlobalData

class ChoiceGraphics {
    /**
     * Sets images
     * @param activity
     * @param requestManager
     */
    fun setImages(activity : Activity, requestManager : RequestManager) {
        requestManager.load(OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "light_choice")).apply(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)).into(activity.findViewById(R.id.choice_image_first))
        requestManager.load(OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "forest_choice")).apply(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)).into(activity.findViewById(R.id.choice_image_second))
    }
}