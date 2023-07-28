package com.example.jetpacknotes

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.example.jetpacknotes.navigation.MainNavGraph
import com.example.jetpacknotes.navigation.rememberNavigationState
import com.example.jetpacknotes.notes.NoteEditScreen
import com.example.jetpacknotes.reminders.ReminderEditScreen
import com.example.jetpacknotes.viewModels.MainAppViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    mainAppViewModel: MainAppViewModel
) {
    val navigationState = rememberNavigationState()
    MainNavGraph(
        navHostController = navigationState.navHostController,
        bottomNavigationScreen = {
            BottomNavigationScreen(
                mainAppViewModel = mainAppViewModel,
                navigateToNote = { noteId ->
                    navigationState.navigateToNoteEdit(noteId)
                },
                navigateToReminder = { reminderId ->
                    navigationState.navigateToReminderEdit(reminderId)
                }
            )
        },
        noteEditScreenContent = { noteId ->
            NoteEditScreen(
                mainAppViewModel = mainAppViewModel,
                noteId = noteId,
                onBackPressed = {
                    navigationState.navHostController.popBackStack()
                }
            )
        },
        reminderEditScreenContent = { reminderId ->
            ReminderEditScreen(
                mainAppViewModel = mainAppViewModel,
                reminderId = reminderId,
                onBackPressed = {
                    navigationState.navHostController.popBackStack()
                }
            )
        },
    )
}