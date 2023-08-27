package com.example.jetpacknotes.tasks

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.myItems.CategoryDialog
import com.example.jetpacknotes.myItems.CategoryItem
import com.example.jetpacknotes.myItems.SearchItem
import com.example.jetpacknotes.myItems.TaskItem
import com.example.jetpacknotes.viewModels.MainAppViewModel
import com.example.jetpacknotes.viewModels.TasksListScreenViewModel
import com.example.jetpacknotes.viewModels.TasksListScreenViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    mainAppViewModel: MainAppViewModel
) {
    val viewModel: TasksListScreenViewModel = viewModel(
        factory = TasksListScreenViewModelFactory(mainAppViewModel)
    )
    val categoriesList = mainAppViewModel.categoryOfTasks.observeAsState(listOf())
    val tasksList = mainAppViewModel.allTasks.observeAsState(listOf())
    val selectedTasks = viewModel.selectedTasks.observeAsState(listOf())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val searchText = rememberSaveable {
        mutableStateOf<String?>(null)
    }
    val categoryState = rememberSaveable {
        mutableStateOf<Category?>(null)
    }
    categoryState.value?.let { category ->
        if (category !in categoriesList.value) {
            if (category.id in categoriesList.value.map { it.id }) {
                categoryState.value = categoriesList.value.find { it.id == category.id }
            } else {
                categoryState.value = null
            }
        }
    }
    val openCategoryDialog = rememberSaveable {
        mutableStateOf<Category?>(null)
    }
    val openTaskDialog = rememberSaveable {
        mutableStateOf<TaskForDialog?>(null)
    }
    openTaskDialog.value?.let {
        TaskEditDialog(
            taskState = openTaskDialog,
            mainAppViewModel = mainAppViewModel
        )
    }
    val scope = rememberCoroutineScope()
    CategoryDialog(
        openCategoryDialog = openCategoryDialog,
        insertCategory = {
            mainAppViewModel.insertCategory(it)
        },
        deleteCategory = {
            viewModel.deleteCategory(it)
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TasksListAppBar(
            title = categoryState.value?.name ?: "All Tasks",
            drawerState = drawerState,
            scope = scope,
            viewModel = viewModel,
            searchText = searchText
        )
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    openTaskDialog.value = TaskForDialog(task = null)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(((if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp) / 5 * 4).dp)
                    ) {
                        DrawerHeader(
                            onClickAdd = {
                                val category = viewModel.createCategory()
                                openCategoryDialog.value = category
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryItem(
                            name = "Все категории",
                            onClick = {
                                categoryState.value = null
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            onLongClick = {
                                categoryState.value = null
                                scope.launch {
                                    drawerState.close()
                                }
                            })
                        CategoriesList(
                            categoriesList = categoriesList.value,
                            onClick = { category, long ->
                                viewModel.clickOnCategory(category, long, categoryState, openCategoryDialog)
                                if (!long) {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }

                            })
                    }
                }
            ) {
                TasksList(
                    tasksList = viewModel.filterTasks(
                        tasksList.value,
                        searchText.value,
                        categoryState.value
                    ),
                    selectedTasks = selectedTasks.value,
                    onClick = { task, long ->
                        if (long) {
                            viewModel.changeSelectionStateOf(task)
                        } else {
                            if (selectedTasks.value.isNotEmpty()) {
                                viewModel.changeSelectionStateOf(task)
                            } else {
                                openTaskDialog.value = TaskForDialog(task = task)
                            }
                        }
                    },
                    onCheckedChange = { task, done ->
                        mainAppViewModel.insertTask(task.copy(done = done))
                    }
                )
            }

        }
    }
}

@Composable
private fun TasksList(
    tasksList: List<Task>,
    selectedTasks: List<Task>,
    onClick: (Task, Boolean) -> Unit,
    onCheckedChange: (Task, Boolean) -> Unit
) {
    val doneTasks = tasksList.filter { it.done }
    val notDoneTasks = tasksList.filter { !it.done }
    LazyColumn {
        items(items = notDoneTasks,
            key = { it.id }
        ) { task ->
            TaskItem(
                title = task.title,
                color = Colors.colors[task.colorIndex],
                done = task.done,
                selected = task in selectedTasks,
                onClick = {
                    onClick(task, false)
                },
                onLongClick = {
                    onClick(task, true)
                },
                onCheckChange = {
                    onCheckedChange(task, it)
                }
            )
        }
        if (doneTasks.isNotEmpty()) {
            item{
                Divider()
            }
            items(items = doneTasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    title = task.title,
                    color = Colors.colors[task.colorIndex],
                    done = task.done,
                    selected = task in selectedTasks,
                    onClick = {
                        onClick(task, false)
                    },
                    onLongClick = {
                        onClick(task, true)
                    },
                    onCheckChange = {
                        onCheckedChange(task, it)
                    }
                )
            }
        }

    }
}

@Composable
private fun CategoriesList(
    categoriesList: List<Category>,
    onClick: (Category, Boolean) -> Unit,
) {
    LazyColumn {
        items(items = categoriesList,
            key = { it.id }
        ) { category ->
            CategoryItem(
                name = category.name,
                onClick = {
                    onClick(category, false)
                },
                onLongClick = {
                    onClick(category, true)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksListAppBar(
    title: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: TasksListScreenViewModel,
    searchText: MutableState<String?>
) {
    val selectedNotes = viewModel.selectedTasks.observeAsState(listOf())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {
            scope.launch {
                drawerState.apply {
                    if (isClosed) open() else close()
                }
            }
        }) {
            Icon(Icons.Filled.Menu, contentDescription = null)
        }
        if (searchText.value == null) {
            Text(
                text = title,
                fontSize = 24.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchItem(
                modifier = Modifier.weight(1f),
                value = searchText.value,
                onValueChange = {
                    searchText.value = it
                }
            )
            if (selectedNotes.value.isNotEmpty()) {
                val context = LocalContext.current
                IconButton(onClick = { viewModel.deleteSelectedTasks(context) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }

    }
}

@Composable
private fun DrawerHeader(onClickAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "Категории",
            color = Color.Black,
            fontSize = 22.sp
        )
        IconButton(
            modifier = Modifier.size(60.dp),
            onClick = onClickAdd
        ) {
            Icon(
                Icons.Filled.Add,
                modifier = Modifier.size(40.dp),
                contentDescription = null
            )
        }
    }
}