package fr.purpletear.sutoko.util

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import fr.purpletear.sutoko.R

/**
 * Utility class to provide resources for GameCard component
 */
object GameCardResources {
    
    /**
     * Get the premium icon painter for GameCard
     */
    @Composable
    fun getPremiumIconPainter(): Painter {
        return painterResource(id = R.drawable.sutoko_ic_dollar_coin)
    }
    
    /**
     * Get the bold font family for GameCard
     */
    @Composable
    fun getBoldFontFamily(): FontFamily {
        return FontFamily(Font(R.font.font_poppins_bold, FontWeight.Bold))
    }
}