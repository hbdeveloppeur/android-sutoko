package purpletear.fr.purpleteartools

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AtomicFile
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


@Keep
class TableOfSymbols(gameId: Int) : Serializable, Parcelable {

    var gameId: Int = gameId
    private var map: HashMap<Int, ArrayList<Symbol>> = HashMap()
    var route: HashMap<Int, ArrayList<String>> = HashMap()
    private val SYMBOLS_DIR_PATH = "symbols/"
    private val SYMBOLS_FILE_NAME = "symbols.json"

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


    fun hasReportedUserStory(fsid: String): Boolean {
        val r = get(-3, fsid)
        return r != null && r == "true"
    }

    fun addReportedUserStory(fsid: String) {
        addOrSet(-3, fsid, "true")
    }

    fun addChapterToRoute(chapterCode: String) {
        if (this.route[this.gameId] == null) {
            this.route[this.gameId] = ArrayList()
        }
        var toRemove: Int? = null
        val n1 = if (chapterCode.length < 2) 1 else chapterCode.substring(
            0,
            chapterCode.length - 1
        ).toInt()
        this.route[this.gameId]?.forEachIndexed { index, elt ->
            val n2 = if (elt.length < 2) 1 else elt.substring(
                0,
                elt.length - 1
            ).toInt()
            if (n2 == n1) {
                toRemove = index
            }
        }
        if (toRemove != null) {
            this.route[this.gameId]?.removeAt(toRemove!!)
        }
        this.route[this.gameId]?.add(chapterCode)
    }

    fun userPlayedChapter(code: String): Boolean {
        this.route[this.gameId]?.forEach {
            if (it.lowercase() == code.lowercase()) {
                return true
            }
        } ?: return false
        return false
    }


    fun setHasFinishedStoryOnce() {
        addOrSet(-3, "hasFinishedStoryAtLeastOnce", "true")
    }


    val chapterNumber: Int
        get() {
            val code = chapterCode
            return if (code.length < 2) 1 else code.substring(
                0,
                code.length - 1
            ).toInt()
        }


    var firstName: String
        get() {
            return (get(gameId, "prenom") ?: "Nick").capitalize(Locale.getDefault())
        }
        set(value) {
            addOrSet(gameId, "prenom", value)
        }

    var globalFirstName: String
        get() {
            return (get(0, "prenom") ?: "Nick").capitalize(Locale.getDefault())
        }
        set(value) {
            addOrSet(0, "prenom", value)
        }


    val isRealTime: Boolean
        get() {
            return condition(gameId, "realTime", "true")
        }


    fun getArray(id: Int): ArrayList<Symbol> {
        return map[id] ?: ArrayList()
    }

    fun read(context: Context): Boolean {
        val file = File(File(context.filesDir, SYMBOLS_DIR_PATH), SYMBOLS_FILE_NAME)
        if (!file.exists()) return false

        val json = file.bufferedReader().use { it.readText() }
        val table = try {
            gson.fromJson(json, TableOfSymbols::class.java)
        } catch (e: JsonParseException) {
            e.printStackTrace()
            val sanitized = removeSaveLock(json) ?: return false
            gson.fromJson(sanitized, TableOfSymbols::class.java).also {
                val atomic = AtomicFile(file)
                val out = atomic.startWrite()
                out.bufferedWriter().use { it.write(sanitized) }
                atomic.finishWrite(out)
            }
        }

        map = table.map
        route = table.route
        return true
    }

    private fun removeSaveLock(json: String): String? = try {
        val root = JsonParser.parseString(json).asJsonObject
        root.remove("saveLock")
        root.toString()
    } catch (_: Exception) {
        null
    }


    /**
     * Removes vars that are from chapters upper than chapterNumber
     * @param chapterNumber the chapterNumber;
     */
    fun removeFromASpecificChapterNumber(id: Int, chapterNumber: Int) {
        val newArray: ArrayList<Symbol> =
            java.util.ArrayList<Symbol>()
        for (s in map[id] ?: ArrayList()) {
            if (s.identifier < chapterNumber) {
                newArray.add(s)
            }
        }
        map[id] = newArray
    }


    @field:Transient               // <- important : cible le champ
    private val saveLock = ReentrantLock()

    fun save(context: Context): Boolean {
        val dir = File(context.filesDir, SYMBOLS_DIR_PATH)
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("TableOfSymbols", "Impossible de créer ${dir.absolutePath}")
            return false
        }

        val atomicFile = AtomicFile(File(dir, SYMBOLS_FILE_NAME))

        val json = try {
            gson.toJson(this)
        } catch (e: Exception) {
            Log.e("TableOfSymbols", "Sérialisation JSON impossible", e)
            return false
        }

        return saveLock.withLock {
            var output: FileOutputStream? = null
            try {
                output = atomicFile.startWrite()
                output.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(json)
                }
                atomicFile.finishWrite(output)
                true
            } catch (e: IOException) {
                atomicFile.failWrite(output)
                Log.e("TableOfSymbols", "Sauvegarde échouée", e)
                false
            }
        }
    }

    fun removeVar(rowId: Int, name: String) {
        if (!map.containsKey(rowId)) {
            return
        }

        if (map[rowId] != null) {
            map[rowId]!!.remove(Symbol(rowId, name, ""))
        }
    }


    fun reset(activity: Activity, id: Int) {
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
        save(activity)
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
        return true
    }

    fun copy(context: Context, table: TableOfSymbols): Boolean {
        map = table.map
        route = table.route

        return true
    }


    fun get(rowId: Int, symbolName: String): String? {
        createIfDontExist(rowId)

        val indexOfSymbol: Int = (map[rowId] ?: throw NullPointerException("Symbol row not found"))
            .indexOf(Symbol(rowId, symbolName, ""))
        val hasSymbol: Boolean = indexOfSymbol != -1

        return if (hasSymbol) {
            (map[rowId]
                ?: throw NullPointerException("Symbol row not found"))[indexOfSymbol].v
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
        private val gson: Gson = GsonBuilder()
            .create()

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

class Symbol(var rowId: Int, var n: String, var v: String, var identifier: Int) :
    Parcelable {

    constructor(rowId: Int, n: String, v: String) : this(rowId, n, v, -1)


    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Symbol) return false

        if (other.rowId != rowId) {
            return false
        }

        if (other.n != n) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = rowId
        result = 31 * result + n.hashCode()
        result = 31 * result + v.hashCode()
        return result
    }

    protected constructor(`in`: Parcel) : this(-1, "", "", -1) {
        this.rowId = `in`.readInt()
        this.n = `in`.readString() ?: ""
        this.v = `in`.readString() ?: ""
        this.identifier = `in`.readInt()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Symbol> = object : Parcelable.Creator<Symbol> {
            override fun createFromParcel(`in`: Parcel): Symbol {
                return Symbol(`in`)
            }

            override fun newArray(size: Int): Array<Symbol?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(rowId)
        dest.writeString(n)
        dest.writeString(v)
        dest.writeInt(identifier)
    }
}
