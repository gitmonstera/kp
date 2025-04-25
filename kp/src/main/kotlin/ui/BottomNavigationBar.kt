package ui


import Screen
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label = { Text("Главная") },
            selected = currentScreen == Screen.MainMenu,
            onClick = { onScreenSelected(Screen.MainMenu) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Экзамены") },
            label = { Text("Экзамены") },
            selected = currentScreen == Screen.Exams,
            onClick = { onScreenSelected(Screen.Exams) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Билеты") },
            label = { Text("Билеты") },
            selected = currentScreen == Screen.Tickets,
            onClick = { onScreenSelected(Screen.Tickets) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Статистика") },
            label = { Text("Статистика") },
            selected = currentScreen == Screen.Statistics,
            onClick = { onScreenSelected(Screen.Statistics) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
            label = { Text("Настройки") },
            selected = currentScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) }
        )
    }
}
