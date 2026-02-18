package com.example.sutokosharedelements.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sutokosharedelements.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = Color.Black,
)


val Poppins = FontFamily(
    Font(R.font.font_poppins_regular, FontWeight.Normal),
    Font(R.font.font_poppins_medium, FontWeight.Medium),
    Font(R.font.font_poppins_semibold, FontWeight.SemiBold),
    Font(R.font.font_poppins_bold, FontWeight.Bold),
    Font(R.font.font_poppins_extrabold, FontWeight.ExtraBold)
)

val Boldonse = FontFamily(
    Font(R.font.boldonse_regular, FontWeight.Bold),
)


val SutokoTypography = Typography(
    body1 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        color = Color.White,
    ),
    body2 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        color = Color.White,
    ),
    h1 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        color = Color.White,
    ),
    h2 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        color = Color.White,
    ),
    h3 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        color = Color.White,
    ),
)


@Composable
fun SutokoTheme(content: @Composable () -> Unit) {
    val colors = DarkColorPalette
    val systemUiController = rememberSystemUiController()
    systemUiController.setNavigationBarColor(Color(0xFF070509))
    systemUiController.setStatusBarColor(Color.Transparent)
    MaterialTheme(
        colors = colors,
        typography = SutokoTypography,
        shapes = Shapes,
        content = content
    )
}