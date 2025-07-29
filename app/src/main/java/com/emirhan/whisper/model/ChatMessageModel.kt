package com.emirhan.whisper.model

data class ChatMessageModel(
    val senderId: String="",
    val receiverId: String = "",
    val message: String = "",
    val imageUrl: String?=null,
    val videoUrl: String?=null,
    val audioUrl: String?=null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
