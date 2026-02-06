package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R
import com.example.bloom_russo.databinding.FragmentOnboardingDateBinding

class OnboardingDateFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingDateBinding
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingDateBinding.inflate(inflater, container, false)

        // Impostiamo la data di default a oggi nel ViewModel appena si apre la schermata
        // Così se l'utente preme "Done" senza toccare lo spinner, salva oggi.
        viewModel.setDateToToday()

        binding.btnDone.setOnClickListener {
            // Prende data dal DatePicker
            val day = binding.datePicker.dayOfMonth
            val month = binding.datePicker.month + 1
            val year = binding.datePicker.year

            val formattedDate = String.format("%04d-%02d-%02d", year, month, day)
            viewModel.lastPeriodDate = formattedDate

            // False = Non ho saltato, salva i dati veri
            completeOnboarding(isSkipped = false)
        }

        binding.btnSkip.setOnClickListener {
            // True = Ho saltato, salva NULL e non generare giorni
            completeOnboarding(isSkipped = true)
        }

        return binding.root
    }

    private fun completeOnboarding(isSkipped: Boolean) {
        viewModel.saveData(isSkipped)
        findNavController().navigate(R.id.action_date_to_home)
    }
}