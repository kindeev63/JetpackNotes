package com.example.jetpacknotes.viewModels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpacknotes.FilterData
import com.example.jetpacknotes.FilterType
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.CategoryType
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.receivers.AlarmReceiver

class NotesListScreenViewModel(private val mainAppViewModel: MainAppViewModel) :
    ViewModel() {
    private val _selectedNotes = MutableLiveData<List<Note>>(emptyList())
    val selectedNotes: LiveData<List<Note>> = _selectedNotes
    private val _searchText = MutableLiveData<String?>(null)
    val searchText: LiveData<String?> = _searchText
    private val _category = MutableLiveData<Category?>(null)
    val category: LiveData<Category?> = _category

    private val _filterData = MutableLiveData<FilterData?>(null)
    val filterData: LiveData<FilterData?> = _filterData

    private val _notesList = MutableLiveData<List<Note>>(emptyList())
    val notesList: LiveData<List<Note>> = _notesList

    fun checkCategory(category: Category?, allCategories: List<Category>) {
        if (category == null) return
        if (category !in allCategories) {
            setCategory(allCategories.find { it.id == category.id })
        }
    }

    fun setFilterData(filterData: FilterData?) {
        _filterData.value = filterData
        filterNotes()
    }

    fun search(searchText: String?) {
        _searchText.value = searchText
        filterNotes()
    }

    fun setCategory(category: Category?) {
        _category.value = category
        filterNotes()
    }

    fun changeSelectionStateOf(note: Note) {
        _selectedNotes.value?.let { selNotes ->
            if (note in selNotes) {
                _selectedNotes.value = ArrayList(selNotes).apply {
                    remove(note)
                }
            } else {
                _selectedNotes.value = ArrayList(selNotes).apply {
                    add(note)
                }
            }
        }
    }

    fun deleteSelectedNotes(context: Context) {
        selectedNotes.value?.let { notes ->
            val remindersForDelete =
                mainAppViewModel.allReminders.value?.filter { reminder ->
                    notes.any { note ->
                        reminder.noteId == note.id
                    }
                }
            remindersForDelete?.let { remindersList ->
                remindersList.map { it.id }.forEach { reminderId ->
                    cancelAlarm(reminderId, context)
                }
                mainAppViewModel.deleteReminders(remindersList)
            }
            mainAppViewModel.deleteNotes(notes)
        }
        _selectedNotes.value = emptyList()
    }

    private fun cancelAlarm(reminderId: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    fun createCategory(): Category {
        val idsList = (mainAppViewModel.categoryOfNotes.value ?: emptyList()).map { it.id }
        var categoryId = 0
        while (true) {
            if (categoryId !in idsList) break
            categoryId++
        }
        return Category(categoryId, "", CategoryType.Note)
    }

    fun filterNotes() {
        val filteredNotes = (mainAppViewModel.allNotes.value ?: emptyList())
            // By category
            .filter { note ->
                category.value == null || note.categories.split(" | ")
                    .contains(category.value?.id.toString())
            }
            // By search text
            .filter { note ->
                note.title.lowercase().contains(searchText.value?.lowercase() ?: "")
            }
            // By color
            .filter { note ->
                filterData.value?.colorIndex == null || note.colorIndex == filterData.value?.colorIndex
            }.reversed()

        // Ordering by filter type
        _notesList.value = when (filterData.value?.type) {

            FilterType.Create -> {
                filteredNotes
            }

            FilterType.Edit -> {
                filteredNotes.sortedBy { it.lastEditTime }.reversed()
            }

            FilterType.Color -> {
                filteredNotes.sortedBy { it.colorIndex }
            }

            FilterType.Hand -> {
                arrayListOf<Note>().apply {
                    filterData.value?.data?.split(" | ")?.map { it.toInt() }?.forEach { id ->
                        filteredNotes.find { note -> note.id == id }?.let { note ->
                            add(note)
                        }
                    }
                }.toList()
            }

            else -> {
                filteredNotes
            }
        }
    }

    fun deleteCategory(category: Category) {
        mainAppViewModel.allNotes.value?.forEach { note ->
            if (note.categories.contains(category.id.toString())) {
                mainAppViewModel.insertNote(
                    note.copy(
                        categories = ArrayList(note.categories.split(" | ")).apply {
                            remove(category.id.toString())
                        }.joinToString(" | ")
                    )
                )
            }
        }
        mainAppViewModel.deleteCategory(category)
    }

    fun checkHandNotes(allNotes: List<Note>, data: String?): String? {
        if (allNotes.isEmpty()) {
            return null
        }
        if (data.isNullOrEmpty()) {
            return allNotes.map { it.id }.joinToString(separator = " | ")
        }
        val notesIds = allNotes.map { it.id }
        val dataIds = ArrayList(data.split(" | ").map { it.toInt() })
        notesIds.reversed().forEach { noteId ->
            if (!dataIds.contains(noteId)) {
                dataIds.add(0, noteId)
            }
        }
        dataIds.forEach { handNoteId ->
            if (!notesIds.contains(handNoteId)) {
                dataIds.remove(handNoteId)
            }
        }
        return dataIds.joinToString(separator = " | ")
    }

    fun moveHandNote(data: String?, id: Int, index: Int): String {
        val ids = ArrayList(data?.split(" | ")?.map { it.toInt() } ?: emptyList())
        ids.remove(id)
        ids.add(index, id)
        return ids.joinToString(" | ")
    }
}