package fr.purpletear.sutoko.objects

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
@Keep
class CalendarEvent(
    var id: String,
    var type: String,
    var players: Int,
    @PropertyName("cp")
    @get:PropertyName("cp")
    @SerializedName("cp")
    var catchingPhrase: String,
    var title: String,
    var subtitle: String,
    @PropertyName("subsubtitle")
    @get:PropertyName("subsubtitle")
    @SerializedName("subsubtitle")
    var subSubtitle: String?,
    var itemId: Int?,
    @PropertyName("background.image")
    @get:PropertyName("background.image")
    @SerializedName("background.image")
    var backgroundImageUrl: String,
    @PropertyName("background.video")
    @get:PropertyName("background.video")
    @SerializedName("background.video")
    var backgroundVideoUrl: String?,
    var keywords: String
) : Serializable, Parcelable

fun CalendarEvent.nbPlayersToString(): String? {
    return when {
        players >= 1000000 -> "1M"
        players >= 700000 -> "700k"
        players >= 600000 -> "600k"
        players >= 500000 -> "500k"
        players >= 400000 -> "400k"
        players >= 300000 -> "300k"
        players >= 200000 -> "200k"
        players >= 100000 -> "100k"
        players >= 50000 -> "50k"
        players >= 10000 -> "10k"
        players >= 5000 -> "5k"
        else -> return null
    }
}

fun CalendarEvent.isGame(): Boolean {
    return this.type == "story"
}

fun CalendarEvent.isUpdate(): Boolean {
    return this.type == "update"
}
