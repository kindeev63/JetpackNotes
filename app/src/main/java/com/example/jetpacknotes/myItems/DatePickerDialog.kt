package com.example.jetpacknotes.myItems

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

@Composable
fun DatePickerDialog(
    time: Long,
    colors: DatePickerDialogColors = DatePickerDialogDefaults.colors,
    saveButtonText: String = "save",
    cancelButtonText: String = "cancel",
    weekDaysNames: List<String> = listOf(
        "ПН",
        "ВТ",
        "СР",
        "ЧТ",
        "ПТ",
        "СБ",
        "ВС"
    ),
    onCloseDialog: () -> Unit,
    onPick: (year: Int, month: Int, dat: Int) -> Unit
) {
    val instant = Instant.ofEpochMilli(time)
    val localDateTime =
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).atZone(ZoneId.systemDefault())
    DatePickerDialog(
        year = localDateTime.year,
        month = localDateTime.month.value,
        day = localDateTime.dayOfMonth,
        saveButtonText = saveButtonText,
        cancelButtonText = cancelButtonText,
        weekDaysNames = weekDaysNames,
        colors = colors,
        onCloseDialog = onCloseDialog,
        onPick = onPick
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DatePickerDialog(
    year: Int,
    month: Int,
    day: Int,
    colors: DatePickerDialogColors = DatePickerDialogDefaults.colors,
    saveButtonText: String = "save",
    cancelButtonText: String = "cancel",
    weekDaysNames: List<String> = listOf(
        "ПН",
        "ВТ",
        "СР",
        "ЧТ",
        "ПТ",
        "СБ",
        "ВС"
    ),
    onCloseDialog: () -> Unit,
    onPick: (year: Int, month: Int, dat: Int) -> Unit
) {
    val screenWidthDp = getScreenWidthDp()
    val date = rememberSaveable {
        mutableStateOf(DateForDialog(year, month, day))
    }
    val calendarScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val monthList = rememberSaveable {
        getMonthList().apply {
            scope.launch {
                calendarScrollState.scrollToItem(
                    indexOf(
                        CalendarItemData(
                            year = date.value.year,
                            month = date.value.month
                        )
                    )
                )
            }
        }
    }
    val pickYear = rememberSaveable {
        mutableStateOf(false)
    }
    Dialog(
        onDismissRequest = onCloseDialog,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .width(screenWidthDp.dp - 20.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.backgroundColor)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DialogHeader(date = date, colors = colors)
            DialogMonthAndYearChanger(
                pickYear = pickYear,
                scrollState = calendarScrollState,
                monthList = monthList,
                colors = colors
            )
            Divider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenWidthDp.dp - 60.dp)
            ) {
                DialogCalendar(
                    pickYear = pickYear,
                    date = date,
                    scrollState = calendarScrollState,
                    monthList = monthList,
                    colors = colors,
                    weekDaysNames = weekDaysNames
                )
                YearPicking(
                    pickYear = pickYear,
                    colors = colors,
                    scrollState = calendarScrollState,
                    monthList = monthList
                )
            }
            ActionButtons(
                date = date,
                colors = colors,
                onCloseDialog = onCloseDialog,
                onPick = onPick,
                saveButtonText = saveButtonText,
                cancelButtonText = cancelButtonText
            )
        }
    }
}

@Composable
private fun DialogHeader(
    date: MutableState<DateForDialog>,
    colors: DatePickerDialogColors
) {
    val dateFormatter = SimpleDateFormat("d MMM yyyy г.", Locale.getDefault())
    val symbols = dateFormatter.dateFormatSymbols
    symbols.months = symbols.months.map { it.capitalize() }.toTypedArray()
    dateFormatter.dateFormatSymbols = symbols
    val localDateTime =
        LocalDateTime.of(date.value.year, date.value.month, date.value.day, 0, 0).atZone(
            ZoneId.systemDefault()
        )
    Text(
        modifier = Modifier.padding(5.dp),
        text = dateFormatter.format(localDateTime.toInstant().toEpochMilli()),
        fontSize = largeFontSize(),
        color = colors.dateTextColor
    )
}

