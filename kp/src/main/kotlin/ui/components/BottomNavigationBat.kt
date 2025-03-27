package ui.components

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ui.navigation.Screen  // Добавь этот импорт

@Composable
fun BottomNavigationBar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "ПДД") },
            label = { Text("ПДД") },
            selected = selectedScreen == Screen.PDD,
            onClick = { onScreenSelected(Screen.PDD) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Билеты") },
            label = { Text("Билеты") },
            selected = selectedScreen == Screen.BILETY,
            onClick = { onScreenSelected(Screen.BILETY) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Экзамен") },
            label = { Text("Экзамен") },
            selected = selectedScreen == Screen.EXAMEN,
            onClick = { onScreenSelected(Screen.EXAMEN) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Теория") },
            label = { Text("Теория") },
            selected = selectedScreen == Screen.THEORY,
            onClick = { onScreenSelected(Screen.THEORY) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label = { Text("Профиль") },
            selected = selectedScreen == Screen.PROFILE,
            onClick = { onScreenSelected(Screen.PROFILE) }
        )
    }
}