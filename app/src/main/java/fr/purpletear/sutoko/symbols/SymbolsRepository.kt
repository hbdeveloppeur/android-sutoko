package fr.purpletear.sutoko.symbols

import kotlinx.coroutines.flow.StateFlow
import purpletear.fr.purpleteartools.TableOfSymbols

/**
 * Repository that owns the in-memory [TableOfSymbols] instance.
 *
 * The table is loaded asynchronously on first request; subsequent callers receive the cached
 * instance. This keeps the main thread unblocked during startup.
 */
interface SymbolsRepository {
    /**
     * Emits the loaded [TableOfSymbols] once available, or null before the first successful load.
     */
    val symbols: StateFlow<TableOfSymbols?>

    /**
     * Loads the symbols table on a background dispatcher and caches the result.
     * Returns immediately with the cached value on subsequent calls.
     */
    suspend fun load(): TableOfSymbols
}
