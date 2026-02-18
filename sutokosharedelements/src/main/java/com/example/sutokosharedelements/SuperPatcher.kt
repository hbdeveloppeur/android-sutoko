package com.example.sutokosharedelements

import android.content.Context
import java.io.File

object SuperPatcher {

    fun downloadWentWell(localFile : File) : Long {
        return (localFile.length() / 1024)
    }

    fun extractionWentWell(context: Context, storyId : Int, chapterCode : String, langCode : String) : Int {
        return logFilePathsExam(context, storyId, chapterCode, langCode)
    }

    /**
     * Examines the paths and logs it in Crashlytics if a problem has been found.
     */
    private fun logFilePathsExam(context: Context, storyId : Int, chapterCode : String, langCode : String) : Int {
        val paths : ArrayList<File> = ArrayList()
        paths.apply {
            paths.add(File(SmsGameTreeStructure.getStoriesDirectoryPath(context)))
            paths.add(SmsGameTreeStructure.getStoryDirectory(context,storyId))
            paths.add(File(SmsGameTreeStructure.getStoryChapterDirPath(context,storyId, chapterCode, langCode)))
            paths.add(SmsGameTreeStructure.getStoryPhrasesFile(context,storyId, chapterCode, langCode))
            paths.add(SmsGameTreeStructure.getStoryLinksFile(context,storyId, chapterCode, langCode))
            paths.add(SmsGameTreeStructure.getStoryCharactersFile(context,storyId, chapterCode, langCode))
        }

        paths.forEachIndexed { index, file ->
            if(!file.exists()) {
                return index
            }
        }
        return 0
    }
}