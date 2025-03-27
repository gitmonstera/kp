package data

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val login: String,
    val password: String,
    val remembered: Boolean
)