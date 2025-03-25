import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationScreen(onLoginClick: () -> Unit, onRegisterSuccess: (User, Boolean) -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var registrationStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Регистрация", fontSize = 24.sp)

        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("ФИО") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        PasswordTextField(password, onPasswordChange = { password = it })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        Button(onClick = {
            if (!isValidEmail(email)) {
                registrationStatus = "Некорректный email!"
                return@Button
            }
            if (password.length < 6) {registrationStatus = "Пароль должен быть минимум 6 символов!"
                return@Button
            }
            val newUser = User(fullName, login, password)
            onRegisterSuccess(newUser, rememberMe)
        }) {
            Text("Зарегистрироваться")
        }

        registrationStatus?.let { Text(it, color = Color.Red) }

        Spacer(Modifier.height(8.dp))
        Text("Уже зарегистрированы?", color = Color.Blue, modifier = Modifier.clickable { onLoginClick() })
    }
}