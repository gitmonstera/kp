import androidx.compose.foundation.background
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
fun LoginScreen(onRegisterClick: () -> Unit, onLoginSuccess: (User, Boolean) -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var loginStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Авторизация", fontSize = 24.sp)

        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        PasswordTextField(password, onPasswordChange = { password = it })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        Button(onClick = {
            val user = User("Иван Иванов", login, password)
            onLoginSuccess(user, rememberMe)
        }) {
            Text("Войти")
        }

        loginStatus?.let { Text(it, color = Color.Red) }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { onRegisterClick() }) {
            Text("Вернуться к регистрации")
        }
    }
}