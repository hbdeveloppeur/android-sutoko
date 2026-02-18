package fr.purpletear.friendzone2.activities.phone.sms

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.activities.phone.photos.GridSpacingItemDecoration
import fr.purpletear.friendzone2.activities.main.GameConversationAdapter
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Measure


class SmsScreenGraphics {

    /**
     * Sets the initial images
     * @param a : Activity
     * @param rm : RequestManager
     */
    fun setImages(a: Activity, rm: RequestManager) {
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_call_miss")).into(a.findViewById(R.id.phone_statusbar_icon_call))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_sms_miss")).into(a.findViewById(R.id.phone_statusbar_icon_sms))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_battery")).into(a.findViewById(R.id.phone_statusbar_icon_battery))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "evaphone")).into(a.findViewById(R.id.phone_smsscreen_background))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ic_arrow_left_white")).into(a.findViewById(R.id.phone_smsscreen_button_back_image))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "parents_profil")).into(a.findViewById(R.id.phone_smsscreen_currentconversation_image))
        // rm.load(R.drawable.ico_info).into(a.findViewById(R.id.main_header_icons_info))
        // rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "friendzone).apply(requestOptions).into(a.findViewById(R.id.main_lost))
    }

    /**
     * Sets the conversation's status
     * @param activity : Activity
     * @param contact : Contact
     * @param text : String
     */
    fun setConversationStatus(activity: Activity, contact: SmsScreenModel.Contact, text: String) {
        val status = getViewFromContact(activity, contact).findViewById<TextView>(R.id.preview_message_preview)
        status.text = text
    }

    /**
     * Sets the conversation's name
     * @param activity : Activity
     * @param contact : Contact
     * @param text : String
     */
    fun setConversationName(activity: Activity, contact: SmsScreenModel.Contact, text: String) {
        val status = getViewFromContact(activity, contact).findViewById<TextView>(R.id.preview_name)
        status.text = text
    }

    /**
     * Sets the preview's image
     * @param activity : Activity
     * @param requestManager : RequestManager
     * @param contact : Contact
     * @param pictureId : Int
     */
    fun setConversationImage(activity: Activity, requestManager: RequestManager, contact: SmsScreenModel.Contact, pictureId: String) {
        val image = getViewFromContact(activity, contact).findViewById<ImageView>(R.id.preview_avatar)
        requestManager.load(OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id, pictureId)).into(image)
    }

    /**
     * Returns the view from the contact
     * @param activity : Activity
     * @parma contact : Contact
     * @return View
     */
    private fun getViewFromContact(activity: Activity, contact: SmsScreenModel.Contact): View {
        return when (contact) {
            SmsScreenModel.Contact.KLOMOBILE -> activity.findViewById<View>(R.id.phone_smsscreen_preview_klomobile)
            SmsScreenModel.Contact.CHRISTOPHE -> activity.findViewById<View>(R.id.phone_smsscreen_preview_christophe)
            SmsScreenModel.Contact.ZOE -> activity.findViewById<View>(R.id.phone_smsscreen_preview_zoe)
            SmsScreenModel.Contact.SYLVIE -> activity.findViewById<View>(R.id.phone_smsscreen_preview_sylvie)
            SmsScreenModel.Contact.LUCIE -> activity.findViewById<View>(R.id.phone_smsscreen_preview_lucie)
            SmsScreenModel.Contact.BRYAN -> activity.findViewById<View>(R.id.phone_smsscreen_preview_bryan)
            SmsScreenModel.Contact.OTHER_1 -> activity.findViewById<View>(R.id.phone_smsscreen_preview_other1)
            SmsScreenModel.Contact.OTHER_2 -> activity.findViewById<View>(R.id.phone_smsscreen_preview_other2)
        }
    }

    /**
     * Displays a page
     * @param activity : Activity
     * @param page : Page
     * @return page : Page
     */
    fun display(activity: Activity, page: SmsScreenModel.Page) : SmsScreenModel.Page {
        when (page) {
            SmsScreenModel.Page.CONVERSATION -> {
                SmsScreenModel.Contact.values().forEach {
                    getViewFromContact(activity, it).visibility = View.INVISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_recyclerview).visibility = View.VISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_currentconversation_image).visibility = View.VISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_currentconversation_name).visibility = View.VISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_bottomarea).visibility = View.INVISIBLE
                }
            }

            SmsScreenModel.Page.PREVIEWS -> {
                SmsScreenModel.Contact.values().forEach {
                    getViewFromContact(activity, it).visibility = View.VISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_recyclerview).visibility = View.INVISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_currentconversation_image).visibility = View.INVISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_currentconversation_name).visibility = View.INVISIBLE
                    activity.findViewById<View>(R.id.phone_smsscreen_bottomarea).visibility = View.VISIBLE
                }
            }
        }
        return page
    }

    /**
     * Updates the current conversation's image
     * @param activity : Activity
     * @param imageId : Int
     */
    fun setCurrentConversationImage(activity: Activity, requestManager : RequestManager, imageId: String) {
        val image = activity.findViewById<ImageView>(R.id.phone_smsscreen_currentconversation_image)
        requestManager.load(imageId).into(image)
    }

    /**
     * Updates the current conversation's image
     * @param activity : Activity
     * @param imageId : Int
     */
    fun setCurrentConversationName(activity: Activity, name: String) {
        val textview = activity.findViewById<TextView>(R.id.phone_smsscreen_currentconversation_name)
        textview.text = name
    }

    /**
     * Sets the recyclerview
     * @param activity : Activity
     * @param adapter : GameConversationAdapter
     */
    fun setRecyclerView(activity : Activity, adapter : GameConversationAdapter) {
        val recyclerView  = activity.findViewById<RecyclerView>(R.id.phone_smsscreen_recyclerview)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.setItemViewCacheSize(20)
        val lLayout = LinearLayoutManager(activity)
        lLayout.stackFromEnd = false
        recyclerView.addItemDecoration(
                GridSpacingItemDecoration(1,
                        Math.round(Measure.percent(Measure.Type.HEIGHT,
                                1.5f,
                                activity.windowManager.defaultDisplay
                        )), true, 0))
        recyclerView.layoutManager = lLayout
    }

    /**
     * Sets the preview's visibility
     * @param activity : Activity
     * @param contact : SmsScreenModel.Contact
     * @param isVisible : Boolean
     */
    fun previewVisibility(activity : Activity, contact: SmsScreenModel.Contact, isVisible : Boolean) {
        getViewFromContact(activity, contact).visibility = if(isVisible) View.VISIBLE else View.GONE
    }
}
