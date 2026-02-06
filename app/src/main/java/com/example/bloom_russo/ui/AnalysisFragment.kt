package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R

class AnalysisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        view.findViewById<TextView>(R.id.btnMyCycles).setOnClickListener {
            findNavController().navigate(R.id.action_analysis_to_myCycles)
        }

        view.findViewById<TextView>(R.id.btnReminders).setOnClickListener {
            findNavController().navigate(R.id.action_analysis_to_reminders)
        }

        view.findViewById<TextView>(R.id.btnSettings).setOnClickListener {
            Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
            // Qui implementeremo Settings se richiesto
        }

        return view
    }
}