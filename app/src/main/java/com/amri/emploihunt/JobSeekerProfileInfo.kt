package com.amri.emploihunt

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.datastore : DataStore<Preferences> by preferencesDataStore("JOB_SEEKER_PROFILE_INFO")

