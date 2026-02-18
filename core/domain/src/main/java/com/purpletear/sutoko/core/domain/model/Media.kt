package com.purpletear.sutoko.core.domain.model

import android.os.Parcelable
import androidx.annotation.Keep

@Keep
sealed class Media : Parcelable {
    abstract val id: Long
    abstract val type: String
}
