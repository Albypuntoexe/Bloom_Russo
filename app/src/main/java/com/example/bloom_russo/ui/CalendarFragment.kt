package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bloom_russo.R
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
            viewModel.onDateSelected(date)
            (binding.calendarRecyclerView.adapter as CalendarAdapter).setSelectedDate(date)
        }

        val layoutManager = GridLayoutManager(context, 7)
        layoutManager.spanSizeLookup = adapter.spanSizeLookup

        binding.calendarRecyclerView.layoutManager = layoutManager
        binding.calendarRecyclerView.adapter = adapter

        viewModel.calendarItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.calendarRecyclerView.scrollToPosition(items.size / 2)
        }

        binding.btnEdit.setOnClickListener {
            // Naviga verso Edit Period
            findNavController().navigate(R.id.action_calendar_to_edit)
        }

        return binding.root
    }
}