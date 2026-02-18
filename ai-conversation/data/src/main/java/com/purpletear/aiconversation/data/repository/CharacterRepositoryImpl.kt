package com.purpletear.aiconversation.data.repository


import com.purpletear.aiconversation.data.BuildConfig
import com.purpletear.aiconversation.data.exception.NoResponseException
import com.purpletear.aiconversation.data.remote.CharacterApi
import com.purpletear.aiconversation.data.remote.dto.toDomain
import com.purpletear.aiconversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.AiCharacterStatus
import com.purpletear.aiconversation.domain.model.AiCharacterWithStatus
import com.purpletear.aiconversation.domain.model.AvatarBannerPair
import com.purpletear.aiconversation.domain.repository.CharacterRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger

class CharacterRepositoryImpl(
    private val api: CharacterApi
) : CharacterRepository {
    private val seed = AtomicInteger(0)

    private var _accountCharacters: MutableStateFlow<List<AiCharacter>> =
        MutableStateFlow(emptyList())
    override val accountCharacters: StateFlow<List<AiCharacter>>
        get() = _accountCharacters


    override suspend fun loadCharacters(
        userId: String?,
        userToken: String?,
    ): Flow<Result<Unit>> = flow {
        val apiResponse = api.getAll(
            userId = userId,
            token = userToken,
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                delay(280L)
                val characters = response.map { it.toDomain() }
                _accountCharacters.value = characters
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            exception.printStackTrace()
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }


    override suspend fun getRandomAvatarAndBannerPair(
        isFemale: Boolean
    ): Flow<Result<AvatarBannerPair>> = flow {
        val apiResponse = api.getRandomAvatarAndBannerPair(
            isFemale = isFemale,
            n = seed.getAndIncrement(),
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                delay(280L)
                emit(Result.success(response.toDomain()))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    private fun insertCharacter(aiCharacter: AiCharacter) {
        _accountCharacters.update {
            val characters = it.toMutableList()
            characters.add(aiCharacter)
            characters
        }
    }

    override suspend fun insertCharacter(
        userId: String,
        token: String,
        firstName: String,
        lastName: String,
        gender: String,
        description: String,
        avatarId: Int?,
        bannerId: Int?,
        styleId: Int,
    ): Flow<Result<Unit>> = flow {


        val apiResponse = api.insertCharacter(
            userId = userId,
            token = token,
            gender = gender,
            firstName = firstName,
            lastName = lastName,
            description = description,
            avatarId = avatarId,
            bannerId = bannerId,
            styleId = styleId,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                val character = response.toDomain(
                    firstName = firstName,
                    lastName = lastName,
                    description = description
                )
                insertCharacter(character)
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun getStatus(
        userId: String,
        characterId: Int
    ): Flow<Result<AiCharacterStatus>> = flow {
        val apiResponse = api.getStatus(
            userId = userId,
            characterId = characterId,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                val status = response.toDomain()
                emit(Result.success(status))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun deleteCharacter(
        userId: String,
        token: String,
        aiCharacter: AiCharacter
    ): Flow<Result<Unit>> = flow {
        val apiResponse = api.deleteCharacter(
            userId = userId,
            token = token,
            aiCharacterId = aiCharacter.id,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let {
                _accountCharacters.update {
                    val characters = it.toMutableList()
                    characters.remove(aiCharacter)
                    characters
                }
                emit(Result.success(Unit))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun getAccessibleCharactersWithStatus(userId: String): Flow<Result<List<AiCharacterWithStatus>>> =
        flow {
            val apiResponse = api.getAccessibleCharactersWithStatus(
                userId = userId,
                appVersion = BuildConfig.VERSION_NAME
            )

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { response ->
                    emit(Result.success(response.map { it.toDomain() }))
                } ?: run {
                    emit(Result.failure(NoResponseException()))
                    return@flow
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                emit(Result.failure(exception))
            }
        }.catch {
            emit(Result.failure(it))
        }

    override suspend fun inviteCharacters(
        userId: String,
        conversationCharacterId: Int,
        characters: List<AiCharacter>
    ): Flow<Result<Unit>> =
        flow {
            val apiResponse = api.inviteCharacters(
                userId = userId,
                characterId = conversationCharacterId,
                charactersId = characters.map { it.id },
                appVersion = BuildConfig.VERSION_NAME
            )

            delay(1000L)

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let {
                    emit(Result.success(Unit))
                } ?: run {
                    emit(Result.failure(NoResponseException()))
                    return@flow
                }
            } else {
                val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
                emit(Result.failure(exception))
            }
        }.catch {
            emit(Result.failure(it))
        }
}