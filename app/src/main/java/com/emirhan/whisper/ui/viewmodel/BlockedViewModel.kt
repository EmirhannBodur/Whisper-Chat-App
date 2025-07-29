package com.emirhan.whisper.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


data class BlockedUser (
    val uid: String,
    val name:String,
    val numberID: String,
    val timestamp: Long
)
@HiltViewModel
class BlockedViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _blockedUser=MutableStateFlow<List<BlockedUser>>(emptyList())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUser

    fun listenBlockedUsers() {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(currentUid)
            .collection("blocked")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val documents = snapshot.documents
                    val blockedList = mutableListOf<BlockedUser>()

                    documents.forEach { doc ->
                        val blockedUid = doc.id
                        val timestamp = doc.getLong("timestamp") ?: return@forEach

                        // UID'den name & numberID çek
                        db.collection("users")
                            .document(blockedUid)
                            .get()
                            .addOnSuccessListener { userSnapshot ->
                                val name = userSnapshot.getString("name") ?: "Bilinmiyor"
                                val numberID = userSnapshot.getString("numberID") ?: "Yok"
                                val blockedUser = BlockedUser(blockedUid, name, numberID, timestamp)

                                blockedList.add(blockedUser)
                                _blockedUser.value = blockedList.sortedByDescending { it.timestamp }
                            }
                    }
                }
            }
    }
    fun unblockUser(blockedUid: String) {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(currentUid)
            .collection("blocked")
            .document(blockedUid)
            .delete()
            .addOnSuccessListener {
                _blockedUser.value = _blockedUser.value.filterNot { it.uid == blockedUid }
                Log.d("UNBLOCK", "Kullanıcı engeli kaldırıldı: $blockedUid")
            }
            .addOnFailureListener { e ->
                Log.e("UNBLOCK", "Engel kaldırılamadı: ${e.message}")
            }
    }
}