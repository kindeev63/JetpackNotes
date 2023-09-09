package com.example.jetpacknotes.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.R
import com.example.jetpacknotes.db.Category
import com.example.jetpacknotes.db.Task
import com.example.jetpacknotes.myItems.ColorSpinner
import com.example.jetpacknotes.myItems.DialogActionButtons
import com.example.jetpacknotes.myItems.PlaceholderTextField
import com.example.jetpacknotes.viewModels.MainAppViewModel
import java.util.ArrayList

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TaskEditDialog(
    task: Task?,
    mainAppViewModel: MainAppViewModel,
    onDismissRequest: () -> Unit
) {
    val newTask = rememberSaveable {
        mutableStateOf(task ?: createTask(mainAppViewModel = mainAppViewModel))
    }
    val openCategoriesDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val allCategories = mainAppViewModel.categoryOfTasks.observeAsState(emptyList())
    if (openCategoriesDialog.value) {
        CategoryDialog(
            openDialog = openCategoriesDialog,
            allCategoriesList = allCategories.value,
            task = newTask
        )
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (LocalConfiguration.current.screenHeightDp / 6 * 4).dp)
                .padding(PaddingValues(horizontal = 24.dp))
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black)
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .verticalScroll(scrollState)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TitleAndDone(task = newTask)
                ColorAndCategories(
                    task = newTask,
                    openCategoriesDialog = openCategoriesDialog,
                    showCategoryButton = allCategories.value.isNotEmpty()
                )
                TaskDescription(task = newTask)
                DialogActionButtons(
                    onSave = {
                        mainAppViewModel.insertTask(newTask.value) {
                            onDismissRequest()
                        }
                    },
                    onCancel = onDismissRequest
                )
            }
        }
    }
}

@Composable
private fun TaskDescription(
    task: MutableState<Task>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
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
            PlaceholderTextField(
                value = task.value.text,
                onValueChange = { text ->
                    task.value = task.value.copy(text = text)
                },
                hintText = "Описание"
            )
        }
    }
}

@Composable
private fun TitleAndDone(task: MutableState<Task>) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(5.dp))
        PlaceholderTextField(
            value = task.value.title,
            onValueChange = { title ->
                task.value = task.value.copy(title = title)
            },
            hintText = "Заголовок",
            modifier = Modifier
                .height(50.dp)
                .weight(1f)
        )
        Checkbox(
            colors = CheckboxDefaults.colors(
                checkedColor = Color(Colors.colors[task.value.colorIndex].primary),
                uncheckedColor = Color(Colors.colors[task.value.colorIndex].primary),
            ),
            checked = task.value.done,
            onCheckedChange = { done ->
                task.value = task.value.copy(done = done)
            }
        )
    }
}

@Composable
private fun ColorAndCategories(
    task: MutableState<Task>,
    openCategoriesDialog: MutableState<Boolean>,
    showCategoryButton: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ColorSpinner(
            colors = Colors.colors.map { Color(it.primary) },
            colorIndex = task.value.colorIndex
        ) { index ->
            task.value = task.value.copy(colorIndex = index)
        }
        if (showCategoryButton) {
            Button(
                modifier = Modifier.size(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    openCategoriesDialog.value = true
                }
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = painterResource(id = R.drawable.ic_category),
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }
    }
}

private fun createTask(mainAppViewModel: MainAppViewModel): Task {
    val idsList = (mainAppViewModel.allTasks.value ?: emptyList()).map { it.id }
    var taskId = 0
    while (true) {
        if (taskId !in idsList) break
        taskId++
    }
    return Task(
        id = taskId,
        title = "",
        text = "",
        done = false,
        categories = "",
        colorIndex = 0
    )
}

@Composable
private fun CategoryDialog(
    openDialog: MutableState<Boolean>,
    allCategoriesList: List<Category>,
    task: MutableState<Task>
) {
    if (openDialog.value) {
        Dialog(onDismissRequest = {
            openDialog.value = false
        }) {
            var categoriesIds by rememberSaveable {
                mutableStateOf(task.value.categories)
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
                            task.value = task.value.copy(categories = categoriesIds)
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