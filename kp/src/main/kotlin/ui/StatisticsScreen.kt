import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsScreen(login: String, onBackClick: () -> Unit) {
    var stats by remember(login) { mutableStateOf(StatisticsData()) }

    LaunchedEffect(login) {
        stats = StatisticsRepository.getUserStats(login)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊 Ваша статистика", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(24.dp))

        Text("✅ Правильные ответы: ${stats.correctAnswers}", fontSize = 18.sp)
        Text("❌ Неправильные ответы: ${stats.incorrectAnswers}", fontSize = 18.sp)
        Text("🎫 Завершено билетов: ${stats.completedTickets}", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text("Назад")
            }
            Button(
                onClick = {
                    StatisticsRepository.clearStats(login)
                    stats = StatisticsData()
                }
            ) {
                Text("Сбросить")
            }
        }
    }
}

