package com.amri.emploihunt.util

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.androidnetworking.AndroidNetworking
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        

        try {
            StrictMode.setVmPolicy(VmPolicy.Builder().build())
            AndroidNetworking.initialize(applicationContext)

        } catch (e: Exception) {
            Log.e("MainApp", "MainApp onCreate Error: ${e.message}")
        }
    }
}