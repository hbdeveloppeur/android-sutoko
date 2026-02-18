package fr.purpletear.friendzone2.activities.phone.sms

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.activities.main.GameConversationAdapter
import fr.purpletear.friendzone2.activities.main.MainInterface
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.TableOfCharacters
import purpletear.fr.purpleteartools.Finger
import purpletear.fr.purpleteartools.GlobalData

class SmsScreen : AppCompatActivity(), MainInterface {
    override fun onClickChoice(p: Phrase) {

    }

    override fun onInsertPhrase(position: Int, isSmoothScroll: Boolean) {
    }

    override fun onClickSound(name: String) {
    }

    /**
     * Handles the model settings
     * @see SmsScreenModel
     */
    private lateinit var model: SmsScreenModel

    /**
     * Handles the graphic settings
     * @see SmsScreenGraphics
     */
    private lateinit var graphics: SmsScreenGraphics

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_smsscreen_)
        load()
        listeners()
    }

    override fun onStart() {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onStart")
        super.onStart()
    }

    override fun onBackPressed() {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onBackPressed")
        if(model.currentPage == SmsScreenModel.Page.CONVERSATION) {
            model.currentPage = graphics.display(this, SmsScreenModel.Page.PREVIEWS)
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onDestroy")
        super.onDestroy()
    }

    override fun onPause() {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onPause")
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.d("purpletearDebug", "[STATE] SmsScreen : onWindowFocusChanged ()")
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics()
        }
    }

    private fun listeners() {
        Finger.registerListener(this, R.id.phone_smsscreen_button_back_button, ::onBackPressed)
        Finger.registerListener(this, R.id.phone_smsscreen_preview_klomobile, ::onKloMobilePressed)
        Finger.registerListener(this, R.id.phone_smsscreen_preview_christophe, ::onChristophePressed)
        Finger.registerListener(this, R.id.phone_smsscreen_preview_zoe, ::onZoePressed)
        Finger.registerListener(this, R.id.phone_smsscreen_preview_sylvie, ::onSylviePressed)
        Finger.registerListener(this, R.id.phone_smsscreen_preview_lucie, ::onLuciePressed)
        Finger.registerListener(this, R.id.phone_smsscreen_preview_bryan, ::onBryanPressed)
    }

    /**
     * Inits the Activity's vars
     */
    private fun load() {
        model = SmsScreenModel(this, Glide.with(this), intent.getBooleanExtra("signalActivated", false))
        val adapter = GameConversationAdapter(this, ArrayList(), TableOfCharacters(this, SmsScreenModel.getChapterCodeFromContact(SmsScreenModel.Contact.ZOE)), model.requestManager, this)
        adapter.backgroundMediaId = OnlineAssetsManager.getImageFilePath(this, GlobalData.Game.FRIENDZONE2.id, "evaphone")
        model.adapter = adapter
        model.adapter.isPhoneMode = true
        graphics = SmsScreenGraphics()
        graphics.setRecyclerView(this, model.adapter)
    }

    /**
     * Sets initial graphics settings
     */
    private fun graphics() {
        graphics.setImages(this, model.requestManager)

        SmsScreenModel.Contact.values().forEach {
            setConversationName(it)
            setConversationStatus(it)
            setConversationImage(it)
        }

        graphics.previewVisibility(this, SmsScreenModel.Contact.KLOMOBILE, model.signalActivated)
    }

    /**
     * Updates the conversation's status
     * @param contact : Contact
     */
    private fun setConversationStatus(contact: SmsScreenModel.Contact) {
        graphics.setConversationStatus(this, contact, model.getConversationStatus(this, contact, model.symbols))
    }

    /**
     * Updates the conversation's status
     * @param contact : Contact
     */
    private fun setConversationName(contact: SmsScreenModel.Contact) {
        graphics.setConversationName(this, contact, model.getConversationName(this, contact))
    }

    /**
     * Updates the conversation's image
     * @param contact : Contact
     */
    private fun setConversationImage(contact: SmsScreenModel.Contact) {
        graphics.setConversationImage(this, model.requestManager, contact, model.getConversationPicture(contact))
    }

    private fun onKloMobilePressed() {
        displayConversation(SmsScreenModel.Contact.KLOMOBILE)
    }

    private fun onChristophePressed() {
        displayConversation(SmsScreenModel.Contact.CHRISTOPHE)
    }

    private fun onZoePressed() {
        displayConversation(SmsScreenModel.Contact.ZOE)
    }

    private fun onSylviePressed() {
        displayConversation(SmsScreenModel.Contact.SYLVIE)
    }

    private fun onLuciePressed() {
        displayConversation(SmsScreenModel.Contact.LUCIE)
    }

    private fun onBryanPressed() {
        displayConversation(SmsScreenModel.Contact.BRYAN)
    }

    /**
     * Displays a conversation
     * @param contact : Contact
     */
    private fun displayConversation(contact : SmsScreenModel.Contact) {
        model.currentPage = graphics.display(this, SmsScreenModel.Page.CONVERSATION)
        graphics.setCurrentConversationImage(this, model.requestManager, model.getConversationPicture(contact))
        graphics.setCurrentConversationName(this, model.getConversationName(this, contact))
        model.adapter.characters = TableOfCharacters(this, SmsScreenModel.getChapterCodeFromContact(contact))
        model.discuss(this, contact, model.symbols)
    }
}
