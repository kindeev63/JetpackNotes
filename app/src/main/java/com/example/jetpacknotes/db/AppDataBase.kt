package com.example.jetpacknotes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Category::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun getDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDataBase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, AppDataBase::class.java, "notes.db"
                ).build()
            }
        }
    }

}