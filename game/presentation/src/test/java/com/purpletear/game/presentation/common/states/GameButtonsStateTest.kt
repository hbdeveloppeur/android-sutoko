package com.purpletear.game.presentation.common.states

import com.purpletear.game.presentation.game_preview.GamePreviewAction
import com.purpletear.game.presentation.model.GameActionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameButtonsStateTest {

    private fun buttonsFor(state: GameActionState, onAction: (GamePreviewAction) -> Unit) =
        state.toButtonsState(onAction)

    @Test
    fun `Purchase with try - left is Try, right is Buy`() {
        val actions = mutableListOf<GamePreviewAction>()
        val buttons = buttonsFor(GameActionState.Purchase(chapterNumber = 1, showTry = true, price = 100, isUserConnected = true)) {
            actions += it
        }

        assertNotNull(buttons.left.onClick)
        buttons.left.onClick?.invoke()
        buttons.right.onClick?.invoke()

        assertEquals(listOf(GamePreviewAction.OnTry, GamePreviewAction.OnBuy), actions)
        // Try + Buy must share the row equally.
        assertEquals(1f, buttons.left.weight, 0.00001f)
        assertEquals(buttons.left.weight, buttons.right.weight, 0.00001f)
    }

    @Test
    fun `Purchase past chapter 1 without try - left is Restart, right is Buy`() {
        val actions = mutableListOf<GamePreviewAction>()
        val buttons = buttonsFor(GameActionState.Purchase(chapterNumber = 2, showTry = false, price = 100, isUserConnected = true)) {
            actions += it
        }

        assertNotNull(buttons.left.onClick)
        buttons.left.onClick?.invoke()
        buttons.right.onClick?.invoke()

        assertEquals(listOf(GamePreviewAction.OnRestart, GamePreviewAction.OnBuy), actions)
        // Without Try, the established Restart(2) : Buy(3) ratio is unchanged.
        assertEquals(2f, buttons.left.weight, 0.00001f)
        assertEquals(3f, buttons.right.weight, 0.00001f)
    }

    @Test
    fun `Purchase at chapter 1 without try - left hidden, right is Buy`() {
        val actions = mutableListOf<GamePreviewAction>()
        val buttons = buttonsFor(GameActionState.Purchase(chapterNumber = 1, showTry = false, price = 100, isUserConnected = true)) {
            actions += it
        }

        assertNull(buttons.left.onClick)
        buttons.right.onClick?.invoke()
        assertEquals(listOf(GamePreviewAction.OnBuy), actions)
    }

    @Test
    fun `Purchase with try while chapter loading - Try visible but disabled`() {
        // chapterNumber = -1 is the loading sentinel: tapping Try without a
        // chapter would navigate nowhere, so the button must be disabled.
        val buttons = buttonsFor(GameActionState.Purchase(chapterNumber = -1, showTry = true, price = 100, isUserConnected = true)) {}

        assertNotNull(buttons.left.onClick)
        assertFalse(buttons.left.isEnabled)
    }

    @Test
    fun `Play while chapter loading - disabled and no subtitle`() {
        val buttons = buttonsFor(GameActionState.Play(chapterNumber = -1)) {}

        assertFalse(buttons.right.isEnabled)
        assertNull(buttons.right.subtitle)
    }

    @Test
    fun `Play with loaded chapter - enabled with chapter subtitle`() {
        val buttons = buttonsFor(GameActionState.Play(chapterNumber = 2)) {}

        assertTrue(buttons.right.isEnabled)
        assertNotNull(buttons.right.subtitle)
    }
}
