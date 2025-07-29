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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavController
import com.emirhan.whisper.ui.theme.MyCustomFontFamily
import com.emirhan.whisper.ui.theme.WhisperTheme
import com.emirhan.whisper.ui.theme.ozelBackground
import com.emirhan.whisper.ui.theme.ozelBorder
import androidx.hilt.navigation.compose.hiltViewModel
import com.emirhan.whisper.ui.viewmodel.AuthViewModel
import com.emirhan.whisper.R
import com.emirhan.whisper.ui.util.RegistrationState


@Composable
fun RegisterScreen(navController: NavController){
    val viewModel: AuthViewModel =hiltViewModel()
    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = ozelBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        var isim by remember { mutableStateOf("") }
        var eMail by remember { mutableStateOf("") }
        var sifre by rememberSaveable { mutableStateOf("") }
        var sifreVisibility by remember { mutableStateOf(false) }
        val context= LocalContext.current
        val state =viewModel.registrationState.value
        Text(
            text = "CREATE ACCOUNT",
            fontSize = 30.sp,
            fontFamily = MyCustomFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = Color.Blue
        )
        Spacer(modifier = Modifier.padding(all = 10.dp))
        OutlinedTextField(
            value = isim,
            onValueChange = {isim=it},
            placeholder = {Text("Name:", color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = MyCustomFontFamily)},
            label = {Text("NAME", color = Color.White, fontFamily = MyCustomFontFamily)},
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = Color.White,
                focusedTextColor = Color.White,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = ozelBorder
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Person,contentDescription = null, tint = Color.White)
            }
        )
        OutlinedTextField(
            value = eMail,
            onValueChange = {eMail=it},
            placeholder = {Text("E mail:", color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = MyCustomFontFamily)},
            label = {Text("E-MAIL", color = Color.White, fontFamily = MyCustomFontFamily)},
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = Color.White,
                focusedTextColor = Color.White,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = ozelBorder
            ), keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Email,contentDescription = null, tint = Color.White)
            }
        )
        OutlinedTextField(
            value = sifre,
            onValueChange = {sifre=it},
            placeholder = {Text("Password:", color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = MyCustomFontFamily)},
            label = {Text("PASSWORD", color = Color.White, fontFamily = MyCustomFontFamily)},
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = Color.White,
                focusedTextColor = Color.White,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = ozelBorder
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            trailingIcon = { IconButton(onClick = {sifreVisibility=!sifreVisibility}, colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)){
                    val image = if (sifreVisibility){
                        painterResource(R.drawable.outline_visibility_24)
                    }else{
                        painterResource(R.drawable.outline_visibility_off_24)
                    }
                Icon(painter = image,contentDescription = null)
            } },
            visualTransformation = if (sifreVisibility) VisualTransformation.None
            else PasswordVisualTransformation(),
            leadingIcon = {Icon(imageVector = Icons.Outlined.Lock,contentDescription = null, tint = Color.White)}
        )
        Spacer(modifier = Modifier.padding(all = 5.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Do you have an account?",
                fontFamily = MyCustomFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = Color.White)
            Spacer(modifier = Modifier.padding(horizontal = 3.dp))
            Text("Log in",
                fontFamily = MyCustomFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable{
                    navController.navigate("login_page")
                })
        }
        Spacer(modifier = Modifier.padding(all = 5.dp))
        OutlinedButton(onClick = {
            if (isim.isNotEmpty()&&eMail.isNotEmpty()&&sifre.isNotEmpty()){
                viewModel.registerUser(eMail.trim(), sifre.trim(),isim.trim())
                //Toast.makeText(context,"Kayıt başarılı", Toast.LENGTH_SHORT).show()
                navController.navigate("login_page")
            }
            else{
                Toast.makeText(context,"Kayıt başarısız.Lütfen sizden istenen bilgileri doldurun",
                    Toast.LENGTH_SHORT).show()
            }

        },border = BorderStroke(width = 3.dp,ozelBorder), enabled = state !is RegistrationState.Loading) {
            Text("Sign Up", fontFamily = MyCustomFontFamily, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        when(state){
            is RegistrationState.Loading -> CircularProgressIndicator()
            is RegistrationState.success -> Text("Kayıt başarılı", color = Color.White)
            is RegistrationState.Error -> Text("Hata:${state.message}", color = Color.Red)
            else -> {}
        }
    }
}
@Preview(showBackground = true)
@Composable
fun RegisterPreview(){
    WhisperTheme {
        //RegisterScreen()
    }
}