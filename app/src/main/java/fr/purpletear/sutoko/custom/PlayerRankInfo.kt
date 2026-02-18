package fr.purpletear.sutoko.custom

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class PlayerRankInfo(
    var id: Int = -1,
    var username: String = "",
    var hasPicture: Boolean = false,
    var rank: Int = -1
) : Parcelable