package fr.purpletear.friendzone2.activities.chapterSwitcher

import android.app.Activity
import android.widget.EditText
import android.widget.Toast
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

class ChapterSwitcherModel {
    private var isFirstStart = true
    private var symbols : TableOfSymbols = TableOfSymbols(GlobalData.Game.FRIENDZONE2.id)

    /**
     * Request the focus on the edit text
     * @param activity : Activity
     */
    fun gainFocus(activity : Activity) {
        activity.findViewById<EditText>(R.id.cs_edittext).requestFocus()
    }

    /**
     * Determines if it is a first start
     * @return Boolean
     */
    fun isFirstStart(): Boolean {
        val value = isFirstStart
        isFirstStart = false
        return value
    }

    /**
     * Saves the params configurations
     * @param a : Activity
     */
    fun save(a : Activity) {
        symbols.read(a)
        val code = getChapterCode(a)
        if(chapterCodeAllowed(code)) {
            symbols.chapterCode = code
            symbols.save(a)
            a.finish()
           return
        }
        Toast.makeText(a.applicationContext, "Chapter code $code not found.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Determines if the input is allowed
     * @param code: String
     * @return Boolean
     */
    private fun chapterCodeAllowed(code : String) : Boolean {
        val array = arrayOf("1a", "2a", "3a", "4a", "5a", "5b", "6a", "6b", "7a", "7b", "8a", "8b", "8c", "8d", "8e", "8f", "8g", "8h","9a", "9f", "10a", "10b", "10c")
        return array.contains(code)
    }

    /**
     * Returns the chapter's code
     * @param activity
     * @return String
     */
    private fun getChapterCode(activity : Activity) : String {
        return activity.findViewById<EditText>(R.id.cs_edittext).text.toString().replace(" ", "")
    }
}