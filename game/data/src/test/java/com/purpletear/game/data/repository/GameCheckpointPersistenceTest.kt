package com.purpletear.game.data.repository

import androidx.room.Room
import androidx.room.withTransaction
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.sutoko.game.engine.*
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.*
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE,sdk=[28])
class GameCheckpointPersistenceTest {
    @Test fun coldDatabaseReopenPreservesCheckpointAndPreviousChapterHistory() = runBlocking {
        val context=RuntimeEnvironment.getApplication();val name="checkpoint-cold-reopen"
        context.deleteDatabase(name)
        try {
            val first=Room.databaseBuilder(context,GameDatabase::class.java,name).allowMainThreadQueries().build()
            val progress=UserGameProgressRepositoryImpl(first.userGameProgressDao())
            progress.save(UserGameProgress("story","8A","8a","Alex"))
            val memories=MemoryRepositoryImpl(first.memoryDao())
            memories.save("story",mapOf("oldDecision" to MemoryEntry("yes",7)))
            val memory=GameMemory(memories,progress,GameProgressTransaction { first.withTransaction { it() } })
            memory.setCurrentChapter("8A");memory.load("story",8);memory.set("IStayed","true")
            val checkpoint=GameCheckpoint("8A","fingerprint","after",GameCheckpoint.Gate.TAP,memory.entries(),
                listOf(GameMessageText("read","A later message",2)))
            memory.saveCheckpoint(checkpoint);first.close()
            val reopened=Room.databaseBuilder(context,GameDatabase::class.java,name).allowMainThreadQueries().build()
            try {
                val repository=UserGameProgressRepositoryImpl(reopened.userGameProgressDao())
                val restored=requireNotNull(repository.get("story").checkpoint)
                assertEquals("after",restored.nodeId);assertEquals("true",restored.memories["IStayed"]?.value)
                assertEquals("A later message",(restored.messages.single() as GameMessageText).text)
                val replay=MemoryRepositoryImpl(reopened.memoryDao()).load("story",8)
                assertEquals("yes",replay["oldDecision"]?.value);assertFalse(replay.containsKey("IStayed"))
            } finally {reopened.close()}
        } finally {context.deleteDatabase(name)}
    }

    @Test fun failedProgressWriteRollsBackTheMemoryAndKeepsTheOldGate() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).allowMainThreadQueries().build()
        try {
            val progress=UserGameProgressRepositoryImpl(db.userGameProgressDao())
            val old=GameCheckpoint("8A","fp","old",GameCheckpoint.Gate.TAP,emptyMap(),emptyList())
            progress.save(UserGameProgress("story","8A","8a",checkpoint=old))
            val failing=object:UserGameProgressRepository by progress {
                override suspend fun save(progress:UserGameProgress){throw IllegalStateException("write failed")}
            }
            val memories=MemoryRepositoryImpl(db.memoryDao())
            val memory=GameMemory(memories,failing,GameProgressTransaction { db.withTransaction { it() } })
            memory.setCurrentChapter("8A");memory.restore("story",8,emptyMap());memory.set("IStayed","true")
            try {memory.saveCheckpoint(old.copy(nodeId="new",memories=memory.entries()));fail("Expected write failure")}
            catch (_:IllegalStateException){}
            assertEquals("old",progress.get("story").checkpoint?.nodeId)
            assertTrue(db.memoryDao().getAllForGameUpToChapter("story",8).isEmpty())
        } finally {db.close()}
    }

    @Test fun selectingAnotherChapterClearsOnlyTheCheckpointAndKeepsTheHero() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).allowMainThreadQueries().build()
        try {
            val progress=UserGameProgressRepositoryImpl(db.userGameProgressDao())
            val saved=GameCheckpoint("8A","fp","node",GameCheckpoint.Gate.TAP,emptyMap(),emptyList())
            progress.save(UserGameProgress("story","8A","8a","Alex",saved))
            val select=SelectChapterUseCase(progress,GameProgressTransaction { db.withTransaction { it() } })
            assertTrue(select("story","8a").isSuccess);assertNotNull(progress.get("story").checkpoint)
            assertTrue(select("story","7A").isSuccess);assertNull(progress.get("story").checkpoint)
            assertEquals("Alex",progress.get("story").heroName)
        } finally {db.close()}
    }
}
