package com.example.jetpacknotes.myItems

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.io.Serializable
import java.util.Calendar

@Composable
fun TimePickerDialog(
    hour: Int,
    minute: Int,
    onCloseDialog: () -> Unit,
    colors: TimePickerDialogColors = TimePickerDialogDefaults.colors,
    onPick: (hour: Int, minute: Int) -> Unit
) {
    val time = rememberSaveable {
        mutableStateOf(TimeForDialog(hour, minute))
    }
    Dialog(onDismissRequest = onCloseDialog) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.backgroundColor)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val selected = rememberSaveable {
                mutableStateOf(0)
            }
            TimeText(selected = selected, time = time, colors = colors)
            Spacer(modifier = Modifier.height(5.dp))
            NumberButtons(colors = colors, selected = selected, time = time)
            Spacer(modifier = Modifier.height(5.dp))
            ActionButtons(
                time = time,
                colors = colors,
                onCloseDialog = onCloseDialog,
                onPick = onPick
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    time: Long,
    colors: TimePickerDialogColors = TimePickerDialogDefaults.colors,
    onCloseDialog: () -> Unit,
    onPick: (hour: Int, minute: Int) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
    }
    TimePickerDialog(
        hour = calendar[Calendar.HOUR_OF_DAY],
        minute = calendar[Calendar.MINUTE],
        colors = colors,
        onCloseDialog = onCloseDialog,
        onPick = onPick
    )
}

private fun nextTime(selected: MutableState<Int>) {
    if (selected.value < 3) selected.value++ else selected.value = 0
}

private fun previousTime(selected: MutableState<Int>) {
    if (selected.value > 0) selected.value-- else selected.value = 3
}

private fun numberButtonClicked(
    number: Int,
    selected: MutableState<Int>,
    time: MutableState<TimeForDialog>
) {
    var hour = time.value.hour
    var minute = time.value.minute
    var twiceMoves = false
    when (selected.value) {
        0 -> {
            hour = if (number > 2) {
                twiceMoves = true
                number
            } else {
                hour % 10 + number * 10
            }
        }

        1 -> {
            hour = (hour / 10).toInt() * 10 + number
        }

        2 -> {
            minute = if (number > 5) {
                twiceMoves = true
                number
            } else {
                minute % 10 + number * 10
            }
        }

        3 -> {
            minute = (minute / 10).toInt() * 10 + number
        }
    }
    time.value = time.value.copy(hour = hour, minute = minute)
    if (twiceMoves) {
        selected.value = if (selected.value == 0) 2 else 0
    } else {
        nextTime(selected)
    }


}

@Composable
private fun NumberButtons(
    colors: TimePickerDialogColors,
    selected: MutableState<Int>,
    time: MutableState<TimeForDialog>
) {
    val screenWidth = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenWidth.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                NumberButton(text = (it + 1).toString(), colors = colors) {
                    numberButtonClicked(it + 1, selected, time)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                NumberButton(text = (it + 4).toString(), colors = colors) {
                    numberButtonClicked(it + 4, selected, time)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                NumberButton(text = (it + 7).toString(), colors = colors) {
                    numberButtonClicked(it + 7, selected, time)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ArrowButton(
                icon = Icons.Rounded.KeyboardArrowLeft,
                colors = colors
            ) {
                previousTime(selected)
            }
            NumberButton(text = "0", colors = colors) {
                numberButtonClicked(0, selected, time)
            }
            ArrowButton(
                icon = Icons.Rounded.KeyboardArrowRight,
                colors = colors
            ) {
                nextTime(selected)
            }
        }
    }
}

@Composable
private fun NumberButton(text: String, colors: TimePickerDialogColors, onClick: () -> Unit) {
    val screenWidth = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp
    Button(
        modifier = Modifier
            .size((screenWidth / 4).dp - 4.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.numberButtonColor
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = (screenWidth / 10).sp,
            color = colors.numberButtonTextColor
        )
    }
}

@Composable
private fun ArrowButton(icon: ImageVector, colors: TimePickerDialogColors, onClick: () -> Unit) {
    val screenWidth = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp
    Button(
        modifier = Modifier
            .size((screenWidth / 4).dp - 4.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.arrowButtonColor
        ),
        onClick = onClick
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.arrowButtonTextColor)
    }
}

