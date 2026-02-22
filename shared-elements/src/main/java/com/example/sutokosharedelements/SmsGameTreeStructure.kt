package com.example.sutokosharedelements

import android.content.Context
import java.io.File
import java.util.*

/**
 * Files Tree Structure
 * The kit to handle the main files tree structure operations and data
 */
class SmsGameTreeStructure {
    companion object {
        private const val storiesDirectoryName: String = "games"
        private const val userStoriesDirectoryName: String = "ugames"
        private const val storyAssetsDirectoryName: String = "assets"
        private const val charactersAssetsDirName: String = "characters"
        private const val metadataFileName: String = "metadata.json"


        const val userStoriesFileName : String = "stories.json"
        const val userStorCharactersFileName : String = "characters.json"
        const val userStoryLinksFileName : String = "links.json"
        const val userStoryPhrasesFileName : String = "phrases.json"
        const val userStorySideHandlerFileName : String = "sidehandler.json"
        /*** Paths ***/
        /**
         * Returns the Stories directory's path
         * @param context : Context
         * @return String
         */
        fun getStoriesDirectoryPath(context: Context): String {
            return File(context.filesDir,
                storiesDirectoryName
            ).absolutePath + File.separator
        }


        /**
         * Returns the Stories directory's path
         * @param context : Context
         * @return String
         */
        fun getUserStoriesDirectoryPath(context: Context): String {
            return userStoriesDirectoryName + File.separator
        }

        /**
         * Returns the Story's directory path
         * @param context : Context
         * @param storyId : String
         * @return String
         */
        fun getStoryDirectoryPath(context: Context, storyId: String): String {
            return getStoriesDirectoryPath(
                context
            ) + storyId + File.separator
        }



        /**
         * Returns the Story's assets directory
         * @param context : Context
         * @param storyId : String
         * @return String
         */
        private fun getStoryAssetsDirectoryPath(context: Context, storyId: String): String {
            return getStoryDirectoryPath(
                context,
                storyId
            ) + "characters" + File.separator + "assets" + File.separator
        }

        /**
         * Returns the Story's characters table file path
         * @param context : Context
         * @param storyId : String
         * @return String
         */
        private fun getCharactersTableFilePath(context: Context, storyId: String, chapterCode: String, langCode: String): String {
            return getStoryChapterDirPath(
                context,
                storyId,
                chapterCode,
                langCode
            ) + "characters-${chapterCode.uppercase()}.json"
        }

        /**
         * Returns the story's storyMetadata file path
         * @param context : Context
         * @param storyId : String
         * @return String
         */
        private fun getStoryMetadataFilePath(context: Context, storyId: String): String {
            return getStoryDirectoryPath(
                context,
                storyId
            ) + File.separator + metadataFileName
        }

        /**
         * Returns the Story's character profil picture file path
         * @param context : Context
         * @param storyId : String
         * @param characterId : Int
         * @return String
         */
        fun getCharactersPictureFilePath(context: Context, storyId: String, characterId: Int): String {
            return getStoryAssetsDirectoryPath(
                context,
                storyId
            ) + characterId + ".jpeg"
        }

        /**
         * Returns the Story's character profil picture file path
         * @param context : Context
         * @param storyId : String
         * @param characterId : Int
         * @return String
         */
        fun getCharactersPictureFilePathByName(context: Context, storyId: String, filename: String): String {
            return getStoryAssetsDirectoryPath(
                context,
                storyId
            ) + filename
        }


        /**
         * Returns the Story's character profil picture file path
         * @param context : Context
         * @param storyId : String
         * @param characterId : Int
         * @return String
         */
        fun getCharactersMinPictureFilePath(context: Context, storyId: String, characterId: Int): String {
            return getStoryAssetsDirectoryPath(
                context,
                storyId
            ) + characterId + "_min.jpeg"
        }

        /**
         * Returns the requested image name File for the given Story's id
         * @param context : Context
         * @param storyId : String
         * @param nameWithExtension : String
         * @return String
         */
        private fun getStoryBackgroundImageFilePath(context: Context, storyId: String, nameWithExtension: String): String {
            return getMediaDirPath(
                context,
                storyId
            ) +
                    File.separator + nameWithExtension
        }

        /**
         * Returns the requested Story background video's file path
         * @param context: Context
         * @param storyId : String
         * @param videoNameWithExtension : String
         */
        private fun getStoryBackgroundVideoFilePath(context: Context, storyId: String, videoNameWithExtension: String): String {
            return getMediaFilePath(
                context,
                storyId ,
                videoNameWithExtension
            )
        }

        /**
         * Returns the path to the file that contains every Phrase of a given id Story
         * @param context : Context
         * @param storyId : String
         * @return String
         */
        private fun getStoryPhrasesFilePath(context: Context, storyId: String, chapterCode: String, langCode: String): String {
            return getStoryChapterDirPath(
                context,
                storyId,
                chapterCode,
                langCode
            ) + "phrases-${chapterCode.uppercase()}.json"
        }

        /**
         * Returns the path to the file thats contains every Link of a given id Story
         * @param context : Context
         * @param storyId : String
         * @return String
         */
        private fun getStoryLinksFilePath(context: Context, storyId: String, chapterCode: String, langCode: String): String {
            return getStoryChapterDirPath(
                context,
                storyId,
                chapterCode,
                langCode
            ) + "links-${chapterCode.uppercase()}.json"
        }

        /*** Files ***/

        /**
         * Returns the stories' directory File
         * @param context : Context
         * @see File
         */
        fun getStoriesDirectory(context: Context): File {
            return File(
                getStoriesDirectoryPath(
                    context
                )
            )
        }

        /**
         * Returns the stories' directory File
         * @param context : Context
         * @param storyId : String
         * @see File
         */
        fun getStoryDirectory(context: Context, storyId: String): File {
            return File(
                getStoryDirectoryPath(
                    context,
                    storyId
                )
            )
        }

        /**
         * Returns the File object to the Story's Characters list
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getStoryCharactersFile(context: Context, storyId: String, chapterCode: String, langCode: String): File {
            return File(
                getCharactersTableFilePath(
                    context,
                    storyId,
                    chapterCode,
                    langCode
                )
            )
        }

        /**
         * Returns the File object to the Story's Characters list
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getUserStoryFile(context: Context, storyId: String): File {
            return File(
                getUserStoriesDirectoryPath(context) + File.separator + storyId
            )

        }

        /**
         * Returns the File object to the Story's Characters list
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getUserStoryCharactersFile(context: Context, storyId: String): File {
            return File(
                getUserStoriesDirectoryPath(context) + File.separator + storyId + File.separator + userStorCharactersFileName
            )
        }

        /**
         * Returns the File object to the Story's Characters list
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getUserStoryPhrasesFile(context: Context, storyId: String): File {
            return File(
                getUserStoriesDirectoryPath(context) + File.separator + storyId + File.separator + userStoryPhrasesFileName
            )
        }

        /**
         * Returns the File object to the Story's Characters list
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getUserStorySideHandlerFile(context: Context, storyId: String): File {
            return File(
                getUserStoriesDirectoryPath(context) + File.separator + storyId + File.separator + userStorySideHandlerFileName
            )
        }

        /**
         * Returns the File object to the Story's Characters list
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getUserStoryLinksFile(context: Context, storyId: String): File {
            return File(
                getUserStoriesDirectoryPath(context) + File.separator + storyId + File.separator + userStoryLinksFileName
            )
        }

        /**
         * Returns the story's chapter dir
         * @param context: Context
         * @param storyId : String
         * @param chapterCode : String
         * @param langCode : String
         * @return String
         */
        fun getStoryChapterDirPath(context: Context, storyId: String, chapterCode: String, langCode: String): String {
            return getStoryDirectoryPath(
                context,
                storyId
            ) + "chapters" + File.separator + chapterCode.lowercase(
                Locale.ENGLISH) + File.separator + langCode + File.separator
        }

        /**
         * Returns the story's storyMetadata File
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getStoryMetadataFile(context: Context, storyId: String): File {
            return File(
                getStoryMetadataFilePath(
                    context,
                    storyId
                )
            )
        }

        /**
         * Returns the requested Story's background video
         * @param context : Context
         * @param storyId : String
         * @param videoNameWithExtension : String
         * @return File
         */
        fun getStoryBackgroundVideoFile(context: Context, storyId: String, videoNameWithExtension: String): File {
            return File(
                getStoryBackgroundVideoFilePath(
                    context,
                    storyId,
                    videoNameWithExtension
                )
            )
        }

        /**
         * Returns the file that contains every Phrase of a given Story id
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getStoryPhrasesFile(context: Context, storyId: String, chapterCode: String, langCode: String): File {
            return File(
                getStoryPhrasesFilePath(
                    context,
                    storyId,
                    chapterCode,
                    langCode
                )
            )
        }

        /**
         * Returns the file that contains every Link of a given Story id
         * @param context : Context
         * @param storyId : String
         * @return File
         */
        fun getStoryLinksFile(context: Context, storyId: String, chapterCode: String, langCode: String): File {
            return File(
                getStoryLinksFilePath(
                    context,
                    storyId,
                    chapterCode,
                    langCode
                )
            )
        }

        /**
         * Returns the File to the requested image for the given Story id
         * @param context : Context
         * @param storyId : String
         * @param imageNameWithExtension : String
         * @return File
         */
        fun getStoryBackgroundImageFile(context: Context, storyId: String, imageNameWithExtension: String): File {
            return File(
                getStoryBackgroundImageFilePath(
                    context,
                    storyId,
                    imageNameWithExtension
                )
            )
        }

        /**
         * Returns the Media dir path
         * @param context: Context
         * @param storyId : String
         */
        private fun getMediaDirPath(context: Context, storyId: String): String {
            return getStoryDirectoryPath(
                context,
                storyId
            ) + "medias" + File.separator
        }

        /**
         * Returns the Media file path
         * @param context: Context
         * @param storyId : String
         * @param filename : String
         */
        fun getMediaFilePath(context: Context, storyId: String, filename: String): String {
            return getMediaDirPath(
                context,
                storyId
            ) + filename
        }

    }
}
