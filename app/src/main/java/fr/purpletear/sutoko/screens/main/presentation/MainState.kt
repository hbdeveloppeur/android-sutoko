package fr.purpletear.sutoko.screens.main.presentation

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.objects.CalendarEvent

@Keep
data class MainState(
    val initialStories: List<Game>,
    val events: List<CalendarEvent>,
    var isPopUpDisplayed: Boolean = false,
    var popUp: fr.purpletear.sutoko.popup.domain.PopUp? = null,
    var notificationsOn: Boolean = false,
    var isLoading: Boolean = false,
    var isLoadingMoreStories: Boolean = false,
)

