package com.purpletear.game_presentation.components

import androidx.compose.ui.graphics.Color

/**
 * Predefined gradient themes for use with AnimatedGradientBorderBox.
 * Each theme contains a list of gradient color lists that create different visual effects
 * when animated.
 */
object GradientThemes {
    
    /**
     * Warm theme with vibrant reds, oranges, and yellows.
     * Creates a fiery, energetic appearance.
     */
    val Warm = listOf(
        listOf(Color(0xFFFF5722), Color(0xFFFFEB3B), Color.Transparent),
        listOf(Color(0xFFFF9800), Color(0xFFFF5722), Color.Transparent),
        listOf(Color(0xFFFFEB3B), Color(0xFFFF9800), Color.Transparent),
        listOf(Color(0xFFFF5722), Color.Transparent, Color.Transparent),
        listOf(Color(0xFFFF9800), Color.Transparent, Color.Transparent),
        listOf(Color(0xFFFFEB3B), Color.Transparent, Color.Transparent)
    )
    
    /**
     * Cool theme with blues, purples, and cyans.
     * Creates a calm, soothing appearance.
     */
    val Cool = listOf(
        listOf(Color(0xFF2196F3), Color(0xFF9C27B0), Color.Transparent),
        listOf(Color(0xFF00BCD4), Color(0xFF2196F3), Color.Transparent),
        listOf(Color(0xFF9C27B0), Color(0xFF00BCD4), Color.Transparent),
        listOf(Color(0xFF2196F3), Color.Transparent, Color.Transparent),
        listOf(Color(0xFF00BCD4), Color.Transparent, Color.Transparent),
        listOf(Color(0xFF9C27B0), Color.Transparent, Color.Transparent)
    )
    
    /**
     * Nature theme with greens, browns, and yellows.
     * Creates an organic, natural appearance.
     */
    val Nature = listOf(
        listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color.Transparent),
        listOf(Color(0xFF795548), Color(0xFFCDDC39), Color.Transparent),
        listOf(Color(0xFF8BC34A), Color(0xFF795548), Color.Transparent),
        listOf(Color(0xFF4CAF50), Color.Transparent, Color.Transparent),
        listOf(Color(0xFFCDDC39), Color.Transparent, Color.Transparent),
        listOf(Color(0xFF795548), Color.Transparent, Color.Transparent)
    )
    
    /**
     * The original default theme from AnimatedGradientBorderBox.
     * Includes yellow, pink, and blue gradients.
     */
    val Original = listOf(
        listOf(Color(0xFFFECF00), Color.Transparent),
        listOf(Color(0xFFFECF00), Color(0xFFFF7FDF), Color.Transparent),
        listOf(Color(0xFFFECF00), Color(0xFFFF7FDF), Color(0xFF5D8BFF), Color.Transparent),
        listOf(Color(0xFFFECF00), Color(0xFFFF7FDF), Color.Transparent, Color.Transparent),
        listOf(Color(0xFFFECF00), Color.Transparent, Color.Transparent, Color.Transparent),
        listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent)
    )
}