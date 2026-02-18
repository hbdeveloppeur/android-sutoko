@file:Suppress("SameParameterValue")

package com.purpletear.smsgame.activities.smsgame.objects

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.sharedelements.SutokoSharedElementsData
import com.github.kittinunf.fuel.httpGet
import com.purpletear.smsgame.R
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols
import java.lang.Exception

object UserStoryHelper {

    fun reportIfConfirm(
        activity: Activity?,
        story: Story,
        symbol: TableOfSymbols,
        onConfirm: () -> Unit
    ) {
        if (activity == null) {
            return
        }
        Std.confirm(
            activity,
            R.string.sutoko_report_ustory_confirm_title,
            R.string.sutoko_report_ustory_confirm_text,
            R.string.sutoko_report,
            R.string.sutoko_abort,
            {
                report(activity, story, symbol)
                symbol.save(activity)
                Handler(Looper.getMainLooper()).post(onConfirm)
            },
            {})
    }

    private fun report(activity: Activity?, story: Story, symbol: TableOfSymbols) {
        symbol.addReportedUserStory(story.firebaseId)
        symbol.save(activity ?: return)
        report(activity, story, null)
        Toast.makeText(
            activity.applicationContext,
            activity.getString(R.string.sutoko_report_story_done),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun report(activity: Activity?, story: Story, onComplete: (() -> Unit)?) {
        try {
            SutokoSharedElementsData.getFlagStoryUrl(story.firebaseId).httpGet().timeout(6000)
                .responseString f@{ request, response, result ->
                    if (activity == null || activity.isFinishing) {
                        return@f
                    } else {
                        if (onComplete != null) {
                            Handler(Looper.getMainLooper()).post(onComplete)
                        }
                    }
                }
        } catch (e: Exception) {
            if (onComplete != null) {
                Handler(Looper.getMainLooper()).post(onComplete)
            }
        }
    }
}