package fr.purpletear.sutoko.popup.domain

import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Stable
@Keep
data class PopUpEvent(
    val tag: String?,
    val event: PopUpUserInteraction,
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                (other is PopUpEvent &&
                        this.tag == other.tag &&
                        this.event == other.event)
    }

    override fun hashCode(): Int {
        var result = tag?.hashCode() ?: 0
        result = 31 * result + event.hashCode()
        return result
    }
}