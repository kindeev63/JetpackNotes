package com.example.jetpacknotes

import java.io.Serializable

sealed class FilterType(val name: String): Serializable {
    object Create: FilterType("Времени создания")
    object Edit: FilterType("Времени редактирования")
    object Color: FilterType("Цвету")
}