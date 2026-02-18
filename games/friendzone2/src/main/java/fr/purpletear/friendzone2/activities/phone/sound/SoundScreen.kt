package fr.purpletear.friendzone2.activities.phone.sound

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.Finger

class SoundScreen : AppCompatActivity() {

    /**
     * Handles the model settings
     * @see SoundScreenModel
     */
    private lateinit var model: SoundScreenModel

    /**
     * Handles the graphic settings
     * @see SoundScreenGraphics
     */
    private lateinit var graphics: SoundScreenGraphics

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_soundscreen)
        load()
        listeners()
    }

    override fun onStart() {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onStart")
        super.onStart()
    }

    override fun onBackPressed() {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onBackPressed")
        super.onBackPressed()
    }

    override fun onResume() {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onDestroy")
        super.onDestroy()
    }

    override fun onPause() {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onPause")
        model.stopSound()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.d("purpletearDebug", "[STATE] SoundScreen : onWindowFocusChanged ()")
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics()
        }
    }

    private fun listeners() {
        Finger.registerListener(this, R.id.phone_soundscreen_button_sound,  ::onSoundPressed)
        Finger.registerListener(this, R.id.phone_soundscreen_button_back_button,  ::onBackPressed)
    }

    private fun onSoundPressed() {
        model.updateSound(this)
        graphics.updateButton(this, model.currentSoundState, model.requestManager)
    }

    /**
    * Inits the Activity's vars
    */
    private fun load() {
        model = SoundScreenModel(this, Glide.with(this))
        graphics = SoundScreenGraphics()
    }

    /**
     * Sets initial graphics settings
     */
    private fun graphics() {
        graphics.setImages(this, model.requestManager)
    }
}
