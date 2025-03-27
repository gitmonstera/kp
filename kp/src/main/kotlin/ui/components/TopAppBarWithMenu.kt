package ui.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable

@Composable
fun TopAppBarWithMenu() {
    TopAppBar(
        title = { Text("Учебное приложение") },
        navigationIcon = {
            IconButton(onClick = { /* Обработчик меню */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Меню")
            }
        }
    )
}