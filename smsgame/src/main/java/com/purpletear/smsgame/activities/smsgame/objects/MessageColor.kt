package com.purpletear.smsgame.activities.smsgame.objects

import com.purpletear.smsgame.BuildConfig

class MessageColor {
    var background: String = "#11FFFFFF"
    var text: String = "#FFF"

    fun setValues(text: String?) {
        if (!this.isValidColorText(text)) {
            if (BuildConfig.DEBUG) {
                throw IllegalStateException("Failed parse color '$text'")
            }
            return
        }

        val values = text!!.split("|")
        this.background = values[0]
        this.text = values[1]
    }

    /**
     *
     * @param text String?
     * @return Boolean
     */
    private fun isValidColorText(text: String?): Boolean {
        if (text.isNullOrBlank()) {
            return false
        }

        val values = text.split("|")
        values.forEach { color ->
            if (color.length != 7 && color.length != 9) {
                return false
            }
        }
        return true
    }
}