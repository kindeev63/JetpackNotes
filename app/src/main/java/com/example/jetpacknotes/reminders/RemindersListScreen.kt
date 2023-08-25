package com.example.jetpacknotes.reminders

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.db.ReminderAction
import com.example.jetpacknotes.myItems.ReminderItem
import com.example.jetpacknotes.myItems.SearchItem
import com.example.jetpacknotes.viewModels.MainAppViewModel
import com.example.jetpacknotes.viewModels.RemindersListScreenViewModel
import com.example.jetpacknotes.viewModels.RemindersListScreenViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersListScreen(
    mainAppViewModel: MainAppViewModel
) {
    val viewModel: RemindersListScreenViewModel = viewModel(
        factory = RemindersListScreenViewModelFactory(mainAppViewModel)
    )
    val context = LocalContext.current
    val remindersList = mainAppViewModel.allReminders.observeAsState(listOf())
    val selectedReminders = viewModel.selectedReminders.observeAsState(listOf())
    val searchText = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    val scope = rememberCoroutineScope()
    val openReminderDialog = rememberSaveable {
        mutableStateOf<ReminderForDialog?>(null)
    }
    val showNotificationPermissionNotGrantedDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val showNotificationPermissionGrantedDialog = rememberSaveable {
        mutableStateOf(false)
    }
    openReminderDialog.value?.let {
        ReminderEditDialog(
            reminderState = openReminderDialog,
            mainAppViewModel = mainAppViewModel
        )
    }
    if (showNotificationPermissionNotGrantedDialog.value) {
        NotificationPermissionNotGrantedDialog(open = showNotificationPermissionNotGrantedDialog)
    }
    if (showNotificationPermissionGrantedDialog.value) {
        NotificationPermissionGrantedDialog(open = showNotificationPermissionGrantedDialog)
    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showNotificationPermissionGrantedDialog.value = true
            } else {
                showNotificationPermissionNotGrantedDialog.value = true
            }
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        RemindersListAppBar(
            title = "Reminders",
            viewModel = viewModel,
            searchText = searchText
        )
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        openReminderDialog.value = ReminderForDialog(null)
                    } else {
                        scope.launch {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        ) {
            RemindersList(
                remindersList = viewModel.filterReminders(
                    remindersList.value,
                    searchText.value,
                ),
                selectedReminders = selectedReminders.value,
                mainAppViewModel = mainAppViewModel,
                onClick = { reminder, long ->
                    val permission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        if (long) {
                            viewModel.changeSelectionStateOf(reminder)
                        } else {
                            if (selectedReminders.value.isNotEmpty()) {
                                viewModel.changeSelectionStateOf(reminder)
                            } else {
                                openReminderDialog.value = ReminderForDialog(reminder)
                            }
                        }
                    } else {
                        scope.launch {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun NotificationPermissionNotGrantedDialog(open: MutableState<Boolean>) {
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
                text = "Вы не сможете добавлять напоминания, если вы не дадите разрешение на отправку уведомлений в настройках приложения"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
            }
        }
    }
}

@Composable
private fun NotificationPermissionGrantedDialog(open: MutableState<Boolean>) {
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
                text = "На устройствах некоторых производителей нужно дать приложению разрешения на запуск сервисов (для обновления напоминаний после перезагрузки устройства) и для работы в фоновом режиме (для показа напоминаний). Возможно на вашей модели устройства необходимо выдать эти разрешения. Просмотрите настройки приложения и выдать их если найдёте нужные разрешения. Скорее всего у вас нет этих настроек, но некоторые производители, например realme добавляют эти настройки. Если вы не найдёте этих настроек, то вам не нужно ничего включать, у вас всё будет работать и так."
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
            }
        }
    }
}

@Composable
private fun RemindersListAppBar(
    title: String,
    viewModel: RemindersListScreenViewModel,
    searchText: MutableState<String?>
) {
    val selectedReminders = viewModel.selectedReminders.observeAsState(listOf())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        if (searchText.value == null) {
            Text(
                text = title,
                fontSize = 24.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchItem(
                searchText = searchText,
                modifier = Modifier.weight(1f)
            )
            if (selectedReminders.value.isNotEmpty()) {
                val context = LocalContext.current
                IconButton(onClick = { viewModel.deleteSelectedReminders(context) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun RemindersList(
    remindersList: List<Reminder>,
    selectedReminders: List<Reminder>,
    mainAppViewModel: MainAppViewModel,
    onClick: (Reminder, Boolean) -> Unit,
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    LazyColumn {
        items(items = remindersList,
            key = { it.id }
        ) { reminder ->
            ReminderItem(
                title = reminder.title,
                time = timeFormatter.format(reminder.time),
                date = dateFormatter.format(reminder.time),
                selected = reminder in selectedReminders,
                actionIcon =
                when (reminder.action) {
                    ReminderAction.OpenApp -> {
                        val packageName =
                            if (isAppInstalled(reminder.packageName, LocalContext.current)) {
                                reminder.packageName
                            } else {
                                mainAppViewModel.insertReminder(reminder.copy(packageName = LocalContext.current.packageName))
                                LocalContext.current.packageName
                            }
                        LocalContext.current.packageManager.getApplicationIcon(
                            LocalContext.current.packageManager.getApplicationInfo(
                                packageName, PackageManager.GET_META_DATA
                            )
                        )
                    }
                    ReminderAction.OpenNote -> {
                        LocalContext.current.getDrawable(R.drawable.ic_note)!!
                    }
                    ReminderAction.OpenTask -> {
                        LocalContext.current.getDrawable(R.drawable.ic_task)!!
                    }
                },
                sound = reminder.sound,
                onClick = {
                    onClick(reminder, false)
                },
                onLongClick = {
                    onClick(reminder, true)
                }
            )
        }
    }
}

private fun isAppInstalled(packageName: String, context: Context): Boolean {
    val packageManager = context.packageManager
    return try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}