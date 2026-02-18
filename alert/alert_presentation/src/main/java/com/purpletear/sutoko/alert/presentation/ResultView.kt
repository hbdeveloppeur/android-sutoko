package com.purpletear.sutoko.alert.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.sutoko.alert.presentation.enum.Result

@Composable
fun ResultView(text: String, result: Result = Result.SUCCESS) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(result.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Bold text
        Text(
            text,
            fontSize = 13.sp,
            color = result.textColor, fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
@Preview
private fun ResultPreview() {
    Column {
        ResultView(
            "I love cooking and this is a very long text to test the wrapping of the text",
            Result.SUCCESS
        )
        ResultView("Error", Result.ERROR)
        ResultView("Warning", Result.WARNING)
    }
}