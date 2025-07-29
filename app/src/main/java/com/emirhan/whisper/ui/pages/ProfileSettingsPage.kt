package com.emirhan.whisper.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.emirhan.whisper.R
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.viewmodel.ProfileSettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileSettingsScreen(navController: NavController){
    val viewModel: ProfileSettingsViewModel = hiltViewModel()
    val username = viewModel.currentName.collectAsState()
    var newName by remember { mutableStateOf("") }
    val photoUrl by viewModel.photoUrl.collectAsState()
    val context= LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showRationaleDialog by remember { mutableStateOf(false) }

    @SuppressLint("UseKtx")
    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "ProfilePhoto", null)
        return Uri.parse(path)
    }
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.uploadProfilePhoto(it, onSuccess = {
                    Toast.makeText(context, "Fotoğraf yüklendi", Toast.LENGTH_SHORT).show()
                }, onFailure = {
                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                })
            }
        }

    val launcherCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = getImageUriFromBitmap(context, it)
                uri?.let {
                    viewModel.uploadProfilePhoto(it, onSuccess = {
                        Toast.makeText(context, "Fotoğraf yüklendi", Toast.LENGTH_SHORT).show()
                    }, onFailure = {
                        Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }

    // İzin gerekçesi dialog'u
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Kamera İzni Gerekli") },
            text = { Text("Profil fotoğrafınızı çekebilmemiz için kamera iznine ihtiyacımız var. Lütfen izin verin.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    cameraPermissionState.launchPermissionRequest()
                }) {
                    Text("İzin Ver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("PROFILE SETTINGS", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold)},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ozelBackground,
                titleContentColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = {navController.popBackStack()}) {
                    Icon(imageVector = Icons.Outlined.ArrowBack,contentDescription = null)
                }
            }
        )
    },
        content = { innerPadding->
            Column(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = Color.White), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(all = 5.dp)
                        .size(150.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.baseline_person_24)
                )
                Row {
                    Button(onClick = { launcherGallery.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = ozelBackground, contentColor = Color.White)) {
                        Text("Galeriden Seç")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        when {
                            cameraPermissionState.status.isGranted -> {
                                launcherCamera.launch(null)
                            }
                            cameraPermissionState.status.shouldShowRationale -> {
                                // Kullanıcıya neden izne ihtiyacımız olduğunu açıklayan bir dialog göster
                                showRationaleDialog = true
                            }
                            else -> {
                                // İlk kez izin istenecek veya kullanıcı "Tekrar Sorma" demiş olabilir
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = ozelBackground, contentColor = Color.White)) {
                        Text("Kamera")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = newName,
                    onValueChange = {newName=it},
                    placeholder = {Text("Mevcut İsim:$username")},
                    label = {Text("New Name")},
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = ozelBackground,
                        unfocusedPlaceholderColor = Color.Black,
                        focusedPlaceholderColor = Color.Black,
                        focusedLabelColor = ozelBackground,
                        focusedIndicatorColor = ozelBackground,
                        unfocusedLabelColor = ozelBackground,
                        unfocusedIndicatorColor = ozelBackground
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = {
                    if (newName.isNotBlank()) { // Boş değilse güncelle
                        viewModel.updateUserName(newName)
                    } else {
                        Toast.makeText(context, "Yeni isim boş olamaz.", Toast.LENGTH_SHORT).show()
                    }
                }, enabled = newName.isNotBlank()&&newName!=username.value, colors = ButtonDefaults.buttonColors(containerColor = ozelBackground, contentColor = Color.White)) {
                    Text("Save")
                }
                val nameUpdateSuccessful by viewModel.nameUpdateSuccess.collectAsState()
                LaunchedEffect(nameUpdateSuccessful) {
                    if (nameUpdateSuccessful) {
                        Toast.makeText(context, "İsim başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                        newName = "" // Input alanını temizle
                        viewModel.resetNameUpdateStatus() // ViewModel'de bu state'i sıfırlayacak bir fonksiyon ekle
                    }
                }
                val photoUploadSuccessful by viewModel.photoUploadSuccess.collectAsState()
                LaunchedEffect(photoUploadSuccessful) {
                    if (photoUploadSuccessful) {

                        viewModel.resetPhotoUploadStatus() // ViewModel'de bu state'i sıfırlayacak bir fonksiyon ekle
                    }
                }
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun ProfileSettingsPreview(){
    WhisperTheme {
        //ProfileSettingsScreen()
    }
}