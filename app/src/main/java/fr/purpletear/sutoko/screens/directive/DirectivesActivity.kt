package fr.purpletear.sutoko.screens.directive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.purpletear.sutoko.databinding.ActivityDirectivesBinding
import purpletear.fr.purpleteartools.FingerV2

class DirectivesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDirectivesBinding
    private lateinit var model: DirectivesActivityModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDirectivesBinding.inflate(layoutInflater)
        model = DirectivesActivityModel(this)
        setContentView(binding.root)
        setListeners()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart) {
            DirectivesActivityGraphics.set(this.binding, model.requestManager)
        }
    }

    private fun onButtonPressed() {
        this.finish()
    }

    private fun setListeners() {
        FingerV2.register(binding.sutokoDirectivesButtonOkay, null, ::onButtonPressed)
    }
}