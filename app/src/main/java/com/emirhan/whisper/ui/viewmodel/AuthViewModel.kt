package com.emirhan.whisper.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emirhan.whisper.ui.util.LoginState
import com.emirhan.whisper.ui.util.RegistrationState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(): ViewModel() {
    private val auth= FirebaseAuth.getInstance()
    private val db= FirebaseFirestore.getInstance()

    var registrationState= mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set
    fun registerUser(email: String,password: String,name: String){
        registrationState.value= RegistrationState.Loading

        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener { result ->
                val uid=result.user?.uid?:return@addOnSuccessListener
                assingUniqueNumberIdToUser(uid,name,email)
            }
            .addOnFailureListener { exception ->
                registrationState.value= RegistrationState.Error("Kayıt Hatası:${exception.message}")
            }
    }
    private fun updateFcmToken(uid: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("AuthViewModel", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                if (token != null) {
                    db.collection("users")
                        .document(uid)
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "FCM token successfully updated for UID: $uid")
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "Error updating FCM token for UID: $uid, Error: ${e.message}", e)
                        }
                } else {
                    Log.w("AuthViewModel", "FCM token is null, cannot update for UID: $uid")
                }
            }
    }

    private fun assingUniqueNumberIdToUser(uid: String,name: String,email: String){
        val numberID=(1000000..9999999).random().toString()

        db.collection("users")
            .whereEqualTo("numberID",numberID)
            .get()
            .addOnSuccessListener { result->
                if (result.isEmpty){
                    val userData=mapOf(
                        "uid" to uid,
                        "name" to  name,
                        "email" to email,
                        "numberID" to numberID,
                        "createAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            registrationState.value= RegistrationState.success
                            updateFcmToken(uid)
                        }
                        .addOnFailureListener {
                            registrationState.value= RegistrationState.Error("Veri FireStore'a kaydedilmedi.")
                        }
                }else{
                    //ID çakıştı,tekrar üret
                    assingUniqueNumberIdToUser(uid,name,email)
                }
            }
            .addOnFailureListener {
                registrationState.value= RegistrationState.Error("ID kontrol hatası${it.message}")
            }
    }
    var loginState= mutableStateOf<LoginState>(LoginState.Idle)
        private set
    fun loginUser(email: String,password: String){
        loginState.value= LoginState.Loading
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                val uid= FirebaseAuth.getInstance().currentUser?.uid
                uid?.let { updateFcmToken(it) }
                loginState.value= LoginState.Success
            }
            .addOnFailureListener { exception ->
                loginState.value= LoginState.Error("Giriş hatası:${exception.message}")
            }
    }
    fun logOut(){
        FirebaseAuth.getInstance().signOut()
    }

}