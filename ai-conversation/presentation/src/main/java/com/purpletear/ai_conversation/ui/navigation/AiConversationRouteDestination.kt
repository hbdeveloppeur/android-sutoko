package com.purpletear.ai_conversation.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class AiConversationRouteDestination(
    val route: String,
    val destination: String = route,
    val namedNavArgument: NamedNavArgument? = null
) {

    data object Home : AiConversationRouteDestination(route = "ai-conversation/home")
    data object Conversation :
        AiConversationRouteDestination(route = "conversation/{character_id}",
            namedNavArgument = navArgument("character_id")
            { type = NavType.IntType })

    data object AddCharacter : AiConversationRouteDestination(route = "add_character")
    data object GenerateImage : AiConversationRouteDestination(route = "generate_image")
    data class ImageViewer(val url: String = "url") :
        AiConversationRouteDestination(
            route = "image_viewer/{url}",
            destination = "image_viewer/$url",
            namedNavArgument = navArgument("url")
            { type = NavType.StringType }
        )
}