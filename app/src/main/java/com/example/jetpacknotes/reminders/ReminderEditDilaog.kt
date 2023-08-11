package com.example.jetpacknotes.reminders

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.core.graphics.drawable.toBitmap
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.myItems.AppItem
import com.example.jetpacknotes.myItems.ApplicationData
import com.example.jetpacknotes.myItems.DatePickerDialog
import com.example.jetpacknotes.myItems.NoteItem
import com.example.jetpacknotes.myItems.TimePickerDialog
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


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
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                    .padding(5.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DialogContent(reminder = reminder, mainAppViewModel = mainAppViewModel)
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
private fun DialogContent(reminder: MutableState<Reminder>, mainAppViewModel: MainAppViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time, Date and SoundIcon
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimeRow(reminder = reminder)
                DateRow(reminder = reminder)
            }
            SoundIcon(
                modifier = Modifier.align(Alignment.TopEnd),
                reminder = reminder
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Title(reminder = reminder)
        Spacer(modifier = Modifier.height(5.dp))
        Description(reminder = reminder)
        ActionRadioGroup(reminder = reminder, mainAppViewModel = mainAppViewModel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            if (reminder.value.action == ReminderAction.OpenApp) {
                AppCard(reminder = reminder)
            } else {
                NoteCard(reminder = reminder, mainAppViewModel = mainAppViewModel)
            }
        }

    }
}

@Composable
private fun TimeRow(
    modifier: Modifier = Modifier,
    reminder: MutableState<Reminder>
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showTimePickerDialog) {
        TimePickerDialog(
            time = reminder.value.time,
            onCloseDialog = {
                showTimePickerDialog = false
            },
            onPick = { hour, minute ->
                val instant = Instant.ofEpochMilli(reminder.value.time)
                val reminderTime =
                    LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).atZone(
                        ZoneId.systemDefault()
                    )
                val newTime = reminderTime.withHour(hour).withMinute(minute)
                reminder.value = reminder.value.copy(time = newTime.toInstant().toEpochMilli())
            }
        )
    }
    Text(
        modifier = modifier.clickable {
            showTimePickerDialog = true
        },
        text = timeFormatter.format(reminder.value.time),
        fontSize = ((if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp) * 0.1).sp
    )
}

@Composable
private fun SoundIcon(
    modifier: Modifier = Modifier,
    reminder: MutableState<Reminder>
) {
    val soundIcon by animateIntAsState(if (reminder.value.sound) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
    IconButton(
        modifier = modifier,
        onClick = {
            reminder.value = reminder.value.copy(sound = !reminder.value.sound)
        }) {
        Icon(
            painter = painterResource(id = soundIcon),
            contentDescription = null
        )
    }
}

@Composable
private fun DateRow(
    modifier: Modifier = Modifier,
    reminder: MutableState<Reminder>
) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDatePickerDialog) {
        DatePickerDialog(
            time = reminder.value.time,
            onCloseDialog = {
                showDatePickerDialog = false
            },
            onPick = { year, month, day ->
                val instant = Instant.ofEpochMilli(reminder.value.time)
                val reminderTime =
                    LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).atZone(
                        ZoneId.systemDefault()
                    )
                val localDateTime =
                    reminderTime.withYear(year).withMonth(month).withDayOfMonth(day)
                reminder.value =
                    reminder.value.copy(time = localDateTime.toInstant().toEpochMilli())
            }
        )
    }
    Text(
        modifier = modifier.clickable {
            showDatePickerDialog = true
        },
        text = dateFormatter.format(reminder.value.time),
        fontSize = ((if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp) * 0.07).sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Title(reminder: MutableState<Reminder>) {
    OutlinedTextField(
        value = reminder.value.title,
        onValueChange = {
            reminder.value = reminder.value.copy(title = it)
        },
        placeholder = {
            Text(text = "Title")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Description(reminder: MutableState<Reminder>) {
    OutlinedTextField(
        value = reminder.value.description,
        onValueChange = {
            reminder.value = reminder.value.copy(description = it)
        },
        placeholder = {
            Text(text = "Description")
        }
    )
}

@Composable
private fun ActionRadioGroup(reminder: MutableState<Reminder>, mainAppViewModel: MainAppViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = reminder.value.action == ReminderAction.OpenApp, onClick = {
            if (reminder.value.action != ReminderAction.OpenApp) {
                reminder.value = reminder.value.copy(action = ReminderAction.OpenApp)
            }
        })
        Text(text = "Open App")
        RadioButton(selected = reminder.value.action == ReminderAction.OpenNote, onClick = {
            if (mainAppViewModel.allNotes.value?.isNotEmpty() == true) {
                if (reminder.value.action != ReminderAction.OpenNote) {
                    reminder.value = reminder.value.copy(action = ReminderAction.OpenNote)
                }
            }

        })
        Text(text = "Open Note")
    }

}

@Composable
private fun AppCard(reminder: MutableState<Reminder>) {
    val context = LocalContext.current
    val appInfo = context.packageManager.getApplicationInfo(
        reminder.value.packageName,
        PackageManager.GET_META_DATA
    )
    val appIcon = context.packageManager.getApplicationIcon(appInfo).toBitmap().asImageBitmap()
    val appName = context.packageManager.getApplicationLabel(appInfo).toString()
    val openAppDialog = rememberSaveable {
        mutableStateOf(false)
    }
    if (openAppDialog.value) {
        PickAppDialog(open = openAppDialog, reminder = reminder)
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                openAppDialog.value = true
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.size(45.dp),
                bitmap = appIcon,
                contentDescription = null
            )
            Text(
                text = appName,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 1
            )
        }

    }
}

