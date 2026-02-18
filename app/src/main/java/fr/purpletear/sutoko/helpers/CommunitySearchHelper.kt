package fr.purpletear.sutoko.helpers

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.smsgame.activities.smsgame.objects.Story
import fr.purpletear.sutoko.custom.PlayerRankInfo
import java.lang.Exception

object CommunitySearchHelper {

    fun search(activity: Activity, sentence: String, onComplete: (ArrayList<Story>) -> Unit) {
        try {
            "https://create.sutoko.app/api/search/story/${formatSentence(sentence)}".httpGet()
                .timeout(7000).responseString f@{ request, response, result ->
                if (activity.isFinishing) {
                    return@f
                } else {
                    when (result) {
                        is com.github.kittinunf.result.Result.Failure -> {
                            val ex = result.getException()
                            println(ex)
                            Handler(Looper.getMainLooper()).post {
                                onComplete(ArrayList())
                            }
                        }
                        is com.github.kittinunf.result.Result.Success -> {
                            val gson = Gson()
                            val results =
                                gson.fromJson(
                                    result.get(),
                                    object : TypeToken<List<UserStorySearchResult?>?>() {}.type
                                ) as ArrayList<UserStorySearchResult>
                            val stories = ArrayList<Story>()
                            results.forEach { r ->
                                stories.add(r.toStory())
                            }

                            Handler(Looper.getMainLooper()).post {
                                onComplete(stories)
                            }
                        }
                    }

                }
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onComplete(ArrayList())
            }
        }
    }


    fun getUserRank(activity: Activity, onComplete: (ArrayList<PlayerRankInfo>) -> Unit) {
        try {
            "https://create.sutoko.app/api/get/users/top-4".httpGet().timeout(7000)
                .responseString f@{ request, response, result ->
                    if (activity.isFinishing) {
                        return@f
                    } else {
                        when (result) {
                            is com.github.kittinunf.result.Result.Failure -> {
                                val ex = result.getException()
                                println(ex)
                                Handler(Looper.getMainLooper()).post {
                                    onComplete(ArrayList())
                                }
                            }
                            is com.github.kittinunf.result.Result.Success -> {
                                val gson = Gson()
                                val results =
                                    gson.fromJson(
                                        result.get(),
                                        object : TypeToken<List<PlayerRankInfo?>?>() {}.type
                                    ) as ArrayList<PlayerRankInfo>

                                Handler(Looper.getMainLooper()).post {
                                    onComplete(results)
                                }
                            }
                        }

                    }
                }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onComplete(ArrayList())
            }
        }
    }

    private fun formatSentence(input: String): String {
        val a = arrayOf(";", "/", "$$$")
        var output = input
        a.forEach {
            output = output.replace(it, "")
        }
        return output
    }
}

class UserStorySearchResult() {
    val id: Int = -1
    val title: String = ""
    val authorCachedName: String = ""
    val themeId: Int = 1
    val points: Int = -1
    val fid: String? = null
    val hasProfilPicture: Boolean = false
    val uid: String = ""
    val likes: Int = -1

    fun toStory(): Story {
        val story = Story()
        story.authorCachedName = authorCachedName
        story.firebaseId = fid ?: ""
        story.title = title
        story.hasProfilPicture = hasProfilPicture
        story.id = id
        story.userId = uid
        story.theme = themeId
        story.likes = likes
        return story
    }
}