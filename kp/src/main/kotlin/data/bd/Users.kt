import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val fullName = varchar("fullName", 255)
    val email = varchar("email", 255)
    val login = varchar("login", 100).uniqueIndex()
    val password = varchar("password", 100)
    val rememberMe = bool("rememberMe")

    override val primaryKey = PrimaryKey(id)
}
