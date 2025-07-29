package com.emirhan.whisper.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.viewmodel.BlockedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedPersonScreen(navController: NavController){
    val viewModel: BlockedViewModel = hiltViewModel()
    val blockedUsers by viewModel.blockedUsers.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.listenBlockedUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLOCKED", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.Medium)},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ozelBackground,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(imageVector = Icons.Outlined.ArrowBack,contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        content = { innerPadding->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.White)) {
                LazyColumn { items(blockedUsers.size){
                    index->
                    val user=blockedUsers[index]
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = user.name,color = Color.Black, fontFamily = MyCustomFontFamily, fontWeight = FontWeight.Medium)
                        IconButton(onClick = {viewModel.unblockUser(user.uid)}) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
                }
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun BlockedPersonPreview(){
    WhisperTheme {
        //BlockedPersonScreen()
    }
}