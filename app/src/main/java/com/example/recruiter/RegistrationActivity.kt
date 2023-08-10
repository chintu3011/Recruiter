package com.example.recruiter

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.example.recruiter.basedata.BaseActivity
import com.example.recruiter.model.UserExistOrNotModel
import com.example.recruiter.networking.NetworkUtils
import com.example.recruiter.util.LATITUDE
import com.example.recruiter.util.LONGITUDE
import com.example.recruiter.util.PrefManager
import com.example.recruiter.util.PrefManager.get
import com.example.recruiter.util.PrefManager.set
import com.example.recruiter.util.Utils
import com.example.recruiter.util.Utils.isGPSEnabled
import com.example.recruiter.util.Utils.showNoInternetBottomSheet
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.hbb20.CountryCodePicker
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.concurrent.TimeUnit


class RegistrationActivity : BaseActivity() ,OnClickListener{

    private lateinit var Fname:EditText
    private lateinit var Lname:EditText
    private lateinit var cpp: CountryCodePicker
    private lateinit var inputPhoneNo: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputCity: EditText
    private lateinit var mainLayout: ConstraintLayout
    lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks
    //    lateinit var btnTerms:TextView
//    lateinit var btnConditions:TextView
    private lateinit var checkBox:CheckBox
    private lateinit var btnRegistration: Button
    private lateinit var progressBar:ProgressBar
    private lateinit var prefmanger: SharedPreferences


    private lateinit var mAuth: FirebaseAuth

    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var phoneNo: String
    private lateinit var email: String
    private lateinit var city: String
    private lateinit var userType: String
    private lateinit var termsConditionsAcceptance:String


    private lateinit var decorView: View
    private lateinit var copyCredential : PhoneAuthCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        val window: Window = this@RegistrationActivity.window
        val background =ContextCompat.getDrawable(this@RegistrationActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@RegistrationActivity,android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this@RegistrationActivity,android.R.color.white)
        window.setBackgroundDrawable(background)
        prefmanger = PrefManager.prefManager(this)
        setXmlIDs()
        setOnClickListener()
        mAuth = FirebaseAuth.getInstance()
        Firebase.initialize(context = this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        cpp.registerCarrierNumberEditText(inputPhoneNo)
    }

