package com.emirhan.whisper.ui.util

import android.os.Message

sealed class ProfileState {
    object  Loading: ProfileState()
    data class Success (val username: String?,val uid: String?): ProfileState()
    data class Error(val message: String): ProfileState()
}