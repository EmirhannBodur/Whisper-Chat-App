package com.emirhan.whisper.model

data class ChatListModel(
    val uid: String = "",
    val name: String = "",
    val numberID: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val profilePhotoUrl: String = "",
    val unreadCount: Int=0
)
