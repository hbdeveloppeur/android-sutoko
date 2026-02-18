package com.purpletear.smsgame.activities.manga

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import purpletear.fr.purpleteartools.TableOfSymbols

object MangaHelper {

    fun drawText(
        context: Context,
        onBitmap: Bitmap,
        text: String,
        textSize: Float,
        xPercent: Float,
        yPercent: Float,
        constrainedWidthPercent: Float
    ): Bitmap {

        // Get text dimensions
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
        textPaint.style = Paint.Style.FILL
        textPaint.color = Color.parseColor("#000000")
        textPaint.textSize = textSize
        textPaint.typeface = Typeface.createFromAsset(context.assets, "fonts/manga.ttf")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textPaint.letterSpacing = 0.1f
        }

        // Check if we're running on Android 6.0 or higher -
        val calculatedWidth: Int = (constrainedWidthPercent * onBitmap.width / 100).toInt()
        val mTextLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(
                text as CharSequence,
                0,
                text.length,
                textPaint,
                calculatedWidth
            ).setAlignment(
                Layout.Alignment.ALIGN_CENTER
            )
                .setLineSpacing(0f, 1f)
                .setIncludePad(false).build()
        } else {
            StaticLayout(
                text,
                textPaint,
                calculatedWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                false
            )
        }

        // Create bitmap and canvas to draw to
        val c = Canvas(onBitmap)

        // Draw text
        c.save()

        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)

        val x: Float =
            (xPercent * (onBitmap.width.toFloat()) / 100) - (mTextLayout.width.toFloat()) / 2

        c.translate(
            x,
            (yPercent * onBitmap.height / 100) - mTextLayout.height / 2
        )
        mTextLayout.draw(c)
        c.restore()
        return onBitmap
    }

    fun parseMessage(phrase: Phrase, symbols: TableOfSymbols): MangaMessage? {
        val values = phrase.sentence.split("\n")
        if (values.size < 4) {
            return null
        }
        val map = mutableMapOf<String, String>()
        values.forEach { text ->
            val v = text.split(":")
            if (v.size == 2) {
                map[v[0]] = v[1]
            }
        }
        val message = MangaMessage()
        message.text = (map["text"] ?: "").replace("[prenom]", symbols.firstName)
        message.size = (map["size"] ?: "30").toFloat()
        message.x = (map["x"] ?: "1").toFloat()
        message.y = (map["y"] ?: "1").toFloat()
        message.w = (map["w"] ?: "10").toFloat()
        return message
    }
}