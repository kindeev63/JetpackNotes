package com.example.jetpacknotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun BottomNavigationNavGraph(
    navHostController: NavHostController,
    notesListScreenContent: @Composable () -> Unit,
    remindersListScreenContent: @Composable () -> Unit,
    tasksListScreenContent: @Composable () -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.NotesList.route
    ) {
        composable(Screen.NotesList.route) {
            notesListScreenContent()
        }
        composable(Screen.RemindersList.route) {
            remindersListScreenContent()
        }
        composable(Screen.TasksList.route) {
            tasksListScreenContent()
        }

    }
}