    private fun setOnClickListener() {
        btnRegistration.setOnClickListener(this)
        FirebaseApp.initializeApp(this)
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.btnRegistration -> {
                registerUser()
            }
        }
    }
    private fun registerUser() {

        firstName = Fname.text.toString()
        lastName = Lname.text.toString()
        phoneNo = "+" + cpp.fullNumber.toString()
        email = inputEmail.text.toString()
        city = inputEmail.text.toString()
        userType = intent.getStringExtra("userType").toString()
        termsConditionsAcceptance =  if (checkBox.isChecked) {
            "Accepted"
        }
        else{
            "Not Accepted"
        }
        val correct = inputFieldConformation(userType,firstName,lastName,phoneNo,email,city,termsConditionsAcceptance)

        isPhNoRegBefore(phoneNo,exist)
        if (!correct) return
        else{


            Log.d("test", "registerUser: $phoneNo")
//            val options = PhoneAuthOptions.newBuilder(mAuth)
//                .setPhoneNumber(phoneNo)
//                .setTimeout(60L, TimeUnit.SECONDS)
//                .setActivity(this)
//                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                        progressBar.visibility = GONE
//                        btnRegistration.visibility = VISIBLE
//                        copyCredential = credential
//                        makeToast("onVerificationCompleted:$credential",1)
//                        passInfoToNextActivity()
//                    }
//                    override fun onVerificationFailed(e: FirebaseException) {
//                        progressBar.visibility = GONE
//                        btnRegistration.visibility = VISIBLE
//                        makeToast("Verification failed: ${e.message}",1)
//                        Log.d("test", "onVerificationFailed: ${e.message}")
//                    }
//                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                        progressBar.visibility = GONE
//                        btnRegistration.visibility = VISIBLE
//                        Log.d("test", "onCodeSent: $verificationId")
//                        makeToast("code sent",0)
//                        storedVerificationId = verificationId
//                        resendToken = token
//                        passInfoToNextActivity()
//                    }
//                })
//                .build()
//            PhoneAuthProvider.verifyPhoneNumber(options)

            getUserTypeIfNotSignIn(phoneNo) { userType ->

                Log.d("###", "registerUser: $userType")
                if (userType.isNotEmpty()) {
                    mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            progressBar.visibility = GONE
                            btnRegistration.visibility = VISIBLE
                            copyCredential = credential
                            makeToast("onVerificationCompleted:$credential",1)
                            passInfoToNextActivity()
                        }
                        override fun onVerificationFailed(e: FirebaseException) {
                            progressBar.visibility = GONE
                            btnRegistration.visibility = VISIBLE
                            makeToast("Verification failed: ${e.message}",1)
                            Log.d("test", "onVerificationFailed: ${e.message}")
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            hideProgressDialog()
                            Log.d("test", "onCodeSent: $verificationId")
                            makeToast("code sent",0)
                            storedVerificationId = verificationId
                            resendToken = token
                            passInfoToNextActivity()

                        }
                    }
                    val options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(
                            phoneNo
                        ) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this) // Activity (for callback binding)
                        .setCallbacks(mCallback) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }

            }


        }
        }

    private fun passInfoToNextActivity() {
        firstName = Fname.text.toString()
        phoneNo = "+" + cpp.fullNumber.toString()
        email = inputEmail.text.toString()
        lastName = Lname.text.toString()
        city = inputCity.text.toString()
        isPhNoRegBefore(phoneNo,exist)
        val correct = inputFieldConformation(
            userType,
            firstName,
            lastName,
            phoneNo,
            email,
            city,
            termsConditionsAcceptance
        )

        if (!correct) return
        else{

            if (!isGPSEnabled(this)) {
                turnGPSOn()
            } else if (isGrantedPermission()) {
                getLatLng()
            } else {
                requestPermissions()
            }
        }
    }

    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }
    var exist:Boolean=false
    private fun inputFieldConformation(
        userType: String,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String,
        city: String,
        termsConditionsAcceptance: String
    ): Boolean {
        if(userType.isEmpty()){
            makeToast("Please go back and select one job type.", 1)
            return false
        }
        if (firstName.isEmpty()) {
            Fname.error = "Please provide a first-name"
            Fname.requestFocus()
            return false
        }
        if (lastName.isEmpty()) {
            Lname.error = "Please provide a last-name"
            Lname.requestFocus()
            return false
        }
        if (inputPhoneNo.text.toString().isEmpty()) {
            inputPhoneNo.error = "Please provide a mobile no."
            inputPhoneNo.requestFocus()
            return false
        }
        if (inputPhoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            inputPhoneNo.error = "Please provide valid 10 digit mobile no"
            inputPhoneNo.requestFocus()
            return false
        }
//        makeToast(exist.toString(),0)
        if(exist){
            inputPhoneNo.error = "This phone number is already exist"
            inputPhoneNo.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            inputEmail.error = "Please provide a email address"
            inputEmail.requestFocus()
            return  false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.error = "Please provide valid email address"
            inputEmail.requestFocus()
            return false
        }
        if (city.isEmpty()) {
            inputCity.error = "Please provide a email address"
            inputCity.requestFocus()
            return  false
        }
        if (termsConditionsAcceptance != "Accepted"){
            checkBox.error = "Accept terms & conditions"
            return false
        }
        return true
    }

    private fun isPhNoRegBefore(phoneNo: String, exist: Boolean){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val userMobileNo = userSnapshot.child("phoneNo").getValue(String::class.java)
                        if (userMobileNo == phoneNo) {
                            this@RegistrationActivity.exist = true
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                makeToast("error: ${error.message}",0)
            }
        })
    }


    private fun setXmlIDs() {
        Fname = findViewById(R.id.userFName)
        Lname = findViewById(R.id.userLName)
        cpp = findViewById(R.id.cpp)
        inputPhoneNo = findViewById(R.id.phoneNo)
        inputEmail = findViewById(R.id.email)
        inputCity = findViewById(R.id.city)
//        btnTerms = findViewById(R.id.btnTerms)
//        btnConditions = findViewById(R.id.btnConditions)
        checkBox = findViewById(R.id.checkBox)
        btnRegistration = findViewById(R.id.btnRegistration)
        progressBar = findViewById(R.id.progressBar)
        mainLayout = findViewById(R.id.mainLayout)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@RegistrationActivity,AskActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        finish()
    }
    // Permission Code Started
    private fun requestPermissions() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                //Log.e("#####", "onPermissionsChecked ${report?.deniedPermissionResponses}")
                // Called when all permissions are granted
                if (report?.areAllPermissionsGranted()!!) {
                    getLatLng()
                }
                if (report.isAnyPermissionPermanentlyDenied) {
                    // Show dialog when user denied permission permanently, show dialog message.
                    showSettingsDialog()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
                Log.e("#####", "onPermissionRationaleShouldBeShown ${permissions.toString()}")
            }
        }).withErrorListener { error -> Log.e("#####", "onError $error") }.check()
    }

    private fun isGrantedPermission(): Boolean {
        val isGranted1 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val isGranted2 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return isGranted1 == PackageManager.PERMISSION_GRANTED && isGranted2 == PackageManager.PERMISSION_GRANTED
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.permission_title))
        builder.setMessage(resources.getString(R.string.permission_message))
        builder.setPositiveButton(resources.getString(R.string.permission_go_to_settings)) { dialog, which ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }
    // Permission Code Ended

    private fun getLatLng() {
        // Show progress here, because get Lat Lng takes 2-3 seconds to fetch
        showProgressDialog(resources.getString(R.string.please_wait))

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val latLast = task.result?.latitude.toString()
                val lngLast = task.result?.longitude.toString()
                Log.e("#####", "lastLocation lat: $latLast & lng: $lngLast")
                prefmanger.set(LATITUDE,latLast)
                prefmanger.set(LONGITUDE,lngLast)
                val intent = Intent(this@RegistrationActivity,OTPVerificationRegistrationActivity::class.java)
                intent.putExtra("fName",firstName)
                intent.putExtra("lName",lastName)
                intent.putExtra("phoneNo",phoneNo)
                intent.putExtra("email",email)
                intent.putExtra("city",city)
                intent.putExtra("userType",userType)
                intent.putExtra("termsConditions",termsConditionsAcceptance)
                intent.putExtra("storedVerificationId",storedVerificationId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                finish()
            } else {
                Log.e("#####", "lastLocation exception ${task.exception?.message}")
                // Get current location here, if last location not found
                getCustomCurrentLocation(fusedLocationClient)
            }
        }.addOnFailureListener { error -> Log.e("#####", "onFailure: ${error.message}") }
    }

    private fun getCustomCurrentLocation(fusedLocationClient: FusedLocationProviderClient) {
        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )
        currentLocationTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val latCurrent = task.result?.latitude.toString()
                val lngCurrent = task.result?.longitude.toString()
                Log.e("#####", "currentLocation lat: $latCurrent & lng: $lngCurrent")
                prefmanger.set(LATITUDE,latCurrent)
                prefmanger.set(LONGITUDE,lngCurrent)
                val intent = Intent(this@RegistrationActivity,OTPVerificationRegistrationActivity::class.java)
                intent.putExtra("fName",firstName)
                intent.putExtra("lName",lastName)
                intent.putExtra("phoneNo",phoneNo)
                intent.putExtra("email",email)
                intent.putExtra("userType",userType)
                intent.putExtra("termsConditions",termsConditionsAcceptance)
                intent.putExtra("storedVerificationId",storedVerificationId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                finish()
            } else {
                Log.e("#####", "currentLocation exception ${task.exception?.message}")
            }
        }
    }

    private fun turnGPSOn() {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnCompleteListener { result ->
            try {
                val response = result.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException(exception.status).startResolutionForResult(
                                this, 123
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                            Log.e("#####", "SendIntentException: ${e.message}")
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                            Log.e("#####", "ClassCastException: ${e.message}")
                        }
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                    }
                }
            }
        }
    }
    private fun getUserTypeIfNotSignIn(mobileNo: String, callback: (String) -> Unit){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.CHECK_USER_EXISTING)
                .addQueryParameter("mobile", mobileNo)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    UserExistOrNotModel::class.java,
                    object : ParsedRequestListener<UserExistOrNotModel> {
                        override fun onResponse(response: UserExistOrNotModel?) {
                            try {

                                val snackbar = Snackbar
                                    .make(
                                        mainLayout,
                                        "Sorry! you are Already register, Please login.",
                                        Snackbar.LENGTH_LONG
                                    )
                                    .setAction(
                                        "LOGIN"
                                    )  // If the Undo button
// is pressed, show
// the message using Toast
                                    {
                                        startActivity(Intent(this@RegistrationActivity,LoginActivity::class.java))
                                        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                                    }

                                snackbar.show()

                                hideProgressDialog()
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )
                                if (it.errorCode == 404){
                                    callback(it!!.errorBody!!)
                                }

                            }


                        }
                    })
        }else{
            showNoInternetBottomSheet(this,this)
        }

        /* val database = FirebaseDatabase.getInstance()
         val usersRef = database.reference.child("Users")

         usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 var grandParentKey = String()
                 Log.d(TAG,"Finding User for :${mobileNo}")
                 for (userTypeSnapshot in snapshot.children) {
                     for (userSnapshot in userTypeSnapshot.children) {
                         val userMobileNo = userSnapshot.child("userPhoneNumber").getValue(String::class.java)
                         if (userMobileNo.equals(mobileNo)) {
                             grandParentKey =
                                 userTypeSnapshot.key.toString() // Key of the grandparent ("Job Seeker" or "Recruiter")
                             Log.d(TAG,"userPhoneNumber: $userMobileNo => userType: $grandParentKey")
                             userType = grandParentKey
                             callback(grandParentKey)
                             break
                         }
                         else{
                             Log.d(TAG,"$userMobileNo : Not match")
                         }
                     }
                 }
             }
             override fun onCancelled(error: DatabaseError) {
                 Log.d(TAG,"error: ${error.message}")
                 makeToast("error: ${error.message}",0)
             }
         })*/
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                Log.e("#####", "GPS active, ask permission, get Lat-Lng & pass to API")
                if (isGrantedPermission()) {
                    getLatLng()
                } else {
                    requestPermissions()
                }
            } else if (resultCode == RESULT_CANCELED) {
                makeToast(resources.getString(R.string.plz_enable_gps),0)
            }
        }
    }
}