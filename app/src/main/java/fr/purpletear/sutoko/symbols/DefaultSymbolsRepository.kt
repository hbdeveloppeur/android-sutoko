package fr.purpletear.sutoko.symbols

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.symbols.SymbolsStorage
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Default implementation of [SymbolsRepository].
 *
 * The [SymbolsStorage] is provided lazily via a [Provider] so that building the underlying Room
 * database happens on the IO dispatcher, not on the main thread during Hilt injection.
 */
@Singleton
class DefaultSymbolsRepository @Inject constructor(
    private val storageProvider: Provider<SymbolsStorage>
) : SymbolsRepository {

    private val mutex = Mutex()
    private val _symbols = MutableStateFlow<TableOfSymbols?>(null)
    override val symbols: StateFlow<TableOfSymbols?> = _symbols.asStateFlow()

    override suspend fun load(): TableOfSymbols {
        val cached = _symbols.value
        if (cached != null) return cached

        return mutex.withLock {
            _symbols.value ?: run {
                val loaded = withContext(Dispatchers.IO) {
                    storageProvider.get().load()
                } ?: TableOfSymbols(-1)
                _symbols.value = loaded
                loaded
            }
        }
    }
}
