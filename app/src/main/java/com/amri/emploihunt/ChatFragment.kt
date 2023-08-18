package com.amri.emploihunt

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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