package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class AnalysisFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = TextView(context)
        view.text = "Analysis Screen (Coming Soon)"
        view.gravity = android.view.Gravity.CENTER
        view.textSize = 24f
        return view
    }
}