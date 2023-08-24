package com.example.jetpacknotes.myItems

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.ItemColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    title: String,
    time: String?,
    date: String?,
    color: ItemColor,
    done: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
            .clip(
                shape = RoundedCornerShape(
                    topStart = 8.dp, bottomStart = 8.dp
                )
            )
            .background(if (selected) Color.Black else Color.Transparent)
            .padding(2.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 8.dp, bottomStart = 8.dp
                    )
                )
                .background(Color(-2500135))

        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(color.primary),
                        uncheckedColor = Color(color.primary),
                    ),
                    checked = done,
                    onCheckedChange = onCheckChange
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = onLongClick
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(IntrinsicSize.Min)
                            .padding(end = 16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = time ?: "",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = date ?: "",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }

            }

        }
    }

}

@Composable
@Preview
private fun TaskItemSelectedPreview() {
    TaskItem("Задача 1", "14:30", "20.08.2007", Colors.colors[0], true, true, {}, {}, {})
}

@Composable
@Preview
private fun TaskItemNoSelectedPreview() {
    TaskItem("Задача 2", null, null, Colors.colors[1], false, false, {}, {}, {})
}