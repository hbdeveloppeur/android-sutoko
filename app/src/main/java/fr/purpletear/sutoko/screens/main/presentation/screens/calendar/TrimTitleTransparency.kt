package fr.purpletear.sutoko.screens.main.presentation.screens.calendar

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

internal object TrimTitleTransparency : Transformation {
    override val cacheKey: String = "calendar-title-transparency-v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (!input.hasAlpha()) return input
        var left = input.width
        var top = input.height
        var right = -1
        var bottom = -1
        val row = IntArray(input.width)
        for (y in 0 until input.height) {
            input.getPixels(row, 0, input.width, 0, y, input.width, 1)
            for (x in row.indices) {
                if (row[x] ushr 24 < 3) continue
                left = minOf(left, x)
                top = minOf(top, y)
                right = maxOf(right, x)
                bottom = y
            }
        }
        if (right < left || (left == 0 && top == 0 && right == input.width - 1 && bottom == input.height - 1)) {
            return input
        }
        return Bitmap.createBitmap(input, left, top, right - left + 1, bottom - top + 1)
    }
}
