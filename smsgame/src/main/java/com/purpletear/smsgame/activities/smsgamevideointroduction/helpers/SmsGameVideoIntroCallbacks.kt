package com.purpletear.smsgame.activities.smsgamevideointroduction.helpers

interface SmsGameVideoIntroCallbacks {

    fun onVideoFound(filename: String, isLooping: Boolean)
    fun onImageFound(filename: String)
    fun onSoundFound(filename: String, delay: Int)
    fun onTextFound(text: String, duration: Int)
    fun onCompletion()
}