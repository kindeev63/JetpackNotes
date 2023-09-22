package com.example.jetpacknotes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("data_store")

class DataStoreManager(val context: Context) {

    suspend fun saveNotesFilterData(filterData: FilterData) {
        context.dataStore.edit { preference ->
            val stringFilterData = Gson().toJson(filterData)
            preference[stringPreferencesKey("notes_filter_data")] = stringFilterData
        }
    }

    suspend fun saveTasksFilterData(filterData: FilterData) {
        context.dataStore.edit { preference ->
            val stringFilterData = Gson().toJson(filterData)
            preference[stringPreferencesKey("tasks_filter_data")] = stringFilterData
        }
    }

    fun getNotesFilterData(): Flow<FilterData?> = context.dataStore.data.map { preference ->
        val stringFilterData = preference[stringPreferencesKey("notes_filter_data")]
        stringFilterData?.let {Gson().fromJson(stringFilterData, FilterData::class.java)}
    }

    fun getTasksFilterData(): Flow<FilterData?> = context.dataStore.data.map { preference ->
        val stringFilterData = preference[stringPreferencesKey("tasks_filter_data")]
        stringFilterData?.let {Gson().fromJson(stringFilterData, FilterData::class.java)}
    }
}