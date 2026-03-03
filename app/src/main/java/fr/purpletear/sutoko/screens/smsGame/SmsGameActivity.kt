package fr.purpletear.sutoko.screens.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sharedelements.theme.SutokoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for playing SMS-style games.
 * Receives the game parameters via intent extras using [SmsGameActivityModel].
 */
@AndroidEntryPoint
class SmsGameActivity : AppCompatActivity() {

    private lateinit var model: SmsGameActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extractedModel = SmsGameActivityModel.fromIntent(this)
        if (extractedModel == null) {
            Log.e(TAG, "SmsGameActivityModel not found in intent extras")
            finish()
            return
        }
        model = extractedModel

        setContent {
            SutokoTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Game: ${model.gameId}",
                        color = Color.White
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "SmsGameActivity"

        /**
         * Creates an Intent to launch SmsGameActivity with the specified parameters.
         *
         * @param activity The activity to use for creating the intent
         * @param model The model containing game parameters
         * @return An Intent configured to launch SmsGameActivity
         */
        fun require(activity: Activity, model: SmsGameActivityModel): Intent {
            return Intent(activity, SmsGameActivity::class.java).apply {
                putExtra(SmsGameActivityModel.extraKey, model)
            }
        }
    }
}
