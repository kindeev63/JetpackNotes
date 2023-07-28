package com.example.jetpacknotes.notes

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Note
import com.example.jetpacknotes.viewModels.MainAppViewModel
import com.example.jetpacknotes.viewModels.NoteEditScreenViewModel
import com.example.jetpacknotes.viewModels.NoteEditScreenViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Calendar

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun NoteEditScreen(
    mainAppViewModel: MainAppViewModel,
    noteId: Int?,
    onBackPressed: () -> Unit
) {
    val viewModel: NoteEditScreenViewModel = viewModel(
        factory = NoteEditScreenViewModelFactory(mainAppViewModel)
    )
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val scope = rememberCoroutineScope()
        var note by rememberSaveable {
            mutableStateOf<Note?>(null)
        }
        val openDialog = rememberSaveable {
            mutableStateOf(false)
        }
        val currentNoteState = viewModel.currentNoteState.observeAsState(
            NoteState(
                TextFieldValue(),
                TextFieldValue(),
                0,
                ""
            )
        )
        val allCategories = mainAppViewModel.categoryOfNotes.observeAsState(emptyList())
        if (note == null) {
            scope.launch {
                if (noteId==null) {
                    viewModel.createNote {
                        note = it
                        viewModel.addNoteState(
                            NoteState(
                                title = TextFieldValue(it.title),
                                text = TextFieldValue(it.text),
                                colorIndex = it.colorIndex,
                                categories = it.categories
                            )
                        )
                    }
                } else {
                    mainAppViewModel.getNoteById(noteId) {
                        note = it
                        viewModel.addNoteState(
                            NoteState(
                                title = TextFieldValue(it?.title ?: ""),
                                text = TextFieldValue(it?.text ?: ""),
                                colorIndex = it?.colorIndex ?: 0,
                                categories = it?.categories ?: ""
                            )
                        )
                    }
                }

            }
        }
        CategoryDialog(
            openDialog = openDialog,
            allCategoriesList = allCategories.value,
            currentNoteState = currentNoteState,
            viewModel = viewModel
        )
        BackHandler {
            saveNote(
                mainAppViewModel = mainAppViewModel,
                note = note!!,
                title = currentNoteState.value.title.text,
                text = currentNoteState.value.text.text,
                colorIndex = currentNoteState.value.colorIndex,
                categories = currentNoteState.value.categories
            ) {
                onBackPressed()
            }

        }
        NoteEditAppBar(
            mainAppViewModel = mainAppViewModel,
            onBackPressed = {
                saveNote(
                    mainAppViewModel = mainAppViewModel,
                    note = note!!,
                    title = currentNoteState.value.title.text,
                    text = currentNoteState.value.text.text,
                    colorIndex = currentNoteState.value.colorIndex,
                    categories = currentNoteState.value.categories
                ) {
                    onBackPressed()
                }

            },
            onUndoPressed = {
                viewModel.undoState()
            },
            onRedoPressed = {
                viewModel.redoState()
            },
            onAddReminderPressed = {},
            onCategoryPressed = {
                openDialog.value = true
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(3.dp)
                    )
                    .shadow(1.dp)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    BasicTextField(
                        value = currentNoteState.value.title,
                        onValueChange = {
                            viewModel.addNoteState(title = it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(fontSize = 20.sp)
                    )
                    if (currentNoteState.value.title.text.isEmpty()) {
                        Text(
                            text = "Заголовок",
                            style = TextStyle(color = Color.Gray),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 20.sp
                        )
                    }
                }
                NoteEditSpinner(
                    currentNoteState = currentNoteState,
                    viewModel = viewModel
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(3.dp)
                    )
                    .shadow(1.dp)
                    .padding(2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    BasicTextField(
                        value = currentNoteState.value.text,
                        onValueChange = {
                            viewModel.addNoteState(text = it)
                        },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle.Default.copy(fontSize = 18.sp)
                    )
                    if (currentNoteState.value.text.text.isEmpty()) {
                        Text(
                            text = "Текст заметки",
                            style = TextStyle(color = Color.Gray),
                            modifier = Modifier.padding(start = 4.dp),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    openDialog: MutableState<Boolean>,
    allCategoriesList: List<Category>,
    currentNoteState: State<NoteState>,
    viewModel: NoteEditScreenViewModel
) {
    if (openDialog.value) {
        Dialog(onDismissRequest = {
            openDialog.value = false
        }) {
            var categoriesIds by rememberSaveable {
                mutableStateOf(currentNoteState.value.categories)
            }
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black)
                    .padding(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn {
                        items(
                            items = allCategoriesList,
                            key = { it.id }
                        ) { category ->
                            CategoryDialogItem(
                                name = category.name,
                                checked = categoriesIds.contains(category.id.toString())
                            ) {
                                categoriesIds =
                                    if (categoriesIds.contains(category.id.toString())) {
                                        ArrayList(categoriesIds.split(" | ")).apply {
                                            remove(category.id.toString())
                                        }.joinToString(" | ")
                                    } else {
                                        ArrayList(categoriesIds.split(" | ")).apply {
                                            add(category.id.toString())
                                        }.joinToString(" | ")
                                    }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { openDialog.value = false }) {
                            Text(text = "cancel")
                        }
                        TextButton(onClick = {
                            viewModel.addNoteState(categories = categoriesIds)
                            openDialog.value = false
                        }) {
                            Text(text = "save")
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun CategoryDialogItem(name: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            })
        Text(
            text = name,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun NoteEditAppBar(
    mainAppViewModel: MainAppViewModel,
    onBackPressed: () -> Unit,
    onUndoPressed: () -> Unit,
    onRedoPressed: () -> Unit,
    onAddReminderPressed: () -> Unit,
    onCategoryPressed: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(-14720766)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackPressed
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithOnTouch(
                iconResourceId = R.drawable.ic_undo,
                action = onUndoPressed
            )
            IconButtonWithOnTouch(
                iconResourceId = R.drawable.ic_redo,
                action = onRedoPressed
            )
            IconButton(onClick = onAddReminderPressed) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reminder),
                    contentDescription = null,
                    tint = Color.White
                )
            }
            if (mainAppViewModel.categoryOfNotes.value?.isNotEmpty() == true) {
                IconButton(onClick = onCategoryPressed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_category),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteEditSpinner(
    currentNoteState: State<NoteState>,
    viewModel: NoteEditScreenViewModel
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Box {
        NoteEditSpinnerItem(color = Color(Colors.colors[currentNoteState.value.colorIndex].primary)) {
            expanded = true
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Colors.colors.forEachIndexed { index, color ->
                NoteEditSpinnerItem(color = Color(color.primary)) {
                    viewModel.addNoteState(colorIndex = index)
                    expanded = false
                }
            }
        }
    }
}

@Composable
private fun NoteEditSpinnerItem(color: Color, clickable: () -> Unit = {}) {
    Box(
        modifier = if (clickable != {}) {
            Modifier
                .size(50.dp)
                .background(color)
                .border(BorderStroke(2.dp, Color.Black))
                .clickable { clickable() }
        } else {
            Modifier
                .size(50.dp)
                .background(color)
                .border(BorderStroke(2.dp, Color.Black))
        }


    )
}

private fun saveNote(
    mainAppViewModel: MainAppViewModel,
    note: Note,
    title: String,
    text: String,
    colorIndex: Int,
    categories: String,
    function: () -> Unit
) {
    mainAppViewModel.insertNote(
        note.copy(
            title = title,
            text = text,
            colorIndex = colorIndex,
            categories = categories,
            time = Calendar.getInstance().timeInMillis
        )
    ) {
        function()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IconButtonWithOnTouch(iconResourceId: Int, action: () -> Unit) {
    var isPressed by rememberSaveable { mutableStateOf(false) }
    val colorTransparent by animateColorAsState(targetValue = Color.Transparent, animationSpec = spring())
    val colorGray by animateColorAsState(targetValue = Color.Black.copy(alpha = 0.2f), animationSpec = spring())
    var color by remember {
        mutableStateOf(colorTransparent)
    }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = color, shape = CircleShape)
                .pointerInteropFilter(
                    onTouchEvent = { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isPressed = true
                                color = colorGray
                                true
                            }

                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isPressed = false
                                color = colorTransparent
                                true
                            }

                            else -> false
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResourceId),
                contentDescription = null,
                tint = Color.White
            )
    }

    var delayInMillis = rememberSaveable {
        650L
    }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            while (isPressed) {
                action()
                delay(delayInMillis)
                if (delayInMillis > 100) {
                    delayInMillis -= 200
                }
            }
            delayInMillis = 600L
        }
    }
}