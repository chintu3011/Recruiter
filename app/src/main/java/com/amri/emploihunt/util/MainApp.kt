package com.amri.emploihunt.util

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        

        try {
            StrictMode.setVmPolicy(VmPolicy.Builder().build())
            AndroidNetworking.initialize(applicationContext)
            Firebase.initialize(context = this)
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance(),
            )


        } catch (e: Exception) {
            Log.e("MainApp", "MainApp onCreate Error: ${e.message}")
        }
    }
}