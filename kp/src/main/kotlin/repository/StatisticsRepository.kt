import java.util.prefs.Preferences

object StatisticsRepository {
    private val prefs: Preferences = Preferences.userRoot().node(this::class.java.name)

    private fun key(login: String, type: String): String {
        return "${type}_$login"
    }

    fun getUserStats(login: String): StatisticsData {
        return StatisticsData(
            correctAnswers = prefs.getInt(key(login, "correct_answers"), 0),
            incorrectAnswers = prefs.getInt(key(login, "incorrect_answers"), 0),
            completedTickets = prefs.getInt(key(login, "completed_tickets"), 0)
        )
    }

    fun saveOrUpdateStats(login: String, stats: StatisticsData) {
        prefs.putInt(key(login, "correct_answers"), stats.correctAnswers)
        prefs.putInt(key(login, "incorrect_answers"), stats.incorrectAnswers)
        prefs.putInt(key(login, "completed_tickets"), stats.completedTickets)
    }

    fun addAnswers(login: String, correct: Int, incorrect: Int) {
        val correctKey = key(login, "correct_answers")
        val incorrectKey = key(login, "incorrect_answers")

        prefs.putInt(correctKey, prefs.getInt(correctKey, 0) + correct)
        prefs.putInt(incorrectKey, prefs.getInt(incorrectKey, 0) + incorrect)
    }

    fun incrementCompletedTickets(login: String) {
        val key = key(login, "completed_tickets")
        prefs.putInt(key, prefs.getInt(key, 0) + 1)
    }

    fun clearStats(login: String) {
        prefs.remove(key(login, "correct_answers"))
        prefs.remove(key(login, "incorrect_answers"))
        prefs.remove(key(login, "completed_tickets"))
    }
}
