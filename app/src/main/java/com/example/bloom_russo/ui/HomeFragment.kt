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
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Observer per i dati
        viewModel.dataObserver.observe(viewLifecycleOwner) {}

        // Click Bottone Centrale
        binding.actionButton.setOnClickListener {
            viewModel.onActionButtonClick()
        }

        // Navigazione
        viewModel.navigateToEdit.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_home_to_edit)
                viewModel.onNavigationComplete()
            }
        }

        // Click Icone Header
        binding.iconNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_reminders)
        }
        binding.iconSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        // --- LOGICA SWITCH GATTO/CANE ---
        // Imposta lo stato iniziale
        binding.petImage.setImageResource(R.drawable.gatto)
        binding.petSwitch.isChecked = false
        binding.petSwitch.text = "Switch to Dog"

        // Listener per il cambio di stato dello Switch
        binding.petSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch attivo -> Mostra Cane
                // Assicurati di avere 'cane.png' nella cartella drawable
                binding.petImage.setImageResource(R.drawable.cane)
                binding.petSwitch.text = "Switch to Cat"
            } else {
                // Switch disattivo -> Mostra Gatto
                binding.petImage.setImageResource(R.drawable.gatto)
                binding.petSwitch.text = "Switch to Dog"
            }
        }
        // --------------------------------

        return binding.root
    }
}