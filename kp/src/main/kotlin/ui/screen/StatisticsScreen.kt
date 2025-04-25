package ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.StatisticsData
import data.repository.StatisticsRepository
//import org.jetbrains.compose.chart.pie.PieChart
//import org.jetbrains.compose.chart.pie.PieChartData

@Composable
fun StatisticsScreen(login: String, onBackClick: () -> Unit) {
    var stats by remember { mutableStateOf(StatisticsData()) }
    var allStats by remember { mutableStateOf(emptyList<Pair<String, StatisticsData>>()) }

    val totalAnswers = stats.correctAnswers + stats.incorrectAnswers
    val correctProgress = if (totalAnswers > 0) stats.correctAnswers / totalAnswers.toFloat() else 0f
    val incorrectProgress = if (totalAnswers > 0) stats.incorrectAnswers / totalAnswers.toFloat() else 0f
    val ticketsProgress = stats.completedTickets / 40f // допустим 40 билетов всего

    val animatedCorrect by animateFloatAsState(correctProgress)
    val animatedIncorrect by animateFloatAsState(incorrectProgress)
    val animatedTickets by animateFloatAsState(ticketsProgress.coerceIn(0f, 1f))

    // Загружаем при первом открытии
    LaunchedEffect(login) {
        stats = StatisticsRepository.getUserStats(login)
        allStats = StatisticsRepository.getAllUserStats()
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("📊 Ваша статистика", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            elevation = 46.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column() {
                Box(modifier = Modifier.padding(20.dp)) {
                    Text("✅ Правильных: ${stats.correctAnswers}", fontSize = 16.sp)

                    LinearProgressIndicator(
                        progress = animatedCorrect,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            elevation = 46.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                Text("❌ Ошибок: ${stats.incorrectAnswers}", fontSize = 16.sp)

                LinearProgressIndicator(
                    progress = animatedIncorrect,
                    color = Color(0xFFF44336),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            elevation = 46.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                Text("🎫 Билетов решено: ${stats.completedTickets}", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

//        if (totalAnswers > 0) {
//            Text("🧁 Распределение ответов", fontSize = 20.sp)
//            PieChart(
//                data = listOf(
//                    PieChartData(value = stats.correctAnswers.toFloat(), color = Color(0xFF4CAF50), label = "Правильные"),
//                    PieChartData(value = stats.incorrectAnswers.toFloat(), color = Color(0xFFF44336), label = "Ошибки")
//                ),
//                modifier = Modifier.size(220.dp)
//            )
//        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            StatisticsRepository.clearStats(login)
            stats = StatisticsData()
        }) {
            Text("Очистить мою статистику")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

//        Text("🏆 Общая статистика пользователей", style = MaterialTheme.typography.h6)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // 🔄 Кнопка для обновления общей статистики вручную
//        Button(onClick = {
//            allStats = StatisticsRepository.getAllUserStats()
//        }) {
//            Text("🔄 Обновить общую статистику")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))

//        allStats
//            .sortedByDescending { it.second.correctAnswers }
//            .forEach { (userLogin, userStats) ->
//                Text(
//                    "👤 $userLogin: ✅ ${userStats.correctAnswers}, ❌ ${userStats.incorrectAnswers}, 🎫 ${userStats.completedTickets}",
//                    fontSize = 14.sp
//                )
//            }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(onClick = onBackClick) {
//            Text("Назад")
//        }
    }
}
