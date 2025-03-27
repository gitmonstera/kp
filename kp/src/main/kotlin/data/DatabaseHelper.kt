package data

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

object DatabaseHelper {
    private const val DB_URL = "jdbc:sqlite:users.db"

    init {
        createTables()
    }

    private fun createTables() {
        try {
            val connection = connect()
            val statement: Statement = connection.createStatement()

            val createUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    login TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    remembered BOOLEAN DEFAULT 0
                )
            """.trimIndent()

            statement.execute(createUserTable)
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun connect(): Connection {
        return DriverManager.getConnection(DB_URL)
    }
}