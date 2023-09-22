package com.example.jetpacknotes.viewModels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpacknotes.FilterData
import com.example.jetpacknotes.FilterType
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.CategoryType
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.receivers.AlarmReceiver

class TasksListScreenViewModel(private val mainAppViewModel: MainAppViewModel) : ViewModel() {
    private val _selectedTasks = MutableLiveData<List<Task>>(emptyList())
    val selectedTasks: LiveData<List<Task>> = _selectedTasks
    private val _searchText = MutableLiveData<String?>(null)
    val searchText: LiveData<String?> = _searchText
    private val _category = MutableLiveData<Category?>(null)
    val category: LiveData<Category?> = _category

    private val _filterData = MutableLiveData<FilterData?>(null)
    val filterData: LiveData<FilterData?> = _filterData

    private val _tasksList = MutableLiveData<List<Task>>(emptyList())
    val tasksList: LiveData<List<Task>> = _tasksList

    fun checkCategory(category: Category?, allCategories: List<Category>) {
        if (category == null) return
        if (category !in allCategories) {
            setCategory(allCategories.find { it.id == category.id })
        }
    }

    fun setFilterData(filterData: FilterData?) {
        _filterData.value = filterData
        filterTasks()
    }

    fun search(searchText: String?) {
        _searchText.value = searchText
        filterTasks()
    }

    fun setCategory(category: Category?) {
        _category.value = category
        filterTasks()
    }

    fun changeSelectionStateOf(task: Task) {
        _selectedTasks.value?.let { selTasks ->
            if (task in selTasks) {
                _selectedTasks.value = ArrayList(selTasks).apply {
                    remove(task)
                }
            } else {
                _selectedTasks.value = ArrayList(selTasks).apply {
                    add(task)
                }
            }
        }
    }

    fun deleteCategory(category: Category) {
        mainAppViewModel.allTasks.value?.forEach { task ->
            if (task.categories.contains(category.id.toString())) {
                mainAppViewModel.insertTask(
                    task.copy(
                        categories = ArrayList(task.categories.split(" | ")).apply {
                            remove(category.id.toString())
                        }.joinToString(" | ")
                    )
                )
            }
        }
        mainAppViewModel.deleteCategory(category)
    }

    fun createCategory(): Category {
        val idsList = (mainAppViewModel.categoryOfTasks.value ?: emptyList()).map { it.id }
        var categoryId = 0
        while (true) {
            if (categoryId !in idsList) break
            categoryId++
        }
        return Category(categoryId, "", CategoryType.Task)
    }

    fun filterTasks() {
        val filteredTasks = (mainAppViewModel.allTasks.value ?: emptyList())
            // By category
            .filter { task ->
                category.value == null || task.categories.split(" | ")
                    .contains(category.value?.id.toString())
            }
            // By search text
            .filter { task ->
                task.title.lowercase().contains(searchText.value?.lowercase() ?: "")
            }
            // By color
            .filter { task ->
                filterData.value?.colorIndex == null || task.colorIndex == filterData.value?.colorIndex
            }.reversed()

        // Ordering by filter type
        _tasksList.value = when (filterData.value?.type) {

            FilterType.Create -> {
                filteredTasks
            }

            FilterType.Edit -> {
                filteredTasks.sortedBy { it.lastEditTime }.reversed()
            }

            FilterType.Color -> {
                filteredTasks.sortedBy { it.colorIndex }
            }

            FilterType.Hand -> {
                arrayListOf<Task>().apply {
                    filterData.value?.data?.split(" | ")?.map { it.toInt() }?.forEach { id ->
                        filteredTasks.find { note -> note.id == id }?.let { note ->
                            add(note)
                        }
                    }
                }.toList()
            }

            else -> {
                filteredTasks
            }
        }
    }


    fun deleteSelectedTasks(context: Context) {
        selectedTasks.value?.let { tasks ->
            val remindersForDelete =
                mainAppViewModel.allReminders.value?.filter { reminder ->
                    tasks.any { task ->
                        reminder.taskId == task.id
                    }
                }
            remindersForDelete?.let { remindersList ->
                remindersList.map { it.id }.forEach { reminderId ->
                    cancelAlarm(reminderId, context)
                }
                mainAppViewModel.deleteReminders(remindersList)
            }
            mainAppViewModel.deleteTasks(tasks)
        }
        _selectedTasks.value = emptyList()
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

    fun checkHandTasks(allTasks: List<Task>, data: String?): String? {
        if (allTasks.isEmpty()) {
            return null
        }
        if (data.isNullOrEmpty()) {
            return allTasks.map { it.id }.joinToString(separator = " | ")
        }
        val tasksIds = allTasks.map { it.id }
        val dataIds = ArrayList(data.split(" | ").map { it.toInt() })
        tasksIds.reversed().forEach { taskId ->
            if (!dataIds.contains(taskId)) {
                dataIds.add(0, taskId)
            }
        }
        dataIds.forEach { handTaskId ->
            if (!tasksIds.contains(handTaskId)) {
                dataIds.remove(handTaskId)
            }
        }
        val doneTasksIds = allTasks.filter { it.done }.map { it.id }
        val resultTasksIds = arrayListOf<Int>().apply {
            dataIds.forEach { id ->
                if (!doneTasksIds.contains(id)) {
                    add(id)
                }
            }
            dataIds.forEach { id ->
                if (doneTasksIds.contains(id)) {
                    add(id)
                }
            }
        }
        return resultTasksIds.joinToString(separator = " | ")
    }

    fun moveHandTask(data: String?, id: Int, index: Int): String {
        val ids = ArrayList(data?.split(" | ")?.map { it.toInt() } ?: emptyList())
        ids.remove(id)
        ids.add(index, id)
        return ids.joinToString(" | ")
    }
}