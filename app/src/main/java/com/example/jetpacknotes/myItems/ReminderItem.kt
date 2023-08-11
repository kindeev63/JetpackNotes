package com.example.jetpacknotes.myItems

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.jetpacknotes.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReminderItem(
    title: String,
    time: String,
    date: String,
    actionIcon: Drawable,
    sound: Boolean,
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
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
                Image(
                    modifier = Modifier.size(25.dp),
                    bitmap = actionIcon.toBitmap().asImageBitmap(),
                    contentDescription = null
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        modifier = Modifier.size(17.dp),
                        painter = painterResource(id = if (sound) R.drawable.ic_sound_on else R.drawable.ic_sound_off),
                        contentDescription = null
                    )
                }
                
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }

}

@Composable
@Preview
private fun ReminderItemSelectedPreview() {
    ReminderItem("Напоминание", "14:30", "20.08.2007", LocalContext.current.getDrawable(R.drawable.ic_note)!!, true, true, {}, {})
}

@Composable
@Preview
private fun ReminderItemNoSelectedPreview() {
    ReminderItem("Напоминание", "14:30", "20.08.2007", LocalContext.current.getDrawable(R.drawable.ic_note)!!, false,false, {}, {})
}