package com.emirhan.whisper.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.emirhan.whisper.ui.viewmodel.AuthViewModel
import com.emirhan.whisper.R
import com.emirhan.whisper.model.UserModel
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController){
    val viewModel: AuthViewModel =hiltViewModel()

    val auth= FirebaseAuth.getInstance()
    val db= FirebaseFirestore.getInstance()
    var userState by remember { mutableStateOf<UserModel?>(null) }
    var selectedPageIndex by remember { mutableStateOf(3) }



    LaunchedEffect(Unit) {
        val currentUser=auth.currentUser
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        userState = UserModel(
                            uid = snapshot.getString("uid") ?: "",
                            name = snapshot.getString("name") ?: "",
                            numberID = snapshot.getString("numberID") ?: "",
                            profilePhotoUrl = snapshot.getString("profilePhotoUrl") ?: ""
                        )
                    }
                }
        }
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("SETTINGS", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold)},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ozelBackground,
                titleContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
    },
        content = { innerPadding->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(color = Color.White), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    AsyncImage(
                        model = userState?.profilePhotoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(all = 5.dp)
                            .size(50.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(R.drawable.baseline_person_24),
                        error = painterResource(id = R.drawable.baseline_person_24)
                    )
                    Column(modifier = Modifier.padding(all = 1.dp)) {
                        Text(
                            text = userState?.name?:"Yükleniyor",
                            color = Color.Black,
                            fontSize = 25.sp,
                            fontFamily = MyCustomFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text =userState?.numberID?:"",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontFamily = MyCustomFontFamily,
                            fontWeight = FontWeight.Thin
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 70.dp))
                    IconButton(onClick = {navController.navigate("profile_settings")}, colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Black)) {
                        Icon(imageVector = Icons.Outlined.Edit,contentDescription = null)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable{
                    navController.navigate("blocked_page")
                }) {
                    Icon(imageVector = Icons.Outlined.Lock,contentDescription = null, tint = Color.Black, modifier = Modifier.padding(all = 5.dp))
                    Text("BLOCKED OR RESTRICTED", color = Color.Black, fontFamily = MyCustomFontFamily)
                }
                Text(
                    text = "ÇIKIŞ",
                    color = Color.Red,
                    fontFamily = MyCustomFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable{
                            //Navigate
                            viewModel.logOut()
                            navController.navigate("login_page")
                        }
                )
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


@Preview(showSystemUi = true)
@Composable
fun SettingsPreview(){
    WhisperTheme {
        //SettingsScreen()
    }
}