package com.example.jetpacknotes.tasks

import com.example.jetpacknotes.db.Task
import java.io.Serializable

data class TaskForDialog(val task: Task?): Serializable
