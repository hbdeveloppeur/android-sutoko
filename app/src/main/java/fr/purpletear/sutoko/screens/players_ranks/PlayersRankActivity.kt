package fr.purpletear.sutoko.screens.players_ranks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import com.example.sutokosharedelements.Data
import com.example.sutokosharedelements.theme.SutokoTheme
import fr.purpletear.sutoko.screens.players_ranks.presentation.PlayersRankScreen
import fr.purpletear.sutoko.screens.players_ranks.presentation.PlayersRankViewModel
import fr.purpletear.sutoko.custom.PlayerRankInfo

@AndroidEntryPoint
class PlayersRankActivity : AppCompatActivity() {


    private val viewModel: PlayersRankViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Compose
        setContent {
            SutokoTheme {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    PlayersRankScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    companion object {
        fun require(intent: Intent, playersRanks: ArrayList<PlayerRankInfo>): Intent {
            intent.putExtra(
                Data.Companion.Extra.PLAYER_RANK_LIST.id,
                playersRanks
            )
            return intent
        }
    }
}