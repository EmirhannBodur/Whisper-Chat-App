package com.emirhan.whisper.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhan.whisper.model.ChatMessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _messages= MutableStateFlow<List<ChatMessageModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageModel>> = _messages

    private val _isFriend = MutableStateFlow(false)
    val isFriend: StateFlow<Boolean> = _isFriend

    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked


    private val _receiverPhotoUrl = MutableStateFlow<String?>(null)
    val receiverPhotoUrl: StateFlow<String?> = _receiverPhotoUrl

    private val _receiverName = MutableStateFlow("")
    val receiverName: StateFlow<String> = _receiverName

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount


    fun loadReceiverUserData(uid: String) {
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _receiverName.value = snapshot.getString("name") ?: ""
                    _receiverPhotoUrl.value = snapshot.getString("profilePhotoUrl")
                }
            }
    }




    @SuppressLint("UseKtx")
    fun sendImageMessage(receiverId: String, imageUri: Uri, context: Context) {
        val senderId = auth.currentUser?.uid ?: return
        val chatId = generateChatId(senderId, receiverId)
        val storageRef = FirebaseStorage.getInstance().reference
            .child("chat_images/$chatId/${System.currentTimeMillis()}.jpg")

        viewModelScope.launch { // Coroutine başlatıldı
            try {
                val data = withContext(Dispatchers.IO) { // Arka planda sıkıştırma
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream) // Original bitmap
                    inputStream?.close()

                    val MAX_IMAGE_SIZE = 1920 // Maksimum genişlik veya yükseklik. Örneğin Full HD genişliği.
                    // Bu değeri test ederek optimal boyutu bulabilirsin.
                    var finalBitmap = originalBitmap // Son işlem görecek bitmap

                    // Eğer resmin herhangi bir boyutu belirlenen maksimum boyutu aşıyorsa yeniden boyutlandır
                    if (originalBitmap.width > MAX_IMAGE_SIZE || originalBitmap.height > MAX_IMAGE_SIZE) {
                        val ratio = Math.min(
                            MAX_IMAGE_SIZE.toFloat() / originalBitmap.width,
                            MAX_IMAGE_SIZE.toFloat() / originalBitmap.height
                        )
                        val newWidth = (originalBitmap.width * ratio).toInt()
                        val newHeight = (originalBitmap.height * ratio).toInt()
                        finalBitmap = Bitmap.createScaledBitmap(
                            originalBitmap,
                            newWidth,
                            newHeight,
                            true // Filtreleme (anti-aliasing) için true, kaliteyi artırır
                        )
                    }

                    val baos = ByteArrayOutputStream()
                    // Yeniden boyutlandırılmış (veya orijinal) bitmap'i %90 kalitede sıkıştır
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos) // ÖNEMLİ DÜZELTME: %90 kalite ve 'finalBitmap' kullanıldı
                    baos.toByteArray()
                }

                storageRef.putBytes(data) // Sıkıştırılmış byte dizisini yükle
                    .continueWithTask { task ->
                        if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                        storageRef.downloadUrl
                    }
                    .addOnSuccessListener { downloadUrl ->
                        val imageMessage = ChatMessageModel(
                            senderId = senderId,
                            receiverId = receiverId,
                            message = "",
                            imageUrl = downloadUrl.toString(),
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )

                        db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .add(imageMessage)

                        updateChatList(senderId, receiverId, "Fotoğraf")
                    }
                    .addOnFailureListener { e ->
                        println("Image upload failed: ${e.message}")
                        // Kullanıcıya hata bildirimi (Snackbar, Toast vb.)
                    }
            } catch (e: Exception) {
                println("Error processing image: ${e.message}")
                // Kullanıcıya hata bildirimi
            }
        }
    }
    fun sendVideoMessage(context: Context, videoUri: Uri, receiverId: String) {
        val senderId = auth.currentUser?.uid ?: return
        val chatId = generateChatId(senderId, receiverId)
        val videoRef = FirebaseStorage.getInstance().reference
            .child("chat_videos/$chatId/${System.currentTimeMillis()}.mp4")

        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(videoUri)
                val data = inputStream?.readBytes()
                inputStream?.close()

                if (data != null) {
                    videoRef.putBytes(data)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                            videoRef.downloadUrl
                        }
                        .addOnSuccessListener { downloadUrl ->
                            val videoMessage = ChatMessageModel(
                                senderId = senderId,
                                receiverId = receiverId,
                                message = "",
                                videoUrl = downloadUrl.toString(),
                                timestamp = System.currentTimeMillis(),
                                isRead = false
                            )

                            db.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .add(videoMessage)

                            updateChatList(senderId, receiverId, "Video")
                        }
                }
            } catch (e: Exception) {
                println("Video upload error: ${e.message}")
            }
        }
    }
    fun sendVoiceMessage(context: Context, audioUri: Uri, receiverId: String) {
        val senderId = auth.currentUser?.uid ?: return
        val chatId = generateChatId(senderId, receiverId)
        val audioRef = FirebaseStorage.getInstance().reference
            .child("chat_audios/$chatId/${System.currentTimeMillis()}.m4a")

        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(audioUri)
                val data = inputStream?.readBytes()
                inputStream?.close()

                if (data != null) {
                    audioRef.putBytes(data)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                            audioRef.downloadUrl
                        }
                        .addOnSuccessListener { downloadUrl ->
                            val audioMessage = ChatMessageModel(
                                senderId = senderId,
                                receiverId = receiverId,
                                message = "",
                                audioUrl = downloadUrl.toString(),
                                timestamp = System.currentTimeMillis(),
                                isRead = false
                            )

                            db.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .add(audioMessage)

                            updateChatList(senderId, receiverId, "Ses Kaydı")
                        }
                }
            } catch (e: Exception) {
                println("Audio upload error: ${e.message}")
            }
        }
    }
    fun deleteMessageForBothUsers(receiverId: String, messageId: String) {
        val currentUser = auth.currentUser?.uid ?: return
        val chatId = generateChatId(currentUser, receiverId)

        val messageRef = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)

        messageRef.delete()
            .addOnSuccessListener {
                println("Mesaj silindi.")
            }
            .addOnFailureListener { e ->
                println("Silme hatası: ${e.message}")
            }
    }
    fun sendMessage(receiverId: String,message: String){
        val senderId=auth.currentUser?.uid?:return
        val chatId=generateChatId(senderId,receiverId)

        val chatMessage = ChatMessageModel(
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            isRead = false
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(chatMessage)
        updateChatList(senderId,receiverId,message)


       
    }
    fun listenForMessages(receiverId: String) {
        val senderId = auth.currentUser?.uid ?: return
        val chatId = generateChatId(senderId, receiverId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessageModel::class.java)
                    _messages.value = messages

                    val count=messages.count{msg->
                        msg.receiverId==senderId&&!msg.isRead
                    }
                    _unreadCount.value=count
                }
            }
    }
    fun markMessagesAsRead(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = generateChatId(currentUserId, otherUserId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.update("isRead", true)
                }
                resetUnreadCount(otherUserId)
            }
    }
    private fun incrementUnreadCount(receiverId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatListDocRef = db.collection("users")
            .document(receiverId)
            .collection("chatList")
            .document(currentUserId)

        chatListDocRef.update("unreadCount", FieldValue.increment(1))
            .addOnFailureListener {
                // Eğer doküman yoksa oluşturabiliriz
                chatListDocRef.set(mapOf("unreadCount" to 1))
            }
    }
    private fun resetUnreadCount(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatListDocRef = db.collection("users")
            .document(currentUserId)
            .collection("chatList")
            .document(otherUserId)

        chatListDocRef.update("unreadCount", 0)
    }

    fun listenFriendAndBlockStatus(receiverId: String) {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(currentUid)
            .collection("friends")
            .document(receiverId)
            .addSnapshotListener { snapshot, _ ->
                _isFriend.value = snapshot?.exists() == true
            }

        db.collection("users")
            .document(currentUid)
            .collection("blocked")
            .document(receiverId)
            .addSnapshotListener { snapshot, _ ->
                _isBlocked.value = snapshot?.exists() == true
            }
    }

    fun addToFriendList(receiverId: String, name: String, numberID: String) {
        val currentUid = auth.currentUser?.uid ?: return


        db.collection("users").document(receiverId)
            .get()
            .addOnSuccessListener { snapshot ->
                val photoUrl = snapshot.getString("profilePhotoUrl") ?: ""
                val email = snapshot.getString("email") ?: ""

                val friendData = hashMapOf(
                    "uid" to receiverId,
                    "name" to name,
                    "numberID" to numberID,
                    "email" to email,
                    "profilePhotoUrl" to photoUrl
                )

                db.collection("users")
                    .document(currentUid)
                    .collection("friends")
                    .document(receiverId)
                    .set(friendData)
            } }

    fun blockUser(receiverId: String) {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users").document(receiverId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
                    val name=snapshot.getString("name")?:""
                    val email=snapshot.getString("email")?:""
                    val numberID=snapshot.getString("numberID")?:""
                    val blockedData=hashMapOf(
                        "uid" to receiverId,
                        "name" to name,
                        "email" to email,
                        "numberID" to numberID,
                        "timestamp" to System.currentTimeMillis()
                    )
                }
            }

        // 1. Engellenen kişiyi "blocked" koleksiyonuna ekle
        db.collection("users")
            .document(currentUid)
            .collection("blocked")
            .document(receiverId)
            .set(mapOf("timestamp" to System.currentTimeMillis()))

        // 2. Karşı tarafın arkadaş listesinden seni sil
        db.collection("users")
            .document(receiverId)
            .collection("friends")
            .document(currentUid)
            .delete()
    }
    private fun updateChatList(senderId: String, receiverId: String, message: String) {
        val senderRef = db.collection("users").document(senderId)
        val receiverRef = db.collection("users").document(receiverId)

        // 1. Gönderenin chatList güncellemesi (unreadCount yok burada)
        receiverRef.get().addOnSuccessListener { receiverSnapshot ->
            val receiverName = receiverSnapshot.getString("name") ?: ""
            val receiverNumberID = receiverSnapshot.getString("numberID") ?: ""
            val receiverPhotoUrl = receiverSnapshot.getString("profilePhotoUrl") ?: ""

            val senderChat = hashMapOf(
                "uid" to receiverId,
                "name" to receiverName,
                "numberID" to receiverNumberID,
                "lastMessage" to message,
                "timestamp" to System.currentTimeMillis(),
                "profilePhotoUrl" to receiverPhotoUrl
            )

            senderRef.collection("chatList").document(receiverId).set(senderChat)
        }

        // 2. Alıcının chatList güncellemesi (burada unreadCount artırılacak)
        senderRef.get().addOnSuccessListener { senderSnapshot ->
            val senderName = senderSnapshot.getString("name") ?: ""
            val senderNumberID = senderSnapshot.getString("numberID") ?: ""
            val senderPhotoUrl = senderSnapshot.getString("profilePhotoUrl") ?: ""

            val receiverChatDoc = receiverRef.collection("chatList").document(senderId)
            receiverChatDoc.get().addOnSuccessListener { docSnapshot ->
                val currentUnread = docSnapshot.getLong("unreadCount") ?: 0

                val updatedReceiverChat = hashMapOf(
                    "uid" to senderId,
                    "name" to senderName,
                    "numberID" to senderNumberID,
                    "lastMessage" to message,
                    "timestamp" to System.currentTimeMillis(),
                    "profilePhotoUrl" to senderPhotoUrl,
                    "unreadCount" to currentUnread + 1
                )

                receiverChatDoc.set(updatedReceiverChat)
            }
        }
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }
}