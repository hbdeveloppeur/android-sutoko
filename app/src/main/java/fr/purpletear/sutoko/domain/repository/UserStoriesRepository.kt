package fr.purpletear.sutoko.domain.repository

import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.smsgame.activities.smsgame.objects.Story
import fr.purpletear.sutoko.helpers.UserStorySearchResult
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getStories(): Flow<Resource<List<Story>>>

    fun getStories(startingStory: Story): Flow<Resource<List<Story>>>

    suspend fun getStories(keyword: String): List<UserStorySearchResult>

}