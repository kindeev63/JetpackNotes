package com.example.jetpacknotes.reminders

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.db.ReminderAction
import com.example.jetpacknotes.viewModels.MainAppViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.jetpacknotes.R
import com.example.jetpacknotes.myItems.DatePickerDialog
import com.example.jetpacknotes.myItems.TimePickerDialog
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalField


@Composable
fun ReminderEditDialog(
    reminderState: MutableState<ReminderForDialog?>,
    mainAppViewModel: MainAppViewModel
) {
    val packageName = LocalContext.current.packageName
    val reminder = rememberSaveable {
        mutableStateOf(
            reminderState.value?.reminder ?: createReminder(
                mainAppViewModel,
                packageName
            )
        )
    }
    Dialog(onDismissRequest = { reminderState.value = null }) {
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
                DialogContent(reminder = reminder)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { reminderState.value = null }) {
                        Text(text = "cancel")
                    }
                    TextButton(onClick = {
                        mainAppViewModel.insertReminder(reminder.value) {
                            reminderState.value = null
                        }
                    }) {
                        Text(text = "save")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogContent(reminder: MutableState<Reminder>) {
    TimeRow(reminder = reminder)
    DateRow(reminder = reminder)
}

@Composable
private fun TimeRow(reminder: MutableState<Reminder>) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val showTimePickerDialog = rememberSaveable {
            mutableStateOf(false)
        }
        if (showTimePickerDialog.value) {
            TimePickerDialog(
                time = reminder.value.time,
                onCloseDialog = {
                    showTimePickerDialog.value = false
                },
                onPick = { hour, minute ->
                    Log.e("test", "$hour:$minute")
                    val instant = Instant.ofEpochMilli(reminder.value.time)
                    val reminderTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).atZone(
                        ZoneId.systemDefault())
                    val newTime = reminderTime.withHour(hour).withMinute(minute)
                    reminder.value = reminder.value.copy(time = newTime.toInstant().toEpochMilli())
                }
            )
        }

        Text(
            modifier = Modifier.clickable {
                showTimePickerDialog.value = true
            },
            text = timeFormatter.format(reminder.value.time),
            fontSize = ((if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp) * 0.1).sp
        )

        Spacer(modifier = Modifier.width(5.dp))
        IconButton(onClick = {
            reminder.value = reminder.value.copy(sound = !reminder.value.sound)
        }) {
            Icon(
                painter = painterResource(id = if (reminder.value.sound) R.drawable.ic_sound_on else R.drawable.ic_sound_off),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun DateRow(reminder: MutableState<Reminder>) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val showDatePickerDialog = rememberSaveable {
            mutableStateOf(false)
        }
        if (showDatePickerDialog.value) {
            DatePickerDialog(
                time = reminder.value.time,
                onCloseDialog = {
                    showDatePickerDialog.value = false
                },
                onPick = { year, month, day ->
                    val localDateTime = LocalDateTime.of(year, month, day, 0, 0)
                    reminder.value = reminder.value.copy(time = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli())
                }
            )
        }

        Text(
            modifier = Modifier.clickable {
                showDatePickerDialog.value = true
            },
            text = dateFormatter.format(reminder.value.time),
            fontSize = ((if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp) * 0.07).sp
        )
    }
}

@Composable
private fun Title() {

}

@Composable
private fun Description() {

}

@Composable
private fun ActionRadioGroup() {

}

@Composable
private fun AppCard() {

}

@Composable
private fun NoteCard() {

}

private fun createReminder(mainAppViewModel: MainAppViewModel, packageName: String): Reminder {
    val idsList = (mainAppViewModel.allReminders.value ?: emptyList()).map { it.id }
    var reminderId = 0
    while (true) {
        if (reminderId !in idsList) break
        reminderId++
    }
    return Reminder(
        reminderId,
        "",
        "",
        LocalDateTime.now().atZone(ZoneId.systemDefault()).withSecond(0).toInstant().toEpochMilli(),
        null,
        packageName,
        true,
        ReminderAction.OpenApp
    )
}