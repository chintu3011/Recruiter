package com.example.recruiter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.security.cert.Extension

class MainActivity2 : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadStorageGranted = false
    private var isWriteStorageGranted = false
    private var isCallPhoneGranted = false
    private  var isReceiveSmsGranted = false
    private var isSendSmsGranted = false

    companion object{
        private const val STORAGE_EXTERNAL_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


    }

    private fun requestPermission(){
        try {
            Log.d(TAG,"Request permission: try")
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION

        }
        catch (e:Exception){
            
        }
    }

    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}