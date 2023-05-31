package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.concurrent.TimeUnit
import android.view.View.*
class JobRegisterActivity2 : AppCompatActivity() {
    lateinit var btn_next: Button
    lateinit var btn_prev: Button
    lateinit var upload: Button
    lateinit var select: TextView
    lateinit var pdfTextView: TextView; lateinit var tv : TextView
    lateinit var pdfUri: Uri
    lateinit var mStorage: StorageReference
    val pdf: Int = 0
    var fileUrl = ""
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference
    var pdfName: String = ""
    var phonereceived = ""
    private lateinit var phoneAuthProvider: PhoneAuthProvider
    private var verificationId: String? = null
    lateinit var decorView: View
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_register2)
        fullScreen()
        btn_next = findViewById(R.id.otpbtnrej)
        btn_prev = findViewById(R.id.prevbtnregj)
        select = findViewById(R.id.uploadfile)
        upload = findViewById(R.id.uploadbtnregj)
        pdfTextView = findViewById(R.id.pdftv)
        tv = findViewById(R.id.loginbtnregj3)
        auth=FirebaseAuth.getInstance()
        phoneAuthProvider = PhoneAuthProvider.getInstance()
        mStorage = FirebaseStorage.getInstance().getReference("pdfs")
        tv.setOnClickListener {
            startActivity(Intent(this,JobLoginActivity::class.java))
            overridePendingTransition(R.anim.flip_in,R.anim.flip_out)
            finish()
        }
        select.setOnClickListener(object : OnClickListener {
            override fun onClick(p0: View?) {
                selectpdf()
            }
        })
        btn_next.setOnClickListener {
            //adddata()
            val bundle = intent.extras

            if(bundle!=null)
            {
                phonereceived = "+91 " + bundle.getString("phone").toString()
            }
            sendOtp()
//            var intent = Intent(this, OTPJobActivity::class.java)
//            intent.putExtra("phonenum",phonereceived)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
//            finish()
        }
        btn_prev.setOnClickListener {
            startActivity(Intent(this, JobRegisterActivity1::class.java))
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            finish()
        }
    }

    private val callbacks = object : OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            //signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Handle verification failure
        }

        override fun onCodeSent(
            verificationId: String,
            token: ForceResendingToken
        ) {
            this@JobRegisterActivity2.verificationId = verificationId
            // Start the OTP verification activity
            val intent = Intent(this@JobRegisterActivity2, OTPJobActivity::class.java)
            intent.putExtra("verification_id", verificationId)
            intent.putExtra("phonenum",phonereceived)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
        }
    }

    private fun sendOtp() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phonereceived)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun selectpdf() {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pdfIntent, 12)
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                12 -> if (resultCode == RESULT_OK) {
                    pdfUri = data?.data!!
                    val uri: Uri = data.data!!
                    val uriString: String = uri.toString()
                    pdfName = null.toString()
                    if (uriString.startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = this.contentResolver.query(
                                uri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                pdfName =
                                    myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                pdfTextView.text = pdfName
                            }
                        } finally {
                            myCursor?.close()
                        }
                    }
                }
            }
        }
        val pdfRef = mStorage.child("$pdfName")
        pdfRef.putFile(pdfUri).addOnSuccessListener {
            pdfRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                fileUrl = downloadUrl.toString()
            }
        }

    }
    private fun adddata() {
        val bundle1 = intent.extras
        var fname : String = ""; var lname : String = ""
        var phone : String = ""; var email : String = ""
        var city : String = ""; var expsal : String = ""
        var workmode : String = ""; var degree : String = ""
        var bio : String = ""; var jobtype : String = ""
        var frshexp : String = ""; var compname : String = ""
        var expesal : String = ""; var expdur : String = ""
        if(bundle1!=null)
        {
            fname = bundle1.getString("fname").toString()
            lname = bundle1.getString("lname").toString()
            phone = bundle1.getString("phone").toString()
            email = bundle1.getString("email").toString()
            city = bundle1.getString("city").toString()
            expsal = bundle1.getString("expsal").toString()
            workmode = bundle1.getString("workmode").toString()
            degree = bundle1.getString("degree").toString()
            bio = bundle1.getString("bio").toString()
            jobtype = bundle1.getString("jobtype").toString()
            frshexp = bundle1.getString("frshexp").toString()
            compname = bundle1.getString("compname").toString()
            expesal = bundle1.getString("expesal").toString()
            expdur = bundle1.getString("expdur").toString()
        }
        database = FirebaseDatabase.getInstance().getReference("JobSeekers")
        val JobSeeker = JobSeekers(fname, lname, phone, email, city, expsal, workmode, degree,
            bio, jobtype, frshexp, compname, expesal, expdur, fileUrl)
        if (phone != null) {
            database.child(phone).setValue(JobSeeker).addOnSuccessListener {
                Toast.makeText(this,"Data Added", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this,"Failed", Toast.LENGTH_LONG).show()
            }
        }
        else{
            Toast.makeText(this,"Enter Phone", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, JobRegisterActivity1::class.java))
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        finish()

    }

    private fun fullScreen() {
        decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { i ->
            if (i == 0) {
                decorView.systemUiVisibility = hideSystemBars()
            }
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            decorView.systemUiVisibility = hideSystemBars()
        }
    }

    private fun hideSystemBars(): Int {
        return (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

}