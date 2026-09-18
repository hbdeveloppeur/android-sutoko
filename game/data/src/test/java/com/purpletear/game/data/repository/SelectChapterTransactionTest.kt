package com.purpletear.game.data.repository

import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.di.GameDataModule
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class SelectChapterTransactionTest {
    private lateinit var database: GameDatabase
    private lateinit var progress: UserGameProgressRepositoryImpl
    private lateinit var memories: MemoryRepositoryImpl
    private val original = UserGameProgress("story", "3A", "3a", "Alex")
    private val decision = MemoryEntity("story", "trusted_friend", "true", 1)

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), GameDatabase::class.java,
        ).allowMainThreadQueries().build()
        progress = UserGameProgressRepositoryImpl(database.userGameProgressDao())
        memories = MemoryRepositoryImpl(database.memoryDao())
        progress.save(original)
        database.memoryDao().insert(decision)
    }

    @After
    fun tearDown() = database.close()

    private fun selection(progressRepository: UserGameProgressRepository = progress) = SelectChapterUseCase(
        progressRepository, GameDataModule.provideGameProgressTransaction(database),
    )

    @Test
    fun `failed progress save preserves memories and retry selects chapter`() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_save BEFORE INSERT ON user_game_progress " +
                "BEGIN SELECT RAISE(ABORT, 'injected failure'); END",
        )

        assertTrue(selection()("story", "2A").isFailure)
        assertEquals(original, progress.get("story"))
        assertEquals(listOf(decision), database.memoryDao().getAllForGameUpToChapter("story", 3))

        database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_save")
        assertTrue(selection()("story", "2A").isSuccess)
        assertEquals("2A", progress.get("story").currentChapterCode)
        assertEquals("Alex", progress.get("story").heroName)
        assertEquals(listOf(decision), database.memoryDao().getAllForGameUpToChapter("story", 3))
    }

    @Test
    fun `cancellation after saving progress rolls back selection`() = runBlocking {
        val cancelledProgress = object : UserGameProgressRepository by progress {
            override suspend fun save(progress: UserGameProgress) {
                this@SelectChapterTransactionTest.progress.save(progress)
                throw CancellationException("injected cancellation")
            }
        }
        val result = runCatching { selection(cancelledProgress)("story", "2A") }
        assertTrue(result.exceptionOrNull() is CancellationException)
        assertEquals(original, progress.get("story"))
        assertEquals(listOf(decision), database.memoryDao().getAllForGameUpToChapter("story", 3))
    }

    @Test
    fun `current chapter preserves progress and decisions in real database`() = runBlocking {
        assertTrue(selection()("story", "3a").isSuccess)
        assertEquals(original, progress.get("story"))
        assertEquals(listOf(decision), database.memoryDao().getAllForGameUpToChapter("story", 3))
    }
}
