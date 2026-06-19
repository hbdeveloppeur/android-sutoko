package purpletear.fr.purpleteartools

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import purpletear.fr.purpleteartools.symbols.SymbolsStorage
import java.util.Locale

@Keep
class TableOfSymbols(gameId: Int) : Parcelable {

    var gameId: Int = gameId
    private var map: HashMap<Int, ArrayList<Symbol>> = HashMap()
    var route: HashMap<Int, ArrayList<String>> = HashMap()

    var chapterCode: String
        get() {
            val code = get(gameId, "chapterCode")
            if (code == null || code.replace(" ", "") == "") {
                return "1a"
            }
            return code.lowercase(Locale.getDefault())
        }
        set(value) {
            addOrSet(gameId, "chapterCode", value.lowercase(Locale.getDefault()))
        }

    val isFirebaseNotificationEnabled: Boolean
        get() {
            val r = get(0, "firebaseNotifications")
            return r == null || r == "true"
        }

    val chapterNumber: Int
        get() {
            val code = chapterCode
            return if (code.length < 2) 1 else code.substring(0, code.length - 1).toInt()
        }

    var firstName: String
        get() {
            return (get(gameId, "prenom") ?: "Nick").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
        set(value) {
            addOrSet(gameId, "prenom", value)
        }

    var globalFirstName: String
        get() {
            return (get(0, "prenom") ?: "Nick").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
        set(value) {
            addOrSet(0, "prenom", value)
        }

    fun getArray(id: Int): ArrayList<Symbol> {
        return map[id] ?: ArrayList()
    }

    fun getAllRowIds(): Set<Int> = map.keys.toSet()

    @Deprecated("Context no longer needed", ReplaceWith("read()"))
    fun read(context: Context): Boolean = read()

    fun read(): Boolean {
        val loaded = storage?.load() ?: return false
        if (loaded.getAllRowIds().isEmpty() && loaded.route.isEmpty()) {
            return false
        }
        this.map = loaded.map
        this.route = loaded.route
        return true
    }

    @Deprecated("Context no longer needed", ReplaceWith("save()"))
    fun save(context: Context): Boolean = save()

    fun save(): Boolean {
        return storage?.save(this) ?: false
    }

    /**
     * Removes vars that are from chapters upper than chapterNumber
     * @param chapterNumber the chapterNumber;
     */
    fun removeFromASpecificChapterNumber(id: Int, chapterNumber: Int) {
        val newArray: ArrayList<Symbol> = ArrayList()
        for (s in map[id] ?: ArrayList()) {
            if (s.identifier < chapterNumber) {
                newArray.add(s)
            }
        }
        map[id] = newArray
    }

    fun removeVar(rowId: Int, name: String) {
        if (!map.containsKey(rowId)) {
            return
        }
        map[rowId]?.remove(Symbol(rowId, name, ""))
    }

    @Deprecated("Activity no longer needed", ReplaceWith("reset(id).also { save() }"))
    fun reset(activity: Activity, id: Int) {
        reset(id)
        save()
    }

    fun reset(id: Int) {
        if (!map.containsKey(id)) {
            return
        }
        val hasEscapeGame = hasSymbol(id, "escapeGame")
        val version = getStoryVersion(id)
        map.remove(id)
        setStoryVersion(id, version)
        addOrSet(id, "replayedStory", "true", 0)
        if (hasEscapeGame) {
            addOrSet(id, "escapeGame", "true", -1)
        }
    }

    /**
     * Deletes all data for a specific rowId
     *
     * @param rowId the id of the row to delete
     * @return true if data was deleted, false if rowId didn't exist
     */
    fun deleteRowData(rowId: Int): Boolean {
        if (!map.containsKey(rowId)) {
            return false
        }
        map.remove(rowId)
        route.remove(rowId)
        return true
    }

    fun get(rowId: Int, symbolName: String): String? {
        createIfDontExist(rowId)

        val indexOfSymbol: Int = (map[rowId] ?: throw NullPointerException("Symbol row not found"))
            .indexOf(Symbol(rowId, symbolName, ""))
        val hasSymbol: Boolean = indexOfSymbol != -1

        return if (hasSymbol) {
            (map[rowId] ?: throw NullPointerException("Symbol row not found"))[indexOfSymbol].v
        } else {
            null
        }
    }

    fun getStoryVersion(rowId: Int): String {
        return get(rowId, "currentVersion") ?: "none"
    }

    fun setStoryVersion(rowId: Int, version: String) {
        addOrSet(rowId, "currentVersion", version)
    }

    fun setFirebaseNotification(isEnabled: Boolean) {
        addOrSet(0, "firebaseNotifications", if (isEnabled) "true" else "false")
    }

    fun setAppControlTime(time: Long) {
        addOrSet(0, "testAppTime", time.toString())
    }

    fun getAppControlTime(): String? {
        return get(0, "testAppTime")
    }

    /**
     * Adds or set a Symbol given a rowId
     *
     * @param rowId
     * @param symbolName
     * @param symbolValue
     */
    fun addOrSet(rowId: Int, symbolName: String, symbolValue: String) {
        addOrSet(rowId, symbolName, symbolValue, -1)
    }

    /**
     * Adds or set a Symbol given a rowId
     *
     * @param rowId
     * @param symbolName
     * @param symmbolValue
     */
    fun addOrSet(rowId: Int, symbolName: String, symmbolValue: String, chapterNumber: Int) {
        createIfDontExist(rowId)

        val indexOfSymbol: Int = (map[rowId] ?: throw NullPointerException("Symbol row not found"))
            .indexOf(Symbol(rowId, symbolName, symmbolValue))
        val hasSymbol: Boolean = indexOfSymbol != -1

        if (hasSymbol) {
            set(rowId, indexOfSymbol, symbolName, symmbolValue, chapterNumber)
        } else {
            add(rowId, symbolName, symmbolValue, chapterNumber)
        }
    }

    fun hasSymbol(rowId: Int, symbolName: String): Boolean {
        createIfDontExist(rowId)
        val indexOfSymbol: Int = (map[rowId] ?: throw NullPointerException("Symbol row not found"))
            .indexOf(Symbol(rowId, symbolName, ""))
        return indexOfSymbol != -1
    }

    /**
     * Determines if the condition is true or not
     *
     * @param rowId
     * @param symbolName
     * @param symmbolValue
     * @return
     */
    fun condition(rowId: Int, symbolName: String, symmbolValue: String): Boolean {
        val indexOfSymbol: Int =
            (map[rowId] ?: return false).indexOf(Symbol(rowId, symbolName, symmbolValue))
        if (indexOfSymbol == -1) {
            return false
        }
        val symbol = (map[rowId]
            ?: throw java.lang.NullPointerException("Cannot find $rowId"))[indexOfSymbol]
        return symbol.v == symmbolValue && symbol.n == symbolName
    }

    /**
     * Creates the game row if it doesn't exist
     *
     * @param rowId
     */
    private fun createIfDontExist(rowId: Int) {
        if (map[rowId] == null) {
            map[rowId] = ArrayList()
        }
    }

    /**
     * Adds a Symbol to the given row
     *
     * @param rowId
     * @param symbolName
     * @param symmbolValue
     */
    private fun add(rowId: Int, symbolName: String, symmbolValue: String, chapterNumber: Int) {
        (map[rowId] ?: throw NullPointerException("Symbol row not found"))
            .add(Symbol(rowId, symbolName, symmbolValue, chapterNumber))
    }

    /**
     * Updates a Symbol to the given row
     *
     * @param rowId
     * @param position
     * @param symbolName
     * @param symmbolValue
     */
    private fun set(
        rowId: Int,
        position: Int,
        symbolName: String,
        symmbolValue: String,
        chapterNumber: Int
    ) {
        (map[rowId] ?: throw NullPointerException("Symbol row not found"))[position] =
            Symbol(rowId, symbolName, symmbolValue, chapterNumber)
    }

    /**
     * Updates a Symbol to the given row
     *
     * @param rowId
     * @param position
     * @param symbolName
     * @param symbolValue
     */
    private fun set(rowId: Int, position: Int, symbolName: String, symmbolValue: String) {
        (map[rowId] ?: throw NullPointerException("Symbol row not found"))[position] =
            Symbol(rowId, symbolName, symmbolValue)
    }

    protected constructor(`in`: Parcel) : this(-1) {
        val bundle = `in`.readBundle(Symbol::class.java.classLoader)
        this.map = bundle?.getSerializable("map") as HashMap<Int, ArrayList<Symbol>>
        this.route =
            (bundle.getSerializable("route") as HashMap<Int, ArrayList<String>>?) ?: hashMapOf()
        this.gameId = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val bundle = Bundle()
        bundle.putSerializable("map", map)
        bundle.putSerializable("route", route)
        dest.writeBundle(bundle)
        dest.writeInt(gameId)
    }

    companion object {
        @JvmStatic
        var storage: SymbolsStorage? = null

        @JvmField
        val CREATOR: Parcelable.Creator<TableOfSymbols> =
            object : Parcelable.Creator<TableOfSymbols> {
                override fun createFromParcel(`in`: Parcel): TableOfSymbols {
                    return TableOfSymbols(`in`)
                }

                override fun newArray(size: Int): Array<TableOfSymbols?> {
                    return arrayOfNulls(size)
                }
            }
    }

    override fun describeContents(): Int {
        return 0
    }
}
