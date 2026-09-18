package com.purpletear.sutoko.game.repository

/** Runs progress and memory changes in one transaction, rolling back on failure or cancellation. */
fun interface GameProgressTransaction {
    suspend fun run(block: suspend () -> Unit)
}
