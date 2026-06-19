package fr.purpletear.sutoko.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.purpletear.aiconversation.domain.enums.ContentType
import com.purpletear.aiconversation.domain.messaging.ImageGenerationRequestMessageHandler
import com.purpletear.aiconversation.domain.messaging.NewCharacterMessageHandler
import com.purpletear.sutoko.domain.repository.UserConfigRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var imageGenerationRequestMessageHandler: ImageGenerationRequestMessageHandler

    @Inject
    lateinit var newCharacterMessageHandler: NewCharacterMessageHandler

    @Inject
    lateinit var userConfigRepository: UserConfigRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        when (remoteMessage.data["contentType"]) {
            ContentType.ImageGenerationRequest.code -> {
                serviceScope.launch {
                    imageGenerationRequestMessageHandler.handleMessage(remoteMessage.data)
                }
            }

            ContentType.CharacterMessage.code -> {
                serviceScope.launch {
                    newCharacterMessageHandler.handleMessage(remoteMessage.data)
                }
            }

            else -> {
                Log.d(TAG, "Unhandled contentType: ${remoteMessage.data["contentType"]}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            runCatching {
                userConfigRepository.updateDeviceToken().first()
            }.fold(
                onSuccess = { result ->
                    Log.d(TAG, "onNewToken: $result")
                },
                onFailure = { error ->
                    Log.e(TAG, "onNewToken failed", error)
                }
            )
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }
}
