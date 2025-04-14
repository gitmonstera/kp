import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson

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