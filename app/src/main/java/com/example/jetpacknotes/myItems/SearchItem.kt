package com.example.jetpacknotes.myItems

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.jetpacknotes.notes.PlaceholderTextField

@Composable
fun SearchItem(
    modifier: Modifier = Modifier,
    searchText: MutableState<String?>,
) {
    if (searchText.value == null) {
        IconButton(
            onClick = {
                searchText.value = ""
            }
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceholderTextField(
                boxModifier = Modifier.weight(1f),
                textModifier = Modifier.fillMaxWidth(),
                value = searchText.value ?: "",
                onValueChange = {
                    searchText.value = it
                },
                singleLine = true,
                textStyle = TextStyle.Default.copy(fontSize = 18.sp),
                hintText = "Введите текст..."
            )
            IconButton(
                modifier = Modifier.alpha(0.5f),
                onClick = {
                    if (searchText.value == "") {
                        searchText.value = null
                    } else {
                        searchText.value = ""
                    }

                }
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
    }
}