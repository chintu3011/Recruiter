package com.example.recruiter.profile

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.Preferences
import com.example.recruiter.jobSeekerSide.JobSeekerProfileInfo
import com.example.recruiter.recruiterSide.RecruiterProfileInfo
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
        Log.d("UpdateProfileDataService","Service started for updating data for user $userId")
        jobSeekerProfileInfo = JobSeekerProfileInfo(this)
        recruiterProfileInfo = RecruiterProfileInfo(this)
        

        storeUpdatedDataInServer()
        
        return super.onStartCommand(intent, flags, startId)
    }

    private fun storeUpdatedDataInServer() {
        Log.d("UpdateProfileDataService","running")
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
                            Log.d("UpdateProfileDataService","Process is terminating...")

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("UpdateProfileDataService","Process is Killed.")
                    Toast.makeText(this@UpdateProfileDataService,"Data is not stored properly in database",Toast.LENGTH_LONG).show()
                }
            })

    }
}