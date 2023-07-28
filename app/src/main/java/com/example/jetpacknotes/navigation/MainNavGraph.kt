package com.example.jetpacknotes.navigation

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
    noteEditScreenContent: @Composable (Int) -> Unit,
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
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                }
            )
        ) {
            val noteId = it.arguments?.getInt("noteId") ?: 0
            noteEditScreenContent(noteId)
        }
    }
}