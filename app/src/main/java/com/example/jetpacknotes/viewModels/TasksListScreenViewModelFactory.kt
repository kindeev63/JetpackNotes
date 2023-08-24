package com.example.jetpacknotes.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TasksListScreenViewModelFactory(
    private val mainAppViewModel: MainAppViewModel
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TasksListScreenViewModel(mainAppViewModel) as T
    }
}