package fr.purpletear.sutoko.screens.main.presentation.screens.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sutokosharedelements.theme.SutokoTypography
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.main.domain.popup.util.MainMenuCategory

/**
 * A composable that displays the category menu with three options: All, Free, and Premium.
 *
 * @param currentCategory The currently selected category
 * @param onTap Callback invoked when a category is selected
 * @param modifier Modifier to be applied to the layout
 */
@Composable
fun Menu(
    currentCategory: MainMenuCategory,
    onTap: (MainMenuCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .align(alignment = Alignment.Center)
        ) {
            MenuButton(
                text = stringResource(R.string.sutoko_menu_games),
                image = R.drawable.sutoko_ic_games,
                isSelected = currentCategory == MainMenuCategory.All,
                onClick = { onTap(MainMenuCategory.All) }
            )
            MenuButton(
                text = stringResource(R.string.sutoko_menu_free),
                isSelected = currentCategory == MainMenuCategory.Free,
                onClick = { onTap(MainMenuCategory.Free) }
            )
            MenuButton(
                text = stringResource(R.string.sutoko_menu_premium),
                isSelected = currentCategory == MainMenuCategory.New,
                onClick = { onTap(MainMenuCategory.New) }
            )
        }
    }
}

/**
 * Individual menu button composable.
 *
 * @param text The button text
 * @param image Optional image resource ID to display next to text
 * @param isSelected Whether this button is currently selected
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Modifier to be applied to the layout
 */
@Composable
private fun MenuButton(
    text: String,
    image: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp * 0.94f
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1F else 0.55F,
        label = "MenuButtonAnimation"
    )

    Box(
        modifier = modifier
            .width(screenWidth / 3)
            .alpha(alpha)
            .drawBehind {
                val borderSize = 1.dp.toPx()
                drawLine(
                    Color.LightGray,
                    Offset(0f, size.height),
                    Offset(size.width, size.height),
                    borderSize
                )
            }
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(alignment = Alignment.Center)
        ) {
            Text(
                text = text.uppercase(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.font_poppins_semibold, FontWeight.SemiBold)),
                style = SutokoTypography.h3.copy(
                    letterSpacing = 0.5.sp,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )

            if (image != null) {
                Image(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 4.dp, bottom = 2.dp),
                    painter = painterResource(image),
                    contentDescription = null
                )
            }
        }
    }
}
