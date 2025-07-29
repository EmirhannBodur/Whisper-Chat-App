package com.emirhan.whisper.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.emirhan.whisper.model.ChatListModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel(){
    private val _chatList= MutableStateFlow<List<ChatListModel>>(emptyList())
    val chatList: StateFlow<List<ChatListModel>> = _chatList

    fun loadChatList() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(currentUid)
            .collection("chatList")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val chatDocs = snapshot.documents
                    val tempList = mutableListOf<ChatListModel>()

                    if (chatDocs.isEmpty()) {
                        _chatList.value = emptyList()
                        return@addSnapshotListener
                    }
                    for (doc in chatDocs) {
                        val chat = doc.toObject(ChatListModel::class.java)

                        if (chat != null) {
                            val chatId = generateChatId(currentUid, chat.uid)

                            db.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .whereEqualTo("receiverId", currentUid)
                                .whereEqualTo("isRead", false)
                                .get()
                                .addOnSuccessListener { unreadSnapshot ->
                                    val unreadCount = unreadSnapshot.size()
                                    val updatedChat = chat.copy(unreadCount = unreadCount)
                                    tempList.add(updatedChat)

                                    // 🔁 Tüm sohbetlerin unread sorgusu bittiyse listeyi güncelle
                                    if (tempList.size == chatDocs.size) {
                                        _chatList.value = tempList.sortedByDescending { it.timestamp }
                                    }
                                }
                        }
                    }
                } else {
                    _chatList.value = emptyList()
                }
            }
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    fun deleteChat(uidToDelete: String) {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(currentUid)
            .collection("chatList")
            .document(uidToDelete)
            .delete()
    }
}