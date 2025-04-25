package data.repository

import org.jetbrains.exposed.sql.*
import data.bd.Users
import data.bd.Statistics
import data.model.StatisticsData
import org.jetbrains.exposed.sql.transactions.transaction

object StatisticsRepository {

    private fun getUserIdByLogin(login: String): Int? = transaction {
        val user = Users.select { Users.login eq login }.singleOrNull()
        val userId = user?.get(Users.id)
        println("🔍 getUserIdByLogin('$login') → $userId")
        userId
    }

    fun getUserStats(login: String): StatisticsData = transaction {
        val userId = getUserIdByLogin(login) ?: return@transaction StatisticsData()

        val row = Statistics.select { Statistics.userId eq userId }.singleOrNull()

        if (row != null) {
            println("📊 Loaded stats for $login → $row")
            StatisticsData(
                correctAnswers = row[Statistics.correctAnswers],
                incorrectAnswers = row[Statistics.incorrectAnswers],
                completedTickets = row[Statistics.completedTickets]
            )
        } else {
            println("ℹ️ No stats found for $login. Creating new row.")
            Statistics.insert {
                it[Statistics.userId] = userId
                it[correctAnswers] = 0
                it[incorrectAnswers] = 0
                it[completedTickets] = 0
            }
            StatisticsData()
        }
    }

    fun addAnswers(login: String, correct: Int, incorrect: Int) = transaction {
        val userId = getUserIdByLogin(login) ?: run {
            println("❌ User '$login' not found — cannot add answers.")
            return@transaction
        }

        val updated = Statistics.update({ Statistics.userId eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(Statistics.correctAnswers, Statistics.correctAnswers + correct)
                it.update(Statistics.incorrectAnswers, Statistics.incorrectAnswers + incorrect)
            }
        }

        println("📥 addAnswers($login): updated = $updated")

        if (updated == 0) {
            println("➕ No existing row — inserting new statistics row.")
            Statistics.insert {
                it[Statistics.userId] = userId
                it[correctAnswers] = correct
                it[incorrectAnswers] = incorrect
                it[completedTickets] = 0
            }
        }
    }

    fun incrementCompletedTickets(login: String) = transaction {
        val userId = getUserIdByLogin(login) ?: run {
            println("❌ User '$login' not found — cannot increment tickets.")
            return@transaction
        }

        val updated = Statistics.update({ Statistics.userId eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(Statistics.completedTickets, Statistics.completedTickets + 1)
            }
        }

        println("📈 incrementCompletedTickets($login): updated = $updated")

        if (updated == 0) {
            println("➕ No row found, inserting new row with completedTickets = 1")
            Statistics.insert {
                it[Statistics.userId] = userId
                it[correctAnswers] = 0
                it[incorrectAnswers] = 0
                it[completedTickets] = 1
            }
        }
    }

    fun clearStats(login: String) = transaction {
        val userId = getUserIdByLogin(login) ?: return@transaction

        Statistics.update({ Statistics.userId eq userId }) {
            it[correctAnswers] = 0
            it[incorrectAnswers] = 0
            it[completedTickets] = 0
        }
        println("🧹 Cleared statistics for $login")
    }

    fun getAllUserStats(): List<Pair<String, StatisticsData>> = transaction {
        (Users innerJoin Statistics).selectAll().map {
            val login = it[Users.login]
            val stats = StatisticsData(
                correctAnswers = it[Statistics.correctAnswers],
                incorrectAnswers = it[Statistics.incorrectAnswers],
                completedTickets = it[Statistics.completedTickets]
            )
            login to stats
        }
    }
}
