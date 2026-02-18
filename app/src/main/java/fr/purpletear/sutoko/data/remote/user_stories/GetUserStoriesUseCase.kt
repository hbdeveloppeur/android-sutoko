package fr.purpletear.sutoko.data.remote.user_stories

import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.smsgame.activities.smsgame.objects.Story
import fr.purpletear.sutoko.domain.repository.UserStoriesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUserStoriesUseCase @Inject constructor(
    private val repository: UserStoriesRepository
) {

    /**
     * Returns a flow of Resource<List<Story>> containing the stories that match the given keyword.
     * Initially, the flow emits a loading resource, then it waits for 1280ms before emitting the actual result.
    The keyword passed in is formatted by removing specified characters using the formatSentence() function.
    @param keyword the keyword used to search for stories
    @return a flow of resource containing the matching stories, or an error if something went wrong
     */
    fun getStories(keyword: String): Flow<Resource<List<Story>>> = flow {
        try {
            emit(Resource.Loading())
            delay(1280)
            val result = repository.getStories(
                formatSentence(keyword)
            )
            val stories = result.map { it.toStory() }
            emit(Resource.Success(stories))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }

    /**
     * Formats a given input sentence by removing specified characters.
     * @param input the input sentence to be formatted
     * @return the formatted sentence with specified characters removed
     */
    private fun formatSentence(input: String): String {
        val a = arrayOf(";", "/", "$$$")
        var output = input
        a.forEach {
            output = output.replace(it, "")
        }
        return output
    }
}