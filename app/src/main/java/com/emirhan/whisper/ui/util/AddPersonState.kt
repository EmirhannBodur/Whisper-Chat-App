package com.emirhan.whisper.ui.util

sealed class AddPersonState {
    object Idle: AddPersonState()
    object Loading: AddPersonState()
    object Success: AddPersonState()
    data class Error(val message: String): AddPersonState()
}