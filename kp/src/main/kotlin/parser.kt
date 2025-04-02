import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


data class TrafficRuleResponse(val traffic_rules: List<TrafficRule>)
data class TrafficRule(val category: String, val rules: List<Rule>, val subcategories: List<Subcategory>? = null)
data class Rule(val id: Int, val termin: String, val description: String, val tags: List<String>)
data class Subcategory(val type: String, val rules: List<Rule>)


fun loadJsonFromFile(filePath: String): String {
    val path = Paths.get(filePath)
    return String(Files.readAllBytes(path))
}
