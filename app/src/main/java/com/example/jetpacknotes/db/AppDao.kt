package com.example.jetpacknotes.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppDao {

    @Query("SELECT * FROM table_notes")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM table_reminders")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM table_tasks")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM table_categories WHERE type = :type")
    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>>

    @Insert(Note::class, OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete(Note::class)
    suspend fun deleteNotes(notes: List<Note>)

    @Query("SELECT * FROM table_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Insert(Category::class, OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete(Category::class)
    suspend fun deleteCategory(category: Category)

    @Insert(Reminder::class, OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete(Reminder::class)
    suspend fun deleteReminders(reminders: List<Reminder>)

    @Query("SELECT * FROM table_reminders")
    fun getAllRemindersNotLiveData(): List<Reminder>

    @Insert(Task::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete(Task::class)
    suspend fun deleteTasks(tasks: List<Task>)
}