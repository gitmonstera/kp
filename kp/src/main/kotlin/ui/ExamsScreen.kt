import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExamsScreen(login: String, onBackClick: () -> Unit) {
    val gson = remember { Gson() }
    val allQuestions = remember { gson.fromJson(ticketJson, TicketData::class.java).tickets.flatMap { it.questions } }

    var started by remember { mutableStateOf(false) }
    var questions by remember { mutableStateOf(listOf<Question>()) }
    var currentIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableStateListOf<Int?>() }
    var mistakes by remember { mutableStateOf(0) }
    var timer by remember { mutableStateOf(20 * 60) }
    var examFinished by remember { mutableStateOf(false) }
    var showFail by remember { mutableStateOf(false) }
    var statsSaved by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun startExam() {
        started = true
        examFinished = false
        mistakes = 0
        currentIndex = 0
        statsSaved = false
        timer = 20 * 60
        questions = allQuestions.shuffled().take(20)
        answers.clear()
        repeat(questions.size) { answers.add(null) }
    }

    LaunchedEffect(started) {
        if (started) {
            scope.launch {
                while (timer > 0 && !examFinished && !showFail) {
                    delay(1000)
                    timer--
                }
                if (timer <= 0) examFinished = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!started) {
            Text("📝 Экзамен", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { startExam() }) { Text("Начать") }
            Button(onClick = onBackClick, modifier = Modifier.padding(top = 8.dp)) { Text("Назад") }

            if (examFinished) {
                val correct = questions.withIndex().count { (i, q) -> answers.getOrNull(i) == q.correctAnswer }
                val incorrect = questions.size - correct

                if (!statsSaved) {
                    StatisticsRepository.addAnswers(login, correct, incorrect)
                    StatisticsRepository.incrementCompletedTickets(login)
                    statsSaved = true
                }

                Text("✅ Правильно: $correct из ${questions.size}")
                Text("❌ Ошибок: $mistakes")
            }
        } else {
            val question = questions.getOrNull(currentIndex)
            question?.let {
                Text("Вопрос ${currentIndex + 1}/${questions.size}")
                Text(it.question)
                Spacer(Modifier.height(8.dp))

                it.answers.forEachIndexed { index, answer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (answers[currentIndex] == null) {
                                    answers[currentIndex] = index
                                    if (index != it.correctAnswer) {
                                        mistakes++
                                        if (mistakes > 2) showFail = true
                                    }
                                    currentIndex++
                                    if (currentIndex >= questions.size) examFinished = true
                                }
                            }
                            .padding(8.dp)
                    ) {
                        RadioButton(selected = answers[currentIndex] == index, onClick = null)
                        Text(answer)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Ошибок: $mistakes / 2")
                Text("Осталось: ${timer / 60} мин ${timer % 60} сек")
            }

            if (showFail) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Экзамен не сдан") },
                    text = { Text("Более 2 ошибок.") },
                    confirmButton = {
                        Button(onClick = {
                            started = false
                            showFail = false
                        }) {
                            Text("Ок")
                        }
                    }
                )
            }
        }
    }
}
