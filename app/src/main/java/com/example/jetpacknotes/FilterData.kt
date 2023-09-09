package com.example.jetpacknotes

import java.io.Serializable

data class FilterData(var colorIndex: Int?, var type: FilterType, var data: String? = null): Serializable
