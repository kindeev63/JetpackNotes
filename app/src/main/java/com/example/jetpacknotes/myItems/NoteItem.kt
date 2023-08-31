package com.example.jetpacknotes.myItems

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.db.Note
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.DragNoteItem(
    note: Note,
    offsetY: Float,
    move: Boolean,
    selected: Boolean,
    onDragStart: (Float) -> Unit,
    onVerticalDrag: (positionY: Float) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
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
                    .fillMaxSize()
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = 8.dp, bottomStart = 8.dp
                        )
                    )
                    .background(Color(-2500135))
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(15.dp)
                            .background(color = Color(Colors.colors[note.colorIndex].primary))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(color = Color(Colors.colors[note.colorIndex].secondary))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = note.title,
                        color = Color.Black,
                        maxLines = 1
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Min)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    Text(
                        text = timeFormatter.format(note.lastEditTime),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateFormatter.format(note.lastEditTime),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(5.dp))
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
fun NoteItem(
    note: Note,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
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
                .clickable(onClick = onClick)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),

            ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(15.dp)
                        .background(color = Color(Colors.colors[note.colorIndex].primary))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(color = Color(Colors.colors[note.colorIndex].secondary))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = note.title,
                    color = Color.Black,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                Text(
                    text = timeFormatter.format(note.lastEditTime),
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormatter.format(note.lastEditTime),
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }

}

private fun Modifier.executeIf(condition: Boolean, action: Modifier.() -> Modifier): Modifier {
    return if (condition) this.action() else this
}

@Composable
fun GhostNoteItem(
    note: Note,
    offsetY: Float,
    selected: Boolean,
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
            .background(if (selected) Color.Black else Color.Transparent)
            .padding(2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(15.dp)
                        .background(color = Color(Colors.colors[note.colorIndex].primary))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(color = Color(Colors.colors[note.colorIndex].secondary))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = note.title,
                    color = Color.Black,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                Text(
                    text = timeFormatter.format(note.lastEditTime),
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormatter.format(note.lastEditTime),
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
            Box(
                modifier = Modifier.size(40.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Filled.Menu,
                contentDescription = null
            )
        }
    }

}

data class GhostNoteData(val note: Note, val offsetY: Float)