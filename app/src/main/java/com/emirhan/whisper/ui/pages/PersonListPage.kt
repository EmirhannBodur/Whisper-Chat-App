package com.emirhan.whisper.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.emirhan.whisper.R
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.theme.ozelBorder
import com.emirhan.whisper.ui.viewmodel.PersonListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonListScreen(navController: NavController){
    val viewModel: PersonListViewModel = hiltViewModel()
    var searchPerson by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val personList by viewModel.personList.collectAsState()
    var selectedPageIndex by remember { mutableStateOf(2) }

    val filteredAndSortedList=remember(personList,searchPerson) {
        personList
            .filter { it.name.contains(searchPerson,ignoreCase = true)|| it.numberID.contains(searchPerson,ignoreCase = true) }
            .distinctBy { it.uid }
            .sortedBy { it.name }
    }

    val coroutineScope= rememberCoroutineScope()
    val listState= rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (expanded){
                        TextField(
                            value = searchPerson,
                            onValueChange = {searchPerson=it},
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {Text("Ara")},
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedPlaceholderColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedContainerColor = ozelBackground,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                selectionColors = TextSelectionColors(handleColor = Color.White, backgroundColor = ozelBorder),
                                unfocusedContainerColor = ozelBackground,
                                unfocusedPlaceholderColor = Color.White,
                                focusedIndicatorColor = Color.Blue

                            )
                        )
                    }else{
                        Text("PERSONS", fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                },
                navigationIcon = {
                    if (expanded){
                        IconButton(onClick = {
                            expanded=false
                            searchPerson=""
                        }) {
                            Icon(Icons.Outlined.ArrowBack,contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (!expanded){
                        IconButton(onClick = {expanded=true}) {
                            Icon(Icons.Outlined.Search,contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ozelBackground,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )
        },
        content = { innerPadding->
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.White), horizontalAlignment = Alignment.CenterHorizontally) {
                if (expanded && searchPerson.isNotEmpty() && filteredAndSortedList.isEmpty()) {
                    Text(
                        text = "Sonuç yok",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(all = 5.dp), state = listState) {
                    items(filteredAndSortedList.size){ e->
                        val person=filteredAndSortedList[e]
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                            colors = CardDefaults.cardColors(containerColor = ozelBackground),
                            modifier = Modifier.padding(vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .clickable {
                                        navController.navigate("chat_screen/${person.name}/${person.numberID}/${person.uid}")
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = person.profilePhotoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape),
                                    placeholder = painterResource(id = R.drawable.baseline_person_24),
                                    error = painterResource(id = R.drawable.baseline_person_24)
                                )

                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        text = person.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MyCustomFontFamily
                                    )
                                    Text(
                                        text = person.numberID,
                                        color = Color.White,
                                        fontWeight = FontWeight.Thin,
                                        fontFamily = MyCustomFontFamily,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
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
        })
}

@Composable
fun MyBottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val currentDensity = LocalDensity.current

    CompositionLocalProvider(LocalDensity provides object : Density{
        override val density: Float
            get() = currentDensity.density
        override val fontScale: Float
            get() =1f }
    ) {
        NavigationBar(
            containerColor = ozelBackground,
            contentColor = Color.White,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.fillMaxWidth()
                .height(90.dp)
        ) {
            NavigationBarItem(
                selected = selectedIndex == 0,
                onClick = { onItemSelected(0) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_chat_24),
                        contentDescription = "Chat",
                        modifier = Modifier.size(24.dp)
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,            // Seçili ikon rengi
                    unselectedIconColor = Color.LightGray,     // Seçili olmayan ikon rengi
                    selectedTextColor = Color.Cyan,            // (eğer label görünüyorsa)
                    unselectedTextColor = Color.Gray,          // (eğer label görünüyorsa)
                    indicatorColor = ozelBorder         // Seçili ikonun arka plan rengi (varsayılan animasyonlu highlight)
                )
            )

            NavigationBarItem(
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) },
                icon = {
                    Icon(Icons.Outlined.Add, contentDescription = "Add", modifier = Modifier.size(24.dp))
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,            // Seçili ikon rengi
                    unselectedIconColor = Color.LightGray,     // Seçili olmayan ikon rengi
                    selectedTextColor = Color.Cyan,            // (eğer label görünüyorsa)
                    unselectedTextColor = Color.Gray,          // (eğer label görünüyorsa)
                    indicatorColor = ozelBorder         // Seçili ikonun arka plan rengi (varsayılan animasyonlu highlight)
                )
            )

            NavigationBarItem(
                selected = selectedIndex == 2,
                onClick = { onItemSelected(2) },
                icon = {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp))
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,            // Seçili ikon rengi
                    unselectedIconColor = Color.LightGray,     // Seçili olmayan ikon rengi
                    selectedTextColor = Color.Cyan,            // (eğer label görünüyorsa)
                    unselectedTextColor = Color.Gray,          // (eğer label görünüyorsa)
                    indicatorColor = ozelBorder         // Seçili ikonun arka plan rengi (varsayılan animasyonlu highlight)
                )
            )

            NavigationBarItem(
                selected = selectedIndex == 3,
                onClick = { onItemSelected(3) },
                icon = {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", modifier = Modifier.size(24.dp))
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,            // Seçili ikon rengi
                    unselectedIconColor = Color.LightGray,     // Seçili olmayan ikon rengi
                    selectedTextColor = Color.Cyan,            // (eğer label görünüyorsa)
                    unselectedTextColor = Color.Gray,          // (eğer label görünüyorsa)
                    indicatorColor = ozelBorder         // Seçili ikonun arka plan rengi (varsayılan animasyonlu highlight)
                )
            )
        }
    }

}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PersonListPreview(){
    WhisperTheme {
        //PersonListScreen()
    }
}