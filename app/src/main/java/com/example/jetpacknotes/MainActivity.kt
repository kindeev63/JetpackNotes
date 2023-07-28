package com.example.jetpacknotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.notes.NotesListScreen
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notesList = listOf(
            Note(0, "Заметка 1", "Текст заметки 1", Calendar.getInstance().timeInMillis, "", 0),
            Note(1, "Заметка 2", "Текст заметки 2", Calendar.getInstance().timeInMillis, "", 0)
        )
        val mainAppViewModel = (application as MainApp).mainAppViewModel
        setContent {
            MainScreen(mainAppViewModel = mainAppViewModel)
        }
    }
}