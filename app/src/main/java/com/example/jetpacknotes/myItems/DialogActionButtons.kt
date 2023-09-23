package com.example.jetpacknotes.myItems

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DialogActionButtons(
    saveText: String = "save",
    cancelText: String = "cancel",
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(onClick = onCancel) {
            Text(text = cancelText)
        }
        TextButton(onClick = onSave) {
            Text(text = saveText)
        }
    }
}
