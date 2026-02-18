package com.purpletear.sutoko.alert.presentation.enum

import androidx.compose.ui.graphics.Color

val successColor = Color(0xFF1AC521)
val errorColor = Color(0xFFFA4033)
val warningColor = Color(0xFFF0B70E)

val successTextColor = Color.White
val errorTextColor = Color.White
val warningTextColor = Color.Black

sealed class Result(val backgroundColor: Color, val textColor: Color) {
    object SUCCESS : Result(successColor, successTextColor)
    object ERROR : Result(errorColor, errorTextColor)
    object WARNING : Result(warningColor, warningTextColor)
}