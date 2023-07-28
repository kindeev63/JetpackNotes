package com.example.jetpacknotes

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.example.jetpacknotes.viewModels.MainAppViewModel

class MainApp: Application() {
    val mainAppViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(MainAppViewModel::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        mainAppViewModel
    }
}