package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        // Importante per far funzionare il LiveData nel layout
        binding.lifecycleOwner = viewLifecycleOwner

        // Attiviamo l'osservatore dei dati
        viewModel.dataObserver.observe(viewLifecycleOwner) {
            // I dati sono stati caricati, la UI si aggiorna automaticamente grazie al DataBinding
        }

        binding.actionButton.setOnClickListener {
            // Qui implementeremo la logica per aprire EditPeriod (Image 5.png)
            // Per ora mettiamo un Toast
            Toast.makeText(context, "Open Edit Period Screen", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
}