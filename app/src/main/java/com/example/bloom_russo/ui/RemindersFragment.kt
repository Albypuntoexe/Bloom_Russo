package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R

class RemindersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reminders, container, false)
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }
        return view
    }
}