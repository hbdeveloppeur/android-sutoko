package com.purpletear.game.presentation.game_play

import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChapterContentRequestTest {
    @Test
    fun `incoming chapter stays covered until its own content is ready`() = runTest {
        val request = ChapterContentRequest("2a")
        val readiness = async { request.await(10_000) }
        runCurrent()
        request.complete("1a", ready = true)
        runCurrent()
        assertFalse(readiness.isCompleted)
        request.complete("2a", ready = true)
        assertTrue(readiness.await())
    }
    @Test
    fun `failed chapter releases readiness wait`() = runTest {
        val request = ChapterContentRequest("2a")
        request.complete("2a", ready = false)
        assertFalse(request.await(10_000))
    }

    @Test
    fun `chapter that never becomes ready times out`() = runTest {
        assertFalse(ChapterContentRequest("2a").await(10_000))
    }
}
