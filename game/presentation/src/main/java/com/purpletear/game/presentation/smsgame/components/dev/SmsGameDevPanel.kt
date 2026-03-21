package com.purpletear.game.presentation.smsgame.components.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SmsGameDevPanel(onAction: (SmsGameDevAction) -> Unit = {}) {
    Box(Modifier.fillMaxWidth().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(text = "< Back", onClick = {
                    onAction(SmsGameDevAction.Back)
                }, modifier = Modifier.weight(1f))
                Button(text = "Open debug page", onClick = {
                    onAction(SmsGameDevAction.OpenDebugView)
                }, modifier = Modifier.weight(2f))
            }
        }
    }
}

@Composable
private fun Button(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.background(Color.DarkGray).height(32.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}