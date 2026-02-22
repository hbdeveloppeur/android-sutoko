package fr.purpletear.friendzone2.activities.phone.sound

import android.app.Activity
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.GlobalData

class SoundScreenGraphics {
    
    /**
     * Sets the initial images
     * @param a : Activity
     * @param rm : RequestManager
     */
    fun setImages(a: Activity, rm: RequestManager) {
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ic_arrow_left_white")).into(a.findViewById(R.id.phone_soundscreen_button_back_image))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "album")).into(a.findViewById(R.id.phone_soundscreen_album))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "evaphone")).into(a.findViewById(R.id.phone_soundscreen_background))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "credit_shirosound")).into(a.findViewById(R.id.phone_soundscreen_shirosound))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_play")).into(a.findViewById(R.id.phone_soundscreen_button_sound))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_call_miss")).into(a.findViewById(R.id.phone_statusbar_icon_call))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_sms_miss")).into(a.findViewById(R.id.phone_statusbar_icon_sms))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_battery")).into(a.findViewById(R.id.phone_statusbar_icon_battery))
        // rm.load(R.drawable.friendzone).apply(requestOptions).into(a.findViewById(R.id.main_lost))
    }

    /**
     * Updates the button's image
     * @param activity : Activity
     * @param soundState : SoundState
     */
    fun updateButton(activity: Activity, soundState: SoundScreenModel.SoundState, requestManager: RequestManager) {
        val button = activity.findViewById<ImageView>(R.id.phone_soundscreen_button_sound)
        when (soundState) {
            SoundScreenModel.SoundState.PAUSED -> requestManager.load(OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_play")).into(button)
            SoundScreenModel.SoundState.PLAYING -> requestManager.load(OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_pause")).into(button)
        }
    }
}
