package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom_russo.R
import com.example.bloom_russo.databinding.FragmentAnalysisBinding

class AnalysisFragment : Fragment() {

    private val viewModel: AnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup RecyclerView
        val adapter = MyCyclesAdapter()
        binding.cyclesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.cyclesRecyclerView.adapter = adapter

        // Observer per la lista
        viewModel.cyclesList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // Click Listener per i bottoni RIMASTI
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_analysis_to_settings)
        }

        binding.btnReminders.setOnClickListener {
            findNavController().navigate(R.id.action_analysis_to_reminders)
        }

        // RIMOSSI: btnTheme e btnFeedback (causavano l'errore)

        return binding.root
    }
}