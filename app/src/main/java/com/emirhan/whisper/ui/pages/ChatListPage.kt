@file:Suppress("INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING",
    "TYPE_INTERSECTION_AS_REIFIED_WARNING"
)

package com.emirhan.whisper.ui.pages


import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.emirhan.whisper.ui.viewmodel.ChatListViewModel
import com.emirhan.whisper.R
import com.emirhan.whisper.model.ChatListModel
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(navController: NavController){
    val viewModel: ChatListViewModel =hiltViewModel()
    val chatList by viewModel.chatList.collectAsState()
    var selectedPageIndex by remember { mutableStateOf(0) }
    val context= LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadChatList()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // İzin verildi, bildirimleri gösterebilirsin
            } else {
                // İzin verilmedi, kullanıcıya bir mesaj gösterebilirsin
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WHISPER", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.Medium)},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ozelBackground,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Outlined.MoreVert,contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )
    },
        content = { innerPadding ->
            Column(modifier = Modifier
                .padding(innerPadding)
                .background(color = Color.White)
                .fillMaxSize()) {
                LazyColumn(contentPadding = PaddingValues(10.dp), userScrollEnabled = true) {
                    items(chatList){ chat ->
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 5.dp), colors = CardDefaults.cardColors(containerColor = ozelBackground), modifier = Modifier.padding(vertical = 2.dp).combinedClickable(
                            onClick = {
                                val encodedName= Uri.encode(chat.name.ifBlank { "unknown" })
                                val encodedNumberID = Uri.encode(chat.numberID.ifBlank { "0000" })
                                val encodedUid = Uri.encode(chat.uid)
                                navController.navigate("chat_screen/$encodedName/$encodedNumberID/$encodedUid")
                            },
                            onLongClick = {
                                viewModel.deleteChat(chat.uid)
                            }
                        )) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(color = ozelBackground) // Hafif gri arka plan
                                    .padding(12.dp)
                            ) {
                                ChatListItem(chat = chat)
                            }
                        }

                    }
                }
            }
        },
        bottomBar = {
            MyBottomNavigationBar(
                selectedIndex = selectedPageIndex,
                onItemSelected = { index ->
                    selectedPageIndex = index
                    when(index){
                        0 -> navController.navigate("chatlist_page")
                        1 -> navController.navigate("addperson_page")
                        2 -> navController.navigate("person_page") // şu anki sayfa
                        3 -> navController.navigate("settings_page")
                    }
                }
            )
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(chat: ChatListModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chat.profilePhotoUrl,
            contentDescription = null,
            modifier = Modifier
                .padding(all = 5.dp)
                .size(50.dp)
                .clip(CircleShape),
            placeholder = painterResource(id = R.drawable.baseline_person_24),
            error = painterResource(id = R.drawable.baseline_person_24)
        )

        Column(
            modifier = Modifier
                .weight(1f) // ← Sağ tarafta unread göstereceğimiz için burayı değiştirdik
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = chat.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = MyCustomFontFamily,
                color = Color.White
            )
            Text(
                text = chat.lastMessage,
                color = Color.White,
                fontFamily = MyCustomFontFamily
            )
            Text(
                text = formatTimestamp(chat.timestamp),
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Right,
                fontFamily = MyCustomFontFamily
            )
        }

        // 🔴 Eğer unreadCount > 0 ise göster
        if (chat.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(Color.Red, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = chat.unreadCount.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatListPreview(){
    WhisperTheme {
        //ChatListScreen()
    }
}