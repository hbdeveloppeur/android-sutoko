package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMemoryRepository : MemoryRepository {
    private val memories = mutableMapOf<String, Map<String, MemoryEntry>>()

    override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> {
        return memories[gameId] ?: emptyMap()
    }

    override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) {
        this.memories[gameId] = memories
    }

    override suspend fun clear(gameId: String) {
        memories.remove(gameId)
    }

    override suspend fun delete(gameId: String) {
        memories.remove(gameId)
    }

    override fun observe(gameId: String): Flow<Map<String, String>> = flowOf(emptyMap())

    override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) {
        val current = memories[gameId] ?: emptyMap()
        memories[gameId] = current + (key to MemoryEntry(value, chapterNumber))
    }
}
