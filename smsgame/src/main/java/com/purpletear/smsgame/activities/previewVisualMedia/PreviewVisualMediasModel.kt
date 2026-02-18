package com.purpletear.smsgame.activities.previewVisualMedia

import com.bumptech.glide.RequestManager

class PreviewVisualMediasModel(
    var requestManager: RequestManager,
    val arrayOfDrawableIds: ArrayList<String>,
    var currentPosition: Int
) {

    fun hasNext(): Boolean {
        return arrayOfDrawableIds.count() > currentPosition + 1;
    }

    fun hasPrevious(): Boolean {
        return currentPosition > 0;
    }

    /**
     * Update the currentPosition cursor if possible
     * return true if the operation succeed
     * @return Boolean
     */
    fun next(): Boolean {
        if (hasNext()) {
            currentPosition++
            return true
        }
        return false
    }

    /**
     * Update the currentPosition cursor if possible
     * return true if the operation succeed
     * @return Boolean
     */
    fun previous(): Boolean {
        if (hasPrevious()) {
            currentPosition--
            return true
        }
        return false
    }

    /**
     * Returns the current drawable id using the currentPosition cursor
     * @return Int
     */
    fun getCurrentDrawableId(): String {
        return arrayOfDrawableIds[currentPosition]
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    private var isFirstStart = true

    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }
}