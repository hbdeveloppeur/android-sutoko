package purpletear.fr.purpleteartools.symbols

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.symbols.data.RouteEntity
import purpletear.fr.purpleteartools.symbols.data.SymbolEntity
import purpletear.fr.purpleteartools.symbols.data.SymbolsDatabase
import java.io.File

class SymbolsRoomStorage(
    private val database: SymbolsDatabase,
    private val legacyFile: File
) : SymbolsStorage {

    private val dao = database.symbolsDao()
    private val gson = Gson()

    override fun load(): TableOfSymbols? {
        migrateLegacyFileIfNeeded()

        return try {
            val table = TableOfSymbols(-1)

            val symbolRowIds = dao.getAllSymbolRowIds()
            symbolRowIds.forEach { rowId ->
                dao.getSymbolsForRow(rowId).forEach { entity ->
                    table.addOrSet(entity.rowId, entity.name, entity.value, entity.identifier)
                }
            }

            val routeRowIds = dao.getAllRouteRowIds()
            routeRowIds.forEach { rowId ->
                val routes = dao.getRoutesForRow(rowId)
                if (routes.isNotEmpty()) {
                    table.route[rowId] = ArrayList(routes.map { it.value })
                }
            }

            table
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load symbols from Room", e)
            null
        }
    }

    override fun save(table: TableOfSymbols): Boolean {
        return try {
            database.runInTransaction {
                dao.clearAllSymbols()
                dao.clearAllRoutes()

                table.getAllRowIds().forEach { rowId ->
                    table.getArray(rowId).forEach { symbol ->
                        dao.insertOrUpdate(
                            SymbolEntity(
                                rowId = symbol.rowId,
                                name = symbol.n,
                                value = symbol.v,
                                identifier = symbol.identifier
                            )
                        )
                    }
                }

                table.route.forEach { (rowId, routes) ->
                    routes.forEachIndexed { index, value ->
                        dao.insertRoute(RouteEntity(rowId, index, value))
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save symbols to Room", e)
            false
        }
    }

    private fun migrateLegacyFileIfNeeded() {
        if (!legacyFile.exists()) return

        val hasRoomData = try {
            dao.getAllSymbolRowIds().isNotEmpty() || dao.getAllRouteRowIds().isNotEmpty()
        } catch (e: Exception) {
            false
        }

        if (hasRoomData) {
            if (!legacyFile.delete()) {
                legacyFile.deleteOnExit()
            }
            return
        }

        try {
            val json = legacyFile.bufferedReader().use { it.readText() }
            val legacyType = object : TypeToken<LegacyTableOfSymbols>() {}.type
            val legacy = gson.fromJson<LegacyTableOfSymbols>(json, legacyType)
                ?: return

            database.runInTransaction {
                legacy.map.forEach { (rowId, symbols) ->
                    symbols.forEach { s ->
                        dao.insertOrUpdate(
                            SymbolEntity(rowId, s.n, s.v, s.identifier)
                        )
                    }
                }
                legacy.route.forEach { (rowId, routes) ->
                    routes.forEachIndexed { index, value ->
                        dao.insertRoute(RouteEntity(rowId, index, value))
                    }
                }
            }

            if (!legacyFile.delete()) {
                legacyFile.deleteOnExit()
            }
            Log.i(TAG, "Legacy symbols migration completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Legacy migration failed, will retry on next load", e)
        }
    }

    private data class LegacyTableOfSymbols(
        val gameId: Int = -1,
        val map: HashMap<Int, ArrayList<LegacySymbol>> = HashMap(),
        val route: HashMap<Int, ArrayList<String>> = HashMap()
    )

    private data class LegacySymbol(
        val rowId: Int = -1,
        val n: String = "",
        val v: String = "",
        val identifier: Int = -1
    )

    companion object {
        private const val TAG = "SymbolsRoomStorage"
    }
}
