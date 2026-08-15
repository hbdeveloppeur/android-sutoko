package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameActionStateTest {

    private fun item(
        isFree: Boolean = true,
        isPurchased: Boolean = false,
        legacyId: Int? = null,
        localVersion: Int? = null,
        version: Int = 1,
        downloadProgress: Float? = null,
        price: Int = if (isFree) 0 else 100,
        isUserConnected: Boolean = false,
        canvasTechnologyRequiredVersion: Int = BuildConfig.CANVAS_VERSION_COMPATIBILITY,
    ): GameItem = GameItem(
        id = "game-1",
        title = "Test",
        version = version,
        isPurchased = isPurchased,
        localVersion = localVersion,
        downloadProgress = downloadProgress,
        isFree = isFree,
        legacyId = legacyId,
        canvasTechnologyRequiredVersion = canvasTechnologyRequiredVersion,
        price = price,
    )

    private fun state(
        item: GameItem,
        currentChapter: Chapter? = Chapter(number = 1, code = "1A"),
        isUserConnected: Boolean = false,
    ): GameActionState = item.toGameActionState(
        currentChapter = currentChapter,
        isUserConnected = isUserConnected,
    )

    @Test
    fun `story requires newer canvas technology - UpdateApp`() {
        val result = state(
            item(
                isFree = true,
                localVersion = 1,
                version = 1,
                canvasTechnologyRequiredVersion = BuildConfig.CANVAS_VERSION_COMPATIBILITY + 1,
            )
        )
        assertEquals(GameActionState.UpdateApp, result)
    }

    @Test
    fun `unpurchased story requiring newer canvas technology - UpdateApp not Purchase`() {
        // The compatibility gate wins over ownership: never sell or offer a
        // trial for a story this app's canvas engine cannot run.
        val result = state(
            item(
                isFree = false,
                isPurchased = false,
                canvasTechnologyRequiredVersion = BuildConfig.CANVAS_VERSION_COMPATIBILITY + 1,
            )
        )
        assertEquals(GameActionState.UpdateApp, result)
    }

    @Test
    fun `purchased story requiring newer canvas technology - UpdateApp not Download`() {
        // Once owned, the compatibility gate blocks content operations: no point
        // downloading a story this app's canvas engine cannot run.
        val result = state(
            item(
                isFree = false,
                isPurchased = true,
                localVersion = null,
                canvasTechnologyRequiredVersion = BuildConfig.CANVAS_VERSION_COMPATIBILITY + 1,
            )
        )
        assertEquals(GameActionState.UpdateApp, result)
    }

    @Test
    fun `outdated story requiring newer canvas technology - UpdateApp not UpdateGame`() {
        // Downloading the newer story would not help: the fix is an app update.
        val result = state(
            item(
                isFree = true,
                localVersion = 1,
                version = 2,
                canvasTechnologyRequiredVersion = BuildConfig.CANVAS_VERSION_COMPATIBILITY + 1,
            )
        )
        assertEquals(GameActionState.UpdateApp, result)
    }

    @Test
    fun `story requires current canvas technology - Play`() {
        val result = state(
            item(
                isFree = true,
                localVersion = 1,
                version = 1,
                canvasTechnologyRequiredVersion = BuildConfig.CANVAS_VERSION_COMPATIBILITY,
            )
        )
        assertTrue(result is GameActionState.Play)
    }

    @Test
    fun `free and not installed - Download`() {
        val result = state(item(isFree = true, localVersion = null))
        assertEquals(GameActionState.Download, result)
    }

    @Test
    fun `free installed and up to date - Play`() {
        val result = state(item(isFree = true, localVersion = 1, version = 1))
        assertTrue(result is GameActionState.Play)
    }

    @Test
    fun `paid and owned but not installed - Download`() {
        val result = state(item(isFree = false, isPurchased = true, localVersion = null))
        assertEquals(GameActionState.Download, result)
    }

    @Test
    fun `paid not owned at chapter 1 non friendzoned - Purchase with try`() {
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = null),
            currentChapter = Chapter(number = 1, code = "1A"),
        )
        assertEquals(
            GameActionState.Purchase(chapterNumber = 1, showTry = true, price = 100, isUserConnected = false),
            result
        )
    }

    @Test
    fun `paid not owned past chapter 1 - Purchase without try`() {
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = null),
            currentChapter = Chapter(number = 2, code = "1B"),
        )
        assertEquals(
            GameActionState.Purchase(chapterNumber = 2, showTry = false, price = 100, isUserConnected = false),
            result
        )
    }

    @Test
    fun `paid not owned at chapter 1 friendzoned - Purchase without try`() {
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = 159),
            currentChapter = Chapter(number = 1, code = "1A"),
        )
        assertEquals(
            GameActionState.Purchase(chapterNumber = 1, showTry = false, price = 100, isUserConnected = false),
            result
        )
    }

    @Test
    fun `paid not owned at chapter 1 legacy but not friendzoned - Purchase with try`() {
        // A non-null legacyId alone is not Friendzoned: only ids 159..163 are Buy-only.
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = 999),
            currentChapter = Chapter(number = 1, code = "1A"),
        )
        assertEquals(
            GameActionState.Purchase(chapterNumber = 1, showTry = true, price = 100, isUserConnected = false),
            result
        )
    }

    @Test
    fun `paid not owned with null chapter - Purchase keeps try with loading sentinel`() {
        // No chapter loaded yet: chapterNumber is the -1 loading sentinel so
        // the Try button renders disabled (see GameButtonsState).
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = null),
            currentChapter = null,
        )
        assertEquals(
            GameActionState.Purchase(chapterNumber = -1, showTry = true, price = 100, isUserConnected = false),
            result
        )
    }

    @Test
    fun `free installed with null chapter - Play with loading sentinel`() {
        val result = state(
            item(isFree = true, localVersion = 1, version = 1),
            currentChapter = null,
        )
        assertEquals(GameActionState.Play(chapterNumber = -1), result)
    }
}
