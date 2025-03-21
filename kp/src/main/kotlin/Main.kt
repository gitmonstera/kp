import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var currentScreen by remember { mutableStateOf(Screen.Registration) }

    Window(onCloseRequest = ::exitApplication, title = "Аутентификация") {
        when (currentScreen) {
            Screen.Registration -> RegistrationScreen { currentScreen = Screen.Login }
            Screen.Login -> LoginScreen { currentScreen = Screen.Registration }
        }
    }
}

enum class Screen { Registration, Login }

@Composable
fun RegistrationScreen(onLoginClick: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var registrationStatus by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    BackgroundAnimation()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Регистрация", fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("ФИО") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        Button(onClick = {
            if (!isValidEmail(email)) {
                registrationStatus = "Некорректный email!"
                return@Button
            }

            val success = DatabaseHelper.registerUser(fullName, email, login, password, rememberMe)
            registrationStatus = if (success) "Регистрация успешна!" else "Логин уже существует!"

            if (success) {
                scope.launch {
                    delay(500)
                    onLoginClick()
                }
            }
        }) {
            Text("Зарегистрироваться")
        }

        registrationStatus?.let {
            Text(it, color = if (it == "Регистрация успешна!") Color.Green else Color.Red)
        }

        Spacer(Modifier.height(8.dp))
        Text("Уже зарегистрированы?", color = Color.Blue, modifier = Modifier.clickable { onLoginClick() })
    }
}

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

@Composable
fun BackgroundAnimation() {
    val images = listOf("img/bg1.png", "img/bg2.png", "img/bg3.png", "img/bg4.png", "img/bg5.png", "img/bg6.png")
    var currentImages by remember { mutableStateOf(images.shuffled().take(3)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            currentImages = images.shuffled().take(3)
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        currentImages.forEach { img ->
            Image(
                painter = painterResource(img),
                contentDescription = "Фон",
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return emailPattern.matcher(email).matches()
}