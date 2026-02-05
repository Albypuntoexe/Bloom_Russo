package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bloom_russo.R
import com.example.bloom_russo.databinding.FragmentOnboardingCycleBinding

class OnboardingCycleFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingCycleBinding
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingCycleBinding.inflate(inflater, container, false)

        // Configura NumberPicker per ciclo (es. da 20 a 45 giorni)
        binding.numberPickerCycle.minValue = 20
        binding.numberPickerCycle.maxValue = 45
        binding.numberPickerCycle.value = viewModel.cycleLength

        binding.numberPickerCycle.setOnValueChangedListener { _, _, newVal ->
            viewModel.cycleLength = newVal
        }

        binding.btnNext.setOnClickListener {
            viewModel.cycleLength = binding.numberPickerCycle.value
            findNavController().navigate(R.id.action_cycle_to_date)
        }

        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.action_cycle_to_date)
        }

        return binding.root
    }
}