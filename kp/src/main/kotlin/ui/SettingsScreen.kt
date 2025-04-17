import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    user: User,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSave: (User) -> Unit,
    onLogout: () -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf(user.password) }
    var rememberMe by remember { mutableStateOf(user.rememberMe) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚙️ Настройки", style = MaterialTheme.typography.h5)

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Запомнить меня")
        }

        Button(
            onClick = {
                val updatedUser = user.copy(
                    fullName = fullName,
                    email = email,
                    password = password,
                    rememberMe = rememberMe
                )
                UserRepository.updateUser(updatedUser)
                AppPreferences.saveUserCredentials(updatedUser)
                onSave(updatedUser)
            }
        ) {
            Text("💾 Сохранить изменения")
        }

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
            onClick = {
                AppPreferences.clearCredentials()
                StatisticsRepository.clearStats(user.login)
                onLogout()
            }
        ) {
            Text("🚪 Выйти")
        }

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
            onClick = {
                UserRepository.deleteUser(user.login)
                StatisticsRepository.clearStats(user.login)
                AppPreferences.clearCredentials()
                onLogout()
            }
        ) {
            Text("🗑️ Удалить аккаунт")
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
            Text("Темная тема")
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}
