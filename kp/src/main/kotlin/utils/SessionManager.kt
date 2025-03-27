package utils

import data.User
import data.UserRepository
import java.io.File

object SessionManager {
    private var currentUser: User? = null
    private const val SESSION_FILE = "session.txt"  // Файл для хранения сессии

    init {
        loadSession()
    }

    fun loginUser(login: String, password: String): Boolean {
        val user = UserRepository.getUser(login)
        return if (user != null && user.password == password) {
            currentUser = user
            if (user.remembered) {
                saveSession(user)
            }
            true
        } else {
            false
        }
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }

    fun logout() {
        currentUser = null
        clearSession()
    }

    fun deleteAccount() {
        currentUser?.let {
            UserRepository.deleteUser(it.login)
            logout()
        }
    }

    private fun saveSession(user: User) {
        try {
            File(SESSION_FILE).writeText(user.login)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSession() {
        try {
            val sessionFile = File(SESSION_FILE)
            if (sessionFile.exists()) {
                val login = sessionFile.readText().trim()
                val user = UserRepository.getUser(login)
                if (user != null) {
                    currentUser = user
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearSession() {
        try {
            File(SESSION_FILE).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}