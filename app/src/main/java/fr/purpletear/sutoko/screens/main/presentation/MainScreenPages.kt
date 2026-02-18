package fr.purpletear.sutoko.screens.main.presentation

sealed class MainScreenPages(val route: String) {
    object SplashScreen : MainScreenPages("splash")
    object GamePreview : MainScreenPages("game_preview/{gameId}") {
        fun createRoute(gameId: Int): String {
            return "game_preview/$gameId"
        }
    }

    object Chapters : MainScreenPages("chapters/{gameId}") {
        fun createRoute(gameId: Int): String {
            return "chapters/$gameId"
        }
    }

    object Home : MainScreenPages("home")
    object Create : MainScreenPages("create")
    object AiConversationPreview : MainScreenPages("ai_conversation_preview")

}
