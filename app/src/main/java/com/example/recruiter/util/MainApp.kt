package com.example.recruiter.util

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.androidnetworking.AndroidNetworking

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