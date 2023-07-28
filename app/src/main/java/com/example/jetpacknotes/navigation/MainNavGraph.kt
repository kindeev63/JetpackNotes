package com.example.jetpacknotes.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun MainNavGraph(
    navHostController: NavHostController,
    bottomNavigationScreen: @Composable () -> Unit,
    noteEditScreenContent: @Composable (Int?) -> Unit,
    reminderEditScreenContent: @Composable (Int?) -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.BottomNavigation.route
    ) {
        composable(Screen.BottomNavigation.route) {
            bottomNavigationScreen()
        }
        composable(
            route = Screen.NoteEdit.route,
        ) {
            val noteId = it.arguments?.get("bundle").toString().toIntOrNull()
            noteEditScreenContent(noteId)
        }
        composable(
            route = Screen.ReminderEdit.route,
        ) {
            val reminderId = it.arguments?.get("reminderId").toString().toIntOrNull()
            reminderEditScreenContent(reminderId)
        }
    }
}