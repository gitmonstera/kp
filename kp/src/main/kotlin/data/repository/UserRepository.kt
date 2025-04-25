package data.repository


import org.jetbrains.exposed.sql.*
import data.bd.Users
import data.model.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object UserRepository {

    fun registerUser(user: User): Boolean = transaction {
        if (Users.select { Users.login eq user.login }.count() > 0) return@transaction false

        Users.insert {
            it[fullName] = user.fullName
            it[email] = user.email
            it[login] = user.login
            it[password] = user.password
            it[rememberMe] = user.rememberMe
        }

        true
    }

    fun authenticateUser(login: String, password: String): Boolean = transaction {
        Users.select { (Users.login eq login) and (Users.password eq password) }.count() > 0
    }

    fun getUser(login: String): User? = transaction {
        Users.select { Users.login eq login }.map {
            User(
                fullName = it[Users.fullName],
                email = it[Users.email],
                login = it[Users.login],
                password = it[Users.password],
                rememberMe = it[Users.rememberMe]
            )
        }.singleOrNull()
    }

    fun updateUser(user: User): Boolean = transaction {
        Users.update({ Users.login eq user.login }) {
            it[fullName] = user.fullName
            it[email] = user.email
            it[password] = user.password
            it[rememberMe] = user.rememberMe
        } > 0
    }

    fun deleteUser(login: String): Boolean = transaction {
        Users.deleteWhere { Users.login eq login } > 0
    }
}
