package com.example.jetpacknotes.reminders

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Reminder
import com.example.jetpacknotes.db.ReminderAction
import com.example.jetpacknotes.myItems.ReminderItem
import com.example.jetpacknotes.myItems.SearchItem
import com.example.jetpacknotes.viewModels.MainAppViewModel
import com.example.jetpacknotes.viewModels.RemindersListScreenViewModel
import com.example.jetpacknotes.viewModels.RemindersListScreenViewModelFactory
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

    val remindersList = mainAppViewModel.allReminders.observeAsState(listOf())
    val selectedReminders = viewModel.selectedReminders.observeAsState(listOf())
    val searchText = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    val openReminderDialog = rememberSaveable {
        mutableStateOf<ReminderForDialog?>(null)
    }
    openReminderDialog.value?.let {
        ReminderEditDialog(
            reminderState = openReminderDialog,
            mainAppViewModel = mainAppViewModel
        )
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
                    openReminderDialog.value = ReminderForDialog(null)
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
                onClick = { reminder, long ->
                    if (long) {
                        viewModel.changeSelectionStateOf(reminder)
                    } else {
                        if (selectedReminders.value.isNotEmpty()) {
                            viewModel.changeSelectionStateOf(reminder)
                        } else {
                            openReminderDialog.value = ReminderForDialog(reminder)
                        }
                    }
                },
            )

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
                IconButton(onClick = { viewModel.deleteSelectedReminders() }) {
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
                if (reminder.action == ReminderAction.OpenNote) {
                    LocalContext.current.getDrawable(R.drawable.ic_note)!!
                } else {
                    LocalContext.current.packageManager.getApplicationIcon(
                        LocalContext.current.packageManager.getApplicationInfo(
                            reminder.packageName, PackageManager.GET_META_DATA
                        )
                    )
                },
                sound = reminder . sound,
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