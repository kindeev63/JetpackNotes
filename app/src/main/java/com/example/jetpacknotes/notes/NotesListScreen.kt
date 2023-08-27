package com.example.jetpacknotes.notes

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.viewModels.MainAppViewModel
import com.example.jetpacknotes.viewModels.NotesListScreenViewModel
import com.example.jetpacknotes.viewModels.NotesListScreenViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.FilterData
import com.example.jetpacknotes.FilterType
import com.example.jetpacknotes.R
import com.example.jetpacknotes.myItems.CategoryDialog
import com.example.jetpacknotes.myItems.CategoryItem
import com.example.jetpacknotes.myItems.FilterDialog
import com.example.jetpacknotes.myItems.NoteItem
import com.example.jetpacknotes.myItems.SearchItem
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotesListScreen(
    mainAppViewModel: MainAppViewModel,
    navigateWhenNoteClicked: (Int?) -> Unit
) {
    val viewModel: NotesListScreenViewModel = viewModel(
        factory = NotesListScreenViewModelFactory(mainAppViewModel)
    )
    val allNotes = mainAppViewModel.allNotes.observeAsState(emptyList())
    val allCategories = mainAppViewModel.categoryOfNotes.observeAsState(listOf())
    val selectedNotes = viewModel.selectedNotes.observeAsState(listOf())

    val category = viewModel.category.observeAsState()
    val searchText = viewModel.searchText.observeAsState()
    val filterData = viewModel.filterData.observeAsState(FilterData(null, FilterType.Edit))

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
        FilterDialog(
            currentData = filterData.value,
            onDismissRequest = { openFilterDialog = false },
            setFilter = viewModel::setFilterData
        )
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        NotesListAppBar(
            title = category.value?.name ?: "All Notes",
            showDeleteButton = selectedNotes.value.isNotEmpty(),
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
            onDeleteButtonClicked = { viewModel.deleteSelectedNotes(context) }
        )
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navigateWhenNoteClicked(null)
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
                            name = "Все заметки",
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
                val notesList = viewModel.filterNotes(
                    notes = allNotes.value,
                    category = category.value,
                    searchText = searchText.value,
                    filterData = filterData.value
                )
                NotesList(
                    notesList = notesList,
                    selectedNotes = selectedNotes.value,
                    onClick = { note, long ->
                        if (long || selectedNotes.value.isNotEmpty()) {
                            viewModel.changeSelectionStateOf(note)
                        } else {
                            navigateWhenNoteClicked(note.id)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun NotesListAppBar(
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
private fun NotesList(
    notesList: List<Note>,
    selectedNotes: List<Note>,
    onClick: (Note, Boolean) -> Unit,
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    LazyColumn {
        items(items = notesList,
            key = { it.id }
        ) { note ->
            NoteItem(
                title = note.title,
                time = timeFormatter.format(note.lastEditTime),
                date = dateFormatter.format(note.lastEditTime),
                color = Colors.colors[note.colorIndex],
                selected = note in selectedNotes,
                onClick = {
                    onClick(note, false)
                },
                onLongClick = {
                    onClick(note, true)
                }
            )
        }
    }
}


