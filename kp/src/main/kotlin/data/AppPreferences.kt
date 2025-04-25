package data


import data.bd.Users
import com.google.gson.Gson
import data.model.User
import java.io.File

object AppPreferences {
    private val appDir = File(System.getProperty("user.home"), "AppData/Local/MyApp")

    private val prefsFile = File(appDir, "app_preferences.json")
    private val gson = Gson()

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
    }

    fun saveUserCredentials(user: User) {
        prefsFile.writeText(gson.toJson(user))
    }

    fun loadUserCredentials(): User? {
        return if (prefsFile.exists()) {
            gson.fromJson(prefsFile.readText(), User::class.java)
        } else {
            null
        }
    }

    fun clearCredentials() {
        if (prefsFile.exists()) {
            prefsFile.delete()
        }
    }
}
