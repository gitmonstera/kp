import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import java.io.File

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

object StatisticsHolder {
    var correctAnswers = 0
    var incorrectAnswers = 0
    var completedTickets = 0
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

