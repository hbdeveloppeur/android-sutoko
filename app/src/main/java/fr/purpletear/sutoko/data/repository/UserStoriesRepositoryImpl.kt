package fr.purpletear.sutoko.data.repository

import com.example.sutokosharedelements.Data
import com.example.sutokosharedelements.SutokoSharedElementsData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.smsgame.activities.smsgame.objects.Story
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.domain.repository.UserStoriesApi
import fr.purpletear.sutoko.domain.repository.UserStoriesRepository
import fr.purpletear.sutoko.helpers.UserStorySearchResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserStoriesRepositoryImpl @Inject constructor(
    private val dao: FirebaseFirestore,
    private val api: UserStoriesApi,
    private val langCode: String
) :
    UserStoriesRepository {

    override fun getStories(): Flow<Resource<List<Story>>> = callbackFlow {
        dao.collection("ustories/${langCode}/stories")
            .whereEqualTo("io", true)
            .whereEqualTo("b", false)
            .orderBy("is", Query.Direction.DESCENDING)
            .orderBy("p", Query.Direction.DESCENDING)
            .limit(5.toLong())
            .get().addOnSuccessListener { storiesSnapshot ->
                if (storiesSnapshot.documents.size == 0) {
                    trySend(Resource.Success(listOf()))
                    return@addOnSuccessListener
                }
                val position = storiesSnapshot.documents.size - 1
                val stories: List<Story> =
                    Story.getStoriesFromFirebaseDocumentSnapshot(storiesSnapshot)
                trySend(Resource.Success(stories))
            }
            .addOnFailureListener {
                if (BuildConfig.DEBUG && SutokoSharedElementsData.STRICT_MODE) {
                    throw it
                }
                trySend(Resource.Error(it))
            }
        awaitClose {
            close()
        }
    }

    override fun getStories(startingStory: Story): Flow<Resource<List<Story>>> = callbackFlow {
        trySend(Resource.Loading())
        delay(1280)
        try {
            dao
                .document("${Data.FIREBASE_COLLECTION_USTORIES}/${langCode}/${Data.FIREBASE_COLLECTION_STORIES}/${startingStory.firebaseId}")
                .get().addOnSuccessListener {
                    dao.collection("${Data.FIREBASE_COLLECTION_USTORIES}/$langCode/${Data.FIREBASE_COLLECTION_STORIES}")
                        .whereEqualTo("io", true)
                        .orderBy("is", Query.Direction.DESCENDING)
                        .orderBy("p", Query.Direction.DESCENDING)
                        .startAfter(it)
                        .limit(3).get().addOnCompleteListener { task ->
                            if (task.result != null && task.isSuccessful) {
                                val stories: List<Story> =
                                    Story.getStoriesFromFirebaseDocumentSnapshot(task.result!!)
                                trySend(Resource.Success(stories))
                            } else {
                                trySend(Resource.Error(task.exception))
                            }
                        }
                }.addOnFailureListener {
                    trySend(Resource.Error(it))
                }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG && SutokoSharedElementsData.STRICT_MODE) {
                throw e
            }
            trySend(Resource.Error(e))
        }

        awaitClose {
            close()
        }
    }

    override suspend fun getStories(keyword: String): List<UserStorySearchResult> {
        return api.getStories(keyword)
    }
}