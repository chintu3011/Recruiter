package com.amri.emploihunt.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amri.emploihunt.databinding.ActivityLoginsignupBinding

class LoginsignupActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginsignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginsignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnloginmain.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }
        binding.btnregmain.setOnClickListener {
            startActivity(Intent(this, AskActivity::class.java))

        }
    }
}