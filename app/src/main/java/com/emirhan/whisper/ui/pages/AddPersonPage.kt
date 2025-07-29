package com.emirhan.whisper.ui.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.emirhan.whisper.ui.viewmodel.AddPersonViewModel
import com.emirhan.whisper.R
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.theme.ozelBorder
import com.emirhan.whisper.ui.util.AddPersonState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(navController: NavController){
    val viewModel: AddPersonViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val context= LocalContext.current
    var personNumber by remember { mutableStateOf("") }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight=LocalConfiguration.current.screenHeightDp.dp
    var selectedPageIndex by remember { mutableStateOf(1) }
    LaunchedEffect(state) {
        when(state){
            is AddPersonState.Success -> {
                Toast.makeText(context,"Arkadaş başarıyla eklendi", Toast.LENGTH_SHORT).show()
                navController.navigate("person_page")
            }
            is AddPersonState.Error -> {
                Toast.makeText(context,(state as AddPersonState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("ADD PERSON", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold)},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ozelBackground,
                titleContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
    },
        content = { innerpadding->
            Column(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(innerpadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ozelBackground,
                        contentColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 15.dp
                    ),
                    shape = RoundedCornerShape(corner = CornerSize(10.dp)),
                    modifier = Modifier.height(screenHeight*0.4f).width(screenWidth*0.9f)
                ) {
                    Column(verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()) {
                        OutlinedTextField(
                            value = personNumber,
                            onValueChange = {personNumber=it},
                            label = {Text("NUMBER", fontFamily = MyCustomFontFamily, color = Color.White)},
                            placeholder = {Text("Number...", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold, color = Color.White)},
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = ozelBorder,
                                focusedBorderColor = Color.Blue,
                                focusedTextColor = Color.White,
                                focusedPlaceholderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White
                            ),
                            leadingIcon = {Icon(painter = painterResource(R.drawable.outline_person_add_24),contentDescription = null, tint = Color.White)},
                            modifier = Modifier.fillMaxWidth().padding(all = 10.dp)
                        )
                        OutlinedButton(onClick = {
                            if (personNumber.isNotBlank()){
                                viewModel.addFriendByNumberID(personNumber.trim())
                            }else{
                                Toast.makeText(context,"Numara alanı boş olmaz", Toast.LENGTH_SHORT).show()
                            }
                        }, border = BorderStroke(width = 1.dp, color = ozelBorder)) {
                            Text("Add", color = Color.White, fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold)
                        }
                        if (state is AddPersonState.Loading){
                            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp), color = Color.White)
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
                        2 -> navController.navigate("person_page")
                        3 -> navController.navigate("settings_page")
                    }
                }
            )
        }
    )
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddPersonPreview(){
    WhisperTheme {
        //AddPersonScreen()
    }
}