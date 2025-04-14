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

// Обновите LoginScreen для безопасной работы с пользователем
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRegisterClick: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var loginStatus by remember { mutableStateOf<String?>(null) }

    // Проверяем сохранённые данные при первом открытии
    LaunchedEffect(Unit) {
        val savedUser = AppPreferences.loadUserCredentials()
        savedUser?.let { user ->
            if (DatabaseHelper.getUser(user.login) == null) {
                DatabaseHelper.registerUser(
                    user.fullName,
                    user.email,
                    user.login,
                    user.password,
                    user.rememberMe
                )
            }
            onLoginSuccess(user)
        }
    }

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

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Логин") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text("Запомнить меня")
            }

            Button(onClick = {
                if (DatabaseHelper.authenticateUser(login, password, rememberMe)) {
                    DatabaseHelper.getUser(login)?.let { user ->
                        onLoginSuccess(user)
                    } ?: run {
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
    var searchQuery by remember { mutableStateOf("") }

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

            // 🔍 Поле для поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск по термину или тегу") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // 🔄 Фильтрация и отображение категорий
            trafficRules.traffic_rules.forEach { category ->
                val filteredRules = category.rules?.filter {
                    it.termin?.contains(searchQuery, ignoreCase = true) == true ||
                            it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
                } ?: emptyList()

                if (filteredRules.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(category.category ?: "Без категории", style = MaterialTheme.typography.h6)
                            Spacer(Modifier.height(8.dp))

                            filteredRules.forEach { rule ->
                                RuleItem(rule)
                            }
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
    var imageLoadFailed by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        rule.image?.let { imagePath ->
            if (!imageLoadFailed) {
                // Пытаемся отрисовать
                runCatching {
                    painterResource(imagePath)
                }.onSuccess { painter ->
                    Image(
                        painter = painter,
                        contentDescription = rule.termin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(bottom = 8.dp)
                    )
                }.onFailure {
                    imageLoadFailed = true
                }
            }

            if (imageLoadFailed) {
                Text("Ошибка загрузки изображения", color = Color.Red)
            }
        }

        Text(
            text = rule.termin ?: "No term",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = rule.description ?: "No description",
            style = MaterialTheme.typography.body2
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Теги: ${rule.tags.joinToString(", ")}",
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
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
    val id: String? = null, // Значение по умолчанию
    val termin: String? = null,
    val description: String? = null,
    val image: String? = null,
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


@Composable
fun ExamsScreen(onBackClick: () -> Unit) {
    val gson = remember { Gson() }
    val allQuestions = remember {
        gson.fromJson(ticketJson, TicketData::class.java).tickets.flatMap { it.questions }
    }

    var started by remember { mutableStateOf(false) }
    var questions by remember { mutableStateOf(listOf<Question>()) }
    var currentIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableStateListOf<Int?>() }
    var mistakes by remember { mutableStateOf(0) }
    var timerSeconds by remember { mutableStateOf(20 * 60) }
    var showFailDialog by remember { mutableStateOf(false) }
    var examFinished by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // ⏱️ Таймер
    LaunchedEffect(started) {
        if (started) {
            scope.launch {
                while (timerSeconds > 0 && !examFinished && !showFailDialog) {
                    delay(1000)
                    timerSeconds--
                }
                if (timerSeconds <= 0) {
                    examFinished = true
                }
            }
        }
    }

    fun startExam() {
        started = true
        examFinished = false
        questions = allQuestions.shuffled().take(20)
        answers.clear()
        repeat(questions.size) { answers.add(null) }
        mistakes = 0
        timerSeconds = 20 * 60
        currentIndex = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!started) {
            Text("📝 Экзамен", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { startExam() }) {
                Text("Начать экзамен")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBackClick) {
                Text("Назад")
            }
        } else if (examFinished) {
            Text("✅ Экзамен завершён", fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            val correct = questions.withIndex().count { (i, q) -> answers[i] == q.correctAnswer }
            Text("Правильных ответов: $correct из ${questions.size}", fontSize = 18.sp)
            Text("Ошибок: $mistakes", fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                started = false
            }) {
                Text("Пройти заново")
            }
        } else {
            val question = questions.getOrNull(currentIndex)
            if (question != null) {
                Text("Вопрос ${currentIndex + 1}/${questions.size}", fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                Text(question.question, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))

                question.imageRes?.let {
                    val painter = painterResource(it)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(8.dp)
                    )
                }

                question.answers.forEachIndexed { index, answer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (answers[currentIndex] == null) {
                                    answers[currentIndex] = index
                                    if (index != question.correctAnswer) {
                                        mistakes++
                                        if (mistakes == 1) {
                                            // Добавляем 5 вопросов при 1-й ошибке
                                            val newQuestions = allQuestions
                                                .filterNot { questions.contains(it) }
                                                .shuffled()
                                                .take(5)
                                            questions = questions + newQuestions
                                            repeat(5) { answers.add(null) }
                                        }
                                        if (mistakes > 2) {
                                            showFailDialog = true
                                        }
                                    }

                                    if (currentIndex + 1 < questions.size) {
                                        currentIndex++
                                    } else {
                                        examFinished = true
                                    }
                                }
                            }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = answers[currentIndex] == index,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(answer)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Ошибок: $mistakes / 2")
                Text("Осталось времени: ${timerSeconds / 60} мин ${timerSeconds % 60} сек")
            }
        }

        // ❗ Диалог: экзамен не сдан
        if (showFailDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Экзамен не сдан") },
                text = { Text("Более 2-х ошибок. Попробуйте ещё раз.") },
                confirmButton = {
                    Button(onClick = {
                        showFailDialog = false
                        started = false
                    }) {
                        Text("Ок")
                    }
                }
            )
        }
    }
}



@Composable
fun TicketsScreen(onBackClick: () -> Unit) {
    val gson = remember { Gson() }
    val ticketData = remember { gson.fromJson(ticketJson, TicketData::class.java) }
    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isCompleted by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("📋 Выбор билета", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (selectedTicket == null) {
            ticketData.tickets.forEachIndexed { index, ticket ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            selectedTicket = ticket
                            selectedAnswers.clear()
                            isCompleted = false
                            showResult = false
                            correctCount = 0
                        },
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Text("Билет №${index + 1}", fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Назад")
            }
        } else {
            Text("🧾 Билет", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            if (showResult) {
                Text(
                    text = "✅ Правильных ответов: $correctCount из ${selectedTicket!!.questions.size}",
                    fontSize = 18.sp,
                    color = Color(0xFF388E3C)
                )
            }

            Spacer(Modifier.height(12.dp))

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
                                .clickable(enabled = !isCompleted) {
                                    selectedAnswers[index] = i
                                }
                                .background(
                                    if (selectedAnswers[index] == i) Color(0xFFD0F0C0)
                                    else Color.Transparent
                                )
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedAnswers[index] == i,
                                onClick = {
                                    if (!isCompleted) {
                                        selectedAnswers[index] = i
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(answer)
                        }
                    }
                }
                Divider()
            }

            Spacer(Modifier.height(16.dp))

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
                        StatisticsManager.saveStatistics()
                        showResult = true
                        isCompleted = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Завершить")
                }

                Spacer(Modifier.height(46.dp))

            } else {
                Button(
                    onClick = {
                        selectedTicket = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Назад к билетам")
                }
                Spacer(Modifier.height(46.dp))
            }
        }
    }
}





// Модель данных
data class TicketData(
    val tickets: List<Ticket> = emptyList() // Убрали nullable и оставили значение по умолчанию
)

data class Ticket(
    val questions: List<Question> = emptyList() // Убрали nullable
)

data class Question(
    val question: String = "", // Заменили null на пустую строку
    val answers: List<String> = emptyList(), // Убрали nullable
    val correctAnswer: Int = 0,
    val imageRes: String? = null // Оставили nullable, так как изображение может отсутствовать
)



@Composable
fun StatisticsScreen(onBackClick: () -> Unit) {

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        StatisticsManager.loadStatistics()
    }

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
fun SettingsScreen(user: User, onSave: (User) -> Unit, onLogout: () -> Unit) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var login by remember { mutableStateOf(user.login) }
    var password by remember { mutableStateOf(user.password) }
    var rememberMe by remember { mutableStateOf(user.rememberMe) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление аккаунта") },
            text = { Text("Вы уверены, что хотите удалить свой аккаунт? Это действие нельзя отменить.") },

            confirmButton = {
                Button(
                    onClick = {
                        DatabaseHelper.deleteUser(user.login)
                        AppPreferences.clearCredentials()
                        StatisticsManager.clearStatistics()
                        StatisticsManager.saveStatistics() // Явное сохранение
                        onLogout()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Настройки профиля", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня", modifier = Modifier.padding(start = 8.dp))
        }

        Button(
            onClick = { onSave(User(fullName, email, login, password, rememberMe)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Сохранить изменения")
        }

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Удалить аккаунт", color = Color.White)
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта")
        }
    }
}


data class StatisticsData(
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val completedTickets: Int = 0
)

object StatisticsManager {
    private val appDir = File(System.getProperty("user.home"), "AppData/Local/MyApp")
    private val statsFile = File(appDir, "statistics.json")
    private val gson = Gson()

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
    }

    @Synchronized
    fun saveStatistics() {
        try {
            val data = StatisticsData(
                correctAnswers = StatisticsHolder.correctAnswers,
                incorrectAnswers = StatisticsHolder.incorrectAnswers,
                completedTickets = StatisticsHolder.completedTickets
            )
            statsFile.writeText(gson.toJson(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun loadStatistics() {
        try {
            if (statsFile.exists()) {
                val loadedData = gson.fromJson(statsFile.readText(), StatisticsData::class.java)
                StatisticsHolder.correctAnswers = loadedData.correctAnswers
                StatisticsHolder.incorrectAnswers = loadedData.incorrectAnswers
                StatisticsHolder.completedTickets = loadedData.completedTickets
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearStatistics() {
        StatisticsHolder.correctAnswers = 0
        StatisticsHolder.incorrectAnswers = 0
        StatisticsHolder.completedTickets = 0
        saveStatistics()
    }
}

object StatisticsHolder {
    var correctAnswers = 0
    var incorrectAnswers = 0
    var completedTickets = 0
}



// Обновите класс User с null-безопасными значениями по умолчанию
data class User(
    val fullName: String = "",
    val email: String = "",
    val login: String = "",
    val password: String = "",
    val rememberMe: Boolean = false
)

object DatabaseHelper {
    private val appDir = File(System.getProperty("user.home"), "AppData/Local/MyApp")

    private val usersFile = File(appDir, "users.json")
    private val users = mutableListOf<User>()
    private val gson = Gson()

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        loadUsersFromFile()
    }

    fun saveUsersToFile() {
        usersFile.writeText(gson.toJson(users))
    }

    fun loadUsersFromFile() {
        if (usersFile.exists()) {
            val loadedUsers = gson.fromJson(usersFile.readText(), Array<User>::class.java)
            users.clear()
            users.addAll(loadedUsers)
        }
    }

    fun registerUser(
        fullName: String,
        email: String,
        login: String,
        password: String,
        rememberMe: Boolean
    ): Boolean {
        if (users.any { it.login == login }) {
            return false
        }
        val user = User(fullName, email, login, password, rememberMe)
        users.add(user)
        saveUsersToFile()
        if (rememberMe) {
            AppPreferences.saveUserCredentials(user)
        }
        return true
    }

    fun authenticateUser(login: String, password: String, rememberMe: Boolean): Boolean {
        val user = users.find { it.login == login && it.password == password }
        return if (user != null) {
            if (rememberMe) {
                AppPreferences.saveUserCredentials(user)
            }
            true
        } else {
            false
        }
    }

    fun getUser(login: String): User? = users.find { it.login == login }

    fun deleteUser(login: String): Boolean {
        val user = users.find { it.login == login } ?: return false
        users.remove(user)
        saveUsersToFile()
        return true
    }

    fun updateUser(updatedUser: User): Boolean {
        val index = users.indexOfFirst { it.login == updatedUser.login }
        if (index == -1) return false
        users[index] = updatedUser
        saveUsersToFile()
        if (updatedUser.rememberMe) {
            AppPreferences.saveUserCredentials(updatedUser)
        } else {
            AppPreferences.clearCredentials()
        }
        return true
    }
}

object AppPreferences {
    private val appDir = File(System.getProperty("user.home"), "AppData/Local/MyApp")

    private val prefsFile = File(appDir, "app_preferences.json")
    private val gson = Gson()

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
    }

    fun saveUserCredentials(user: User) {
        prefsFile.writeText(gson.toJson(user))
    }

    fun loadUserCredentials(): User? {
        return if (prefsFile.exists()) {
            gson.fromJson(prefsFile.readText(), User::class.java)
        } else {
            null
        }
    }

    fun clearCredentials() {
        if (prefsFile.exists()) {
            prefsFile.delete()
        }
    }
}
