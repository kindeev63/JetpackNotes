package com.example.jetpacknotes.myItems

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaceholderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String = "",
    singleLine: Boolean = false,
    boxModifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp
) {
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            textStyle = TextStyle.Default.copy(fontSize = fontSize)
        )
        if (value.isEmpty()) {
            Text(
                text = hintText,
                style = TextStyle(color = Color.Gray),
                fontSize = fontSize,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}