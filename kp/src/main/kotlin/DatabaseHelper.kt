import com.google.gson.Gson
import java.io.File

object DatabaseHelper {
    private val appDir = File(System.getProperty("user.home"), "AppData/Local/MyApp")

    private val usersFile = File(appDir, "users.json")
    private val users = mutableListOf<User>()
    private val gson = Gson()

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        loadUsersFromFile()
    }

    fun saveUsersToFile() {
        usersFile.writeText(gson.toJson(users))
    }

    fun loadUsersFromFile() {
        if (usersFile.exists()) {
            val loadedUsers = gson.fromJson(usersFile.readText(), Array<User>::class.java)
            users.clear()
            users.addAll(loadedUsers)
        }
    }

    fun registerUser(
        fullName: String,
        email: String,
        login: String,
        password: String,
        rememberMe: Boolean
    ): Boolean {
        if (users.any { it.login == login }) {
            return false
        }
        val user = User(fullName, email, login, password, rememberMe)
        users.add(user)
        saveUsersToFile()
        if (rememberMe) {
            AppPreferences.saveUserCredentials(user)
        }
        return true
    }

    fun authenticateUser(login: String, password: String, rememberMe: Boolean): Boolean {
        val user = users.find { it.login == login && it.password == password }
        return if (user != null) {
            if (rememberMe) {
                AppPreferences.saveUserCredentials(user)
            }
            true
        } else {
            false
        }
    }

    fun getUser(login: String): User? = users.find { it.login == login }

    fun deleteUser(login: String): Boolean {
        val user = users.find { it.login == login } ?: return false
        users.remove(user)
        saveUsersToFile()
        return true
    }

    fun updateUser(updatedUser: User): Boolean {
        val index = users.indexOfFirst { it.login == updatedUser.login }
        if (index == -1) return false
        users[index] = updatedUser
        saveUsersToFile()
        if (updatedUser.rememberMe) {
            AppPreferences.saveUserCredentials(updatedUser)
        } else {
            AppPreferences.clearCredentials()
        }
        return true
    }
}