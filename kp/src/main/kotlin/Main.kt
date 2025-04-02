import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var currentScreen by remember { mutableStateOf(Screen.Registration) }

    Window(onCloseRequest = ::exitApplication, title = "Аутентификация") {
        Scaffold(
            topBar = {
                if (currentScreen != Screen.Login && currentScreen != Screen.Registration) {
                    TopAppBar(title = { Text("ПДД") })
                }
            },
            bottomBar = {
                if (currentScreen != Screen.Login && currentScreen != Screen.Registration) {
                    BottomNavigationBar(currentScreen) { newScreen -> currentScreen = newScreen }
                }
            },
            content = {
                when (currentScreen) {
                    Screen.Registration -> RegistrationScreen { currentScreen = Screen.Login }
                    Screen.Login -> LoginScreen(
                        onLoginSuccess = { currentScreen = Screen.MainMenu },
                        onRegisterClick = { currentScreen = Screen.Registration }
                    )
                    Screen.MainMenu -> MainMenuScreen { currentScreen = Screen.Login }
                    Screen.Settings -> SettingsScreen { currentScreen = Screen.MainMenu }
                    Screen.Exams -> ExamsScreen { currentScreen = Screen.MainMenu }
                    Screen.Tickets -> TicketsScreen { currentScreen = Screen.MainMenu }
                    Screen.Statistics -> StatisticsScreen { currentScreen = Screen.MainMenu }
                }
            }
        )
    }
}


enum class Screen { Registration, Login, MainMenu, Settings, Exams, Tickets, Statistics }


@Composable
fun BackgroundAnimation() {
    val images = listOf("img/bg1.png", "img/bg2.png", "img/bg3.png", "img/bg4.png", "img/bg5.png")
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

@Composable
fun RegistrationScreen(onLoginClick: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var registrationStatus by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(16.dp),
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
                        delay(1500)
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
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginStatus by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                val success = DatabaseHelper.authenticateUser(login, password)
                loginStatus = if (success) {
                    "Вход выполнен!"
                } else {
                    "Неверные данные"
                }

                if (success) {
                    onLoginSuccess()
                }
            }) {
                Text("Войти")
            }

            loginStatus?.let {
                Text(it, color = if (it == "Вход выполнен!") Color.Green else Color.Red)
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = onRegisterClick) { // Теперь передаём правильный обработчик
                Text("Перейти к регистрации")
            }
        }
    }
}


@Composable
fun MainMenuScreen(onLogoutClick: () -> Unit) {
    var trafficRules by remember { mutableStateOf<List<TrafficRule>>(emptyList()) }


    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            LaunchedEffect(Unit) {
//                val json = loadJsonFromFile("pdd.json") // Загрузка JSON-файла
//                val trafficRuleResponse = Gson().fromJson(json, TrafficRuleResponse::class.java)
//                trafficRules = trafficRuleResponse.traffic_rules
//            }
        }
    }
}

@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val users = mutableListOf<User>()


    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Text("${users.any { it.login }}", fontSize = 24.sp)
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label = { Text("Главная") },
            selected = currentScreen == Screen.MainMenu,
            onClick = { onScreenSelected(Screen.MainMenu) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Экзамены") },
            label = { Text("Экзамены") },
            selected = currentScreen == Screen.Exams,
            onClick = { onScreenSelected(Screen.Exams) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Билеты") },
            label = { Text("Билеты") },
            selected = currentScreen == Screen.Tickets,
            onClick = { onScreenSelected(Screen.Tickets) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Статистика") },
            label = { Text("Статистика") },
            selected = currentScreen == Screen.Statistics,
            onClick = { onScreenSelected(Screen.Statistics) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
            label = { Text("Настройки") },
            selected = currentScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) }
        )
    }
}


fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return emailPattern.matcher(email).matches()
}

// Простая реализация DatabaseHelper для демонстрации

object DatabaseHelper {
    private val users = mutableListOf<User>()

    fun registerUser(fullName: String, email: String, login: String, password: String, rememberMe: Boolean): Boolean {
        return if (users.any { it.login == login }) {
            false // Логин уже существует
        } else {
            users.add(User(fullName, email, login, password, rememberMe))
            true // Регистрация успешна
        }
    }

    fun authenticateUser(login: String, password: String): Boolean {
        return users.any { it.login == login && it.password == password }
    }
}

@Composable
fun ExamsScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Экзамены", fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBackClick) { Text("Назад") }
        }
    }
}

@Composable
fun TicketsScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Билеты", fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBackClick) { Text("Назад") }
        }
    }
}

@Composable
fun StatisticsScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.White.copy(alpha = 0.8f)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Статистика", fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBackClick) { Text("Назад") }
        }
    }
}


data class User(val fullName: String, val email: String, val login: String, val password: String, val rememberMe: Boolean)
