package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.model.Chapter
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
    ): GameItem = GameItem(
        id = "game-1",
        title = "Test",
        version = version,
        isPurchased = isPurchased,
        localVersion = localVersion,
        downloadProgress = downloadProgress,
        isFree = isFree,
        legacyId = legacyId,
        minAppBuild = 1,
    )

    private fun state(
        item: GameItem,
        currentChapter: Chapter? = Chapter(number = 1, code = "1A"),
        appBuildNumber: Int = 100,
    ): GameActionState = item.toGameActionState(
        currentChapter = currentChapter,
        appBuildNumber = appBuildNumber,
    )

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
        assertEquals(GameActionState.Purchase(chapterNumber = 1, showTry = true), result)
    }

    @Test
    fun `paid not owned past chapter 1 - Purchase without try`() {
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = null),
            currentChapter = Chapter(number = 2, code = "1B"),
        )
        assertEquals(GameActionState.Purchase(chapterNumber = 2, showTry = false), result)
    }

    @Test
    fun `paid not owned at chapter 1 friendzoned - Purchase without try`() {
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = 159),
            currentChapter = Chapter(number = 1, code = "1A"),
        )
        assertEquals(GameActionState.Purchase(chapterNumber = 1, showTry = false), result)
    }

    @Test
    fun `paid not owned at chapter 1 legacy but not friendzoned - Purchase with try`() {
        // A non-null legacyId alone is not Friendzoned: only ids 159..163 are Buy-only.
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = 999),
            currentChapter = Chapter(number = 1, code = "1A"),
        )
        assertEquals(GameActionState.Purchase(chapterNumber = 1, showTry = true), result)
    }

    @Test
    fun `paid not owned with null chapter - defaults to chapter 1 with try`() {
        val result = state(
            item(isFree = false, isPurchased = false, legacyId = null),
            currentChapter = null,
        )
        assertEquals(GameActionState.Purchase(chapterNumber = 1, showTry = true), result)
    }
}
