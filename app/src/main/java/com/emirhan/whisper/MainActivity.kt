package com.emirhan.whisper

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emirhan.whisper.ui.pages.AddPersonScreen
import com.emirhan.whisper.ui.pages.AuthCheckScreen
import com.emirhan.whisper.ui.pages.BlockedPersonScreen
import com.emirhan.whisper.ui.pages.ChatListScreen
import com.emirhan.whisper.ui.pages.ChatScreen
import com.emirhan.whisper.ui.pages.LoginScreen
import com.emirhan.whisper.ui.pages.PersonListScreen
import com.emirhan.whisper.ui.pages.ProfileSettingsScreen
import com.emirhan.whisper.ui.pages.RegisterScreen
import com.emirhan.whisper.ui.pages.SettingsScreen
import com.emirhan.whisper.ui.theme.WhisperTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController= rememberNavController()
            WhisperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)){
                        NavHost(navController = navController, startDestination = "screen"){
                            composable("screen") { AuthCheckScreen(navController = navController) }
                            composable("login_page") { LoginScreen(navController = navController) }
                            composable("register_page") { RegisterScreen(navController = navController) }
                            composable("chatlist_page") { ChatListScreen(navController = navController) }
                            composable("settings_page") { SettingsScreen(navController = navController) }
                            composable("addperson_page") { AddPersonScreen(navController = navController) }
                            composable("person_page") { PersonListScreen(navController = navController) }
                            composable("profile_settings") { ProfileSettingsScreen(navController) }
                            composable("blocked_page") { BlockedPersonScreen(navController = navController) }
                            composable("chat_screen/{name}/{numberID}/{uid}",
                                arguments = listOf(
                                    navArgument("name") { type = NavType.StringType },
                                    navArgument("numberID") { type = NavType.StringType },
                                    navArgument("uid") { type = NavType.StringType },
                                )) { backStackEntry->
                                val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "")
                                val numberID = Uri.decode(backStackEntry.arguments?.getString("numberID") ?: "")
                                val uid = Uri.decode(backStackEntry.arguments?.getString("uid") ?: "")
                                ChatScreen(
                                    navController,
                                    name,
                                    numberID,
                                    uid
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

