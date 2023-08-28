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
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.receivers.AlarmReceiver

class TasksListScreenViewModel(private val mainAppViewModel: MainAppViewModel) : ViewModel() {
    private val _selectedTasks = MutableLiveData<List<Task>>(emptyList())
    val selectedTasks: LiveData<List<Task>> = _selectedTasks
    private val _filterData = MutableLiveData(FilterData(null, FilterType.Edit))
    val filterData: LiveData<FilterData> = _filterData
    private val _searchText = MutableLiveData<String?>(null)
    val searchText: LiveData<String?> = _searchText
    private val _category = MutableLiveData<Category?>(null)
    val category: LiveData<Category?> = _category

    fun checkCategory(category: Category?, allCategories: List<Category>) {
        if (category == null) return
        if (category !in allCategories) {
            setCategory(allCategories.find { it.id == category.id })
        }
    }

    fun search(searchText: String?) {
        _searchText.value = searchText
    }

    fun setCategory(category: Category?) {
        _category.value = category
    }

    fun setFilterData(filterData: FilterData) {
        _filterData.value = filterData
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

    fun clickOnCategory(
        category: Category,
        long: Boolean,
        state: MutableState<Category?>,
        openDialog: MutableState<Category?>
    ) {
        if (long) {
            openDialog.value = category.copy()
        } else {
            state.value = category.copy()
        }
    }

    fun filterTasks(
        tasks: List<Task>,
        category: Category?,
        searchText: String?,
        filterData: FilterData?
    ): List<Task> {
        val filteredTasks = tasks
            // By category
            .filter { task ->
                category == null || task.categories.split(" | ")
                    .contains(category.id.toString())
            }
            // By search text
            .filter { task ->
                task.title.lowercase().contains(searchText?.lowercase() ?: "")
            }
            // By color
            .filter { task ->
                filterData?.colorIndex == null || task.colorIndex == filterData.colorIndex
            }.reversed()

        // Ordering by filter type
        return when (filterData?.type) {

            FilterType.Color -> {
                filteredTasks.sortedBy { it.colorIndex }
            }

            else -> {
                filteredTasks
            }
        }
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
}