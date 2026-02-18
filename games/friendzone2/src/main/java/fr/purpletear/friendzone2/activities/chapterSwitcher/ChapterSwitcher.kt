package fr.purpletear.friendzone2.activities.chapterSwitcher

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.purpletear.friendzone2.R

class ChapterSwitcher : AppCompatActivity() {
    var model = ChapterSwitcherModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_switcher)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(model.isFirstStart()) {
            model.gainFocus(this)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun save(view: View) {
        model.save(this)
    }
}