@Composable
private fun PickAppDialog(open: MutableState<Boolean>, reminder: MutableState<Reminder>) {
    val screenWidthDp = getScreenWidthDp()
    val context = LocalContext.current
    val allApps = context.packageManager.getInstalledApplications(PackageManager.MATCH_ALL).filter {
        (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
    }.map {
        ApplicationData(
            name = it.loadLabel(context.packageManager).toString(),
            icon = it.loadIcon(context.packageManager).toBitmap().asImageBitmap(),
            packageName = it.packageName
        )
    }
    Dialog(onDismissRequest = { open.value = false }) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .padding(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenWidthDp * 1.5).dp)
            ) {
                LazyColumn {
                    items(
                        items = allApps,
                        key = { it.packageName }
                    ) { applicationData ->
                        AppItem(applicationData = applicationData) {
                            reminder.value =
                                reminder.value.copy(packageName = applicationData.packageName)
                            open.value = false
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { open.value = false }) {
                    Text(text = "cancel")
                }
            }
        }

    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun NoteCard(reminder: MutableState<Reminder>, mainAppViewModel: MainAppViewModel) {
    val scope = rememberCoroutineScope()
    val noteState = rememberSaveable {
        mutableStateOf<Note?>(null)
    }
    val openNoteDialog = rememberSaveable {
        mutableStateOf(false)
    }
    scope.launch {
        reminder.value.noteId?.let {noteId ->
            mainAppViewModel.getNoteById(noteId) {
                noteState.value = it
            }
        }
    }
    if (openNoteDialog.value) {
        PickNoteDialog(
            open = openNoteDialog,
            reminder = reminder,
            mainAppViewModel = mainAppViewModel
        )
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                openNoteDialog.value = true
            }
    ) {
        val note = noteState.value
        if (note != null) {
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
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
                    Text(
                        text = timeFormatter.format(note.time),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateFormatter.format(note.time),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(-2500135))
            )
        }
    }
}

@Composable
private fun PickNoteDialog(
    open: MutableState<Boolean>,
    reminder: MutableState<Reminder>,
    mainAppViewModel: MainAppViewModel
) {
    val screenWidthDp = getScreenWidthDp()
    val allNotes = mainAppViewModel.allNotes.observeAsState(emptyList())
    Dialog(onDismissRequest = { open.value = false }) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .padding(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenWidthDp * 1.5).dp)
            ) {
                LazyColumn {
                    items(
                        items = allNotes.value,
                        key = { it.id }
                    ) { note ->
                        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        NoteItem(
                            title = note.title,
                            time = timeFormatter.format(note.time),
                            date = dateFormatter.format(note.time),
                            color = Colors.colors[note.colorIndex],
                            selected = false,
                            onClick = {
                                reminder.value = reminder.value.copy(noteId = note.id)
                                open.value = false
                            },
                            onLongClick = {
                                reminder.value = reminder.value.copy(noteId = note.id)
                                open.value = false
                            }
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { open.value = false }) {
                    Text(text = "cancel")
                }
            }
        }

    }
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

@Composable
private fun getScreenWidthDp() =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp
