package purpletear.fr.purpleteartools

import android.content.Context
import android.widget.TextView

object TextViewHelper {


    /**
     * Revisited MaxWidth
     */
    fun replaceBreakWordByNewLines(textView: TextView, context: Context, maxWidthDp: Int) {
        val maxWidth = Measure.px(maxWidthDp, context)
        var formattedText = ""
        var workingText = ""
        val texts = textView.text.split(" ")
        val paint = textView.paint
        for (section in texts) {
            val newPart = (if (workingText.isNotEmpty()) " " else "") + section
            workingText += newPart
            val width = paint.measureText(workingText, 0, workingText.length).toInt()
            if (width > maxWidth) {
                formattedText += (if (formattedText.isNotEmpty()) "\n" else "") + workingText.substring(
                    0,
                    workingText.length - newPart.length
                )
                workingText = section
            }
        }
        if (workingText.isNotEmpty()) formattedText += (if (formattedText.isNotEmpty()) "\n" else "") + workingText
        textView.text = formattedText
    }
}