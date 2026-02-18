package com.purpletear.framework.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.purpletear.aiconversation.domain.enums.ContentType
import com.purpletear.aiconversation.domain.messaging.ImageGenerationRequestMessageHandler
import com.purpletear.aiconversation.domain.messaging.NewCharacterMessageHandler
import com.purpletear.aiconversation.domain.repository.UserConfigRepository
import dagger.hilt.android.AndroidEntryPoint
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    @Inject
    lateinit var customer: Customer

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {

        }

        when (remoteMessage.data["contentType"]) {
            ContentType.ImageGenerationRequest.code -> {
                CoroutineScope(Dispatchers.IO).launch {
                    imageGenerationRequestMessageHandler.handleMessage(remoteMessage.data)
                }
            }

            ContentType.CharacterMessage.code -> {
                CoroutineScope(Dispatchers.IO).launch {
                    newCharacterMessageHandler.handleMessage(remoteMessage.data)
                }
            }
        }
    }


    override fun onNewToken(token: String) {
        if (!customer.isUserConnected()) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {

            userConfigRepository.updateDeviceToken(
                userId = customer.getUserId(),
                userToken = customer.getUserToken(),
            ).collect {
                it.fold(
                    onSuccess = {
                        Log.d("MyFirebaseMessagingService", "onNewToken: $it")
                    },
                    onFailure = {
                        Log.d("MyFirebaseMessagingService", "onNewToken: $it")
                    }
                )
            }
        }
        super.onNewToken(token)
    }
}