@Composable
private fun DialogMonthAndYearChanger(
    pickYear: MutableState<Boolean>,
    scrollState: LazyListState,
    monthList: List<CalendarItemData>,
    colors: DatePickerDialogColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val currentMouthIndex by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex
            }
        }
        TextButton(onClick = { pickYear.value = !pickYear.value }) {
            val dateFormatter = SimpleDateFormat("LLLL yyyy г.", Locale.getDefault())
            val localDateTime = LocalDateTime.of(
                monthList[currentMouthIndex].year,
                monthList[currentMouthIndex].month,
                1,
                0,
                0
            ).atZone(ZoneId.systemDefault())
            Text(
                text = dateFormatter.format(
                    localDateTime.toInstant().toEpochMilli()
                ),
                fontSize = smallFontSize(),
                color = colors.pickYearTextColor
            )
            Icon(
                modifier = Modifier
                    .graphicsLayer(scaleY = if (pickYear.value) -1f else 1f),
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = colors.pickYearTextColor
            )
        }
        if (!pickYear.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val scope = rememberCoroutineScope()
                IconButton(onClick = {
                    if (currentMouthIndex != 0) {
                        scope.launch {
                            scrollState.animateScrollToItem(currentMouthIndex - 1)
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = colors.arrowButtonColor
                    )
                }
                IconButton(onClick = {
                    if (currentMouthIndex != monthList.size - 1) {
                        scope.launch {
                            scrollState.animateScrollToItem(currentMouthIndex + 1)
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.arrowButtonColor
                    )
                }
            }
        }

    }
}

@Composable
private fun YearPicking(
    pickYear: MutableState<Boolean>,
    colors: DatePickerDialogColors,
    scrollState: LazyListState,
    monthList: List<CalendarItemData>
) {
    AnimatedVisibility(
        visible = pickYear.value,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PickYear(
                    colors = colors,
                    pickYear = pickYear,
                    scrollState = scrollState,
                    monthList = monthList
                )
            }
            Divider()
        }

    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun PickYear(
    colors: DatePickerDialogColors,
    pickYear: MutableState<Boolean>,
    scrollState: LazyListState,
    monthList: List<CalendarItemData>
) {
    val yearNow = LocalDate.now().year
    val minYear = (yearNow / 100).toInt() * 100 - 100
    val state = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val currentMouthIndex by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex
        }
    }
    scope.launch {
        state.scrollToItem(monthList[currentMouthIndex].year - minYear)
    }
    val years = arrayListOf<Int>().apply {
        repeat(200) {
            add(minYear + it)
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = state
    ) {
        items(
            items = years,
            key = { it }
        ) { year ->
            DateButton(
                text = year.toString(),
                type = when (year) {
                    monthList[currentMouthIndex].year -> DateButtonType.Selected
                    yearNow -> DateButtonType.Now
                    else -> DateButtonType.Unselected
                },
                colors = colors
            ) {
                if (year != monthList[currentMouthIndex].year) {
                    val currentItem = monthList[currentMouthIndex]
                    scope.launch {
                        scrollState.scrollToItem(monthList.indexOf(currentItem.copy(year = year)))
                    }
                }
                pickYear.value = false
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun DialogCalendar(
    pickYear: MutableState<Boolean>,
    date: MutableState<DateForDialog>,
    scrollState: LazyListState,
    monthList: List<CalendarItemData>,
    colors: DatePickerDialogColors,
    weekDaysNames: List<String>
) {
    AnimatedVisibility(
        visible = !pickYear.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                weekDaysNames.forEach {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it,
                            fontSize = smallFontSize(),
                            color = colors.unselectedButtonTextColor
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(6f)
            ) {
                LazyRow(state = scrollState) {
                    items(
                        items = monthList,
                        key = { "${it.year}${it.month}".toInt() }
                    ) { calendarItemData ->
                        CalendarItem(
                            data = calendarItemData,
                            selected = checkCalendarSelected(calendarItemData, date.value),
                            now = checkCalendarNow(calendarItemData),
                            colors = colors
                        ) { day ->
                            date.value = date.value.copy(
                                year = calendarItemData.year,
                                month = calendarItemData.month,
                                day = day
                            )
                        }
                    }
                }
                LaunchedEffect(scrollState.isScrollInProgress) {
                    if (!scrollState.isScrollInProgress) {
                        val maxScrollOffset =
                            scrollState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.offset } ?: 0
                        val currentIndex = scrollState.firstVisibleItemIndex
                        val currentOffset = scrollState.firstVisibleItemScrollOffset
                        if (currentOffset > 0) {
                            val targetIndex = if (currentOffset > maxScrollOffset) {
                                currentIndex + 1
                            } else {
                                currentIndex
                            }

                            scrollState.animateScrollToItem(targetIndex)
                        }


                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarItem(
    data: CalendarItemData,
    selected: Int? = null,
    now: Int? = null,
    colors: DatePickerDialogColors,
    onClick: (day: Int) -> Unit
) {
    val localDate = LocalDate.of(data.year, data.month, 1)
    val mouthLength = localDate.month.length(localDate.isLeapYear)
    val screenWidthDp = getScreenWidthDp()
    val days = arrayListOf<Int?>().apply {
        repeat(localDate.dayOfWeek.value - 1) {
            add(null)
        }
        repeat(mouthLength) {
            add(it + 1)
        }
        repeat(42 - this.size) {
            add(null)
        }
    }
    Column(
        modifier = Modifier
            .width(screenWidthDp.dp - 60.dp)
            .height((screenWidthDp.dp - 60.dp) / 7 * 6)
    ) {
        repeat(6) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                repeat(7) {
                    val index = it + (7 * row)
                    val day = days[index]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            DateButton(
                                text = day.toString(),
                                contentPadding = PaddingValues(0.dp),
                                type = when (day) {
                                    selected -> DateButtonType.Selected
                                    now -> DateButtonType.Now
                                    else -> DateButtonType.Unselected
                                },
                                colors = colors
                            ) {
                                onClick(day)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    date: MutableState<DateForDialog>,
    colors: DatePickerDialogColors,
    saveButtonText: String,
    cancelButtonText: String,
    onCloseDialog: () -> Unit,
    onPick: (year: Int, month: Int, day: Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(onClick = onCloseDialog) {
            Text(
                text = cancelButtonText,
                color = colors.actionButtonTextColor
            )
        }
        TextButton(onClick = {
            onPick(date.value.year, date.value.month, date.value.day)
            onCloseDialog()
        }) {
            Text(
                text = saveButtonText,
                color = colors.actionButtonTextColor
            )
        }
    }
}

@Composable
private fun DateButton(
    modifier: Modifier = Modifier,
    text: String,
    type: DateButtonType,
    colors: DatePickerDialogColors,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    onClick: () -> Unit
) {
    val data = getDateButtonDataByType(type = type, colors = colors)
    Button(
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = data.containerColor
        ),
        border = data.border,
        onClick = onClick
    ) {
        Text(
            text = text,
            color = data.textColor,
            fontSize = smallFontSize()
        )
    }
}

private fun checkCalendarSelected(
    calendarItemData: CalendarItemData,
    date: DateForDialog
) =
    if (calendarItemData.year == date.year && calendarItemData.month == date.month) date.day else null

private fun checkCalendarNow(
    calendarItemData: CalendarItemData
) =
    if (calendarItemData.year == LocalDate.now().year && calendarItemData.month == LocalDate.now().month.value) LocalDate.now().dayOfMonth else null

private fun getDateButtonDataByType(
    type: DateButtonType,
    colors: DatePickerDialogColors
): DateButtonData {
    return when (type) {
        DateButtonType.Selected -> DateButtonData(
            containerColor = colors.selectedButtonColor,
            textColor = colors.selectedButtonTextColor,
            border = null
        )

        DateButtonType.Now -> DateButtonData(
            containerColor = Color.Transparent,
            textColor = colors.selectedButtonColor,
            border = BorderStroke(2.dp, colors.selectedButtonColor)
        )

        DateButtonType.Unselected -> DateButtonData(
            containerColor = Color.Transparent,
            textColor = colors.unselectedButtonTextColor,
            border = null
        )
    }
}

@Composable
private fun getScreenWidthDp() =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) LocalConfiguration.current.screenWidthDp else LocalConfiguration.current.screenHeightDp

private fun getMonthList(): List<CalendarItemData> {
    val yearNow = LocalDate.now().year
    val minYear = (yearNow / 100).toInt() * 100 - 100
    return arrayListOf<CalendarItemData>().apply {
        repeat(200) { item ->
            val year = minYear + item
            repeat(12) {
                add(CalendarItemData(year = year, month = it + 1))
            }
        }
    }
}

@Composable
private fun smallFontSize(): TextUnit {
    val screenWidthDp = getScreenWidthDp()
    return ((screenWidthDp - 20) / 20).sp
}

@Composable
private fun largeFontSize(): TextUnit {
    val screenWidthDp = getScreenWidthDp()
    return ((screenWidthDp - 20) / 10).sp
}

data class DatePickerDialogColors(
    val backgroundColor: Color,
    val selectedButtonColor: Color,
    val selectedButtonTextColor: Color,
    val unselectedButtonTextColor: Color,
    val actionButtonTextColor: Color,
    val dateTextColor: Color,
    val pickYearTextColor: Color,
    val arrowButtonColor: Color
)

object DatePickerDialogDefaults {
    val colors = DatePickerDialogColors(
        backgroundColor = Color.White,
        selectedButtonColor = Color(0xFF6650a4),
        selectedButtonTextColor = Color.White,
        unselectedButtonTextColor = Color.Black,
        actionButtonTextColor = Color(0xFF6650a4),
        dateTextColor = Color.Black,
        pickYearTextColor = Color(0xFF6650a4),
        arrowButtonColor = Color.Black
    )
}

private data class CalendarItemData(val year: Int, val month: Int) : Serializable
private data class DateForDialog(val year: Int, val month: Int, val day: Int) : Serializable
private enum class DateButtonType { Selected, Now, Unselected }
private data class DateButtonData(
    val containerColor: Color,
    val textColor: Color,
    val border: BorderStroke?
)
