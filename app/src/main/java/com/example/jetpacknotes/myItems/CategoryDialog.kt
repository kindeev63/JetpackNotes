package com.example.jetpacknotes.myItems

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jetpacknotes.db.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    category: Category,
    deleteCategory: (Category) -> Unit,
    insertCategory: (Category) -> Unit,
    onDismissReqest: () -> Unit
) {
        Dialog(onDismissRequest = onDismissReqest) {
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val categoryName = rememberSaveable {
                        mutableStateOf(category.name)
                    }
                    val error = rememberSaveable {
                        mutableStateOf(false)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = categoryName.value,
                            onValueChange = {
                                categoryName.value = it
                                error.value = it == ""
                            },
                            singleLine = true,
                            label = {
                                Text(text = "name")
                            },
                            supportingText = {
                                if (error.value) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Name must not be empty",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            trailingIcon = {
                                if (error.value)
                                    Icon(
                                        Icons.Filled.Info,
                                        "error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                            }
                        )
                        if (category.name != "") {
                            IconButton(onClick = {
                                deleteCategory(category)
                                onDismissReqest()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }

                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = onDismissReqest) {
                            Text(text = "cancel")
                        }
                        TextButton(onClick = {
                            if (categoryName.value != "") {
                                insertCategory(category.copy(name = categoryName.value))
                                onDismissReqest()
                            }
                        }) {
                            Text(text = "save")
                        }
                    }
                }
            }
        }
}