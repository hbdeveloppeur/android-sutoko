package com.purpletear.smsgame.activities.smsgame.notification
 
import android.os.Parcelable
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import kotlinx.parcelize.Parcelize

@Parcelize
class NotificationSmsGameData(
    val storyid: Int,
    val title: String,
    val subtitle: String,
    val message: String,
    val character: StoryCharacter
) : Parcelable