package com.purpletear.ai_conversation.ui.component.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CircularOptionsTab(
    modifier: Modifier = Modifier,
    items: Map<Int, String>,
    selectedItemId: Int,
    onClick: (Int) -> Unit
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(0.92f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items.forEach { (id, text) ->
            CircularOption(
                modifier = Modifier,
                text,
                isSelected = id == selectedItemId,
                onClick = { onClick(id) }
            )
        }
    }
}