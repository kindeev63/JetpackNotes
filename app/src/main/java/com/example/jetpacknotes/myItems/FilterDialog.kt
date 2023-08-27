package com.example.jetpacknotes.myItems

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.jetpacknotes.Colors
import com.example.jetpacknotes.FilterData
import com.example.jetpacknotes.FilterType
import com.example.jetpacknotes.R

@Composable
fun FilterDialog(
    currentData: FilterData,
    onDismissRequest: () -> Unit,
    setFilter: (data: FilterData) -> Unit
) {
    val filterData = rememberSaveable {
        mutableStateOf(currentData)
    }
    Dialog(onDismissRequest = onDismissRequest) {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(PaddingValues(horizontal = 24.dp))
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black)
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = (LocalConfiguration.current.screenHeightDp / 6 * 4).dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .verticalScroll(scrollState)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ColorRow(filterData = filterData)
                Divider()
                Spacer(modifier = Modifier.height(5.dp))
                FilterTypes(filterData = filterData)
                DialogActionButtons(
                    onSave = {
                        setFilter(filterData.value)
                        onDismissRequest()
                    },
                    onCancel = {
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorRow(filterData: MutableState<FilterData>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorFilterSpinner(
            colors = Colors.colors.map { Color(it.primary) },
            colorIndex = filterData.value.colorIndex
        ) { index ->
            val newFilterData = filterData.value.copy(colorIndex = index)
            if (filterData.value.type == FilterType.Color && index != null) {
                newFilterData.type = FilterType.Edit
            }
            filterData.value = newFilterData
        }
        if (filterData.value.colorIndex == null) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "All colors")
        }
    }
}

@Composable
private fun ColorFilterSpinner(
    colors: List<Color>,
    colorIndex: Int?,
    onPick: (index: Int?) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Box {
        if (colorIndex == null) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .border(BorderStroke(2.dp, Color.Black))
                    .clickable {
                        expanded = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(id = R.drawable.ic_block), contentDescription = null)
            }
        } else {
            ColorFilterSpinnerItem(color = colors[colorIndex]) {
                expanded = true
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .border(BorderStroke(2.dp, Color.Black))
                    .clickable {
                        onPick(null)
                        expanded = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(id = R.drawable.ic_block), contentDescription = null)
            }
            colors.forEachIndexed { index, color ->
                ColorFilterSpinnerItem(color = color) {
                    onPick(index)
                    expanded = false
                }
            }
        }
    }
}

@Composable
private fun ColorFilterSpinnerItem(color: Color, clickable: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color)
            .border(BorderStroke(2.dp, Color.Black))
            .clickable {
                clickable()
            }
    )
}

@Composable
private fun FilterTypes(filterData: MutableState<FilterData>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Сортировать по:",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        arrayListOf(FilterType.Create, FilterType.Edit)
            .apply {
                if (filterData.value.colorIndex == null) {
                    add(FilterType.Color)
                }
            }
            .forEach { filterType ->
                val selected = filterData.value.type == filterType
                FilterTypeItem(selected = selected, text = filterType.name) {
                    if (!selected) {
                        filterData.value = filterData.value.copy(type = filterType)
                    }
                }
            }
    }
}

@Composable
private fun FilterTypeItem(selected: Boolean, text: String, onClick: () -> Unit) {
    Box {
        Row(
            modifier = Modifier.height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = { }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = text)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable {
                    onClick()
                }
        )
    }


}

