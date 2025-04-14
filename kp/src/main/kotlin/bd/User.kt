import org.jetbrains.exposed.dao.id.IntIdTable

data class User(
    val fullName: String = "",
    val email: String = "",
    val login: String = "",
    val password: String = "",
    val rememberMe: Boolean = false
)
