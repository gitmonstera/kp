import org.jetbrains.exposed.sql.Table

object Statistics : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("userId") references Users.id
    val correctAnswers = integer("correctAnswers").default(0)
    val incorrectAnswers = integer("incorrectAnswers").default(0)
    val completedTickets = integer("completedTickets").default(0)

    override val primaryKey = PrimaryKey(id)
}
