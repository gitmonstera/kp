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
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    var currentScreen by remember { mutableStateOf(Screen.Registration) }

    var currentUser = User(
        fullName = "Имя Фамилия",
        email = "email@example.com",
        login = "login123",
        password = "pass",
        rememberMe = true
    )


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
                        onLoginSuccess = { user ->
                            currentUser = user
                            currentScreen = Screen.MainMenu
                        },
                        onRegisterClick = { currentScreen = Screen.Registration }
                    )
                    Screen.MainMenu -> MainMenuScreen { currentScreen = Screen.Login }
                    Screen.Settings -> SettingsScreen(user = currentUser) { updatedUser ->
                        currentUser = updatedUser
                    }
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
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRegisterClick: () -> Unit
) {
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
                if (success) {
                    val user = DatabaseHelper.getUser(login)
                    if (user != null) {
                        loginStatus = "Вход выполнен!"
                        onLoginSuccess(user)
                    } else {
                        loginStatus = "Ошибка загрузки данных пользователя"
                    }
                } else {
                    loginStatus = "Неверные данные"
                }
            }) {
                Text("Войти")
            }

            loginStatus?.let {
                Text(it, color = if (it == "Вход выполнен!") Color.Green else Color.Red)
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = onRegisterClick) {
                Text("Перейти к регистрации")
            }
        }
    }
}


@Composable
fun MainMenuScreen(onLogoutClick: () -> Unit) {
    val trafficRules = remember {
        Gson().fromJson(JSON_DATA, TrafficRulesData::class.java)
    }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Главное меню", fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))

            // Отображаем категории
            trafficRules.traffic_rules.forEach { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(category.category ?: "Uncategorized", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(8.dp))

                        // Handle subcategories if they exist
                        category.subcategories?.forEach { subcategory ->
                            Text(subcategory.type ?: "No type", style = MaterialTheme.typography.subtitle1)
                            Spacer(Modifier.height(4.dp))

                            subcategory.rules.forEach { rule ->
                                RuleItem(rule)
                            }
                        }

                        // Handle direct rules if no subcategories
                        category.rules?.forEach { rule ->
                            RuleItem(rule)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onLogoutClick) {
                Text("Выйти")
            }
        }
    }
}

@Composable
fun RuleItem(rule: Rule) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = rule.termin ?: "No term",  // Provide fallback for null
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = rule.description ?: "No description",  // Provide fallback for null
            style = MaterialTheme.typography.body2
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Теги: ${rule.tags?.joinToString(", ") ?: "нет"}",  // Handle null tags
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

data class TrafficRulesData(
    val traffic_rules: List<Category> = emptyList() // Добавьте значение по умолчанию
)

data class Category(
    val category: String? = null, // Добавьте null по умолчанию
    val rules: List<Rule>? = null,
    val subcategories: List<Subcategory>? = null
)

data class Rule(
    val id: Int = 0, // Значение по умолчанию
    val termin: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList()
)

data class Subcategory(
    val type: String? = null,
    val rules: List<Rule> = emptyList()
)

