import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import java.io.File


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {


    var currentScreen by remember { mutableStateOf<Screen?>(null) } // null - пока не знаем куда переходить
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        StatisticsManager.loadStatistics()

        val savedUser = AppPreferences.loadUserCredentials()

        if (savedUser != null && savedUser.rememberMe) {
            // Проверяем есть ли этот пользователь в базе данных
            if (DatabaseHelper.getUser(savedUser.login) == null) {
                // Добавляем его в базу
                DatabaseHelper.registerUser(
                    savedUser.fullName,
                    savedUser.email,
                    savedUser.login,
                    savedUser.password,
                    savedUser.rememberMe
                )
            }

            currentUser = savedUser
            currentScreen = Screen.MainMenu
        } else {
            currentScreen = Screen.Login
        }
    }



    Window(onCloseRequest = {
        // Сохраняем статистику перед выходом
        StatisticsManager.saveStatistics()
        exitApplication()}, title = "Аутентификация")
    {
        if (currentScreen == null) {
            // Пока грузим данные - пустой экран
            Box(Modifier.fillMaxSize())
        } else {
            Scaffold(
                topBar = {
                    if (currentScreen != Screen.Login && currentScreen != Screen.Registration) {
                        TopAppBar(title = { Text("ПДД") })
                    }
                },
                bottomBar = {
                    if (currentScreen != Screen.Login && currentScreen != Screen.Registration) {
                        BottomNavigationBar(currentScreen!!) { newScreen -> currentScreen = newScreen }
                    }
                },
                content = {
                    when (currentScreen) {
                        Screen.Registration -> RegistrationScreen { currentScreen = Screen.Login }
                        Screen.Login -> LoginScreen(
                            onLoginSuccess = { user ->
                                currentUser = user
                                currentScreen = Screen.MainMenu
                            },
                            onRegisterClick = { currentScreen = Screen.Registration }
                        )
                        Screen.MainMenu -> MainMenuScreen {
                            AppPreferences.clearCredentials()
                            StatisticsManager.saveStatistics() // Явное сохранение перед выходом
                            currentScreen = Screen.Login
                        }
                        Screen.Settings -> currentUser?.let { user ->
                            SettingsScreen(
                                user = user,
                                onSave = { updatedUser ->
                                    currentUser = updatedUser
                                    DatabaseHelper.updateUser(updatedUser) // Нужно добавить этот метод в DatabaseHelper
                                },
                                onLogout = {
                                    AppPreferences.clearCredentials()
                                    StatisticsManager.saveStatistics()
                                    currentScreen = Screen.Login
                                }
                            )
                        }
                        Screen.Exams -> ExamsScreen { currentScreen = Screen.MainMenu }
                        Screen.Tickets -> TicketsScreen { currentScreen = Screen.MainMenu }
                        Screen.Statistics -> StatisticsScreen { currentScreen = Screen.MainMenu }
                        null -> {} // для компилятора
                    }
                }
            )
        }
    }
}

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

fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return emailPattern.matcher(email).matches()
}
