package com.example.jetpacknotes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "table_notes")
data class Note(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @ColumnInfo(name = "createTime")
    val createTime: Long,
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "lastEditTime")
    val lastEditTime: Long,
    @ColumnInfo(name = "categories")
    var categories: String,
    @ColumnInfo(name = "colorIndex")
    val colorIndex: Int
): Serializable
