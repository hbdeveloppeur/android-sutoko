package purpletear.fr.purpleteartools

import android.os.Parcel
import android.os.Parcelable

class Symbol(var rowId: Int, var n: String, var v: String, var identifier: Int) :
    Parcelable {

    constructor(rowId: Int, n: String, v: String) : this(rowId, n, v, -1)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Symbol) return false
        if (other.rowId != rowId) return false
        if (other.n != n) return false
        return true
    }

    override fun hashCode(): Int {
        var result = rowId
        result = 31 * result + n.hashCode()
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
