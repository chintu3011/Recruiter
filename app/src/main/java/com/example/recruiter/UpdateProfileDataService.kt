package com.example.recruiter

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.coroutineScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class UpdateProfileDataService : Service() {

    private lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    private lateinit var recruiterProfileInfo: RecruiterProfileInfo
    private var userType:String ?= null
    private var userId:String ?= null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        userType = intent?.getStringExtra("userType").toString()
        userId = intent?.getStringExtra("userId").toString()
        jobSeekerProfileInfo = JobSeekerProfileInfo(this)
        recruiterProfileInfo = RecruiterProfileInfo(this)

        storeUpdatedDataInServer()
        
        return super.onStartCommand(intent, flags, startId)


    }

    private fun storeUpdatedDataInServer() {
        FirebaseDatabase.getInstance().getReference("Users")
            .child(userType.toString())
            .child(userId.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var keySet: Set<Preferences.Key<*>>? = null

                    CoroutineScope(IO).launch(Dispatchers.Main.immediate) {
                        keySet = jobSeekerProfileInfo.readAllKeys()
                        for (variableName in keySet!!) {
                            val childSnapshot = snapshot.child(variableName.toString())
                            if (childSnapshot.exists()) {
                                // Retrieve the value from Firebase
                                val firebaseValue = childSnapshot.value
                                // Retrieve the local value
                                val localValue = jobSeekerProfileInfo.getValueByKey(variableName).toString()
                                if (firebaseValue == localValue) {
                                    Log.d("Comparison","$variableName is the same")
                                } else {
//                                  showUpdateDialog()
                                    childSnapshot.ref.setValue(localValue)
                                    Log.d("Comparison","$variableName is updated")
                                }
                            } else {
                                snapshot.child(variableName.toString()).ref.setValue(jobSeekerProfileInfo.getValueByKey(variableName).toString())
                                Log.d("Comparison","$variableName does not exist in Firebase")
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
//                    makeToast("error: ${error.message}",0)
                }
            })

    }
}