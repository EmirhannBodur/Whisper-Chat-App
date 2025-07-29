package com.emirhan.whisper.ui.util

sealed class RegistrationState {
    object Idle: RegistrationState()
    object Loading: RegistrationState()
    object success: RegistrationState()
    data class Error(val message: String): RegistrationState()
}