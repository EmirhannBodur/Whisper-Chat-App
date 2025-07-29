package com.emirhan.whisper.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhan.whisper.ui.util.AddPersonState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPersonViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _state= MutableStateFlow<AddPersonState>(AddPersonState.Idle)
    val state: StateFlow<AddPersonState> = _state

    fun addFriendByNumberID(numberID: String){
        val currentUser=auth.currentUser?:return
        _state.value= AddPersonState.Loading

        viewModelScope.launch {
            db.collection("users")
                .whereEqualTo("numberID",numberID)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (!queryDocumentSnapshots.isEmpty){
                        val userToAdd=queryDocumentSnapshots.documents[0]
                        val friendUid=userToAdd.getString("uid") ?: ""
                        Log.d("AddPersonViewModel","friendUid=$friendUid")
                        val name=userToAdd.getString("name")?:""
                        if (friendUid==currentUser.uid){
                            _state.value= AddPersonState.Error("Kendini ekleyemezsin")
                            return@addOnSuccessListener
                        }
                        db.collection("users")
                            .document(friendUid)
                            .collection("blocked")
                            .document(currentUser.uid)
                            .get()
                            .addOnSuccessListener { blockedSnapshot ->
                                if(blockedSnapshot.exists()){
                                    _state.value = AddPersonState.Error("Bu kullanıcı engellenmiş")
                                }else{
                                    val friendData=hashMapOf(
                                        "uid" to friendUid,
                                        "name" to name,
                                        "numberID" to numberID
                                    )
                                    db.collection("users")
                                        .document(currentUser.uid)
                                        .collection("friends")
                                        .document(friendUid)
                                        .set(friendData)
                                        .addOnSuccessListener {
                                            _state.value = AddPersonState.Success
                                        }
                                        .addOnFailureListener {
                                            _state.value = AddPersonState.Error("Arkadaş Eklenemedi")
                                        }
                                }
                            }
                            .addOnFailureListener {
                                _state.value = AddPersonState.Error("Engel durumu kontrol edilemedi")
                            }

                    } else {
                        _state.value = AddPersonState.Error("Kullanıcı bulunamadı")
                    }
                }
                .addOnFailureListener {
                    _state.value = AddPersonState.Error("Arama hatası: ${it.message}")
                }
        }
    }
}