@Composable
private fun ActionButtons(
    time: MutableState<TimeForDialog>,
    colors: TimePickerDialogColors,
    onCloseDialog: () -> Unit,
    onPick: (hour: Int, minute: Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(onClick = onCloseDialog) {
            Text(
                text = "cancel",
                color = colors.actionButtonTextColor
            )
        }
        TextButton(onClick = {
            onPick(time.value.hour, time.value.minute)
            onCloseDialog()
        }) {
            Text(
                text = "save",
                color = colors.actionButtonTextColor
            )
        }
    }
}

@Composable
private fun TimeText(
    selected: MutableState<Int>,
    time: MutableState<TimeForDialog>,
    colors: TimePickerDialogColors
) {
    val screenWidth = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp
    val fontSize = screenWidth / 10
    Box(
        modifier = Modifier
            .width((screenWidth / 3.3).dp)
            .height((screenWidth / 7.5).dp)
    ) {
        val padding by animateDpAsState(if (selected.value < 2) 0.dp else (screenWidth / 3.3 / 5 * 3).dp)
        Row {
            Spacer(modifier = Modifier.width(padding))
            Box(
                modifier = Modifier
                    .width((screenWidth / 3.3 / 5 * 2).dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.rectangleColor)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TimeTextNumber(
                    time = time,
                    selected = selected,
                    index = 0,
                    fontSize = fontSize,
                    colors = colors
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TimeTextNumber(
                    time = time,
                    selected = selected,
                    index = 1,
                    fontSize = fontSize,
                    colors = colors
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ":",
                    fontSize = fontSize.sp,
                    color = colors.unselectedTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TimeTextNumber(
                    time = time,
                    selected = selected,
                    index = 2,
                    fontSize = fontSize,
                    colors = colors
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TimeTextNumber(
                    time = time,
                    selected = selected,
                    index = 3,
                    fontSize = fontSize,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TimeTextNumber(
    time: MutableState<TimeForDialog>,
    selected: MutableState<Int>,
    index: Int,
    fontSize: Int,
    colors: TimePickerDialogColors
) {
    val text = when (index) {
        0 -> if (time.value.hour.toString().length == 1) "0" else time.value.hour.toString()[0].toString()
        1 -> time.value.hour.toString().last().toString()
        2 -> if (time.value.minute.toString().length == 1) "0" else time.value.minute.toString()[0].toString()
        3 -> time.value.minute.toString().last().toString()
        else -> ""
    }
    val color by animateColorAsState(if (selected.value == index) colors.selectedTextColor else colors.unselectedTextColor)
    Text(
        text = text,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold
    )
}

private data class TimeForDialog(val hour: Int, val minute: Int) : Serializable
data class TimePickerDialogColors(
    val backgroundColor: Color,
    val numberButtonTextColor: Color,
    val numberButtonColor: Color,
    val arrowButtonTextColor: Color,
    val arrowButtonColor: Color,
    val selectedTextColor: Color,
    val unselectedTextColor: Color,
    val rectangleColor: Color,
    val actionButtonTextColor: Color
)

object TimePickerDialogDefaults {
    val colors = TimePickerDialogColors(
        backgroundColor = Color.White,
        numberButtonTextColor = Color.Black,
        numberButtonColor = Color.Gray.copy(alpha = 0.1f),
        arrowButtonTextColor = Color.Black.copy(alpha = 0.4f),
        arrowButtonColor = Color(0xFF6650a4).copy(alpha = 0.3f),
        selectedTextColor = Color(0xFF6650a4),
        unselectedTextColor = Color.Black,
        rectangleColor = Color(0xFF6650a4).copy(alpha = 0.3f),
        actionButtonTextColor = Color(0xFF6650a4)
    )
}

@Preview
@Composable
private fun PreviewTimePicker() {
    val show = remember {
        mutableStateOf(true)
    }
    if (show.value) {
        TimePickerDialog(
            time = Calendar.getInstance().timeInMillis,
            onCloseDialog = {
                show.value = false
            },
            onPick = { _, _ ->

            }
        )
    }

}