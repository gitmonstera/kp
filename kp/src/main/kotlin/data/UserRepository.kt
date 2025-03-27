package data

import java.sql.PreparedStatement
import java.sql.ResultSet

object UserRepository {

    fun registerUser(fullName: String, email: String, login: String, password: String, rememberMe: Boolean): Boolean {
        val connection = DatabaseHelper.connect()
        val insertUserQuery = """
            INSERT INTO users (full_name, email, login, password, remembered) 
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        return try {
            val statement: PreparedStatement = connection.prepareStatement(insertUserQuery)
            statement.setString(1, fullName)
            statement.setString(2, email)
            statement.setString(3, login)
            statement.setString(4, password)
            statement.setBoolean(5, rememberMe)
            statement.executeUpdate()
            statement.close()
            connection.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun authenticateUser(login: String, password: String): Boolean {
        val user = getUser(login)
        return user?.password == password
    }

    fun getUser(login: String): User? {
        val connection = DatabaseHelper.connect()
        val query = "SELECT * FROM users WHERE login = ?"
        val statement: PreparedStatement = connection.prepareStatement(query)

        return try {
            statement.setString(1, login)
            val resultSet: ResultSet = statement.executeQuery()

            if (resultSet.next()) {
                val user = User(
                    id = resultSet.getInt("id"),
                    fullName = resultSet.getString("full_name"),
                    email = resultSet.getString("email"),
                    login = resultSet.getString("login"),
                    password = resultSet.getString("password"),
                    remembered = resultSet.getBoolean("remembered")
                )
                resultSet.close()
                statement.close()
                connection.close()
                user
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteUser(login: String): Boolean {
        val connection = DatabaseHelper.connect()
        val query = "DELETE FROM users WHERE login = ?"
        val statement: PreparedStatement = connection.prepareStatement(query)

        return try {
            statement.setString(1, login)
            val rowsAffected = statement.executeUpdate()
            statement.close()
            connection.close()
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateRememberMe(login: String, rememberMe: Boolean) {
        val connection = DatabaseHelper.connect()
        val query = "UPDATE users SET remembered = ? WHERE login = ?"
        val statement: PreparedStatement = connection.prepareStatement(query)

        try {
            statement.setBoolean(1, rememberMe)
            statement.setString(2, login)
            statement.executeUpdate()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}