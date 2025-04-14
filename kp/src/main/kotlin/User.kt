import org.jetbrains.exposed.dao.id.IntIdTable

data class User(
    val fullName: String = "",
    val email: String = "",
    val login: String = "",
    val password: String = "",
    val rememberMe: Boolean = false
)



object Users : IntIdTable() {
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255)
    val login = varchar("login", 100).uniqueIndex()
    val password = varchar("password", 255)
    val rememberMe = bool("remember_me")
}


object Statistics : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val correctAnswers = integer("correct_answers").default(0)
    val incorrectAnswers = integer("incorrect_answers").default(0)
    val completedTickets = integer("completed_tickets").default(0)
}
