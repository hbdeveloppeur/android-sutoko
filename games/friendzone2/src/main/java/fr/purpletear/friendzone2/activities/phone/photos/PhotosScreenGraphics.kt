package fr.purpletear.friendzone2.activities.phone.photos

import android.app.Activity
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.GlobalData

class PhotosScreenGraphics {
    
    /**
     * Sets the initial images
     * @param a : Activity
     * @param rm : RequestManager
     */
    fun setImages(a: Activity, rm: RequestManager) {
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_call_miss")).into(a.findViewById(R.id.phone_statusbar_icon_call))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_sms_miss")).into(a.findViewById(R.id.phone_statusbar_icon_sms))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_battery")).into(a.findViewById(R.id.phone_statusbar_icon_battery))
        // rm.load(R.drawable.ico_info).into(a.findViewById(R.id.main_header_icons_info))
        // rm.load(R.drawable.friendzone).apply(requestOptions).into(a.findViewById(R.id.main_lost))
    }
}
