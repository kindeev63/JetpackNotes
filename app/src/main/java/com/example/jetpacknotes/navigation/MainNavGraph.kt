package com.example.jetpacknotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun MainNavGraph(
    navHostController: NavHostController,
    bottomNavigationScreen: @Composable () -> Unit,
    noteEditScreenContent: @Composable (Int?) -> Unit,
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
    }
}