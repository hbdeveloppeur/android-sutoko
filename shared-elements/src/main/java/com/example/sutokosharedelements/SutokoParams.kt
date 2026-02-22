package com.example.sutokosharedelements

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.R
import com.google.gson.Gson
import java.io.Serializable

@Keep
class SutokoParams() : Serializable, Parcelable {
    var isSoundActivated: Boolean = true


    protected constructor(`in`: Parcel) : this() {
        isSoundActivated = `in`.readByte() == 1.toByte()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (isSoundActivated) 1 else 0))
    }

    private fun toJson(): String {
        return Gson().toJson(this)
    }

    /**
     * Saves the params.
     *
     * @param c
     */
    fun save(actvity: Activity) {
        val prefs: SharedPreferences = actvity.getSharedPreferences(
            actvity.packageName, Context.MODE_PRIVATE
        )
        prefs.edit().putString("SutokoParams", this.toJson()).apply()
    }

    /**
     * Read params.json and copies it to the this params
     *
     */
    fun read(actvity: Activity) {
        val prefs: SharedPreferences = actvity.getSharedPreferences(
            actvity.packageName, Context.MODE_PRIVATE
        )
        val json = prefs.getString("SutokoParams", Gson().toJson(SutokoParams()))

        try {
            val p = Gson().fromJson(json, SutokoParams::class.java)
            this.copy(p)
        } catch (e: Exception) {
            this.copy(SutokoParams())
        }
    }

    /**
     * Copies a given Param into the current one
     *
     * @param params Params
     */
    private fun copy(params: SutokoParams) {
        isSoundActivated = params.isSoundActivated
    }

    companion object {

        private fun getDefaultName(context: Context): String {
            return context.getString(R.string.sutoko_default_name)
        }

        /**
         * Determines if the given user's name is valid or not
         *
         * @param name String
         * @return boolean
         */
        fun isValidName(context: Context, name: String): Boolean {
            return name == getDefaultName(context)
                    || name.matches("([a-zA-ZäöüßÄËÖÜẞÁÉÍÓÚáàèëùïéíóú\\- ]+)".toRegex())
                    && formatName(name).length >= 3
                    || formatName(name).isEmpty()
        }

        /**
         * Formats the user's name
         *
         * @param name String
         * @return String
         */
        private fun formatName(name: String): String {
            val sb = StringBuilder()
            var sawSpace = false
            val size = name.length
            if (size < 3) {
                return name
            }

            for (i in 0 until size) {
                if (name[i] == ' ' && sawSpace) {
                    continue
                } else if (i == name.length - 1 && name[i] == ' ') {
                    break
                } else
                    sawSpace = name[i] == ' '
                if (i == 0) {
                    sb.append(Character.toUpperCase(name[i]))
                } else {
                    sb.append(name[i])
                }
            }
            return sb.toString()
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SutokoParams> = object : Parcelable.Creator<SutokoParams> {
            override fun createFromParcel(`in`: Parcel): SutokoParams {
                return SutokoParams(`in`)
            }

            override fun newArray(size: Int): Array<SutokoParams?> {
                return arrayOfNulls(size)
            }
        }
    }
}