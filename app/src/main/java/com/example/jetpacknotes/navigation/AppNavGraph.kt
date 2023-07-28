package com.example.jetpacknotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    notesListScreenContent: @Composable () -> Unit,
    noteEditScreenContent: @Composable (Int) -> Unit,
    remindersScreenContent: @Composable () -> Unit,
    tasksScreenContent: @Composable () -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Notes.route
    ) {
        notesScreenNavGraph(
            notesListScreenContent = notesListScreenContent,
            noteEditScreenContent = noteEditScreenContent
        )
        composable(Screen.Reminders.route) {
            remindersScreenContent()
        }
        composable(Screen.Tasks.route) {
            tasksScreenContent()
        }
    }
}