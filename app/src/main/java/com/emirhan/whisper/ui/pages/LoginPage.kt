package com.emirhan.whisper.ui.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.emirhan.whisper.ui.viewmodel.AuthViewModel
import com.emirhan.whisper.R
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.theme.ozelBorder
import com.emirhan.whisper.ui.util.LoginState

@Composable
fun LoginScreen(navController: NavController){
    val viewModel: AuthViewModel =hiltViewModel()
    val state=viewModel.loginState.value
    var eMail by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var sifreVisibility by remember { mutableStateOf(false) }
    val context= LocalContext.current

    LaunchedEffect(state) {
        if (state is LoginState.Success){
            navController.navigate("chatlist_page"){
                popUpTo("login_page"){inclusive=true}
            }
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(ozelBackground)
            .fillMaxSize()) {
        Text(
            text = "LOGİN ACCOUNT",
            fontFamily = MyCustomFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = Color.Blue,
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.padding(all = 10.dp))
        OutlinedTextField(
            value = eMail,
            onValueChange = {eMail=it},
            placeholder = {Text("Email:", color = Color.White, fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold)},
            label = {Text("EMAIL", fontFamily = MyCustomFontFamily, color = Color.White)},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = ozelBorder,
                focusedPlaceholderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            maxLines = 15,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Email,contentDescription = null, tint = Color.White) }
        )
        OutlinedTextField(
            value = sifre,
            onValueChange = {sifre=it},
            placeholder = {Text("Password:", color = Color.White, fontFamily = MyCustomFontFamily, fontWeight = FontWeight.SemiBold)},
            label = {Text("PASSWORD", fontFamily = MyCustomFontFamily, color = Color.White)},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = ozelBorder,
                focusedPlaceholderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            trailingIcon = { IconButton(onClick = {sifreVisibility=!sifreVisibility}, colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)){
                val image =if (sifreVisibility){
                    painterResource(R.drawable.outline_visibility_24)
                }else{
                    painterResource(R.drawable.outline_visibility_off_24)
                }
                Icon(painter = image,contentDescription = null)
            }
            },
            visualTransformation = if (sifreVisibility) VisualTransformation.None
            else PasswordVisualTransformation(),
            leadingIcon = {Icon(painterResource(R.drawable.outline_password_2_24),contentDescription = null, tint = Color.White)}
        )
        Spacer(modifier = Modifier.padding(all = 5.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Don't have an account?",
                fontFamily = MyCustomFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                fontSize = 15.sp)
            Spacer(modifier = Modifier.padding(horizontal = 3.dp))
            Text("Create an account",
                fontFamily = MyCustomFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.Blue,
                fontSize = 15.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable{
                    navController.navigate("register_page")
                })
        }
        Spacer(modifier = Modifier.padding(all = 5.dp))
        OutlinedButton(onClick = {
            if (eMail.isNotEmpty()&&sifre.isNotEmpty()){
                viewModel.loginUser(eMail.trim(),sifre.trim())
                Toast.makeText(context,"Giriş yapılıyor", Toast.LENGTH_SHORT).show()
                //navController.navigate("chatlist_page")
            }else{
                Toast.makeText(context,"Lütfen istenen bilgileri eksiksiz doldurun", Toast.LENGTH_SHORT).show()
            }
        }, border = BorderStroke(width = 3.dp, color = ozelBorder), enabled = state!= LoginState.Loading) {
            Text(
                text = "Log in",
                fontFamily = MyCustomFontFamily,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        when(state){
            is LoginState.Loading -> CircularProgressIndicator()
            is LoginState.Error-> Text("Hata:${state.message}", color = Color.Red)
            else -> {}
        }
    }
}
@Preview(showBackground = true)
@Composable
fun LoginPreview(){
    WhisperTheme {
        //LoginScreen()
    }
}