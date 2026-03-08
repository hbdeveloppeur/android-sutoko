package com.purpletear.game.presentation.smsgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sharedelements.theme.SutokoTheme
import com.purpletear.game.presentation.components.HideStatusBarEffect
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
                HideStatusBarEffect()
                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

                SmsGameScreen(
                    sessionState = sessionState,
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
