package ui.screens

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.components.TextInputField
import ui.components.ButtonWithAction
import data.UserRepository

@Composable
fun RegistrationScreen(onNavigateToLogin: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextInputField("ФИО", fullName) { fullName = it }
        TextInputField("Email", email) { email = it }
        TextInputField("Логин", login) { login = it }
        TextInputField("Пароль", password) { password = it }

        Row {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        ButtonWithAction("Зарегистрироваться") {
            UserRepository.registerUser(fullName, email, login, password, rememberMe)
            onNavigateToLogin()
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Уже зарегистрированы?")
        }
    }
}