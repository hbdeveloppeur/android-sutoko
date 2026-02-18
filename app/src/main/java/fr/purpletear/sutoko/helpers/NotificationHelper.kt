package fr.purpletear.sutoko.helpers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.purpletear.sutoko.notification.model.Notification
import fr.purpletear.sutoko.screens.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationHelper @Inject constructor() {

    operator fun invoke(context: Context, notification: Notification) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadBitmap(context, notification.image)

            withContext(Dispatchers.Main) {
                showNotification(context, notification, bitmap)
            }
        }
    }

    private suspend fun loadBitmap(context: Context, imageUrl: String?): Bitmap? {
        if (imageUrl == null) return null
        return withContext(Dispatchers.IO) {
            val futureTarget: FutureTarget<Bitmap> = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()

            try {
                futureTarget.get() // Blocking call, must be run on the IO dispatcher
            } catch (e: Exception) {
                // Handle error
                null
            }
        }
    }

    private fun showNotification(context: Context, notification: Notification, bitmap: Bitmap?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("destination", notification.destination)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification content
        var builder = NotificationCompat.Builder(context, "sutoko_fcm")
            .setSmallIcon(fr.purpletear.sutoko.shop.presentation.R.drawable.sutoko_ic_diamond)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (bitmap != null) {
            builder = builder.setLargeIcon(bitmap)
        }

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notification.id, builder.build())
        }
    }
}