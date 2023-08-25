package com.example.jetpacknotes.reminders

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.myItems.AppItem
import com.example.jetpacknotes.myItems.ApplicationData
import com.example.jetpacknotes.myItems.DatePickerDialog
import com.example.jetpacknotes.myItems.NoteItem
import com.example.jetpacknotes.myItems.TaskItem
import com.example.jetpacknotes.myItems.TimePickerDialog
import com.example.jetpacknotes.receivers.AlarmReceiver
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
    Dialog(
        onDismissRequest = { reminderState.value = null },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (LocalConfiguration.current.screenHeightDp / 6 * 4).dp)
                .clip(RoundedCornerShape(5.dp))
                .padding(PaddingValues(horizontal = 24.dp))
                .background(Color.Black)
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(5.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DialogContent(reminder = reminder, mainAppViewModel = mainAppViewModel)
                ActionButtons(
                    reminderState = reminderState,
                    reminder = reminder,
                    mainAppViewModel = mainAppViewModel
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    reminderState: MutableState<ReminderForDialog?>,
    reminder: MutableState<Reminder>,
    mainAppViewModel: MainAppViewModel
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val context = LocalContext.current
        TextButton(onClick = { reminderState.value = null }) {
            Text(text = "cancel")
        }
        TextButton(onClick = {
            if (
                when (reminder.value.action) {
                    ReminderAction.OpenApp -> true
                    ReminderAction.OpenNote -> {
                        if (reminder.value.noteId != null) {
                            true
                        } else {
                            Toast.makeText(context, "Выберите заметку", Toast.LENGTH_SHORT).show()
                            false
                        }
                    }

                    ReminderAction.OpenTask -> {
                        if (reminder.value.taskId != null) {
                            true
                        } else {
                            Toast.makeText(context, "Выберите задачу", Toast.LENGTH_SHORT).show()
                            false
                        }
                    }
                }
            ) {
                mainAppViewModel.insertReminder(reminder.value) {
                    setAlarm(reminder.value, context)
                    reminderState.value = null
                }
            }

        }) {
            Text(text = "save")
        }
    }
}

