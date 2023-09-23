package com.example.jetpacknotes.tasks

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.jetpacknotes.FilterData
import com.example.jetpacknotes.FilterType
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.myItems.CategoryDialog
import com.example.jetpacknotes.myItems.CategoryItem
import com.example.jetpacknotes.myItems.SearchItem
import com.example.jetpacknotes.myItems.TaskItem
import com.example.jetpacknotes.viewModels.MainAppViewModel
import com.example.jetpacknotes.viewModels.TasksListScreenViewModel
import com.example.jetpacknotes.viewModels.TasksListScreenViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.jetpacknotes.DataStoreManager
import com.example.jetpacknotes.R
import com.example.jetpacknotes.myItems.DragTaskItem
import com.example.jetpacknotes.myItems.FilterDialog
import com.example.jetpacknotes.myItems.GhostTaskData
import com.example.jetpacknotes.myItems.GhostTaskItem

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TasksListScreen(
    mainAppViewModel: MainAppViewModel
) {
    val viewModel: TasksListScreenViewModel = viewModel(
        factory = TasksListScreenViewModelFactory(mainAppViewModel)
    )
    val allTasks = mainAppViewModel.allTasks.observeAsState(emptyList())
    mainAppViewModel.allTasks.observe(LocalLifecycleOwner.current) {
        viewModel.filterTasks()
    }
    val allCategories = mainAppViewModel.categoryOfTasks.observeAsState(emptyList())
    val selectedTasks = viewModel.selectedTasks.observeAsState(emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStoreManager = remember { DataStoreManager(context) }
    LaunchedEffect(Unit) {
        dataStoreManager.getTasksFilterData().collect { filDataNullable ->
            val filData = filDataNullable ?: FilterData(
                colorIndex = null,
                type = FilterType.Create
            )
            val checkedData = viewModel.checkHandTasks(
                allTasks = allTasks.value,
                data = filData.data
            )
            val checkedFilterData = filData.copy(data = checkedData)
            if (filDataNullable?.data != checkedData) {
                scope.launch {
                    dataStoreManager.saveTasksFilterData(checkedFilterData)
                }
            }
            viewModel.setFilterData(checkedFilterData)
        }
    }

    val category = viewModel.category.observeAsState()
    val searchText = viewModel.searchText.observeAsState()
    val filterData = viewModel.filterData.observeAsState()

    viewModel.checkCategory(category.value, allCategories.value)
    var openCategoryDialog by rememberSaveable {
        mutableStateOf<Category?>(null)
    }
    openCategoryDialog?.let {
        CategoryDialog(
            category = it,
            insertCategory = mainAppViewModel::insertCategory,
            deleteCategory = viewModel::deleteCategory,
            onDismissReqest = {
                openCategoryDialog = null
            }
        )
    }

    var openFilterDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (openFilterDialog) {

        filterData.value?.let { filData ->
            FilterDialog(
                currentData = filData,
                types = listOf(
                    FilterType.Create, FilterType.Edit, FilterType.Color, FilterType.Hand
                ),
                onDismissRequest = { openFilterDialog = false },
                setFilter = {
                    val newFilterData = it.copy(data = filterData.value?.data)
                    scope.launch {
                        dataStoreManager.saveTasksFilterData(newFilterData)
                    }
                    viewModel.setFilterData(newFilterData)
                })
        }
    }

    var openTaskDialog by rememberSaveable {
        mutableStateOf<TaskForDialog?>(null)
    }
    openTaskDialog?.let {
        TaskEditDialog(
            task = it.task,
            mainAppViewModel = mainAppViewModel,
            onDismissRequest = {
                openTaskDialog = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        TasksListAppBar(
            title = category.value?.name ?: "All Tasks",
            showDeleteButton = selectedTasks.value.isNotEmpty(),
            searchText = searchText.value,
            onSearch = viewModel::search,
            onMenuButtonClicked = {
                scope.launch {
                    drawerState.apply {
                        if (isClosed) open() else close()
                    }
                }
            },
            onFilterButtonClicked = { openFilterDialog = true },
            onDeleteButtonClicked = { viewModel.deleteSelectedTasks(context) }
        )
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    openTaskDialog = TaskForDialog(task = null)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(getDrawerWidth())
                    ) {
                        DrawerHeader(
                            onClickAdd = {
                                openCategoryDialog = viewModel.createCategory()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val setNullCategory: () -> Unit = {
                            viewModel.setCategory(null)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                        CategoryItem(
                            name = "Все задачи",
                            onClick = setNullCategory,
                            onLongClick = setNullCategory
                        )
                        CategoriesList(
                            categoriesList = allCategories.value,
                            onClick = { category, long ->
                                if (long) {
                                    openCategoryDialog = category.copy()
                                } else {
                                    viewModel.setCategory(category)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                            }
                        )
                    }
                }
            ) {
                if (filterData.value?.type != FilterType.Hand) {
                    TasksList(
                        viewModel = viewModel,
                        onClick = { task, long ->
                            if (long || selectedTasks.value.isNotEmpty()) {
                                viewModel.changeSelectionStateOf(task)
                            } else {
                                openTaskDialog = TaskForDialog(task = task)
                            }
                        },
                        onCheckedChange = { task, done ->
                            mainAppViewModel.insertTask(task.copy(done = done))
                        }
                    )
                } else {
                    filterData.value?.let {
                        DragTasksList(
                            viewModel = viewModel,
                            moveTask = { task, index ->
                                val newData = viewModel.moveHandTask(
                                    data = filterData.value?.data, index = index, id = task.id
                                )
                                val newFilterData = filterData.value?.copy(data = newData)
                                scope.launch {
                                    dataStoreManager.saveTasksFilterData(newFilterData!!)
                                }
                                viewModel.setFilterData(newFilterData)
                            },
                            onClick = { task, long ->
                                if (long || selectedTasks.value.isNotEmpty()) {
                                    viewModel.changeSelectionStateOf(task)
                                } else {
                                    openTaskDialog = TaskForDialog(task = task)
                                }
                            },
                            onCheckedChange = { task, done ->
                                mainAppViewModel.insertTask(task.copy(done = done)) {
                                    val newData = viewModel.checkHandTasks(
                                        allTasks = allTasks.value,
                                        data = filterData.value?.data
                                    )
                                    val newFilterData = filterData.value?.copy(data = newData)
                                    scope.launch {
                                        dataStoreManager.saveTasksFilterData(newFilterData!!)
                                    }
                                    viewModel.setFilterData(newFilterData)
                                }

                            }
                        )
                    }

                }
            }

        }
    }
}

@Composable
private fun TasksList(
    viewModel: TasksListScreenViewModel,
    onClick: (Task, Boolean) -> Unit,
    onCheckedChange: (Task, Boolean) -> Unit
) {
    val tasksList by viewModel.tasksList.observeAsState(emptyList())
    val selectedTasks by viewModel.selectedTasks.observeAsState(emptyList())
    val doneTasks = tasksList.filter { it.done }
    val notDoneTasks = tasksList.filter { !it.done }
    LazyColumn {
        items(items = notDoneTasks,
            key = { it.id }
        ) { task ->
            TaskItem(
                task = task,
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
            item {
                Divider()
            }
            items(items = doneTasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
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
private fun DragTasksList(
    viewModel: TasksListScreenViewModel,
    moveTask: (Task, Int) -> Unit,
    onClick: (Task, Boolean) -> Unit,
    onCheckedChange: (Task, Boolean) -> Unit
) {
    val tasksList by viewModel.tasksList.observeAsState(emptyList())
    val selectedTasks by viewModel.selectedTasks.observeAsState(emptyList())
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        var ghostTask by remember {
            mutableStateOf<GhostTaskData?>(null)
        }
        val state = rememberLazyListState()
        val d = LocalDensity.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
        ) {
            items(items = tasksList.filter { !it.done }, key = { it.id }) { task ->
                var offsetY by remember { mutableStateOf(0f) }
                var move by remember { mutableStateOf(false) }
                var dragStartPosition by remember { mutableStateOf(0f) }
                val scope = rememberCoroutineScope()
                var firstItemIndex = remember { state.firstVisibleItemIndex }
                var firstItemOffset = remember { state.firstVisibleItemScrollOffset }
                DragTaskItem(
                    task = task,
                    offsetY = offsetY,
                    move = move,
                    selected = selectedTasks.contains(task),
                    onDragStart = {
                        dragStartPosition = it / d.density
                        move = true
                        firstItemIndex = state.firstVisibleItemIndex
                        firstItemOffset = state.firstVisibleItemScrollOffset
                    },
                    onVerticalDrag = { positionY ->
                        if (state.firstVisibleItemIndex != firstItemIndex || state.firstVisibleItemScrollOffset != firstItemOffset) {
                            scope.launch {
                                state.scrollToItem(firstItemIndex, firstItemOffset)
                            }
                        } else {
                            val positionOffset = (offsetY / 80).toInt()
                            val newIndex = positionOffset + tasksList.filter { !it.done }.indexOf(task)
                            if (positionOffset != 0 && newIndex in tasksList.filter { !it.done }.indices) {
                                offsetY -= positionOffset * 80
                                moveTask(task, newIndex)
                            } else {
                                offsetY += positionY / d.density - dragStartPosition
                            }
                            val yPosition =
                                (tasksList.filter { !it.done }.indexOf(task) - state.firstVisibleItemIndex) * 80 + offsetY - state.firstVisibleItemScrollOffset / d.density
                            ghostTask = GhostTaskData(
                                task = task, offsetY = yPosition
                            )
                        }

                    },
                    onDragEnd = {
                        move = false
                        ghostTask = null
                        scope.launch {
                            animate(
                                initialValue = offsetY, targetValue = 0f, animationSpec = spring()
                            ) { value, _ ->
                                offsetY = value
                            }
                        }
                    },
                    onClick = { onClick(task, false) },
                    onLongClick = { onClick(task, true) },
                    onCheckChange = {
                        onCheckedChange(task, it)
                    }
                )
            }

            if (tasksList.any { it.done }) {
                item {
                    Divider()
                }
                items(items = tasksList.filter { it.done },
                    key = { it.id }
                ) { task ->
                    TaskItem(
                        task = task,
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
        ghostTask?.let { taskData ->
            GhostTaskItem(
                task = taskData.task,
                offsetY = taskData.offsetY,
                selected = selectedTasks.contains(taskData.task)
            )
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

@Composable
private fun TasksListAppBar(
    title: String,
    showDeleteButton: Boolean,
    searchText: String?,
    onSearch: (String?) -> Unit,
    onMenuButtonClicked: () -> Unit,
    onFilterButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuButtonClicked) {
            Icon(Icons.Filled.Menu, contentDescription = null)
        }
        if (searchText == null) {
            Text(text = title, fontSize = 24.sp)
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchItem(
                modifier = Modifier.weight(1f),
                value = searchText,
                onValueChange = onSearch
            )
            IconButton(onClick = onFilterButtonClicked) {
                Icon(painterResource(id = R.drawable.ic_filter), contentDescription = null)
            }
            if (showDeleteButton) {
                IconButton(onClick = onDeleteButtonClicked) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun getDrawerWidth(): Dp {
    val screenSize =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
            LocalConfiguration.current.screenWidthDp
        else
            LocalConfiguration.current.screenHeightDp
    return (screenSize / 5 * 4).dp
}

@Composable
private fun DrawerHeader(onClickAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Text(
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