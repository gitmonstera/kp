package data.bd

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            "jdbc:sqlite:${System.getProperty("user.home")}/AppData/Local/MyApp/app.db",
            driver = "org.sqlite.JDBC"
        )
        transaction {
            SchemaUtils.create(Users, Statistics)
        }
    }
}
