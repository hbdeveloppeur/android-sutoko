package fr.purpletear.sutoko.screens.account.screen.model

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Game

@Keep
data class GameWithOwnership(
    val card: Game,
    val isPossessed: Boolean
)