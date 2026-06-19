package purpletear.fr.purpleteartools.symbols.data

import androidx.room.Entity
import androidx.annotation.Keep

@Entity(tableName = "routes", primaryKeys = ["rowId", "index"])
@Keep
data class RouteEntity(
    val rowId: Int,
    val index: Int,
    val value: String
)
