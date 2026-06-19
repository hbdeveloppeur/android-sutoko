package purpletear.fr.purpleteartools.symbols.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SymbolEntity::class, RouteEntity::class], version = 1, exportSchema = false)
abstract class SymbolsDatabase : RoomDatabase() {
    abstract fun symbolsDao(): SymbolsDao
}
