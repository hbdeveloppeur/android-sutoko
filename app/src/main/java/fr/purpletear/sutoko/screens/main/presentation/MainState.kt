package fr.purpletear.sutoko.screens.main.presentation

import androidx.annotation.Keep
import com.purpletear.sutoko.game.model.game.GameCatalog
import fr.purpletear.sutoko.objects.CalendarEvent

@Keep
data class MainState(
    val initialStories: List<GameCatalog>,
    val events: List<CalendarEvent>,
    var notificationsOn: Boolean = false,
    var isLoading: Boolean = false,
    var isLoadingMoreStories: Boolean = false,
)

