package ui.screens

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.components.ButtonWithAction
import utils.SessionManager

@Composable
fun ProfileScreen() {
    val user = SessionManager.getCurrentUser()
    if (user != null){
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ФИО: ${user.fullName}")
            Text("Логин: ${user.login}")
            Text("Email: ${user.email}")

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    else {
       Text("Пользователь не найден")
    }

}