private fun setAlarm(reminder: Reminder, context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("reminder", reminder)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.id,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, reminder.time, pendingIntent
    )
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
        ReminderActionSpinner(reminder = reminder, mainAppViewModel = mainAppViewModel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            when (reminder.value.action) {
                ReminderAction.OpenApp -> {
                    AppCard(reminder = reminder)
                }

                ReminderAction.OpenNote -> {
                    NoteCard(reminder = reminder, mainAppViewModel = mainAppViewModel)
                }

                ReminderAction.OpenTask -> {
                    TaskCard(reminder = reminder, mainAppViewModel = mainAppViewModel)
                }
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
private fun ReminderActionSpinner(
    reminder: MutableState<Reminder>,
    mainAppViewModel: MainAppViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var expanded by remember {
            mutableStateOf(false)
        }
        val context = LocalContext.current
        val allNotes = mainAppViewModel.allNotes.observeAsState(emptyList())
        val allTasks = mainAppViewModel.allTasks.observeAsState(emptyList())
        Text(text = "Action:")
        Box(
            modifier = Modifier.width((getScreenWidthDp() / 3).dp)
        ) {
            ReminderActionSpinnerItem(
                text = when (reminder.value.action) {
                    ReminderAction.OpenApp -> "Open App"
                    ReminderAction.OpenNote -> "Open Note"
                    ReminderAction.OpenTask -> "Open Task"
                }
            ) {
                expanded = true
            }
            DropdownMenu(
                modifier = Modifier
                    .width((getScreenWidthDp() / 3).dp)
                    .background(Color.White.copy(alpha = 0.5f)),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Divider()
                ReminderActionSpinnerItem(text = "Open App") {
                    reminder.value = reminder.value.copy(action = ReminderAction.OpenApp)
                    expanded = false
                }
                Divider()
                ReminderActionSpinnerItem(text = "Open Note") {
                    if (allNotes.value.isNotEmpty()) {
                        reminder.value = reminder.value.copy(action = ReminderAction.OpenNote)
                    } else {
                        Toast.makeText(context, "Нет заметок", Toast.LENGTH_SHORT).show()
                    }
                    expanded = false
                }
                Divider()
                ReminderActionSpinnerItem(text = "Open Task") {
                    if (allTasks.value.isNotEmpty()) {
                        reminder.value = reminder.value.copy(action = ReminderAction.OpenTask)
                    } else {
                        Toast.makeText(context, "Нет задач", Toast.LENGTH_SHORT).show()
                    }
                    expanded = false
                }
                Divider()
            }
        }
    }
}

@Composable
private fun ReminderActionSpinnerItem(text: String, clickable: () -> Unit = {}) {
    Box(
        modifier = if (clickable != {}) {
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White)
                .clickable { clickable() }
        } else {
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White)
        },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
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
    val showReadAllAppsPermissionNotGrantedDialog = rememberSaveable {
        mutableStateOf(false)
    }
    if (showReadAllAppsPermissionNotGrantedDialog.value) {
        ReadAllAppsPermissionNotGrantedDialog(open = showReadAllAppsPermissionNotGrantedDialog)
    }
    val readAllAppsListPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showReadAllAppsPermissionNotGrantedDialog.value = true
            }
        }
    val scope = rememberCoroutineScope()
    val sharedPreferences = LocalContext.current.applicationContext.getSharedPreferences(
        "settings",
        Context.MODE_PRIVATE
    )
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val permission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (permission == PackageManager.PERMISSION_GRANTED || sharedPreferences.getBoolean(
                        "no_read_storage_permission",
                        false
                    )
                ) {
                    openAppDialog.value = true
                } else {
                    scope.launch {
                        readAllAppsListPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
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
private fun ReadAllAppsPermissionNotGrantedDialog(open: MutableState<Boolean>) {
    Dialog(onDismissRequest = { open.value = false }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вы не сможете выбирать другие приложения, если вы не дадите разрешение на чтение внутренней памяти (необходио для чтения списка приложений) в настройках приложения. Если вы не найдёте этого разрешения то выберите \"не нашёл\""
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val sharedPreferences =
                    LocalContext.current.applicationContext.getSharedPreferences(
                        "settings",
                        Context.MODE_PRIVATE
                    )
                val context = LocalContext.current
                TextButton(onClick = {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                    open.value = false
                }) {
                    Text(
                        text = "в настройки"
                    )
                }
                TextButton(onClick = {
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("no_read_storage_permission", true)
                    editor.apply()
                    open.value = false
                }) {
                    Text(
                        text = "не нашёл"
                    )
                }
            }
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
        reminder.value.noteId?.let { noteId ->
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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun TaskCard(reminder: MutableState<Reminder>, mainAppViewModel: MainAppViewModel) {
    val scope = rememberCoroutineScope()
    val taskState = rememberSaveable {
        mutableStateOf<Task?>(null)
    }
    val openTaskDialog = rememberSaveable {
        mutableStateOf(false)
    }
    scope.launch {
        reminder.value.taskId?.let { taskId ->
            mainAppViewModel.getTaskById(taskId) {
                taskState.value = it
            }
        }
    }
    if (openTaskDialog.value) {
        PickTaskDialog(
            open = openTaskDialog,
            reminder = reminder,
            mainAppViewModel = mainAppViewModel
        )
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                openTaskDialog.value = true
            }
    ) {
        val task = taskState.value
        if (task != null) {
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
                    Checkbox(
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(Colors.colors[task.colorIndex].primary),
                            uncheckedColor = Color(Colors.colors[task.colorIndex].primary),
                        ),
                        checked = task.done,
                        onCheckedChange = {}
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = task.title,
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
                                text = timeFormatter.format(task.time) ?: "",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateFormatter.format(task.time) ?: "",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
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
private fun PickTaskDialog(
    open: MutableState<Boolean>,
    reminder: MutableState<Reminder>,
    mainAppViewModel: MainAppViewModel
) {
    val screenWidthDp = getScreenWidthDp()
    val allTasks = mainAppViewModel.allTasks.observeAsState(emptyList())
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
                        items = allTasks.value,
                        key = { it.id }
                    ) { task ->
                        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        TaskItem(
                            title = task.title,
                            time = timeFormatter.format(task.time),
                            date = dateFormatter.format(task.time),
                            color = Colors.colors[task.colorIndex],
                            done = task.done,
                            selected = false,
                            onClick = {
                                reminder.value = reminder.value.copy(taskId = task.id)
                                open.value = false
                            },
                            onLongClick = {
                                reminder.value = reminder.value.copy(taskId = task.id)
                                open.value = false
                            },
                            onCheckChange = {},
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
        id = reminderId,
        title = "",
        description = "",
        time = LocalDateTime.now().atZone(ZoneId.systemDefault()).withSecond(0).toInstant()
            .toEpochMilli(),
        noteId = null,
        taskId = null,
        packageName = packageName,
        sound = true,
        action = ReminderAction.OpenApp
    )
}

@Composable
private fun getScreenWidthDp() =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp
