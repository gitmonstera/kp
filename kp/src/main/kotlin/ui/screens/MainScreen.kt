package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ui.components.BottomNavigationBar
import ui.components.TopAppBarWithMenu
import ui.navigation.Screen

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

    Scaffold(
        topBar = { if (currentScreen != Screen.LOGIN && currentScreen != Screen.REGISTER) TopAppBarWithMenu() },
        bottomBar = { if (currentScreen !in listOf(Screen.LOGIN, Screen.REGISTER)) BottomNavigationBar(currentScreen) { currentScreen = it } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentScreen) {
                Screen.LOGIN -> LoginScreen { currentScreen = Screen.PDD }
                Screen.REGISTER -> RegistrationScreen { currentScreen = Screen.LOGIN }
                Screen.PDD -> PddScreen()
                Screen.BILETY -> BiletyScreen()
                Screen.EXAMEN -> ExamenScreen()
                Screen.THEORY -> TheoryScreen()
                Screen.PROFILE -> ProfileScreen()
            }
        }
    }
}