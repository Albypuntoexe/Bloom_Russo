package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        // Inizializza il Binding
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Collega il ViewModel al layout XML
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Observer per i dati (attiva il calcolo iniziale)
        viewModel.dataObserver.observe(viewLifecycleOwner) {}

        // Click Bottone Centrale
        binding.actionButton.setOnClickListener {
            viewModel.onActionButtonClick()
        }

        // Navigazione verso Edit (controllata dal ViewModel)
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_home_to_edit)
                viewModel.onNavigationComplete()
            }
        }

        // Click Campanella (Notifiche)
        binding.iconNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_reminders)
        }

        // Click Settings (Ingranaggio)
        binding.iconSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        // IMPORTANTE: Restituisce la View radice, non un File!
        return binding.root
    }
}