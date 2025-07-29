package com.emirhan.whisper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emirhan.whisper.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonListViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _personList= MutableStateFlow<List<UserModel>>(emptyList())
    val personList: StateFlow<List<UserModel>> = _personList

    fun loadFriends(){
        val currentUser=auth.currentUser?:return
        viewModelScope.launch {
            db.collection("users")
                .document(currentUser.uid)
                .collection("friends")
                .get()
                .addOnSuccessListener { result->
                    val list =result.documents.mapNotNull { doc->
                        doc.toObject(UserModel::class.java)
                    }
                    _personList.value=list
                }
                .addOnFailureListener {
                    _personList.value=emptyList()
                }
        }
    }
}