// Константа с JSON данными


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

    fun getUser(login: String): User?{
        return users.find { it.login == login }
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
    val gson = remember { Gson() }
    val ticketData = remember { gson.fromJson(ticketJson, TicketData::class.java) }
    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }
    val selectedAnswers = remember { mutableStateListOf<Int?>() }
    var showResult by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Билеты", fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))

            if (selectedTicket == null) {
                ticketData.tickets.forEachIndexed { index, ticket ->
                    Button(
                        onClick = {
                            selectedTicket = ticket
                            selectedAnswers.clear()
                            repeat(ticket.questions.size) { selectedAnswers.add(null) }
                            showResult = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Билет ${index + 1}")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text("Назад") }
            } else {
                Text("Билет", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                if (showResult) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Результат: $correctCount из ${selectedTicket!!.questions.size}",
                        fontSize = 20.sp,
                        color = Color(0xFF388E3C)
                    )
                }

                Spacer(Modifier.height(16.dp))

                selectedTicket!!.questions.forEachIndexed { index, question ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("${index + 1}. ${question.question}", fontSize = 16.sp)

                        question.imageRes?.let { imageRes ->
                            Spacer(Modifier.height(8.dp))
                            val painter = painterResource(imageRes)
                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(4.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        question.answers.forEachIndexed { i, answer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAnswers[index] = i }
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (selectedAnswers[index] == i) Color(0xFFD0F0C0)
                                        else Color.Transparent
                                    )
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedAnswers[index] == i,
                                    onClick = { selectedAnswers[index] = i }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(answer)
                            }
                        }
                    }
                    Divider()
                }



                if (!isCompleted) {
                    Button(
                        onClick = {
                            val correct = selectedTicket!!.questions.withIndex().count { (i, question) ->
                                selectedAnswers[i] == question.correctAnswer
                            }
                            correctCount = correct
                            StatisticsHolder.correctAnswers += correct
                            StatisticsHolder.incorrectAnswers += selectedTicket!!.questions.size - correct
                            StatisticsHolder.completedTickets += 1
                            showResult = true
                            isCompleted = true // Ставим флаг завершения
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Завершить")
                    }
                }else {
                    // Кнопка "Назад к билетам" после завершения
                    Button(
                        onClick = {
                            selectedTicket = null
                            showResult = false
                            isCompleted = false // Сброс флага завершения
                            selectedAnswers.clear()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Назад к билетам")
                    }
                }

                Spacer(Modifier.height(200.dp)) // отступ для появления кнопки на 1/3 экрана
                Text(
                    text="ticket"
                )
            }
        }
    }
}


// JSON-данные
val ticketJson = """
{
  "tickets": [
    {
      "questions": [
        {
          "question": "Что означает этот знак?",
          "answers": ["Въезд запрещён", "Главная дорога", "Уступи дорогу", "Движение запрещено"],
          "correctAnswer": 0,
          "imageRes": "images/sign.png"
        },
        {
          "question": "С какой максимальной скоростью можно двигаться в городе?",
          "answers": ["50 км/ч", "60 км/ч", "70 км/ч", "80 км/ч"],
          "correctAnswer": 1
        }
      ]
    },
    {
      "questions": [
        {
          "question": "Какой документ подтверждает право на управление транспортом?",
          "answers": ["Паспорт", "Свидетельство о регистрации", "Водительское удостоверение", "Полис ОСАГО"],
          "correctAnswer": 2
        }
        // Еще 19 вопросов
      ]
    },
    {
      "questions": [
        {
          "question": "Разрешено ли обгонять на перекрестке?",
          "answers": ["Разрешено", "Разрешено только на регулируемом", "Запрещено", "Разрешено в любом случае"],
          "correctAnswer": 1
        }
        // Еще 19 вопросов
      ]
    }
  ]
}
""".trimIndent()

// Модель данных
data class TicketData(val tickets: List<Ticket>)
data class Ticket(val questions: List<Question>)
data class Question(
    val question: String,
    val answers: List<String>,
    val correctAnswer: Int,
    val imageRes: String? = null // путь до изображения в ресурсах
)



@Composable
fun StatisticsScreen(onBackClick: () -> Unit) {

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Статистика", fontSize = 24.sp, style = MaterialTheme.typography.h4)
            Spacer(Modifier.height(16.dp))

            // Правильные ответы
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Правильные ответы", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${StatisticsHolder.correctAnswers}",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Неправильные ответы
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Неправильные ответы", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${StatisticsHolder.incorrectAnswers}",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 4.dp
            ) {
                Column (
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("колличество билетов", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${StatisticsHolder.completedTickets}",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }

}


@Composable
fun SettingsScreen(user: User, onSave: (User) -> Unit) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var login by remember { mutableStateOf(user.login) }
    var password by remember { mutableStateOf(user.password) }
    var rememberMe by remember { mutableStateOf(user.rememberMe) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Профиль пользователя", fontSize = 24.sp)

        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("ФИО") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        Button(onClick = {
            onSave(User(fullName, email, login, password, rememberMe))
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Сохранить")
        }
    }
}


object StatisticsHolder {
    var correctAnswers = 0
    var incorrectAnswers = 0
    var completedTickets = 0
}


data class User(val fullName: String, val email: String, val login: String, val password: String, val rememberMe: Boolean)
