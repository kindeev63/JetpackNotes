package com.example.jetpacknotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

fun NavGraphBuilder.notesScreenNavGraph(
    notesListScreenContent: @Composable () -> Unit,
    noteEditScreenContent: @Composable (Int) -> Unit,
) {
    navigation(
        startDestination = Screen.NotesList.route,
        route = Screen.Notes.route
    ) {
        composable(Screen.NotesList.route) {
            notesListScreenContent()
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