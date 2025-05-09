package ui.screen


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import res.ticketJson
import data.repository.StatisticsRepository

@Composable
fun TicketsScreen(login: String, onBackClick: () -> Unit) {
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
        Text("📋 Выбор билета", fontSize = 26.sp)
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
            Button(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Назад")
            }

        } else {
            selectedTicket!!.questions.forEachIndexed { index, question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${index + 1}. ${question.question}", fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))

                        question.imageRes?.let { imagePath ->
                            runCatching {
                                val painter = painterResource(imagePath)
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

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
                                        if (!isCompleted) selectedAnswers[index] = i
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(answer)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!isCompleted) {
                Button(onClick = {
                    val correct = selectedTicket!!.questions.withIndex().count { (i, q) ->
                        selectedAnswers[i] == q.correctAnswer
                    }
                    correctCount = correct
                    val incorrect = selectedTicket!!.questions.size - correct

                    StatisticsRepository.addAnswers(login, correct, incorrect)
                    StatisticsRepository.incrementCompletedTickets(login)

                    showResult = true
                    isCompleted = true
                }) {
                    Text("Завершить")
                }
            } else {
                Text("✅ Правильных: $correctCount из ${selectedTicket!!.questions.size}")
                Button(
                    onClick = { selectedTicket = null },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Назад к билетам")
                }
            }

            Spacer(Modifier.height(56.dp))
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