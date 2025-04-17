import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.AppPreferences
import kotlinx.coroutines.delay
import java.util.regex.Pattern

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }

    DatabaseFactory.init()

    LaunchedEffect(Unit) {
        val savedUser = AppPreferences.loadUserCredentials()
        if (savedUser != null && savedUser.rememberMe) {
            val exists = UserRepository.getUser(savedUser.login)
            if (exists == null) {
                UserRepository.registerUser(savedUser)
            }
            currentUser = savedUser
            currentScreen = Screen.MainMenu
        } else {
            currentScreen = Screen.Login
        }
    }

    Window(
        onCloseRequest = {
            AppPreferences.clearCredentials()
            exitApplication()
        },
        title = "ПДД",
        icon = painterResource("icon.png")
    ) {
        MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
            Box(Modifier
                .fillMaxSize()) {
                // 🎨 Задний фон
                BackgroundAnimation()

                if (currentScreen != null) {
                    Scaffold(
                        backgroundColor = Color.Transparent, // важно!
                        topBar = {
                            if (currentScreen != Screen.Login && currentScreen != Screen.Registration) {
                                TopAppBar(title = { Text("ПДД") })
                            }
                        },
                        bottomBar = {
                            if (currentScreen != Screen.Login && currentScreen != Screen.Registration) {
                                BottomNavigationBar(currentScreen!!) { newScreen ->
                                    currentScreen = newScreen
                                }
                            }
                        },
                        content = {
                            when (currentScreen) {
                                Screen.Registration -> RegistrationScreen {
                                    currentScreen = Screen.Login
                                }

                                Screen.Login -> LoginScreen(
                                    onLoginSuccess = { user ->
                                        currentUser = user
                                        currentScreen = Screen.MainMenu
                                    },
                                    onRegisterClick = {
                                        currentScreen = Screen.Registration
                                    }
                                )

                                Screen.MainMenu -> MainMenuScreen {
                                    AppPreferences.clearCredentials()
                                    currentUser = null
                                    currentScreen = Screen.Login
                                }

                                Screen.Settings -> currentUser?.let { user ->
                                    SettingsScreen(
                                        user = user,
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                                        onSave = { updatedUser ->
                                            currentUser = updatedUser
                                            UserRepository.updateUser(updatedUser)
                                        },
                                        onLogout = {
                                            AppPreferences.clearCredentials()
                                            currentUser = null
                                            currentScreen = Screen.Login
                                        }
                                    )
                                }

                                Screen.Exams -> currentUser?.let { user ->
                                    ExamsScreen(login = user.login) {
                                        currentScreen = Screen.MainMenu
                                    }
                                }

                                Screen.Tickets -> currentUser?.let { user ->
                                    TicketsScreen(login = user.login) {
                                        currentScreen = Screen.MainMenu
                                    }
                                }

                                Screen.Statistics -> currentUser?.let { user ->
                                    StatisticsScreen(login = user.login) {
                                        currentScreen = Screen.MainMenu
                                    }
                                }

                                null -> {}
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundAnimation() {
    val images = listOf("img/bg1.png", "img/bg2.png", "img/bg3.png", "img/bg4.png", "img/bg5.png")
    var currentImages by remember { mutableStateOf(images.shuffled().take(3)) }

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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return pattern.matcher(email).matches()
}
