package com.example.jetpacknotes.myItems

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun SearchItem(
    modifier: Modifier = Modifier,
    value: String?,
    onValueChange: (String?) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    if (value == null) {
        IconButton(
            onClick = {
                onValueChange("")
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
                value = value,
                onValueChange = {
                    onValueChange(it)
                },
                singleLine = true,
                hintText = "Введите текст...",
                modifier = Modifier.weight(1f),
                textFieldModifier = Modifier.focusRequester(focusRequester)
            )
            IconButton(
                modifier = Modifier.alpha(0.5f),
                onClick = {
                    if (value == "") {
                        onValueChange(null)
                    } else {
                        onValueChange("")
                    }

                }
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
    }
    LaunchedEffect(value == null) {
        if (value != null) {
            focusRequester.requestFocus()
        }
    }
}