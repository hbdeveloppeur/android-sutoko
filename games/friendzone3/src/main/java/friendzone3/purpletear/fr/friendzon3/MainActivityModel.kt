package friendzone3.purpletear.fr.friendzon3

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sutokosharedelements.tables.trophies.TableOfCollectedTrophies
import friendzone3.purpletear.fr.friendzon3.MainActivity.Support

class MainActivityModel() : Parcelable {
    var isFirstStartValue = true
        private set
    val collectedTrophies : TableOfCollectedTrophies = TableOfCollectedTrophies()
    /**
     * Determines if it is a first start
     * @return Boolean
     */
    fun isFirstStart(): Boolean {
        val value = isFirstStartValue
        isFirstStartValue = false
        return value
    }


    constructor(parcel: Parcel) : this() {
        isFirstStartValue = parcel.readByte() == 1.toByte()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if(isFirstStartValue) 1.toByte() else 0.toByte())

    }

    override fun describeContents(): Int {
        return 0
    }


    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<MainActivityModel> =
            object : Parcelable.Creator<MainActivityModel> {
                override fun createFromParcel(`in`: Parcel): MainActivityModel {
                    return MainActivityModel(`in`)
                }

                override fun newArray(size: Int): Array<MainActivityModel?> {
                    return arrayOfNulls(size)
                }
            }
    }

    /**
     * Returns the RecyclerView LayoutManager
     *
     * @param a Activity
     * @return LayoutManager
     * @see android.support.v7.widget.RecyclerView.LayoutManager
     */
    fun getRecyclerViewLayoutManager(a: Activity, support: Support): LinearLayoutManager {
        val recyclerView: RecyclerView = when (support) {
            Support.NORMAL -> a.findViewById(R.id.mainActivity_recyclerview)
            Support.SHELL -> a.findViewById(R.id.mainactivity_shell_recyclerview)
            else -> throw IllegalArgumentException()
        }
        return (recyclerView.layoutManager) as LinearLayoutManager
    }


}