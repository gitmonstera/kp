import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Users : Table() {
    val id = integer("id").autoIncrement()
    val fullName = varchar("fullName", 100)
    val email = varchar("email", 100)
    val login = varchar("login", 50).uniqueIndex()
    val password = varchar("password", 100)
    val rememberMe = bool("rememberMe")

    override val primaryKey = PrimaryKey(id)
}

object DatabaseHelper {
    private val dbFile = File("database.db")

    init {
        Database.connect("jdbc:sqlite:${dbFile.absolutePath}", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(Users)
        }
    }

    fun registerUser(fullName: String, email: String, login: String, password: String, rememberMe: Boolean): Boolean {
        return transaction {
            if (Users.select { Users.login eq login }.empty()) {
                Users.insert {
                    it[Users.fullName] = fullName
                    it[Users.email] = email
                    it[Users.login] = login
                    it[Users.password] = password
                    it[Users.rememberMe] = rememberMe
                }
                true
            } else {
                false
            }
        }
    }

    fun authenticateUser(login: String, password: String): Boolean {
        return transaction {
            Users.select { (Users.login eq login) and (Users.password eq password) }
                .count() > 0
        }
    }
}