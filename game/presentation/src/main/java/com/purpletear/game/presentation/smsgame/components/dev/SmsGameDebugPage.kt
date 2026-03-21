package com.purpletear.game.presentation.smsgame.components.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.purpletear.game.presentation.smsgame.SmsGameRoutes
import com.purpletear.sutoko.game.model.GameSessionState


internal fun NavGraphBuilder.debugPage(
    gameId: String,
    gameSessionState: GameSessionState,
    memories: Map<String, String>
) = composable(
    route = SmsGameRoutes.DEBUG,
    arguments = listOf(navArgument("gameId") { type = NavType.StringType })
) {
    SmsGameDebugPage(
        gameSessionState = gameSessionState,
        gameId = gameId,
        memories = memories
    )
}

@Composable
private fun SmsGameDebugPage(
    gameId: String,
    gameSessionState: GameSessionState,
    memories: Map<String, String>,
) {
    val chapter = (gameSessionState as? GameSessionState.Ready)?.chapter

    DebugColumn {
        DebugTitle("Game debug")
        DebugSubtitle(text = "Game identifier", value = gameId)
        DebugSubtitle(
            text = "Game session state",
            value = gameSessionState.toDisplayString()
        )
        DebugTitle("Chapter")
        DebugSubtitle(
            text = "Chapter identifier",
            value = chapter?.normalizedCode ?: "ND"
        )
        DebugSubtitle(
            text = "Chapter code",
            value = chapter?.id ?: "ND"
        )
        DebugTitle("Memories (${memories.size})")
        if (memories.isEmpty()) {
            DebugSubtitle(text = "No memories", value = "")
        } else {
            memories.toSortedMap().forEach { (key, value) ->
                DebugSubtitle(text = key, value = value)
            }
        }
    }
}

private fun GameSessionState.toDisplayString(): String = when (this) {
    is GameSessionState.Loading -> "Loading..."
    is GameSessionState.Error -> "Error: ${type.name}"
    is GameSessionState.Ready -> "Ready"
}

@Composable
private fun DebugTitle(text: String) {
    Text(text = text, fontSize = 12.sp, color = Color(0xFFFFA500))
}

@Composable
private fun DebugSubtitle(text: String, value: String) {
    Row(
        Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = "→",
            fontSize = 12.sp,
            color = Color(0xFFFFA500),
            modifier = Modifier.graphicsLayer {
                translationY = -(7f)
            })
        Text(text = text, fontSize = 11.sp, color = Color.White)
        Text(text = ":", fontSize = 11.sp, color = Color.White)
        Box(
            Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFFFA500).copy(0.05f))
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clipToBounds()
            )
        }
    }
}

@Composable
private fun DebugColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.padding(12.dp),
        content = content,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    )
}