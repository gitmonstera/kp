import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson

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
