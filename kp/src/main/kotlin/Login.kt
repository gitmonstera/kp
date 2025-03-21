import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onRegisterClick: () -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginStatus by remember { mutableStateOf<String?>(null) }

    BackgroundAnimation()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Авторизация", fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(onClick = {
            loginStatus = if (DatabaseHelper.authenticateUser(login, password)) "Вход выполнен!" else "Неверные данные"
        }) {
            Text("Войти")
        }

        loginStatus?.let {
            Text(it, color = if (it == "Вход выполнен!") Color.Green else Color.Red)
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { onRegisterClick() }) {
            Text("Вернуться к регистрации")
        }
    }
}