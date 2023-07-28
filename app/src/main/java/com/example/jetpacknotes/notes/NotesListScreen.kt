package com.example.jetpacknotes.notes

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.myItems.CategoryItem
import com.example.jetpacknotes.myItems.NoteItem
import com.example.jetpacknotes.myItems.SearchItem
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Locale

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
    val categoriesList = mainAppViewModel.categoryOfNotes.observeAsState(listOf())

    val notesList = mainAppViewModel.allNotes.observeAsState(listOf())
    val selectedNotes = viewModel.selectedNotes.observeAsState(listOf())
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
    val openDialog = rememberSaveable {
        mutableStateOf<Category?>(null)
    }
    val scope = rememberCoroutineScope()
    CategoryDialog(openDialog = openDialog, viewModel = viewModel)
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NotesListAppBar(
            title = categoryState.value?.name ?: "All Notes",
            drawerState = drawerState,
            scope = scope,
            viewModel = viewModel,
            searchText = searchText
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
                        modifier = Modifier.width(((if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp) / 5 * 4).dp)
                    ) {
                        DrawerHeader(
                            onClickAdd = {
                                val category = viewModel.createCategory()
                                openDialog.value = category
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryItem(
                            name = "Все заметки",
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
                                viewModel.clickOnCategory(category, long, categoryState, openDialog)
                                if (!long) {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }

                            })
                    }
                }
            ) {
                NotesList(
                    notesList = viewModel.filterNotes(
                        notesList.value,
                        searchText.value,
                        categoryState.value
                    ),
                    selectedNotes = selectedNotes.value,
                    onClick = { note, long ->
                        if (long) {
                            viewModel.changeSelectionStateOf(note)
                        } else {
                            if (selectedNotes.value.isNotEmpty()) {
                                viewModel.changeSelectionStateOf(note)
                            } else {
                                navigateWhenNoteClicked(note.id)
                            }
                        }
                    },
                )
            }

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
                time = timeFormatter.format(note.time),
                date = dateFormatter.format(note.time),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListAppBar(
    title: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: NotesListScreenViewModel,
    searchText: MutableState<String?>
) {
    val selectedNotes = viewModel.selectedNotes.observeAsState(listOf())
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
                searchText = searchText,
                modifier = Modifier.weight(1f)
            )
            if (selectedNotes.value.isNotEmpty()) {
                IconButton(onClick = { viewModel.deleteSelectedNotes() }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }

    }
}

@Composable
fun PlaceholderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String = "",
    singleLine: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    boxModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
) {
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = textModifier,
            singleLine = singleLine,
            textStyle = textStyle
        )
        if (value.isEmpty()) {
            Text(
                text = hintText,
                style = TextStyle(color = Color.Gray),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    openDialog: MutableState<Category?>,
    viewModel: NotesListScreenViewModel
) {
    val category = openDialog.value
    if (category != null) {
        Dialog(onDismissRequest = {
            openDialog.value = null
        }) {
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val categoryName = rememberSaveable {
                        mutableStateOf(category.name)
                    }
                    val error = rememberSaveable {
                        mutableStateOf(false)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = categoryName.value,
                            onValueChange = {
                                categoryName.value = it
                                error.value = it == ""
                            },
                            singleLine = true,
                            label = {
                                Text(text = "name")
                            },
                            supportingText = {
                                if (error.value) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Name must not be empty",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            trailingIcon = {
                                if (error.value)
                                    Icon(
                                        Icons.Filled.Info,
                                        "error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                            }
                        )
                        if (category.name != "") {
                            IconButton(onClick = {
                                viewModel.deleteCategory(category)
                                openDialog.value = null
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }

                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { openDialog.value = null }) {
                            Text(text = "cancel")
                        }
                        TextButton(onClick = {
                            if (categoryName.value != "") {
                                viewModel.insertCategory(category.copy(name = categoryName.value))
                                openDialog.value = null
                            }
                        }) {
                            Text(text = "save")
                        }
                    }
                }
            }
        }
    }
}
