package ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import data.AppPreferences
import data.model.User
import data.repository.UserRepository
import isValidEmail

@Composable
fun RegistrationScreen(onRegistrationComplete: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
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
        Text("📝 Регистрация", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

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
                if (fullName.isBlank() || email.isBlank() || login.isBlank() || password.isBlank()) {
                    errorMessage = "Пожалуйста, заполните все поля"
                    return@Button
                }

                if (!isValidEmail(email)) {
                    errorMessage = "Некорректный email"
                    return@Button
                }

                val newUser = User(
                    fullName = fullName,
                    email = email,
                    login = login,
                    password = password,
                    rememberMe = rememberMe
                )

                val success = UserRepository.registerUser(newUser)
                if (success) {
                    if (rememberMe) AppPreferences.saveUserCredentials(newUser)
                    onRegistrationComplete()
                } else {
                    errorMessage = "Пользователь с таким логином уже существует"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зарегистрироваться")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegistrationComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Уже есть аккаунт? Войти")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }
}