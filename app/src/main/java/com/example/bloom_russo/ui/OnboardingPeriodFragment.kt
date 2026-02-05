package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R
import com.example.bloom_russo.databinding.FragmentOnboardingPeriodBinding

class OnboardingPeriodFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingPeriodBinding
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingPeriodBinding.inflate(inflater, container, false)

        // Configura NumberPicker
        binding.numberPicker.minValue = 1
        binding.numberPicker.maxValue = 15
        binding.numberPicker.value = viewModel.periodDuration

        // Listener per aggiornare il ViewModel quando l'utente scorre
        binding.numberPicker.setOnValueChangedListener { _, _, newVal ->
            viewModel.periodDuration = newVal
        }

        binding.btnNext.setOnClickListener {
            // Salva il valore corrente e vai avanti
            viewModel.periodDuration = binding.numberPicker.value
            findNavController().navigate(R.id.action_period_to_cycle)
        }

        binding.btnSkip.setOnClickListener {
            // Mantieni il default (4) e vai avanti
            findNavController().navigate(R.id.action_period_to_cycle)
        }

        return binding.root
    }
}