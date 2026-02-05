package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bloom_russo.databinding.FragmentEditPeriodBinding

class EditPeriodFragment : Fragment() {

    private val viewModel: EditPeriodViewModel by viewModels()
    private lateinit var binding: FragmentEditPeriodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPeriodBinding.inflate(inflater, container, false)

        val adapter = EditPeriodAdapter(requireContext()) { date ->
            viewModel.toggleDate(date)
        }

        val layoutManager = GridLayoutManager(context, 7)
        layoutManager.spanSizeLookup = adapter.spanSizeLookup
        binding.editRecyclerView.layoutManager = layoutManager
        binding.editRecyclerView.adapter = adapter

        // Osserva sia la griglia che le selezioni
        viewModel.calendarItems.observe(viewLifecycleOwner) { items ->
            // Passiamo le selezioni attuali (o vuote se ancora non caricate)
            val currentSelections = viewModel.selectedDatesLiveData.value ?: emptySet()
            adapter.submitList(items, currentSelections)
            // Scrolla in basso
            binding.editRecyclerView.scrollToPosition(items.size - 1)
        }

        viewModel.selectedDatesLiveData.observe(viewLifecycleOwner) { selections ->
            val items = viewModel.calendarItems.value ?: emptyList()
            adapter.submitList(items, selections)
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveChanges {
                Toast.makeText(context, "Dates Saved", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Torna indietro
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack() // Torna indietro senza salvare
        }

        return binding.root
    }
}