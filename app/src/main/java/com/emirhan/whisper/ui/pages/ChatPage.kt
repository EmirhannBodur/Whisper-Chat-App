package com.emirhan.whisper.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.emirhan.whisper.R
import com.emirhan.whisper.model.ChatMessageModel
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.messageTextColor1
import com.emirhan.whisper.ui.theme.messageTextColor2
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.theme.ozelBorder
import com.emirhan.whisper.ui.viewmodel.ChatViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context, uri: Uri) {
        mediaPlayer?.release()
        mediaPlayer = null

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                prepareAsync() // Asenkron olarak hazırlan
                setOnPreparedListener { mp ->
                    // Hazır olduğunda oynatmaya başla
                    mp.start()
                    Log.d("AudioPlayer", "Playing audio from: $uri")
                }
                setOnCompletionListener { mp ->
                    // Ses bittiğinde serbest bırak
                    mp.release()
                    mediaPlayer = null // Referansı temizle
                    Log.d("AudioPlayer", "Audio playback completed and released.")
                }
                setOnErrorListener { mp, what, extra ->
                    // Hata oluştuğunda serbest bırak ve hata mesajı göster
                    Log.e("AudioPlayer", "Error during playback: what=$what, extra=$extra")
                    mp.release()
                    mediaPlayer = null
                    true // Hatayı tükettik
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error setting data source or preparing MediaPlayer: ${e.message}", e)
                release()
                mediaPlayer = null
            }
        }
    }

}
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    navController: NavController,
    name: String,
    numberID: String,
    receiverUid: String,
    viewModel: ChatViewModel = hiltViewModel()
){
    var messageText by remember { mutableStateOf("") }
    val context=LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }


    //İzin durumları
    val readMediaImagesPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    val readMediaVideoPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_VIDEO)
    val cameraPermissionState= rememberPermissionState(Manifest.permission.CAMERA)
    val readExternalStoragePermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var showPermissionRationalDialog by remember { mutableStateOf(false) }
    var permissionToRequest by remember { mutableStateOf("") }
    var showMediaOptionsDialog by remember { mutableStateOf(false) }
    var showFulScreenImage by remember { mutableStateOf<String?>(null) }


    val audioFileUri=remember { mutableStateOf<Uri?>(null) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            // Composable ekran terk edildiğinde MediaRecorder'ı temizle
            mediaRecorder?.apply {
                try {
                    // Eğer kayıt halindeyse durdur
                    if (isRecording) {
                        stop()
                    }
                    release()
                    Log.d("MediaRecorder", "MediaRecorder released on dispose.")
                } catch (e: Exception) {
                    Log.e("MediaRecorder", "Error releasing MediaRecorder on dispose: ${e.message}")
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoUri.value?.let { uri ->
                    viewModel.sendImageMessage(receiverUid, uri, context)
                }
            }
        }
    )
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), // "image/*" türünde içerik alacak
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.sendImageMessage(receiverUid, it, context)
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), // "video/*" türünde içerik alacak
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.sendVideoMessage(context, it, receiverUid)
            }
        }
    )
    // İzin rasyoneli gösterilecekse bu dialog devreye girer
    if (showPermissionRationalDialog){
        AlertDialog(
            onDismissRequest = { showPermissionRationalDialog = false },
            title = { Text(text = "$permissionToRequest İzni Gerekiyor") },
            text = { Text("$permissionToRequest için bu izne ihtiyacımız var.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationalDialog = false
                    when (permissionToRequest) {
                        "Kamera" -> cameraPermissionState.launchPermissionRequest()
                        "Depolama" -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                readMediaImagesPermissionState.launchPermissionRequest()
                                readMediaVideoPermissionState.launchPermissionRequest()
                            } else {
                                readExternalStoragePermissionState.launchPermissionRequest()
                            }
                        }
                        "Mikrofon" -> recordAudioPermissionState.launchPermissionRequest()
                    }
                }) { Text("İzin Ver") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationalDialog = false }) {
                    Text("İzin Verme")
                }
            }
        )
    }
    // Medya seçim diyaloğu
    if (showMediaOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showMediaOptionsDialog = false },
            title = { Text("Galeri İçeriği Seçin") },
            text = { Text("Galeri'den ne göndermek istersiniz?") },
            confirmButton = {},
            dismissButton = {
                Column {
                    TextButton(onClick = {
                        showMediaOptionsDialog = false
                        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            readMediaImagesPermissionState.status.isGranted
                        } else {
                            readExternalStoragePermissionState.status.isGranted
                        }
                        val shouldShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            readMediaImagesPermissionState.status.shouldShowRationale
                        } else {
                            readExternalStoragePermissionState.status.shouldShowRationale
                        }

                        when {
                            hasPermission -> {
                                imagePickerLauncher.launch("image/*")
                            }
                            shouldShowRationale -> {
                                permissionToRequest = "Depolama"
                                showPermissionRationalDialog = true
                            }
                            else -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    readMediaImagesPermissionState.launchPermissionRequest()
                                } else {
                                    readExternalStoragePermissionState.launchPermissionRequest()
                                }
                            }
                        }
                    }) {
                        Text("Galeriden Fotoğraf Seç")
                    }
                    TextButton(onClick = {
                        showMediaOptionsDialog = false
                        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            readMediaVideoPermissionState.status.isGranted
                        } else {
                            readExternalStoragePermissionState.status.isGranted
                        }
                        val shouldShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            readMediaVideoPermissionState.status.shouldShowRationale
                        } else {
                            readExternalStoragePermissionState.status.shouldShowRationale
                        }

                        when {
                            hasPermission -> {
                                videoPickerLauncher.launch("video/*")
                            }
                            shouldShowRationale -> {
                                permissionToRequest = "Depolama"
                                showPermissionRationalDialog = true
                            }
                            else -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    readMediaVideoPermissionState.launchPermissionRequest()
                                } else {
                                    readExternalStoragePermissionState.launchPermissionRequest()
                                }
                            }
                        }
                    }) {
                        Text("Galeriden Video Seç")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showMediaOptionsDialog = false }) {
                        Text("İptal")
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.listenForMessages(receiverUid)
        viewModel.listenFriendAndBlockStatus(receiverUid)
        viewModel.loadReceiverUserData(receiverUid)
        viewModel.markMessagesAsRead(receiverUid)
    }
    val messages by viewModel.messages.collectAsState()
    val isFriend by viewModel.isFriend.collectAsState()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val receiverName by viewModel.receiverName.collectAsState()
    val receiverPhoto by viewModel.receiverPhotoUrl.collectAsState()


    // Scroll to the latest message whenever messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = receiverPhoto,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            placeholder = painterResource(id = R.drawable.baseline_person_24),
                            error = painterResource(id = R.drawable.baseline_person_24)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = receiverName.ifBlank { name },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = MyCustomFontFamily,
                                color = Color.White
                            )
                            Text(
                                text = numberID,
                                fontSize = 13.sp,
                                fontFamily = MyCustomFontFamily,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ozelBackground)
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                if (!isFriend && !isBlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.addToFriendList(receiverUid, name, numberID)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)) // Mavi
                        ) {
                            Text("Kişilere Ekle", color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.blockUser(receiverUid)
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Engelle", color = Color.White)
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Bottom,
                    state = listState
                ) {
                    items(messages) { message ->
                        MessageBubble(message,
                            onImageonClick = {imageUrl->
                                showFulScreenImage=imageUrl
                            })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ozelBorder,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.Black

                        ),
                        placeholder = { Text("Mesaj yaz...", color = Color.Black) },
                        keyboardActions = KeyboardActions(onSend = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(receiverUid, messageText)
                                messageText = ""
                            }
                        }),
                        trailingIcon = {
                            IconButton(onClick = {
                                when {
                                    cameraPermissionState.status.isGranted -> {
                                        val tempUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            createImageFile(context)
                                        )
                                        photoUri.value = tempUri
                                        cameraLauncher.launch(tempUri)
                                    }
                                    cameraPermissionState.status.shouldShowRationale -> {
                                        permissionToRequest = "Kamera"
                                        showPermissionRationalDialog = true
                                    }
                                    else -> {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                }
                            }) {
                                Icon(painter = painterResource(R.drawable.rounded_photo_camera_24), contentDescription = "Kamera", tint = ozelBackground)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Galeri butonu (medya seçim diyaloğunu açar)
                    IconButton(onClick = { showMediaOptionsDialog = true }) {
                        Icon(imageVector = Icons.Outlined.AddPhotoAlternate, contentDescription = "Galeri", tint = ozelBackground)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Mikrofon butonu
                    IconButton(onClick = {
                        if (isRecording) {
                            // Kaydı durdur ve gönder
                            isRecording = false // Durumu güncelle
                            mediaRecorder?.apply {
                                try {
                                    stop() // Kaydı durdur
                                    release() // Kaynakları serbest bırak
                                    mediaRecorder = null // Referansı temizle
                                    Log.d("MediaRecorder", "Recording stopped and released.")

                                    audioFileUri.value?.let { uri ->
                                        viewModel.sendVoiceMessage(context, uri, receiverUid)
                                        Log.d("MediaRecorder", "Voice message sent: $uri")
                                    }
                                } catch (e: Exception) {
                                    Log.e("MediaRecorder", "Error stopping or releasing MediaRecorder: ${e.message}", e)
                                    // Hata durumunda da kaynakları temizle
                                    mediaRecorder?.release()
                                    mediaRecorder = null
                                    // Kullanıcıya bildirim gösterilebilir
                                    Toast.makeText(context, "Ses kaydı hatası: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } ?: Log.e("MediaRecorder", "MediaRecorder was null when trying to stop.")

                        } else {
                            // Kaydı başlat
                            when {
                                recordAudioPermissionState.status.isGranted -> {
                                    // Yeni bir ses dosyası oluştur
                                    val newAudioFile = createAudioFile(context)
                                    val tempUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        newAudioFile
                                    )
                                    audioFileUri.value = tempUri // Bu URI'yı daha sonra kullanmak için sakla

                                    // MediaRecorder'ı başlat
                                    mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        MediaRecorder(context)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaRecorder()
                                    }

                                    mediaRecorder?.apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // M4A için uygun format
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // Yüksek kaliteli ses için AAC
                                        setAudioSamplingRate(44100)
                                        setAudioEncodingBitRate(96000)
                                        setOutputFile(newAudioFile.absolutePath) // Kaydedilecek dosyanın tam yolu
                                        Log.d("MediaRecorder", "Recording to file: ${newAudioFile.absolutePath}")

                                        try {
                                            prepare()
                                            start()
                                            isRecording = true // Kayıt durumunu güncelle
                                            Log.d("MediaRecorder", "MediaRecorder started recording.")
                                            Toast.makeText(context, "Ses kaydı başladı...", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Log.e("MediaRecorder", "Error preparing or starting MediaRecorder: ${e.message}", e)
                                            release() // Hata durumunda serbest bırak
                                            mediaRecorder = null
                                            isRecording = false // Kayıt başarısız oldu
                                            Toast.makeText(context, "Ses kaydı başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    } ?: Log.e("MediaRecorder", "Failed to initialize MediaRecorder.")
                                }
                                recordAudioPermissionState.status.shouldShowRationale -> {
                                    permissionToRequest = "Mikrofon"
                                    showPermissionRationalDialog = true
                                }
                                else -> {
                                    recordAudioPermissionState.launchPermissionRequest()
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isRecording) Icons.Outlined.Done else Icons.Outlined.Mic, // Bence burası Icons.Outlined.Send değil, Done daha uygun
                            contentDescription = if (isRecording) "Kaydı Bitir ve Gönder" else "Ses Kaydı Başlat",
                            tint = ozelBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(receiverUid, messageText)
                            messageText = ""
                        }
                    }) {
                        Icon(Icons.Outlined.Send, contentDescription = "Gönder", tint = ozelBackground)
                    }
                }
            }
        }
    )
    showFulScreenImage?.let { imageUrl->
        FullScreenImageDialog(
            imageUrl,
            onDismiss = {
                showFulScreenImage=null
            }
        )
    }
}
@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit // Kapatma callback'i
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .pointerInput(Unit){
                    detectTransformGestures { _,pan,zoom,_ ->
                        scale=(scale*zoom).coerceIn(1f,5f)
                        offsetX+=pan.x
                        offsetY+=pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full screen image",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .clickable { onDismiss() }, // Resme tıklayınca da kapatılabilir
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High// Resmi ekrana sığdırmak için
            )
            // Kapatma butonu
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}


@SuppressLint("UseKtx")
@Composable
fun MessageBubble(message: ChatMessageModel,
                  onImageonClick:(String)-> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isCurrentUser = message.senderId == currentUserId
    val context = LocalContext.current

    val bubbleColor = if (isCurrentUser) messageTextColor1 else messageTextColor2
    val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val textColor = if (isCurrentUser) Color.White else Color.White
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(20.dp, 0.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor, bubbleShape)
                .padding(10.dp)
                .widthIn(max = 280.dp)
        ) {
            message.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Fotoğraf",
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp)
                        .clickable { onImageonClick(url) },
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High
                )
                if (message.message.isNotBlank() || message.videoUrl != null || message.audioUrl != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            message.videoUrl?.let { url ->
                // Burada video için bir thumbnail veya play ikonu gösterilebilir.
                // Gerçek video oynatma için ayrı bir Compose VideoPlayer bileşeni gerekli.
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp)
                        .background(Color.Black) // Video alanı için geçici arka plan
                        .clickable {
                            // Video oynatıcıyı başlat
                            //VideoPlayerComposable(videoUrl = url)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.setDataAndType(Uri.parse(url), "video/*")
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                // Cihazda video oynatıcı bulunamadı mesajı gösterilebilir
                                Log.e("MessageBubble", "No app found to open video: ${e.message}")
                                Toast.makeText(context, "Video oynatıcı bulunamadı.", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Video thumbnail'ı veya play ikonu
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play_circle_outline), // Play ikonu (Material Icons'tan alabilirsiniz)
                        contentDescription = "Videoyu Oynat",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                }
                if (message.message.isNotBlank() || message.audioUrl != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            message.audioUrl?.let { url ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        // Ses kaydını oynatma mantığı burada olmalı (ExoPlayer gibi)
                        AudioPlayer.play(context, Uri.parse(url))
                    }) {
                        Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = "Sesi Oynat", tint = Color.White)
                    }
                    Text(
                        text = "Ses Kaydı", // Veya ses kaydının süresi
                        color = textColor,
                        fontSize = 15.sp,
                        fontFamily = MyCustomFontFamily,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                if (message.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            if (message.message.isNotBlank()) {
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 15.sp,
                    fontFamily = MyCustomFontFamily
                )
            }

            message.timestamp?.let { ts ->
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.cacheDir // Geçici dosyalar için cache kullanılıyor
    return File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",         /* suffix */
        storageDir      /* directory */
    )
}
fun createAudioFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val audioFileName = "AUD_${timeStamp}_"
    val storageDir = context.cacheDir
    return File.createTempFile(
        audioFileName,  /* prefix */
        ".m4a",         /* suffix */
        storageDir      /* directory */
    )
}
@Preview(showBackground = true)
@Composable
fun ChatPreview(){
    WhisperTheme {
        //ChatScreen()
    }
}