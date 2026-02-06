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

        viewModel.dataObserver.observe(viewLifecycleOwner) {}

        binding.actionButton.setOnClickListener {
            viewModel.onActionButtonClick()
        }

        viewModel.navigateToEdit.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_home_to_edit)
                viewModel.onNavigationComplete()
            }
        }

        // Click Campanella -> Reminders
        binding.iconNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_reminders)
        }

        // Click Ingranaggio -> Settings
        binding.iconSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        return binding.root
    }
}