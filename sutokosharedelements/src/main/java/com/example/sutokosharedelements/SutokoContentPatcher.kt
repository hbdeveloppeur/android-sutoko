package com.example.sutokosharedelements

import android.app.Activity
import java.io.File

object SutokoContentPatcher {

    fun patch(activity: Activity) {
        val from = activity.getExternalFilesDir(null)
        val to = activity.filesDir

        if(canMove(from, to)) {
            move(from, to)
        }
    }

    private fun canMove(from: File?, to: File) : Boolean {
        return to.canWrite() && from != null && from.canRead()
    }

    private fun move(from : File?, to : File) {

        if(from == null) {
            return
        }

        from.walkTopDown().forEach {
            val name = it.name
            it.renameTo(File(to, name))
        }
    }
}