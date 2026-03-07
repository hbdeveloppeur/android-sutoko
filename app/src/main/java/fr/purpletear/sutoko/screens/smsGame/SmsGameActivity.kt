package fr.purpletear.sutoko.screens.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharedelements.theme.SutokoTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.purpletear.sutoko.game.model.ErrorType
import com.purpletear.sutoko.game.model.GameSessionState
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for playing SMS-style games.
 * Receives the game parameters via intent extras using [SmsGameActivityModel].
 */
@AndroidEntryPoint
class SmsGameActivity : AppCompatActivity() {

    private val viewModel: SmsGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model = SmsGameActivityModel.fromIntent(this)
        if (model == null) {
            Log.e(TAG, "SmsGameActivityModel not found in intent extras")
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            SutokoTheme {
                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

                SmsGameScreen(
                    sessionState = sessionState,
                    onRetry = { viewModel.initialize(model.gameId, model.isGranted) }
                )
            }
        }

        viewModel.initialize(model.gameId, model.isGranted)
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

