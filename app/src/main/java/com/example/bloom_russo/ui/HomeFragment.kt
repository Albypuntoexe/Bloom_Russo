package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R
import com.example.bloom_russo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Collega il ViewModel al Layout
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Attiviamo l'osservatore dei dati
        viewModel.dataObserver.observe(viewLifecycleOwner) {
            // UI si aggiorna via DataBinding
        }

        binding.actionButton.setOnClickListener {
            // Ora l'ID action_home_to_edit esiste grazie al file nav_graph sopra
            findNavController().navigate(R.id.action_home_to_edit)
        }

        return binding.root
    }
}