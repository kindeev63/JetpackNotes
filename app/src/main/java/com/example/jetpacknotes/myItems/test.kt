package com.example.jetpacknotes.myItems

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun Test() {
    val state = rememberLazyListState()
//    val currentColor by remember {
//        derivedStateOf {
//            state.firstVisibleItemIndex
//        }
//    }
    val myColors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        Box(
//            modifier = Modifier
//                .size(25.dp)
//                .background(myColors[currentColor])
//        )
        Spacer(modifier = Modifier.height(25.dp))
        Box(
            modifier = Modifier.size(100.dp)
        ) {
            LazyRow(
                state = state
            ) {
                myColors.forEach {color ->
                    item {
                        MyItem(color = color) {
                            Log.e("test", color.toString())
                        }
                    }
                }
            }
        }
    }
//    LaunchedEffect(state.isScrollInProgress) {
//        if (!state.isScrollInProgress) {
//            val currentIndex = state.firstVisibleItemIndex
//            val currentOffset = state.firstVisibleItemScrollOffset
//            val maxScrollOffset = state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.offset } ?: 0
//
//            val targetIndex = if (currentOffset > maxScrollOffset) {
//                currentIndex + 1
//            } else {
//                currentIndex
//            }
//
//            state.animateScrollToItem(targetIndex)
//        }
//    }


}

@Composable
private fun MyItem(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Blue)
                .clickable {
                    onClick()
                }
        )
    }
}