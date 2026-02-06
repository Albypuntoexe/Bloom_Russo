package com.example.bloom_russo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom_russo.R
import com.example.bloom_russo.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var binding: FragmentCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
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

        // Observer Dati Calendario
        viewModel.calendarItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        // Observer Scroll Automatico (Fix 1)
        viewModel.currentMonthPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
                // Scroll with offset to show the month header at top
                (binding.calendarRecyclerView.layoutManager as GridLayoutManager)
                    .scrollToPositionWithOffset(position, 20)
            }
        }

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.action_calendar_to_edit)
        }

        return binding.root
    }

    // FIX SINCRONIZZAZIONE: Quando torniamo su questa schermata (es. da Edit), ricarica i dati!
    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}