package fr.purpletear.sutoko.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("purpleteartools", "Refreshed token: $token")
    }
}