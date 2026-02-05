package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bloom_russo.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private val viewModel: CalendarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCalendarBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = CalendarAdapter(requireContext()) { date ->
            // Callback al click sulla data
            viewModel.onDateSelected(date)
            // Aggiorna la selezione visiva (bordo viola)
            (binding.calendarRecyclerView.adapter as CalendarAdapter).setSelectedDate(date)
        }

        val layoutManager = GridLayoutManager(context, 7)
        layoutManager.spanSizeLookup = adapter.spanSizeLookup

        binding.calendarRecyclerView.layoutManager = layoutManager
        binding.calendarRecyclerView.adapter = adapter

        viewModel.calendarItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            // Scrolla al centro della lista (dove c'è il mese corrente)
            binding.calendarRecyclerView.scrollToPosition(items.size / 2)
        }

        binding.btnEdit.setOnClickListener {
            // Qui andrà la navigazione verso Edit Period
            Toast.makeText(context, "Go to Edit Period", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
}