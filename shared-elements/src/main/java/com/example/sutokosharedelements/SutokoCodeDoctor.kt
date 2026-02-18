package com.example.sharedelements

import android.content.Context
import com.example.sharedelements.SmsGameTreeStructure
import purpletear.fr.purpleteartools.GameLanguage
import java.io.File
import java.lang.StringBuilder

object SutokoCodeDoctor {

    /**
     * Examines the paths and logs it in Crashlytics if a problem has been found by calling logFilePathsExam
     */
    fun examinStoryFiles(context: Context, storyId: Int) : String {
        val paths = ArrayList<String>()
        paths.add(SmsGameTreeStructure.getStoriesDirectory(context).path)
        paths.add(SmsGameTreeStructure.getStoryDirectoryPath(context, storyId))
        paths.add(SmsGameTreeStructure.getStoryChapterDirPath(context, storyId, "1A", GameLanguage.determineLangDirectory()))
        paths.add(SmsGameTreeStructure.getStoryPhrasesFile(context, storyId, "1A", GameLanguage.determineLangDirectory()).path)
        paths.add(SmsGameTreeStructure.getStoryLinksFile(context, storyId, "1A", GameLanguage.determineLangDirectory()).path)
        paths.add(SmsGameTreeStructure.getStoryCharactersFile(context, storyId, "1A", GameLanguage.determineLangDirectory()).path)
        return logFilePathsExam(paths)
    }

    /**
     * Examines the paths and logs it in Crashlytics if a problem has been found.
     */
    private fun logFilePathsExam(paths : ArrayList<String>) : String {
        val sb : StringBuilder = StringBuilder()
        var hasError : Boolean = false

        paths.forEach {
            path ->
            val file = File(path)
            if(!file.exists()) {
                hasError = true
            }
            val exists = if(file.exists()) {
                "exists"
            } else {
                "doesn't exist"
            }

            val isReadable = if(file.canRead()) {
                "is readable"
            } else {
                "is not readable"
            }

            val isWritable = if(file.canWrite()) {
                "is writable"
            } else {
                "is not writable"
            }

            val isExecutable = if(file.canExecute()) {
                "is writable"
            } else {
                "is not writable"
            }

            sb.append("'$path'<->'${file.absolutePath}'/$exists, $isReadable, $isWritable, $isExecutable;")
        }
        if(!hasError) {
            return "success"
        }
        return sb.toString()
    }
}