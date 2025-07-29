package com.emirhan.whisper

import android.R.attr.title
import android.R.id.message
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // İstersen burada Firestore'a token'ı yazdırabilirsin
        Log.d("FCM_Token", "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_Message", "From: ${remoteMessage.from}")
        Log.d("FCM_Message", "Message data: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Yeni Mesaj"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Yeni bir mesajınız var"

        remoteMessage.notification?.let {
            Log.d("FCM_Message", "Message Notification Body: ${it.body}")
        }

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "chat_messages"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kanal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sohbet Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Whisper uygulamasından gelen mesajlar"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirime tıklanınca MainActivity açılsın
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Buraya kendi ikonunu koy
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun sendRegistrationToServer(token: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid)
            userRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token successfully updated in Firestore for UID: $currentUserUid")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating FCM token in Firestore for UID: $currentUserUid, Error: ${e.message}", e)
                }
        } else {
            // Kullanıcı şu an giriş yapmamışsa, bu token'ı SharedPreferences'e kaydedip
            // kullanıcı giriş yaptığında veya uygulamayı başlattığında Firestore'a yüklemelisin.
            // Bu, uygulamanın ilk açılışında veya logout/login yapmadan token değiştiğinde önemlidir.
            Log.d(TAG, "User not logged in, cannot update FCM token directly in Firestore. Consider saving to SharedPreferences.")
        }
    }

}