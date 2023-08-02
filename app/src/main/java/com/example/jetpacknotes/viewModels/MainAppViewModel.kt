package com.example.jetpacknotes.viewModels

import android.app.Application
import android.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.jetpacknotes.db.AppDao
import com.example.jetpacknotes.db.AppDataBase
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.CategoryType
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.db.Reminder
import kotlinx.coroutines.launch

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao: AppDao
    val allNotes: LiveData<List<Note>>
    val allReminders: LiveData<List<Reminder>>
    val categoryOfNotes: LiveData<List<Category>>
    init {
        appDao = AppDataBase.getDataBase(application).getDao()
        allNotes = appDao.getAllNotes()
        allReminders = appDao.getAllReminders()
        categoryOfNotes = appDao.getCategoriesByType(CategoryType.Note)
    }

    fun insertNote(note: Note, function: (Note) -> Unit = {}) = viewModelScope.launch {
        appDao.insertNote(note)
        function(note)
    }

    fun deleteNotes(notes: List<Note>) = viewModelScope.launch {
        appDao.deleteNotes(notes)
    }

    suspend fun getNoteById(noteId: Int, function: (Note?) -> Unit) {
        viewModelScope.launch {
            val note = appDao.getNoteById(noteId)
            function(note)
        }
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        appDao.insertCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        appDao.deleteCategory(category)
    }

    fun insertReminder(reminder: Reminder, function: (Reminder) -> Unit = {}) = viewModelScope.launch {
        appDao.insertReminder(reminder)
        function(reminder)
    }

    fun deleteReminders(reminders: List<Reminder>) = viewModelScope.launch {
        appDao.deleteReminders(reminders)
    }

    suspend fun getReminderById(reminderId: Int, function: (Reminder?) -> Unit) {
        viewModelScope.launch {
            val reminder = appDao.getReminderById(reminderId)
            function(reminder)
        }
    }
}