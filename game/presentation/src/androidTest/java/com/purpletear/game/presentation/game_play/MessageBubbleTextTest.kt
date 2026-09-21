package com.purpletear.game.presentation.game_play

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sharedelements.theme.WorkSansFontFamily
import com.purpletear.game.debug.PreviewCharacter
import com.purpletear.game.presentation.game_play.components.message.MessageBubble
import com.purpletear.game.presentation.game_play.components.message.MessageText
import com.purpletear.game.presentation.game_play.mapper.ITEMS_HORIZONTAL_PADDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.ceil

@RunWith(AndroidJUnit4::class)
class MessageBubbleTextTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun multilineBubbleFitsItsLongestRenderedLine() {
        verifyBubble("J'admire votre détermination", requireShrink = true)
    }

    @Test
    fun rightBubblePreservesThreeLineWrapping() {
        verifyBubble("Tu as lu les dossiers, tu sais ce qu'il a fait à mon mari", isRightSide = true)
    }

    @Test
    fun otherRightBubbleFitsItsText() {
        verifyBubble("Je ne prendras pas ma retraite tant que je ne l'aurai attrapé", isRightSide = true)
    }

    @Test
    fun shortMessageKeepsItsNaturalWidth() {
        verifyBubble("Oui...")
    }

    @Test
    fun largeFontDoesNotClipOrAddLines() {
        verifyBubble("J'admire votre détermination", fontScale = 2f)
    }

    @Test
    fun rtlParagraphPreservesItsLines() {
        verifyBubble("هذه رسالة طويلة لاختبار عرض النص داخل الفقاعة", direction = LayoutDirection.Rtl)
    }

    @Test
    fun explicitNewlinesArePreserved() {
        verifyBubble("Première ligne\nOui...")
    }

    @Test
    fun narrowParentAndLongWordDoNotClip() {
        verifyBubble("anticonstitutionnellement", containerWidth = 120.dp)
    }

    @Test
    fun emptyMessageDoesNotCrash() {
        verifyBubble("")
    }

    @Test
    fun changedTextIsMeasuredAgain() {
        verifyBubble("Oui...", updatedText = "J'admire votre détermination")
    }

    private fun verifyBubble(
        text: String,
        isRightSide: Boolean = false,
        fontScale: Float = 1f,
        direction: LayoutDirection = LayoutDirection.Ltr,
        containerWidth: Dp = 320.dp,
        requireShrink: Boolean = false,
        updatedText: String? = null,
    ) {
        val displayedText = mutableStateOf(text)
        compose.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(LocalDensity.current.density, fontScale),
                LocalLayoutDirection provides direction,
            ) {
                Column(Modifier.width(containerWidth)) {
                    MessageText(
                        text = displayedText.value,
                        character = PreviewCharacter,
                        showHeader = false,
                        isRightSide = isRightSide,
                    )
                    Box(Modifier.padding(horizontal = ITEMS_HORIZONTAL_PADDING)) {
                        MessageBubble {
                            Text(
                                text = displayedText.value,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                color = Color.White,
                                fontFamily = WorkSansFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }
        assertFittedText(text, requireShrink)
        if (updatedText != null) {
            compose.runOnIdle { displayedText.value = updatedText }
            assertFittedText(updatedText, requireShrink = true)
        }
    }

    private fun assertFittedText(text: String, requireShrink: Boolean) {
        val layouts = (0..1).map { index ->
            val results = mutableListOf<TextLayoutResult>()
            compose.onAllNodesWithText(text)[index]
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            results.single()
        }
        val (fitted, original) = layouts
        if (requireShrink) {
            assertTrue("Scenario must wrap", original.lineCount > 1)
            assertTrue("Bubble must shrink", fitted.size.width < original.size.width)
        }
        assertTrue("Bubble must not grow", fitted.size.width <= original.size.width)
        assertEquals(original.lineCount, fitted.lineCount)
        for (line in 0 until original.lineCount) {
            assertEquals(original.getLineEnd(line), fitted.getLineEnd(line))
        }
        val longestLine = (0 until fitted.lineCount).maxOf {
            fitted.getLineRight(it) - fitted.getLineLeft(it)
        }
        assertEquals(ceil(longestLine).toInt(), fitted.size.width)
        assertTrue("Text must remain visible", !fitted.hasVisualOverflow)
    }
}
