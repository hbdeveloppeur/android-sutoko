package com.purpletear.sutoko.notification.model

import androidx.annotation.Keep
import com.purpletear.sutoko.notification.sealed.Screen
import kotlin.random.Random


@Keep
data class Notification(
    val title: String,
    val message: String,
    val image: String?,
    val destination: String,
    val screen: Screen,
    val id: Int = Random.nextInt(1000),
) {

}
