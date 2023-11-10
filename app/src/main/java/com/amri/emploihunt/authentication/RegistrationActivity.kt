package com.amri.emploihunt.authentication

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityRegistrationBinding
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.model.UserExistOrNotModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.settings.TermsPrivacyActivity
import com.amri.emploihunt.util.LATITUDE
import com.amri.emploihunt.util.LONGITUDE
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.isGPSEnabled
import com.amri.emploihunt.util.Utils.showNoInternetBottomSheet
import com.amri.emploihunt.util.Utils.toast
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class RegistrationActivity : BaseActivity() ,OnClickListener{


    lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var prefmanger: SharedPreferences


    private lateinit var mAuth: FirebaseAuth

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var phoneNo: String
    private lateinit var email: String
    private lateinit var city: String
    private var userType: Int ?= null
    private lateinit var termsConditionsAcceptance:String

    /*var cityList: ArrayList<String> = ArrayList()*/

    private lateinit var decorView: View
    private lateinit var copyCredential : PhoneAuthCredential
    lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        prefmanger = PrefManager.prefManager(this)
        setOnClickListener()
        mAuth = FirebaseAuth.getInstance()
        Firebase.initialize(context = this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        binding.cpp.registerCarrierNumberEditText(binding.phoneNo)
        val cityList:ArrayList<String> = arrayListOf()
        getAllCity(cityList){
            if(cityList.isNotEmpty()){
                val adapter: ArrayAdapter<String> =
                    ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, cityList)
                binding.city.setAdapter(adapter)
            }
            else{
                makeToast(getString(R.string.something_error),0)
            }
        }
        binding.checkBoxtx.setOnClickListener {
            val intent = Intent(this, TermsPrivacyActivity::class.java)
            intent.putExtra("Privacy",false)
            startActivity(intent)
        }
    }


    private fun setOnClickListener() {
        binding.btnRegistration.setOnClickListener(this)
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

        firstName = binding.userFName.text.toString()
        lastName = binding.userLName.text.toString()
        phoneNo = "+" + binding.cpp.fullNumber.toString()
        email = binding.email.text.toString()
        city = binding.city.text.toString()
        userType = intent.getIntExtra("role",-1)
        termsConditionsAcceptance =  if (binding.checkBox.isChecked) {
            "Accepted"
        }
        else{
            "Not Accepted"
        }
        val correct = inputFieldConformation(userType!!,firstName,lastName,phoneNo,email,city,termsConditionsAcceptance)

        isPhNoRegBefore(phoneNo,exist)
        if (!correct) return
        else{

            Log.d("test", "registerUser: $phoneNo")

            checkUserExistingOrNot(phoneNo) { userType ->

                Log.d("###", "registerUser: $userType")
                if (userType.isNotEmpty()) {
                    passInfoToNextActivity()
                }

            }


        }
        }

    private fun passInfoToNextActivity() {
        firstName = binding.userFName.text.toString()
        lastName = binding.userLName.text.toString()
        phoneNo = "+" + binding.cpp.fullNumber.toString()
        email = binding.email.text.toString()
        city = binding.city.text.toString()
        isPhNoRegBefore(phoneNo,exist)
        val correct = inputFieldConformation(
            userType!!,
            firstName,
            lastName,
            phoneNo,
            email,
            city,
            termsConditionsAcceptance
        )

        if (!correct) return
        else{

            val deniedPermissions:MutableList<String> = isGrantedPermission()

            if (!isGPSEnabled(this)) {
                turnGPSOn()
            } else if (deniedPermissions.isEmpty()) {
                getLatLng()
            } else {
                requestPermissions(deniedPermissions){
                    if(it) {
                        getLatLng()
                    }
                    else{
                        val snackbar = Snackbar
                            .make(
                                binding.root,
                                "Sorry! you aren't given required permissions.",
                                Snackbar.LENGTH_LONG
                            )
                            .setAction(
                                "Grant Permissions"
                            )
                            {
                                showSettingsDialog()
                            }

                        snackbar.show()
                    }
                }
            }
        }
    }


    var exist:Boolean=false
    private fun inputFieldConformation(
        userType: Int,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String,
        city: String,
        termsConditionsAcceptance: String
    ): Boolean {
        if(userType == -1){
            makeToast("Please go back and select one job type.", 1)
            return false
        }
        if (firstName.isEmpty()) {
            binding.userFName.error = "Please provide a first-name"
            binding.userFName.requestFocus()
            return false
        }
        if (lastName.isEmpty()) {
            binding.userLName.error = "Please provide a last-name"
            binding.userLName.requestFocus()
            return false
        }
        if (firstName.equals(
                "EmploiHunt", true
            ) || firstName.equals("EmploiHunt", true) || firstName.equals(
                "Emploi", true
            )) {
            toast(resources.getString(R.string.enter_another_first_name))
        }
        if (lastName.equals(
                "EmploiHunt", true
            ) || firstName.equals("EmploiHunt", true) || firstName.equals(
                "Emploi", true
            )) {
            toast(resources.getString(R.string.enter_another_last_name))
        }


        if (firstName == lastName)  {
            toast(resources.getString(R.string.first_last_name_not_same))
        }

        if (firstName.length == 2 || lastName.length == 2)  {
            toast(resources.getString(R.string.first_last_name_required_min_2_letters))
        }
       if (TextUtils.isDigitsOnly(firstName) || TextUtils.isDigitsOnly(lastName))  {
            toast(resources.getString(R.string.first_last_name_should_be_character))
        }

        if (Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]").matcher(firstName)
                .find() || Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]").matcher(lastName)
                .find()) {
            toast(resources.getString(R.string.first_last_name_should_be_character))
        }
        if (binding.phoneNo.text.toString().isEmpty()) {
            binding.phoneNo.error = "Please provide a mobile no."
            binding.phoneNo.requestFocus()
            return false
        }
        if (binding.phoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.phoneNo.error = "Please provide valid 10 digit mobile no"
            binding.phoneNo.requestFocus()
            return false
        }

        if(exist){
            binding.phoneNo.error = "This phone number is already exist"
            binding.phoneNo.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            binding.email.error = "Please provide a email address"
            binding.email.requestFocus()
            return  false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Please provide valid email address"
            binding.email.requestFocus()
            return false
        }
        if (city.isEmpty()) {
            binding.city.error = "Please provide valid city"
            binding.city.requestFocus()
            return  false
        }
        if (termsConditionsAcceptance != "Accepted"){
            binding.checkBox.error = "Accept terms & conditions"
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




    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@RegistrationActivity, AskActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun getLatLng() {
        // Show progress here, because get Lat Lng takes 2-3 seconds to fetch

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val latLast = task.result?.latitude.toString()
                val lngLast = task.result?.longitude.toString()
                Log.e("#####", "lastLocation lat: $latLast & lng: $lngLast")
                prefmanger.set(LATITUDE,latLast)
                prefmanger.set(LONGITUDE,lngLast)
                binding.phoneNo.text!!.clear()
                val intent = Intent(this@RegistrationActivity, OTPVerificationRegistrationActivity::class.java)
                intent.putExtra("fName",firstName)
                intent.putExtra("lName",lastName)
                intent.putExtra("phoneNo",phoneNo)
                intent.putExtra("email",email)
                intent.putExtra("city",city)
                intent.putExtra("role",userType)
                intent.putExtra("termsConditions",termsConditionsAcceptance)
                hideProgressDialog()
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
        val currentLocationTask: Task<Location> = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        else {
            fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
        }
        currentLocationTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val latCurrent = task.result?.latitude.toString()
                val lngCurrent = task.result?.longitude.toString()
                Log.e("#####", "currentLocation lat: $latCurrent & lng: $lngCurrent")
                prefmanger.set(LATITUDE,latCurrent)
                prefmanger.set(LONGITUDE,lngCurrent)
                binding.phoneNo.text!!.clear()
                val intent = Intent(this@RegistrationActivity, OTPVerificationRegistrationActivity::class.java)
                intent.putExtra("fName",firstName)
                intent.putExtra("lName",lastName)
                intent.putExtra("phoneNo",phoneNo)
                intent.putExtra("email",email)
                intent.putExtra("role",userType)
                intent.putExtra("termsConditions",termsConditionsAcceptance)
                hideProgressDialog()
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
    private fun checkUserExistingOrNot(mobileNo: String, callback: (String) -> Unit){

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
                                        binding.mainLayout,
                                        "Sorry! you are Already register, Please login.",
                                        Snackbar.LENGTH_LONG
                                    )
                                    .setAction(
                                        "LOGIN"
                                    )
                                    {
                                        startActivity(Intent(this@RegistrationActivity,
                                            LoginActivity::class.java))
                                        overridePendingTransition(
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                        )
                                        finish()
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
                                    callback(it.errorBody!!)
                                }

                            }
                        }
                    })
        }else{
            showNoInternetBottomSheet(this,this)
        }

    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                Log.e("#####", "GPS active, ask permission, get Lat-Lng & pass to API")

                val deniedPermissions:MutableList<String> = isGrantedPermission()
                if (deniedPermissions.isEmpty()) {
                    getLatLng()
                } else {
                    requestPermissions(deniedPermissions){
                        if(it) {
                            getLatLng()
                        }
                        else{
                            val snackbar = Snackbar
                                .make(
                                    binding.root,
                                    "Sorry! you are not register, Please register first.",
                                    Snackbar.LENGTH_LONG
                                )
                                .setAction(
                                    "REGISTER"
                                )
                                {
                                    showSettingsDialog()
                                }

                            snackbar.show()
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                makeToast(resources.getString(R.string.plz_enable_gps),0)
            }
        }
    }

    /*fun getAllCity(){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        override fun onResponse(response: GetAllCity?) {
                            try {

                                cityList.addAll(response!!.data)

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
                                hideProgressDialog()

                            }


                        }
                    })
        }else{
            showNoInternetBottomSheet(this,this)
        }

    }*/
}