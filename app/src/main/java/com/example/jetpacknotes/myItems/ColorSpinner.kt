package com.example.jetpacknotes.myItems

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorSpinner(
    colors: List<Color>,
    colorIndex: Int,
    onPick: (index: Int) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Box {
        ColorSpinnerItem(color = colors[colorIndex]) {
            expanded = true
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            colors.forEachIndexed { index, color ->
                ColorSpinnerItem(color = color) {
                    onPick(index)
                    expanded = false
                }
            }
        }
    }
}

@Composable
private fun ColorSpinnerItem(color: Color, clickable: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color)
            .border(BorderStroke(2.dp, Color.Black))
            .clickable {
                clickable()
            }
    )
}