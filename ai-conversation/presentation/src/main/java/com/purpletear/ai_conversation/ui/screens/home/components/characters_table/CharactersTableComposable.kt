package com.purpletear.ai_conversation.ui.screens.home.components.characters_table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.purpletear.ai_conversation.domain.enums.Visibility
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.ui.screens.home.components.character_row.CharacterRowComposable

@Composable
internal fun CharactersTableComposable(
    characters: List<AiCharacter>,
    navHostController: NavHostController,
    viewModel: CharactersTableViewModel = hiltViewModel(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        characters.filter { it.visibility == Visibility.Private }.forEach {
            CharacterRowComposable(
                character = it,
                onClickDelete = {
                    viewModel.deleteCharacter(it)
                },
                isDeleting = viewModel.loadingUsers.value.contains(it)
            )
        }

        SexyButton(
            modifier = Modifier.padding(top = 12.dp),
            text = "Créer un nouveau personnage",
            isLoading = false,
            onClick = {
                navHostController.navigate("add_character")
            },
            backgroundColor = Color(0xFF3D1C9E),
        )
    }
}


@Composable
fun SexyButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    backgroundColor: Color = Color(0xFF7649FA)
) {
    Box(
        Modifier
            .then(modifier)
            .widthIn(max = 320.dp)
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = !isLoading,
                onClick = onClick
            )
            .background(backgroundColor)
            .border(.5.dp, Color.White.copy(0.1f), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center)
                    .alpha(1f),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}