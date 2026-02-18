@file:Suppress("JoinDeclarationAndAssignment")

package com.purpletear.smsgame.activities.userStoryLoader

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.sharedelements.Data
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.purpletear.smsgame.activities.smsgame.SmsGameActivity
import com.purpletear.smsgame.activities.smsgame.adapter.AdapterSideHandler
import com.purpletear.smsgame.activities.smsgame.objects.Story
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCharacters
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCreatorResources
import com.purpletear.smsgame.activities.smsgame.tables.TableOfLinks
import com.purpletear.smsgame.activities.smsgame.tables.TableOfPhrases
import purpletear.fr.purpleteartools.Language

internal class UserStoryLoaderModel(activity: UserStoryLoaderActivity) {
    private var misFirstStart: Boolean
    private var currentChapterCode: String
    private var phrases: TableOfPhrases
    private var messageSideHandler: AdapterSideHandler
    private var links: TableOfLinks
    private var characters: TableOfCharacters
    private var instance: FirebaseFirestore
    private var currentLangCode: String
    private var storyId: String? = null
    val creatorResources: TableOfCreatorResources
    val requestManager: RequestManager
    var story: Story?
        private set

    init {
        requestManager = Glide.with(activity)
        story = if (activity.intent.hasExtra(Data.Companion.Extra.STORY.id)) {
            activity.intent.getParcelableExtra(Data.Companion.Extra.STORY.id)
        } else {
            null
        }
        currentLangCode = Language.determineLangDirectory()
        misFirstStart = true
        currentChapterCode = "1a"
        phrases = TableOfPhrases(currentChapterCode, currentLangCode, StoryType.OTHER_USER_STORY)
        links = TableOfLinks(currentChapterCode, currentLangCode, StoryType.OTHER_USER_STORY)
        characters =
            TableOfCharacters(currentChapterCode, currentLangCode, StoryType.OTHER_USER_STORY)
        messageSideHandler = AdapterSideHandler(activity, -1, true)
        instance = FirebaseFirestore.getInstance()
        creatorResources = TableOfCreatorResources()
    }

    fun loadStory(
        activity: UserStoryLoaderActivity,
        onSuccess: (story: Story) -> Unit,
        onError: (error: SutokoError) -> Unit
    ) {
        getStoryIfNecessary(activity, { story ->
            creatorResources.loadEffectList({
                this.story = story
                loadStoryInfo(activity, onSuccess, onError)
            }, onError)
        }, onError)
    }

    /**
     * Load story info
     * @param onSuccess Function0<Unit>
     * @param onError Function1<[@kotlin.ParameterName] SutokoError, Unit>
     */
    private fun loadStoryInfo(
        activity: UserStoryLoaderActivity,
        onSuccess: (story: Story) -> Unit,
        onError: (error: SutokoError) -> Unit
    ) {
        val storyId = story?.firebaseId ?: return
        if (storyId.isBlank()) {

            return
        }
        instance.document("${Data.FIREBASE_COLLECTION_USTORIES}/$currentLangCode/${Data.FIREBASE_COLLECTION_STORIES}/${story!!.firebaseId}/$currentChapterCode/${Data.FIREBASE_DOCUMENT_USTORIES_JSON}")
            .get()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result?.exists() != true) {
                    Handler(Looper.getMainLooper()).post {
                        onError(SutokoError.STORY_NOT_FOUND)
                    }
                    return@addOnCompleteListener
                }
                if (task.isSuccessful) {
                    links.links =
                        ArrayList(StoryValidator.getLinksFromJson(task.result?.get(Data.Companion.FirebaseStoryData.LINKS.fieldName) as String))
                    phrases.array =
                        ArrayList(StoryValidator.getPhrasesFromJson(task.result?.get(Data.Companion.FirebaseStoryData.PHRASES.fieldName) as String))
                    characters.characters =
                        ArrayList(StoryValidator.getCharactersFromJson(task.result?.get(Data.Companion.FirebaseStoryData.CHARACTERS.fieldName) as String))
                    messageSideHandler.left =
                        ArrayList(StoryValidator.getMessageSideFromJson(task.result?.get(Data.Companion.FirebaseStoryData.MESSAGES_SIDE.fieldName) as String))
                    Handler(Looper.getMainLooper()).post {
                        onSuccess(this.story!!)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        onError(SutokoError.UNKNOWN_ERROR)
                    }
                }
            }
    }

    /**
     * Gets the Story if necessary
     * @param onSuccess Function1<[@kotlin.ParameterName] Story, Unit>
     * @param onError Function1<[@kotlin.ParameterName] SutokoError, Unit>
     */
    private fun getStoryIfNecessary(
        activity: UserStoryLoaderActivity,
        onSuccess: (story: Story) -> Unit,
        onError: (error: SutokoError) -> Unit
    ) {
        if (story == null && storyId == null) {
            throw IllegalStateException()
        }
        if (story != null) {
            Handler(Looper.getMainLooper()).post {
                onSuccess(story!!)
            }
            return
        }
        instance.document("${Data.FIREBASE_COLLECTION_USTORIES}/$currentLangCode/${Data.FIREBASE_COLLECTION_STORIES}/$storyId")
            .get()
            .addOnCompleteListener(activity) { task ->
                if (task.result?.exists() != true) {
                    Handler(Looper.getMainLooper()).post {
                        onError(SutokoError.STORY_NOT_FOUND)
                    }
                    return@addOnCompleteListener
                }
                if (task.isSuccessful) {
                    val story = task.result?.toObject(Story::class.java)
                    Handler(Looper.getMainLooper()).post {
                        onSuccess(story!!)
                    }
                    return@addOnCompleteListener
                } else {
                    Handler(Looper.getMainLooper()).post {
                        onError(SutokoErrorHandler.firebaseExceptionToSutokoError(task.exception as FirebaseFirestoreException))
                    }
                }
            }
    }

    fun startActivity(activity: UserStoryLoaderActivity) {
        val intent = SmsGameActivity.require(
            activity,
            story!!,
            StoryType.OTHER_USER_STORY,
            phrases.array,
            links.links,
            characters.characters,
            messageSideHandler.left,
            creatorResources
        )
        activity.startActivity(intent)
    }

    fun handleSharedStoryUrl(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data
        if (Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.lastPathSegment?.also { storyId ->
                this.storyId = storyId
            }
        }
    }

    /**
     * I love you Google <3
     * Determines whether if the user comes from the main screen or from a shared url
     * @return Boolean
     */
    private fun userComesFromMainScreen(): Boolean {
        return story != null
    }

    /**
     * Determines if it is the first start
     * @return Boolean
     */
    fun isFirstStart(): Boolean {
        val v = misFirstStart
        misFirstStart = false
        return v
    }
}