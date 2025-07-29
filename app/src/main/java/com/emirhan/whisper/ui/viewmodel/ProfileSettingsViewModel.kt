package com.emirhan.whisper.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
): ViewModel() {

    private val _nameUpdateSuccess = MutableStateFlow(false)
    val nameUpdateSuccess: StateFlow<Boolean> = _nameUpdateSuccess

    private val _photoUploadSuccess = MutableStateFlow(false)
    val photoUploadSuccess: StateFlow<Boolean> = _photoUploadSuccess

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl

    private val _currentName = MutableStateFlow("")
    val currentName: StateFlow<String> = _currentName

    init {
        fetchCurrentName()
    }

    private fun fetchCurrentName() {
        val uid = auth.uid ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    _currentName.value = snapshot.getString("name") ?: ""
                    _photoUrl.value = snapshot.getString("profilePhotoUrl")
                }
            }
    }

    fun updateUserName(newName: String) {
        val uid = auth.uid ?: return
        db.collection("users").document(uid)
            .update("name", newName)
            .addOnSuccessListener {
                _nameUpdateSuccess.value = true
                updateUserInSubCollections(newName, _photoUrl.value ?: "")
            }
    }
    fun uploadProfilePhoto(imageUri: Uri,
                           onSuccess: () -> Unit,
                           onFailure: (Exception) -> Unit) {
        val uid = auth.uid ?: return
        val ref = storage.reference.child("profile_photos/$uid.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val photoUrl = downloadUrl.toString()
                    val currentName = _currentName.value

                    // Firestore güncelleme
                    db.collection("users").document(uid)
                        .update("profilePhotoUrl", photoUrl)
                        .addOnSuccessListener {
                            _photoUrl.value = photoUrl
                            _photoUploadSuccess.value = true

                            // İsmin boş olup olmadığını kontrol et
                            if (currentName.isNotBlank()) {
                                updateUserInSubCollections(currentName, photoUrl)
                            }

                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    fun updateUserInSubCollections(newName: String, newPhotoUrl: String) {
        val uid = auth.uid ?: return

        // 1. Tüm kullanıcıları tara
        db.collection("users").get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    val userId = doc.id
                    if (userId == uid) continue // Kendinde değişiklik yapma

                    // 2. Bu kullanıcıda, seni arkadaş olarak eklemiş mi kontrol et
                    val friendRef = db.collection("users")
                        .document(userId)
                        .collection("friends")
                        .document(uid)

                    friendRef.get().addOnSuccessListener { friendDoc ->
                        if (friendDoc.exists()) {
                            // 3. Güncelle
                            friendRef.update(
                                "name", newName,
                                "profilePhotoUrl", newPhotoUrl
                            )
                        }
                    }

                    // 4. ChatList içinde seni eklemişse, orayı da güncelle
                    val chatListRef = db.collection("users")
                        .document(userId)
                        .collection("chatList")
                        .document(uid)

                    chatListRef.get().addOnSuccessListener { chatDoc ->
                        if (chatDoc.exists()) {
                            chatListRef.update(
                                "name", newName,
                                "profilePhotoUrl", newPhotoUrl
                            )
                        }
                    }
                }
            }
    }
    fun resetNameUpdateStatus() {
       _nameUpdateSuccess.value = false
     }

     fun resetPhotoUploadStatus() {
         _photoUploadSuccess.value = false
     }


}
