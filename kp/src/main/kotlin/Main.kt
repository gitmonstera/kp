import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import java.util.prefs.Preferences
import java.util.regex.Pattern

fun main() = application {
    val preferences = Preferences.userRoot().node("auth_app")
    val savedLogin = preferences.get("user_login", null)
    val savedFullName = preferences.get("user_full_name", null)

    var currentScreen by remember { mutableStateOf(if (savedLogin != null) Screen.Main else Screen.Login) }
    var currentUser by remember { mutableStateOf<User?>(if (savedLogin != null) User(savedFullName!!, savedLogin, "") else null) }

    Window(onCloseRequest = ::exitApplication, title = "Аутентификация") {
        BackgroundAnimation()
        when (currentScreen) {
            Screen.Registration -> RegistrationScreen(
                onLoginClick = { currentScreen = Screen.Login },
                onRegisterSuccess = { user, rememberMe ->
                    if (rememberMe) saveUser(preferences, user)
                    currentUser = user
                    currentScreen = Screen.Main
                }
            )
            Screen.Login -> LoginScreen(
                onRegisterClick = { currentScreen = Screen.Registration },
                onLoginSuccess = { user, rememberMe ->
                    if (rememberMe) saveUser(preferences, user)
                    currentUser = user
                    currentScreen = Screen.Main
                }
            )
            Screen.Main -> MainScreen(user = currentUser!!)
        }
    }
}

@Composable
fun MainScreen(user: User) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text("Главный экран") },
            actions = {
                IconButton(onClick = { }) {
                    Icon(painterResource("img/menu_icon.png"), contentDescription = "Меню")
                }
            }
        )

        Text("Добро пожаловать, ${user.fullName}")
    }
}

@Composable
fun PasswordTextField(password: String, onPasswordChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Пароль") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
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