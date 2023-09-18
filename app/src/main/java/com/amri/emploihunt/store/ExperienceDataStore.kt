package com.amri.emploihunt.store

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.amri.emploihunt.model.Experience

import com.amri.emploihunt.proto.Experiences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

import java.lang.Exception
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private val Context.dataStore: DataStore<Experiences.ExperienceList> by dataStore(
    fileName = "experience_prefs.pb",
    serializer = ExperienceStoreSettings
)
class ExperienceDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun saveExperienceList(experienceList: List<Experience>) {
        context.dataStore.updateData { currentList ->
            currentList.toBuilder()
                .clearExperiences()
                .addAllExperiences(
                    experienceList.map {
                        it.toProto()
                    }
                )
                .build()
        }
    }

    fun getExperienceList(): Flow<List<Experience>> {
        return context.dataStore.data
            .catch {
                if (this is Exception) {
                    Log.e("ExperienceDataStore", ":${this.message} ")
                }
            }.map { experienceList ->
                experienceList.experiencesList.map { it.toExperience() }
            }
    }

    suspend fun clearExperienceList() {
        context.dataStore.updateData { currentList ->
            currentList.toBuilder()
                .clearExperiences()
                .build()
        }
    }
}

private fun Experience.toProto(): Experiences.Experience {
    return Experiences.Experience.newBuilder()
        .setVDesignation(vDesignation)
        .setVCompanyName(vCompanyName)
        .setVJobLocation(vJobLocation)
        .setBIsCurrentCompany(bIsCurrentCompany)
        .setVDuration(vDuration)
        .build()
}

private fun Experiences.Experience.toExperience(): Experience {
    return Experience(
        vDesignation = vDesignation,
        vCompanyName = vCompanyName,
        vJobLocation = vJobLocation,
        bIsCurrentCompany = bIsCurrentCompany,
        vDuration = vDuration
    )
}


