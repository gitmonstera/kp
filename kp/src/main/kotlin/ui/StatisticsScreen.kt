import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsScreen(login: String, onBackClick: () -> Unit) {
    var stats by remember { mutableStateOf(StatisticsData()) }
    var allStats by remember { mutableStateOf(emptyList<Pair<String, StatisticsData>>()) }

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
            Box(modifier = Modifier.padding(20.dp)) {
                Text("✅ Правильных: ${stats.correctAnswers}", fontSize = 16.sp)
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
