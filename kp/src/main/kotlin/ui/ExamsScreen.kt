import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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