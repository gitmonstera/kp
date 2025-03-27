package ui.components

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp

@Composable
fun ButtonWithAction(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(16.dp)) {
        Text(text)
    }
}