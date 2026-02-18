package fr.purpletear.friendzone2.activities.choice

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Parcelable
import fr.purpletear.friendzone2.R
import purpletear.fr.purpleteartools.Finger
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols


class ChoiceModel(private var symbols : TableOfSymbols) {

    /**
     * Sets the  Activity's listeners
     */
    fun listeners(activity : Activity) {
        Finger.defineOnTouch(
                activity.findViewById(R.id.choice_image_first),
                activity) {

            if (symbols.condition(GlobalData.Game.FRIENDZONE2.id, "backsit", "true"))
                symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id,"choice", "lightbacksit")
            else
                symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id,"choice", "light")

            val i = Intent()
            i.putExtra("symbols", symbols as Parcelable)
            activity.setResult(RESULT_OK, i)
            activity.finish()
        }

        Finger.defineOnTouch(
                activity.findViewById(R.id.choice_image_second),
                activity) {

            if (symbols.condition(GlobalData.Game.FRIENDZONE2.id,"backsit", "true"))
                symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id,"choice", "darkbacksit")
            else
                symbols.addOrSet(GlobalData.Game.FRIENDZONE2.id,"choice", "dark")

            val i = Intent()
            i.putExtra("symbols", symbols as Parcelable)
            activity.setResult(RESULT_OK, i)
            activity.finish()
        }
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