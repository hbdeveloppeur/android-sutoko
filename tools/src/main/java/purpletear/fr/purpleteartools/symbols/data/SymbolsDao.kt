package purpletear.fr.purpleteartools.symbols.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SymbolsDao {
    @Query("SELECT * FROM symbols WHERE rowId = :rowId")
    fun getSymbolsForRow(rowId: Int): List<SymbolEntity>

    @Query("SELECT * FROM symbols WHERE rowId = :rowId AND name = :name LIMIT 1")
    fun getSymbol(rowId: Int, name: String): SymbolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(symbol: SymbolEntity)

    @Query("DELETE FROM symbols WHERE rowId = :rowId")
    fun deleteRow(rowId: Int)

    @Query("DELETE FROM symbols WHERE rowId = :rowId AND name = :name")
    fun deleteSymbol(rowId: Int, name: String)

    @Query("SELECT DISTINCT rowId FROM symbols")
    fun getAllSymbolRowIds(): List<Int>

    @Query("SELECT * FROM routes WHERE rowId = :rowId ORDER BY `index` ASC")
    fun getRoutesForRow(rowId: Int): List<RouteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoute(route: RouteEntity)

    @Query("DELETE FROM routes WHERE rowId = :rowId")
    fun deleteRoutes(rowId: Int)

    @Query("SELECT DISTINCT rowId FROM routes")
    fun getAllRouteRowIds(): List<Int>

    @Query("DELETE FROM symbols")
    fun clearAllSymbols()

    @Query("DELETE FROM routes")
    fun clearAllRoutes()
}
