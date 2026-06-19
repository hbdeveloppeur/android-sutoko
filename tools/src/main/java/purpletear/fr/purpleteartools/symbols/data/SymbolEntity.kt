package purpletear.fr.purpleteartools.symbols.data

import androidx.room.Entity
import androidx.annotation.Keep

@Entity(tableName = "symbols", primaryKeys = ["rowId", "name"])
@Keep
data class SymbolEntity(
    val rowId: Int,
    val name: String,
    val value: String,
    val identifier: Int
)
