package fr.purpletear.friendzone2.activities.choice

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import fr.purpletear.friendzone2.R
import java.lang.IllegalStateException

class Choice : AppCompatActivity() {
    private lateinit var graphics : ChoiceGraphics
    private lateinit var model : ChoiceModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
        load()
        model.listeners(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics.setImages(this, Glide.with(this))
        }
    }

    private fun load() {
        model = ChoiceModel(intent.getParcelableExtra("symbols") ?: throw IllegalStateException())
        graphics = ChoiceGraphics()
    }
}
