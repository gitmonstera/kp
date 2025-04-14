import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/pdd",
            driver = "org.postgresql.Driver",
            user = "postgres",               // или твой пользователь
            password = "password"  // твой пароль
        )
    }
}
