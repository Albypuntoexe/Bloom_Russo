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
import java.util.Calendar

class OnboardingDateFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingDateBinding
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingDateBinding.inflate(inflater, container, false)

        binding.btnDone.setOnClickListener {
            // Prende data dal DatePicker
            val day = binding.datePicker.dayOfMonth
            val month = binding.datePicker.month + 1 // I mesi in Android partono da 0
            val year = binding.datePicker.year

            // Formattiamo "yyyy-MM-dd"
            val formattedDate = String.format("%04d-%02d-%02d", year, month, day)
            viewModel.lastPeriodDate = formattedDate

            completeOnboarding()
        }

        binding.btnSkip.setOnClickListener {
            // Lascia la data di oggi (impostata nel ViewModel)
            completeOnboarding()
        }

        return binding.root
    }

    private fun completeOnboarding() {
        // Salva tutto nel DB
        viewModel.saveData()
        // Naviga alla Home
        findNavController().navigate(R.id.action_date_to_home)
    }
}