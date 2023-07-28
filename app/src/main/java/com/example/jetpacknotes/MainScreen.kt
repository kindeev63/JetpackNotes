package com.example.jetpacknotes

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.jetpacknotes.navigation.AppNavGraph
import com.example.jetpacknotes.navigation.Screen
import com.example.jetpacknotes.navigation.rememberNavigationState
import com.example.jetpacknotes.notes.NoteEditScreen
import com.example.jetpacknotes.notes.NotesListScreen
import com.example.jetpacknotes.viewModels.MainAppViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    mainAppViewModel: MainAppViewModel
) {
    val navigationState = rememberNavigationState()
    val items = listOf(
        BottomNavigationBarItem(0, Screen.Notes, R.drawable.ic_note),
        BottomNavigationBarItem(1, Screen.Reminders, R.drawable.ic_reminder),
        BottomNavigationBarItem(2, Screen.Tasks, R.drawable.ic_task),
    )
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val navBackStackEntry by navigationState.navHostController.currentBackStackEntryAsState()
        val selectedItemId = items.indexOf(items.find{ item ->
            navBackStackEntry?.destination?.hierarchy?.any {
                it.route == item.screen.route
            } ?: false
        })
        Box(modifier = Modifier.weight(1f)) {
            AppNavGraph(
                navHostController = navigationState.navHostController,
                notesListScreenContent = {
                    NotesListScreen(
                        mainAppViewModel = mainAppViewModel,
                        navigateWhenNoteClicked = {note ->
                            navigationState.navigateToNoteEdit(note)
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
                remindersScreenContent = {},
                tasksScreenContent = {}
            )
        }

        BottomNavigationBar(
            items = items,
            selectedItemColor = Color(-15878606),
            unselectedItemColor = Color.Gray,
            selectedItemId = selectedItemId
        ) { item ->
            if (item.id != selectedItemId) {
                navigationState.navigateTo(item.screen.route)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItemColor: Color,
    unselectedItemColor: Color,
    items: List<BottomNavigationBarItem>,
    selectedItemId: Int,
    onItemSelected: (BottomNavigationBarItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clickable {
                        onItemSelected(item)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = item.drawableId),
                    contentDescription = null,
                    tint = if (item.id == selectedItemId) selectedItemColor else unselectedItemColor
                )
            }
        }
    }
}