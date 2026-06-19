package purpletear.fr.purpleteartools.symbols

import purpletear.fr.purpleteartools.TableOfSymbols

interface SymbolsStorage {
    fun load(): TableOfSymbols?
    fun save(table: TableOfSymbols): Boolean
}
