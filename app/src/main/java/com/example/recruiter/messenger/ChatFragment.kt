package com.example.recruiter.messenger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.recruiter.R

class ChatFragment : Fragment() {
    lateinit var fragView : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragView = inflater.inflate(R.layout.fragment_chat, container, false)
        // Inflate the layout for this fragment
        return fragView
    }
}