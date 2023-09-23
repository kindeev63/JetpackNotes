package com.example.jetpacknotes.myItems

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.db.Task


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.DragTaskItem(
    task: Task,
    offsetY: Float,
    move: Boolean,
    selected: Boolean,
    onDragStart: (Float) -> Unit,
    onVerticalDrag: (positionY: Float) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = 0.dp, y = offsetY.dp)
            .fillMaxWidth()
            .height(80.dp)
            .padding(3.dp)
            .clip(
                shape = RoundedCornerShape(
                    topStart = 8.dp, bottomStart = 8.dp
                )
            )
            .executeIf(!move) {
                background(if (selected) Color.Black else Color.Transparent)
            }
            .padding(2.dp)
            .executeIf((offsetY / 80).toInt() == 0) {
                animateItemPlacement()
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        if (!move) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = 8.dp, bottomStart = 8.dp
                        )
                    )
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
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
                            checkedColor = Color(Colors.colors[task.colorIndex].primary),
                            uncheckedColor = Color(Colors.colors[task.colorIndex].primary),
                            disabledCheckedColor = Color(Colors.colors[task.colorIndex].primary),
                            disabledUncheckedColor = Color(Colors.colors[task.colorIndex].primary)
                        ),
                        checked = task.done,
                        enabled = !selected,
                        onCheckedChange = onCheckChange
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = task.title,
                            color = if (task.done) Color.Gray else Color.Black,
                            maxLines = 1,
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            onDragStart(offset.y)
                        },
                        onDragEnd = {
                            onDragEnd()
                        },
                        onVerticalDrag = { change, _ ->
                            onVerticalDrag(change.position.y)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (!move) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Filled.Menu,
                    contentDescription = null
                )
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
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
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
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
                        checkedColor = Color(Colors.colors[task.colorIndex].primary),
                        uncheckedColor = Color(Colors.colors[task.colorIndex].primary),
                        disabledCheckedColor = Color(Colors.colors[task.colorIndex].primary),
                        disabledUncheckedColor = Color(Colors.colors[task.colorIndex].primary)
                    ),
                    checked = task.done,
                    enabled = !selected,
                    onCheckedChange = onCheckChange
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = task.title,
                        color = if (task.done) Color.Gray else Color.Black,
                        maxLines = 1,
                        textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
            }
        }
    }
}

private fun Modifier.executeIf(condition: Boolean, action: Modifier.() -> Modifier): Modifier {
    return if (condition) this.action() else this
}

@Composable
fun GhostTaskItem(
    task: Task,
    offsetY: Float,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .offset(x = 0.dp, y = offsetY.dp)
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
                        checkedColor = Color(Colors.colors[task.colorIndex].primary),
                        uncheckedColor = Color(Colors.colors[task.colorIndex].primary),
                        disabledCheckedColor = Color(Colors.colors[task.colorIndex].primary),
                        disabledUncheckedColor = Color(Colors.colors[task.colorIndex].primary)
                    ),
                    checked = task.done,
                    enabled = false,
                    onCheckedChange = {}
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = task.title,
                        color = if (task.done) Color.Gray else Color.Black,
                        maxLines = 1,
                        textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
            }
        }
    }
}

data class GhostTaskData(val task: Task, val offsetY: Float)