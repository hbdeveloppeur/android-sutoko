package com.purpletear.game.presentation.game_catalog

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.CrimsonTextFontFamily
import com.purpletear.game.presentation.BuildConfig
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.common.components.GameLogo
import com.purpletear.sutoko.game.model.game.GameCatalog
import kotlin.math.abs
import kotlin.math.roundToInt

/** Whitened green for the "new chapters soon" label replacing the themes. */
private val NewChaptersSoonGreen = Color(0xFF90EE90)

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    gameCatalog: GameCatalog,
    isFavorite: Boolean = false,
    hasNewChaptersSoon: Boolean = false,
    onTap: (GameCatalog) -> Unit,
) {
    val themes = remember(gameCatalog.narrativeThemes) {
        gameCatalog.narrativeThemes.map { it.name }
    }
    val newChaptersSoonLabel =
        stringResource(R.string.game_presentation_game_card_new_chapters_soon)
    // The title comes from the logo asset, so TalkBack needs it explicitly.
    val description = remember(
        gameCatalog.metadata.title,
        themes,
        hasNewChaptersSoon,
        newChaptersSoonLabel,
    ) {
        buildString {
            append(gameCatalog.metadata.title)
            if (hasNewChaptersSoon) {
                append(". ")
                append(newChaptersSoonLabel)
            } else if (themes.isNotEmpty()) {
                append(". ")
                append(themes.joinToString(", "))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GAME_CARD_ASPECT)
            .background(Color.Black.copy(alpha = 0.3f))
            // The subtitle must never paint over neighbouring UI (long labels, e.g. German).
            .clipToBounds()
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clickable { onTap(gameCatalog) }
    ) {
        val context = LocalContext.current
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = gameCatalog.bannerImageRequest(context)
                ?: ImageRequest.Builder(context).build(),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
        GameLogo(
            titleUrl = remember(gameCatalog.title) { gameCatalog.titleUrl() },
            modifier = Modifier.titleRect(),
        )
        if (hasNewChaptersSoon) {
            NewChaptersSoonSubtitle(label = newChaptersSoonLabel)
        } else {
            Themes(themes = themes)
        }
        if (isFavorite) {
            Icon(
                painter = painterResource(R.drawable.game_star_selected),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(14.dp),
            )
        }
    }
}


/** Places the content inside the title rectangle, as fractions of the card size. */
private fun Modifier.titleRect(): Modifier = layout { measurable, constraints ->
    val width = (constraints.maxWidth * TITLE_RECT_WIDTH_FRACTION).roundToInt()
    val height = (constraints.maxHeight * TITLE_RECT_HEIGHT_FRACTION).roundToInt()
    val placeable = measurable.measure(Constraints.fixed(width, height))
    val x = (constraints.maxWidth * TITLE_RECT_LEFT_FRACTION).roundToInt()
    val y = (constraints.maxHeight * TITLE_RECT_TOP_FRACTION).roundToInt()
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(x, y)
    }
}

/**
 * Renders up to [MAX_THEMES] theme labels under the title rectangle: uppercase,
 * white, separated by " • ", horizontally centered on the title column.
 *
 * Fitting rule: show as many themes as fit on one line inside the card
 * (3 → 2 → 1); if even a single theme is too wide, it is ellipsized.
 * Blank labels are dropped; extra labels are dropped (logged in debug).
 */
@Composable
private fun Themes(modifier: Modifier = Modifier, themes: List<String>) {
    val sanitized = remember(themes) {
        val clean = themes.map { it.trim() }.filter { it.isNotEmpty() }
        if (BuildConfig.DEBUG && clean.size > MAX_THEMES) {
            Log.w("GameCard", "Dropping ${clean.size - MAX_THEMES} theme(s): max is $MAX_THEMES")
        }
        clean.take(MAX_THEMES).map { theme ->
            if (theme.length <= MAX_THEME_LENGTH) theme
            else theme.take(MAX_THEME_LENGTH - 1) + '…'
        }
    }
    if (sanitized.isEmpty()) return

    val candidates = remember(sanitized) {
        (sanitized.size downTo 1).map { count ->
            buildAnnotatedString {
                sanitized.take(count).forEachIndexed { index, theme ->
                    if (index > 0) withStyle(SpanStyle(color = Color(0xFFFF007A))) { append(" • ") }
                    append(theme.uppercase())
                }
            }
        }
    }

    Subtitle(modifier = modifier, candidates = candidates, color = Color.White)
}

/**
 * Replaces the themes when the game has upcoming chapters: an uppercase
 * "new chapters soon" label in whitened green, wrapped on up to two
 * centered lines inside the card.
 */
@Composable
private fun NewChaptersSoonSubtitle(modifier: Modifier = Modifier, label: String) {
    val candidates = remember(label) { listOf(AnnotatedString(label.uppercase())) }
    Subtitle(
        modifier = modifier,
        candidates = candidates,
        color = NewChaptersSoonGreen,
        maxLines = 2,
        softWrap = true,
        textMaxWidth = 150.dp,
    )
}

/**
 * Renders the first candidate that fits inside the card, under the title
 * rectangle, horizontally centered on the title column. With [softWrap] off,
 * candidates must fit on one line; with it on, they wrap on up to [maxLines]
 * centered lines. If none fits, the last (shortest) candidate is ellipsized
 * to the card width.
 *
 * [textMaxWidth] caps the width wrapping candidates are measured against;
 * the layout itself always spans the whole card so the geometry fractions
 * stay relative to the card size.
 */
@Composable
private fun Subtitle(
    modifier: Modifier = Modifier,
    candidates: List<AnnotatedString>,
    color: Color,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    textMaxWidth: Dp = Dp.Unspecified,
) {
    Layout(
        modifier = modifier,
        content = {
            candidates.forEach { text ->
                Text(
                    text = text,
                    color = color,
                    fontSize = 11.sp,
                    fontFamily = CrimsonTextFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    softWrap = softWrap,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    ) { measurables, constraints ->
        if (BuildConfig.DEBUG) {
            val expectedHeight = (constraints.maxWidth / GAME_CARD_ASPECT).roundToInt()
            check(abs(constraints.maxHeight - expectedHeight) <= 4) {
                "GameCard lost its aspect ratio: w=${constraints.maxWidth}, h=${constraints.maxHeight}"
            }
        }
        // Wrapping text must be measured against the real width to wrap; single-line
        // candidates are measured with infinite width to know their natural size.
        val loose = if (softWrap) {
            val maxWidth = if (textMaxWidth == Dp.Unspecified) constraints.maxWidth
            else minOf(constraints.maxWidth, textMaxWidth.roundToPx())
            constraints.copy(minWidth = 0, minHeight = 0, maxWidth = maxWidth)
        } else {
            Constraints(maxWidth = Constraints.Infinity, maxHeight = constraints.maxHeight)
        }
        var fitting: Placeable? = null
        for (measurable in measurables) {
            val measured = measurable.measure(loose)
            if (measured.width <= constraints.maxWidth) {
                fitting = measured
                break
            }
        }
        // Even a single theme is too wide: ellipsize it to the card width.
        val placeable = fitting ?: measurables.last().measure(
            constraints.copy(minWidth = 0, minHeight = 0)
        )
        val x = (constraints.maxWidth * TITLE_CENTER_X_FRACTION - placeable.width / 2f)
            .roundToInt()
            .coerceAtLeast(0)
        val y = (constraints.maxHeight * TITLE_BOTTOM_Y_FRACTION).roundToInt() + 8.dp.roundToPx()
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(x, y)
        }
    }
}
