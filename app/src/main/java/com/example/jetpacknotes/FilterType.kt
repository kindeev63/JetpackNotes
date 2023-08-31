package com.example.jetpacknotes

import java.io.Serializable

sealed class FilterType(val name: String): Serializable {
    object Create: FilterType("по времени создания")
    object Edit: FilterType("по времени редактирования")
    object Color: FilterType("по цвету")
    object Hand: FilterType("вручную")
}