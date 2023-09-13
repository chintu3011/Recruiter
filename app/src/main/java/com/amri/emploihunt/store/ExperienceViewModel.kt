package com.amri.emploihunt.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.store.ExperienceDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperienceViewModel @Inject
     constructor(
    private val experienceDataStore: ExperienceDataStore
) : ViewModel() {

    fun writeToLocal(experienceList: List<Experience>) = viewModelScope.launch{
        experienceDataStore.saveExperienceList(experienceList)
    }

    fun readFromLocal(): Flow<List<Experience>> {
        return experienceDataStore.getExperienceList()
    }

    fun clearFromLocal() {
        viewModelScope.launch {
            experienceDataStore.clearExperienceList()
        }
    }
}