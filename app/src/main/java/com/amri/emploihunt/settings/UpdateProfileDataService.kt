package com.amri.emploihunt.settings

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CoroutineScope
import com.amri.emploihunt.R
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.model.GetUserById
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
import com.amri.emploihunt.store.UserDataRepository
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateProfileDataService : Service() {

    private lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    private lateinit var recruiterProfileInfo: RecruiterProfileInfo
    private var userType:Int ?= null
    private var userId:String ?= null


    //common data
    private var fName: String? = null
    private var lName: String? = null
    private var fullName: String? = null
    private var phoneNumber: String? = null
    private var emailId: String? = null
    private var tagLine: String? = null

    private var residentialCity:String? = null

    private var profileImgUri: String? = null
    private var profileBannerImgUri: String? = null

    //User Data
    private var bio: String? = null
    private var qualification: String? = null

    private var currentCompany: String? = null
    private var designation: String? = null
    private var jobLocation: String? = null
    private var workingMode:String? = null

    private lateinit var experienceList:MutableList<Experience>

    private var resumeUri: String? = null
    private var resumeFileName: String? = null

    private lateinit var userDataRepository: UserDataRepository

    private lateinit var prefmanger : SharedPreferences
    companion object{
        private const val TAG = "UpdateProfileDataService"
    }

    private lateinit var prefManager: SharedPreferences
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private lateinit var user: User
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "onStartCommand: Profile updated data service started.")
        prefManager = prefManager(this)
        userType = prefManager.get(ROLE,0)
        userId = prefManager.get(FIREBASE_ID)

        jobSeekerProfileInfo = JobSeekerProfileInfo(this)
        recruiterProfileInfo = RecruiterProfileInfo(this)

        /*UpdateSeverHelperClass.instance!!
            .updateData()*/

        prefmanger = prefManager(this)
        userDataRepository = UserDataRepository(this)

        user = intent?.getSerializableExtra("userObject") as User
        updateData()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: service is destroyed")
    }

    private fun updateData() {
        Log.d(TAG, "updateData: Updated data process service running")

        if(user != null){
            if (Utils.isNetworkAvailable(this@UpdateProfileDataService)) {
                if (user!!.iRole == 0) {
                    AndroidNetworking.post(NetworkUtils.UPDATE_PROFILE_DETAILS)
                        .setOkHttpClient(NetworkUtils.okHttpClient)
                        .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN, "")}")
                        .addQueryParameter("vFirstName", user!!.vFirstName)
                        .addQueryParameter("vLastName", user!!.vLastName)
                        .addQueryParameter("vEmail", user!!.vEmail)
                        .addQueryParameter("tBio", user!!.tBio)
                        .addQueryParameter("vcity", user!!.vCity)
                        .addQueryParameter("vCurrentCompany", user!!.vCurrentCompany)
                        .addQueryParameter("vDesignation", user!!.vDesignation)
                        .addQueryParameter("vJobLocation", user!!.vJobLocation)
                        .addQueryParameter("vQualification", user!!.vQualification)
                        .addQueryParameter("tTagLine", user!!.tTagLine)
                        .setPriority(Priority.MEDIUM).build().getAsObject(
                            GetUserById::class.java,
                            object : ParsedRequestListener<GetUserById> {
                                override fun onResponse(response: GetUserById?) {
                                    try {
                                        response?.let {
                                            Log.d(
                                                ProfileActivity.TAG,
                                                "onResponse: Profile Data Updated Successfully"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse Exception: ${e.message}")
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                        )
                                    }
                                }
                            })
                } else {
                    AndroidNetworking.post(NetworkUtils.UPDATE_PROFILE_DETAILS)
                        .setOkHttpClient(NetworkUtils.okHttpClient)
                        .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN, "")}")
                        .addQueryParameter("vFirstName", user!!.vFirstName)
                        .addQueryParameter("vLastName", user!!.vLastName)
                        .addQueryParameter("vEmail", user!!.vEmail)
                        .addQueryParameter("tBio", user!!.tBio)
                        .addQueryParameter("vcity", user!!.vCity)
                        .addQueryParameter("vCurrentCompany", user!!.vCurrentCompany)
                        .addQueryParameter("vDesignation", user!!.vDesignation)
                        .addQueryParameter("vJobLocation", user!!.vJobLocation)
                        .addQueryParameter("vQualification", user!!.vQualification)
                        .addQueryParameter("tTagLine", user!!.tTagLine)
                        .addQueryParameter("vWorkingMode", user!!.vWorkingMode)
                        .setPriority(Priority.MEDIUM).build().getAsObject(
                            GetUserById::class.java,
                            object : ParsedRequestListener<GetUserById> {
                                override fun onResponse(response: GetUserById?) {
                                    try {
                                        response?.let {
                                            Log.d(
                                                ProfileActivity.TAG,
                                                "onResponse: Profile Data Updated Successfully"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse Exception: ${e.message}")
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                        )
                                    }
                                }
                            })
                }
            }

        }
    }
}

/*
private fun storeUpdatedDataInServer() {
    */
/*     FirebaseDatabase.getInstance().getReference("Users")
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
             })*//*


}*/
