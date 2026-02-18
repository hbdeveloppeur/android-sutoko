package com.example.sutokosharedelements.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sutokosharedelements.R

// Define PlayFair Display font family
val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal)
)

// Define PlusJakartaSans font family
val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_regular, FontWeight.Normal)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    h1 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_bold, FontWeight.Bold)),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_semibold, FontWeight.SemiBold)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_medium, FontWeight.Medium)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    h4 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_regular, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    h5 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_regular, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    h6 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_regular, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_regular, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = FontFamily(Font(R.font.font_poppins_regular, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
)
