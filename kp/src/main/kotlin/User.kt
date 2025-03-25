import java.util.prefs.Preferences

data class User(val fullName: String, val login: String, val password: String)

fun saveUser(preferences: Preferences, user: User) {
    preferences.put("user_login", user.login)
    preferences.put("user_full_name", user.fullName)
}

fun clearSavedUser(preferences: Preferences) {
    preferences.remove("user_login")
    preferences.remove("user_full_name")
}