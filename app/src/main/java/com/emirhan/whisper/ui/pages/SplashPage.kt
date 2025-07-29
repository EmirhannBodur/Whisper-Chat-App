package com.emirhan.whisper.ui.pages

// AuthCheckScreen.kt (Yeni bir dosya veya MainActivity.kt içine eklenebilir)

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.emirhan.whisper.R
import com.emirhan.whisper.ui.theme.ozelBackground

@Composable
fun AuthCheckScreen(navController: NavController) {
    // Bu LaunchedEffect, AuthCheckScreen composable'ı ilk kez oluşturulduğunda çalışır
    // ve sadece bir kez çalışması garanti edilir (Unit anahtarı sayesinde).
    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // Kullanıcı oturum açmışsa, doğrudan sohbet listesine git
            navController.navigate("chatlist_page") {
                // Bu noktadan önceki tüm ekranları (yani sadece AuthCheckScreen'i) kaldır.
                // Eğer AuthCheckScreen NavHost'un startDestination'ı ise,
                // graph.startDestinationId doğru şekilde çalışacaktır.
                popUpTo("auth_check_screen") { inclusive = true }
            }
        } else {
            // Kullanıcı oturum açmamışsa, giriş sayfasına git
            navController.navigate("login_page") {
                // AuthCheckScreen'i geri yığından kaldır
                popUpTo("auth_check_screen") { inclusive = true }
            }
        }
    }

    // Bu ekran görünür olduğu sürece bir yükleme göstergesi veya boş bir kutu gösterebilirsiniz.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ozelBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center// Uygulamanızın ana arka plan rengiyle eşleşebilir
    ) {
        Image(painter = painterResource(R.drawable.splash),contentDescription = null, modifier = Modifier.size(200.dp))
    }
}