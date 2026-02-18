package fr.purpletear.sutoko.objects

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

@Keep
class UserProfile() : Serializable, Parcelable {

    // Name
    @PropertyName("n")
    @get:PropertyName("n")
    var name: String = ""

    @PropertyName("r")
    @get:PropertyName("r")
    var reads: Int = -1

    @PropertyName("l")
    @get:PropertyName("l")
    var likes: Int = -1

    @PropertyName("p")
    @get:PropertyName("p")
    var points: Int = 1

    @PropertyName("hpp")
    @get:PropertyName("hpp")
    var hasProfilePicture: Boolean = false

    @PropertyName("uid")
    @get:PropertyName("uid")
    var uid: String? = null

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<UserProfile> = object : Parcelable.Creator<UserProfile> {
            override fun createFromParcel(`in`: Parcel): UserProfile {
                return UserProfile(`in`)
            }

            override fun newArray(size: Int): Array<UserProfile?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    protected constructor(`in`: Parcel) : this() {
        name = `in`.readString() ?: ""
        reads = `in`.readInt()
        likes = `in`.readInt()
        points = `in`.readInt()
        hasProfilePicture = `in`.readByte() == 1.toByte()
        uid = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(reads)
        dest.writeInt(likes)
        dest.writeInt(points)
        dest.writeByte(if (hasProfilePicture) 1 else 0)
        dest.writeString(uid)
    }
}