package ui.screens

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.components.TextInputField
import ui.components.ButtonWithAction

@Composable
fun LoginScreen(onNavigateToMain: () -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextInputField("Логин", login) { login = it }
        TextInputField("Пароль", password) { password = it }

        ButtonWithAction("Войти") {
            if (authenticateUser(login, password)) onNavigateToMain()
        }

        TextButton(onClick = { /* Назад на регистрацию */ }) {
            Text("Вернуться к регистрации")
        }
    }
}

fun authenticateUser(login: String, password: String): Boolean {
    return true // Проверка по базе данных
}