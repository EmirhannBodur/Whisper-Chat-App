package com.emirhan.whisper.model

data class UserModel(
    val uid: String="",
    val email: String="",
    val numberID: String="",
    val name: String="",
    val profilePhotoUrl: String="",
    val fcmToken: String?=null

)
