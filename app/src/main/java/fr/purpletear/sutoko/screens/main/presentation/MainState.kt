package fr.purpletear.sutoko.screens.main.presentation

import androidx.annotation.Keep
import com.purpletear.smsgame.activities.smsgame.objects.Story
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.custom.PlayerRankInfo
import fr.purpletear.sutoko.objects.CalendarEvent

@Keep
data class MainState(
    val initialStories: List<Game>,
    val initialUserStories: List<Story>,
    val userStories: List<Story>,
    val events: List<CalendarEvent>,
    var isPopUpDisplayed: Boolean = false,
    var popUp: fr.purpletear.sutoko.popup.domain.PopUp? = null,
    var notificationsOn: Boolean = false,
    var authorsRank: List<PlayerRankInfo>,
    var isLoading: Boolean = false,
    var isLoadingMoreStories: Boolean = false,
    var userStoriesSearchIsClearable: Boolean = false,
)

