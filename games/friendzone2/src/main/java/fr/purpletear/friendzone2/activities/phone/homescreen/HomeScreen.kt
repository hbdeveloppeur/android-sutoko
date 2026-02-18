package fr.purpletear.friendzone2.activities.phone.homescreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.Finger
import purpletear.fr.purpleteartools.Std
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import fr.purpletear.friendzone2.activities.phone.photos.PhotosScreen
import fr.purpletear.friendzone2.activities.phone.sms.SmsScreen
import fr.purpletear.friendzone2.activities.phone.sound.SoundScreen
import fr.purpletear.friendzone2.tables.Character
import purpletear.fr.purpleteartools.Runnable2
import java.lang.IllegalStateException


class HomeScreen : AppCompatActivity() {

    /**
     * Handles the model settings
     * @see HomeScreenModel
     */
    private lateinit var model: HomeScreenModel

    /**
     * Handles the graphic settings
     * @see HomeScreenGraphics
     */
    private lateinit var graphics: HomeScreenGraphics

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("graphics", graphics)
        outState.putBoolean("signalActivated", model.signalActivated)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        graphics = savedInstanceState.getParcelable("graphics")!!
        model.signalActivated = savedInstanceState.getBoolean("signalActivated")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_homescreen)
        load()
    }

    override fun onStart() {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onStart")
        super.onStart()
    }

    override fun onBackPressed() {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onBackPressed")
        Std.confirm(
                Character.updateNames(this, getString(R.string.close_eva_phone_confirm)),
                getString(R.string.yes),
                getString(R.string.no),
                {
                    super.onBackPressed()
                }, {} ,this
        )
    }

    override fun onResume() {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onDestroy")
        super.onDestroy()
    }

    override fun onPause() {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onPause")
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.d("purpletearDebug", "[STATE] HomeScreen : onWindowFocusChanged ()")
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics()
            listeners()
        }
    }

    /**
    * Inits the Activity's vars
    */
    private fun load() {
        model = HomeScreenModel(Glide.with(this), intent.getParcelableExtra("symbols") ?: throw IllegalStateException())
        graphics = HomeScreenGraphics()
    }

    private fun listeners() {
        Finger.registerListener(this, R.id.phone_homescreen_button_call, ::onCallPressed)
        Finger.registerListener(this, R.id.phone_homescreen_button_back, ::onBackPressed)
        Finger.registerListener(this, R.id.phone_homescreen_button_pictures, ::onPhotosPressed)
        Finger.registerListener(this, R.id.phone_homescreen_button_music, ::onMusicPressed)
        Finger.registerListener(this, R.id.phone_homescreen_button_sms, ::onSmsPressed)


        val s = findViewById<Switch>(R.id.phone_homescreen_switch)
        s.setOnCheckedChangeListener { _, isCheck ->
            graphics.setSwitchVisibility(this@HomeScreen, false)
            turnSignalOn()
            model.signalActivated = true
            s.setOnCheckedChangeListener(null)
        }
    }

    private fun onPhotosPressed() {
        val i = Intent(this, PhotosScreen::class.java)
        startActivity(i)
    }

    private fun onMusicPressed() {
        val i = Intent(this, SoundScreen::class.java)
        startActivity(i)
    }

    private fun onSmsPressed() {
        graphics.setSmsNotificationVisibility(this, false)
        val i = Intent(this, SmsScreen::class.java)
        i.putExtra("signalActivated", model.signalActivated)
        startActivity(i)
    }

    private fun onCallPressed() {
        model.playSound(this, HomeScreenModel.Sound.WRONG)
    }

    /**
     * Sets initial graphics settings
     */
    private fun graphics() {
        graphics.setImages(this, model.requestManager)
        graphics.percentGraphics(this)

        graphics.setSwitchVisibility(this)
        graphics.setSignalIconVisibility(this)
        graphics.setSmsNotificationVisibility(this)

        if(model.symbols.chapterNumber == 8) {
            model.signalActivated = true
            graphics.setSwitchVisibility(this, false)
            graphics.setSignalIconVisibility(this, false)
        }
    }

    private fun turnSignalOn() {
        val runnable = object : Runnable2("turnSignalOn", 1000) {
            override fun run() {
                graphics.setSignalIconVisibility(this@HomeScreen, false)
                model.playSound(this@HomeScreen, HomeScreenModel.Sound.NOTIFICATION)
                graphics.setSmsNotificationVisibility(this@HomeScreen, true)
            }
        }
        model.mh.push(runnable)
        model.mh.run(runnable)
    }
}
