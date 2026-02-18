package fr.purpletear.friendzone4.game.activities.main

import android.content.Context
import fr.purpletear.friendzone4.R
import java.util.*

object DateTools {


    /**
     * Returns the current day name in French
     *
     * @return String
     */
    @ExperimentalStdlibApi
    fun getCurrentDayName(isFirstLetterUppercase: Boolean, c : Context): String {
        val day = getDay(c)
        return if(isFirstLetterUppercase) {
            day.substring(0, 1).uppercase() + day.substring(1, day.length)
        } else {
            day
        }
    }



    private fun getDay(c : Context) : String {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_WEEK]
        return when (day) {
            Calendar.SUNDAY -> {
                c.getString(R.string.fz4_day_sunday)
            }
            Calendar.MONDAY -> {
                c.getString(R.string.fz4_day_monday)
            }
            Calendar.TUESDAY -> {
                c.getString(R.string.fz4_day_tuesday)
            }
            Calendar.WEDNESDAY -> {
                c.getString(R.string.fz4_day_wednesday)
            }
            Calendar.THURSDAY -> {
                c.getString(R.string.fz4_day_thursday)
            }
            Calendar.FRIDAY -> {
                c.getString(R.string.fz4_day_friday)
            }
            Calendar.SATURDAY -> {
                c.getString(R.string.fz4_day_saturday)
            }
            else -> throw IllegalStateException()
        }
    }
}