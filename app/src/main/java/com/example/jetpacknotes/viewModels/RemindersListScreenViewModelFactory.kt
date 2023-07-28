package com.example.jetpacknotes.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RemindersListScreenViewModelFactory(private val mainAppViewModel: MainAppViewModel): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RemindersListScreenViewModel(mainAppViewModel) as T
    }
}