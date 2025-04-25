package ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import data.model.User
import data.repository.UserRepository
import data.AppPreferences

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRegisterClick: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔐 Вход", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (login.isBlank() || password.isBlank()) {
                    errorMessage = "Введите логин и пароль"
                    return@Button
                }

                val authenticated = UserRepository.authenticateUser(login, password)

                if (authenticated) {
                    val user = UserRepository.getUser(login)
                    if (user != null) {
                        val updatedUser = user.copy(rememberMe = rememberMe)
                        if (rememberMe) {
                            AppPreferences.saveUserCredentials(updatedUser)
                        } else {
                            AppPreferences.clearCredentials()
                        }
                        onLoginSuccess(updatedUser)
                    } else {
                        errorMessage = "Пользователь не найден"
                    }
                } else {
                    errorMessage = "Неверный логин или пароль"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Нет аккаунта? Зарегистрироваться")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }
}
